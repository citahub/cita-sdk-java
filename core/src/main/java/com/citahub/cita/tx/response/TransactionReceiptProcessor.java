package com.cryptape.cita.tx.response;

import java.io.IOException;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.cryptape.cita.protocol.core.methods.response.AppGetTransactionReceipt;
import com.cryptape.cita.protocol.exceptions.TransactionException;

/**
 * Abstraction for managing how we wait for transaction receipts to be generated on the network.
 */
public abstract class TransactionReceiptProcessor {

    private final CITAj citaj;

    public TransactionReceiptProcessor(CITAj citaj) {
        this.citaj = citaj;
    }

    public abstract TransactionReceipt waitForTransactionReceipt(
            String transactionHash)
            throws IOException, TransactionException;

    TransactionReceipt sendTransactionReceiptRequest(
            String transactionHash) throws IOException, TransactionException {
        AppGetTransactionReceipt transactionReceipt =
                citaj.appGetTransactionReceipt(transactionHash).send();
        if (transactionReceipt.hasError()) {
            throw new TransactionException("Error processing request: "
                    + transactionReceipt.getError().getMessage());
        }

        return transactionReceipt.getTransactionReceipt();
    }
}
