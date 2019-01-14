package com.cryptape.cita.tx;

import java.io.IOException;
import java.math.BigInteger;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.JsonRpc2_0CITAj;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.cryptape.cita.tx.response.PollingTransactionReceiptProcessor;
import com.cryptape.cita.tx.response.TransactionReceiptProcessor;
import com.cryptape.cita.protocol.exceptions.TransactionException;

/**
 * Transaction manager abstraction for executing transactions with Ethereum client via
 * various mechanisms.
 */

///TODO this class includes ethereum methods: remove them later
public abstract class TransactionManager {

    public static final int DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH = 40;
    public static final long DEFAULT_POLLING_FREQUENCY = JsonRpc2_0CITAj.DEFAULT_BLOCK_TIME;

    private final TransactionReceiptProcessor transactionReceiptProcessor;
    private final String fromAddress;

    protected TransactionManager(
            TransactionReceiptProcessor transactionReceiptProcessor, String fromAddress) {
        this.transactionReceiptProcessor = transactionReceiptProcessor;
        this.fromAddress = fromAddress;
    }

    protected TransactionManager(CITAj citaj, String fromAddress) {
        this(new PollingTransactionReceiptProcessor(
                        citaj, DEFAULT_POLLING_FREQUENCY,
                        DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH),
                fromAddress);
    }

    protected TransactionManager(
            CITAj citaj, int attempts, long sleepDuration, String fromAddress) {
        this(new PollingTransactionReceiptProcessor(
                citaj, sleepDuration, attempts), fromAddress);
    }

    protected TransactionReceipt executeTransaction(
            String to, String data, long quota,
            String nonce, long validUntilBlock,
            int version, BigInteger chainId, String value)
            throws IOException, TransactionException {
        AppSendTransaction appSendTransaction = sendTransaction(
                to, data, quota, nonce, validUntilBlock, version, chainId, value);
        return processResponse(appSendTransaction);
    }

    public abstract AppSendTransaction sendTransaction(String to, String data, long quota, String nonce, long validUntilBlock,
            int version, BigInteger chainId, String value) throws IOException;

    public String getFromAddress() {
        return fromAddress;
    }

    private TransactionReceipt processResponse(AppSendTransaction transactionResponse)
            throws IOException, TransactionException {
        if (transactionResponse.hasError()) {
            throw new RuntimeException("Error processing transaction request: "
                    + transactionResponse.getError().getMessage());
        }

        String transactionHash = transactionResponse.getSendTransactionResult().getHash();

        return transactionReceiptProcessor.waitForTransactionReceipt(transactionHash);
    }


}
