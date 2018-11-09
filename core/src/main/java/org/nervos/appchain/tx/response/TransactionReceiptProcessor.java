package org.nervos.appchain.tx.response;

import java.io.IOException;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.exceptions.TransactionException;

/**
 * Abstraction for managing how we wait for transaction receipts to be generated on the network.
 */
public abstract class TransactionReceiptProcessor {

    private final AppChainj appChainj;

    public TransactionReceiptProcessor(AppChainj appChainj) {
        this.appChainj = appChainj;
    }

    public abstract TransactionReceipt waitForTransactionReceipt(
            String transactionHash)
            throws IOException, TransactionException;

    TransactionReceipt sendTransactionReceiptRequest(
            String transactionHash) throws IOException, TransactionException {
        AppGetTransactionReceipt transactionReceipt =
                appChainj.appGetTransactionReceipt(transactionHash).send();
        if (transactionReceipt.hasError()) {
            throw new TransactionException("Error processing request: "
                    + transactionReceipt.getError().getMessage());
        }

        return transactionReceipt.getTransactionReceipt();
    }
}
