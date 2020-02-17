package com.citahub.cita.protocol.core.filters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import com.citahub.cita.protocol.core.methods.request.AppFilter;
import com.citahub.cita.protocol.core.methods.response.AppLog;
import com.citahub.cita.protocol.core.methods.response.Log;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.Request;

/**
 * Log filter handler.
 */
public class LogFilter extends Filter<Log> {

    private final AppFilter ethFilter;

    public LogFilter(
            CITAj citaj, Callback<Log> callback,
            AppFilter ethFilter) {
        super(citaj, callback);
        this.ethFilter = ethFilter;
    }


    @Override
    com.citahub.cita.protocol.core.methods.response.AppFilter sendRequest() throws IOException {
        return citaj.appNewFilter(ethFilter).send();
    }

    @Override
    void process(List<AppLog.LogResult> logResults) {
        for (AppLog.LogResult logResult : logResults) {
            if (logResult instanceof AppLog.LogObject) {
                Log log = ((AppLog.LogObject) logResult).get();
                callback.onEvent(log);
            } else {
                throw new FilterException(
                        "Unexpected result type: " + logResult.get() + " required LogObject");
            }
        }
    }

    @Override
    protected Request<?, AppLog> getFilterLogs(BigInteger filterId) {
        return citaj.appGetFilterLogs(filterId);
    }
}
