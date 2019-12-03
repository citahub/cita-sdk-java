package com.cryptape.cita.protocol.core.filters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import com.cryptape.cita.protocol.core.methods.response.AppFilter;
import com.cryptape.cita.protocol.core.methods.response.AppLog;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.Request;

/**
 * Handler for working with block filter requests.
 */
public class BlockFilter extends Filter<String> {

    public BlockFilter(CITAj citaj, Callback<String> callback) {
        super(citaj, callback);
    }

    @Override
    AppFilter sendRequest() throws IOException {
        return citaj.appNewBlockFilter().send();
    }

    @Override
    void process(List<AppLog.LogResult> logResults) {
        for (AppLog.LogResult logResult : logResults) {
            if (logResult instanceof AppLog.Hash) {
                String blockHash = ((AppLog.Hash) logResult).get();
                callback.onEvent(blockHash);
            } else {
                throw new FilterException(
                        "Unexpected result type: " + logResult.get() + ", required Hash");
            }
        }
    }

    /**
     * Since the block filter does not support historic filters, the filterId is ignored
     * and an empty optional is returned.
     * @param filterId
     * Id of the filter for which the historic log should be retrieved
     */
    @Override
    protected Request<?, AppLog> getFilterLogs(BigInteger filterId) {
        return citaj.appGetFilterLogs(filterId);
    }
}

