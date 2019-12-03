package com.cryptape.cita.protocol.core.filters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.cryptape.cita.protocol.core.methods.response.AppFilter;
import com.cryptape.cita.protocol.core.methods.response.AppLog;
import com.cryptape.cita.protocol.core.methods.response.AppUninstallFilter;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.Request;
import com.cryptape.cita.protocol.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for creating managed filter requests with callbacks.
 */
public abstract class Filter<T> {

    private static final Logger log = LoggerFactory.getLogger(Filter.class);

    final CITAj citaj;
    final Callback<T> callback;

    private volatile BigInteger filterId;

    private ScheduledFuture<?> schedule;

    public Filter(CITAj citaj, Callback<T> callback) {
        this.citaj = citaj;
        this.callback = callback;
    }

    public void run(ScheduledExecutorService scheduledExecutorService, long blockTime) {
        try {
            AppFilter appFilter = sendRequest();
            if (appFilter.hasError()) {
                throwException(appFilter.getError());
            }

            filterId = appFilter.getFilterId();
            // this runs in the caller thread as if any exceptions are encountered, we shouldn't
            // proceed with creating the scheduled task below
            getInitialFilterLogs();

            /*
            We want the filter to be resilient against client issues. On numerous occasions
            users have reported socket timeout exceptions when connected over HTTP to Geth and
            Parity clients. For examples, refer to
            https://github.com/ethereum/go-ethereum/issues/15243.

            Hence we consume errors and log them as errors, allowing our polling for changes to
            resume. The downside of this approach is that users will not be notified of
            downstream connection issues. But given the intermittent nature of the connection
            issues, this seems like a reasonable compromise.

            The alternative approach would be to have another thread that blocks waiting on
            schedule.get(), catching any Exceptions thrown, and passing them back up to the
            caller. However, the user would then be required to recreate subscriptions manually
            which isn't ideal given the aforementioned issues.
            */
            schedule = scheduledExecutorService.scheduleAtFixedRate(
                    () -> {
                        try {
                            this.pollFilter(appFilter);
                        } catch (Throwable e) {
                            // All exceptions must be caught, otherwise our job terminates without
                            // any notification
                            log.error("Error sending request", e);
                        }
                    },
                    0, blockTime, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            throwException(e);
        }
    }

    private void getInitialFilterLogs() {
        try {
            Request<?, AppLog> maybeRequest = this.getFilterLogs(this.filterId);
            AppLog appLog = null;
            if (maybeRequest != null) {
                appLog = maybeRequest.send();
            } else {
                appLog = new AppLog();
                appLog.setResult(Collections.emptyList());
            }
            process(appLog.getLogs());

        } catch (IOException e) {
            throwException(e);
        }
    }

    private void pollFilter(AppFilter appFilter) {
        AppLog appLog = null;
        try {
            appLog = citaj.appGetFilterChanges(filterId).send();
        } catch (IOException e) {
            throwException(e);
        }
        if (appLog.hasError()) {
            throwException(appFilter.getError());
        } else {
            process(appLog.getLogs());
        }
    }

    abstract AppFilter sendRequest() throws IOException;

    abstract void process(List<AppLog.LogResult> logResults);

    public void cancel() {
        schedule.cancel(false);

        AppUninstallFilter appUninstallFilter = null;
        try {
            appUninstallFilter = citaj.appUninstallFilter(filterId).send();
        } catch (IOException e) {
            throwException(e);
        }

        if (appUninstallFilter.hasError()) {
            throwException(appUninstallFilter.getError());
        }

        if (!appUninstallFilter.isUninstalled()) {
            throwException(appUninstallFilter.getError());
        }
    }

    /**
     * Retrieves historic filters for the filter with the given id.
     * Getting historic logs is not supported by all filters.
     * If not the method should return an empty EthLog object
     *
     * @param filterId Id of the filter for which the historic log should be retrieved
     * @return Historic logs, or an empty optional if the filter cannot retrieve historic logs
     */
    protected abstract Request<?, AppLog> getFilterLogs(BigInteger filterId);

    void throwException(Response.Error error) {
        throw new FilterException("Invalid request: "
                + (error == null ? "Unknown Error" : error.getMessage()));
    }

    void throwException(Throwable cause) {
        throw new FilterException("Error sending request", cause);
    }
}

