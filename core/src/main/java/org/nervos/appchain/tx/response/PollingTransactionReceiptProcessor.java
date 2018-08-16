package org.nervos.appchain.tx.response;

import java.io.IOException;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.exceptions.TransactionException;

/**
 * With each provided transaction hash, poll until we obtain a transaction receipt.
 */
public class PollingTransactionReceiptProcessor extends TransactionReceiptProcessor {

    private final long sleepDuration;
    private final int attempts;

    public PollingTransactionReceiptProcessor(Nervosj nervosj, long sleepDuration, int attempts) {
        super(nervosj);
        this.sleepDuration = sleepDuration;
        this.attempts = attempts;
    }

    @Override
    public TransactionReceipt waitForTransactionReceipt(
            String transactionHash)
            throws IOException, TransactionException {

        return getTransactionReceipt(transactionHash, sleepDuration, attempts);
    }

    private TransactionReceipt getTransactionReceipt(
            String transactionHash, long sleepDuration, int attempts)
            throws IOException, TransactionException {

        TransactionReceipt transactionReceipt =
                sendTransactionReceiptRequest(transactionHash);
        for (int i = 0; i < attempts; i++) {
            if (transactionReceipt == null) {
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    throw new TransactionException(e);
                }
                transactionReceipt = sendTransactionReceiptRequest(transactionHash);
            } else {
                return transactionReceipt;
            }
        }

        throw new TransactionException("Transaction receipt was not generated after "
                + ((sleepDuration * attempts) / 1000
                + " seconds for transaction: " + transactionHash));
    }
}
