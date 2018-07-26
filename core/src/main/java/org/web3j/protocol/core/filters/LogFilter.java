package org.web3j.protocol.core.filters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.AppFilter;
import org.web3j.protocol.core.methods.response.AppLog;
import org.web3j.protocol.core.methods.response.Log;

/**
 * Log filter handler.
 */
public class LogFilter extends Filter<Log> {

    private final org.web3j.protocol.core.methods.request.AppFilter ethFilter;

    public LogFilter(
            Web3j web3j, Callback<Log> callback,
            org.web3j.protocol.core.methods.request.AppFilter ethFilter) {
        super(web3j, callback);
        this.ethFilter = ethFilter;
    }


    @Override
    AppFilter sendRequest() throws IOException {
        return web3j.appNewFilter(ethFilter).send();
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
    protected Optional<Request<?, AppLog>> getFilterLogs(BigInteger filterId) {
        return Optional.of(web3j.appGetFilterLogs(filterId));
    }
}
