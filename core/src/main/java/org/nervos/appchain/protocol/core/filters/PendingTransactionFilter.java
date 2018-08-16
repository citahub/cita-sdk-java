package org.nervos.appchain.protocol.core.filters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.response.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppLog;

/**
 * Handler for working with transaction filter requests.
 */
public class PendingTransactionFilter extends Filter<String> {

    public PendingTransactionFilter(Nervosj nervosj, Callback<String> callback) {
        super(nervosj, callback);
    }

    @Override
    AppFilter sendRequest() throws IOException {
        return nervosj.appNewPendingTransactionFilter().send();
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
     * Since the pending transaction filter does not support historic filters,
     * the filterId is ignored and an empty optional is returned
     * @param filterId
     * Id of the filter for which the historic log should be retrieved
     * @return
     * Optional.empty()
     */
    @Override
    protected Request<?, AppLog> getFilterLogs(BigInteger filterId) {
        return null;
    }
}
