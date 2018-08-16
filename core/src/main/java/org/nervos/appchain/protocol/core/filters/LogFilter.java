package org.nervos.appchain.protocol.core.filters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.request.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppLog;
import org.nervos.appchain.protocol.core.methods.response.Log;

/**
 * Log filter handler.
 */
public class LogFilter extends Filter<Log> {

    private final AppFilter ethFilter;

    public LogFilter(
            Nervosj nervosj, Callback<Log> callback,
            AppFilter ethFilter) {
        super(nervosj, callback);
        this.ethFilter = ethFilter;
    }


    @Override
    org.nervos.appchain.protocol.core.methods.response.AppFilter sendRequest() throws IOException {
        return nervosj.appNewFilter(ethFilter).send();
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
        return nervosj.appGetFilterLogs(filterId);
    }
}
