package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.JsonRpc2_0Web3j;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.exceptions.TransactionException;
import org.nervos.appchain.tx.response.PollingTransactionReceiptProcessor;
import org.nervos.appchain.tx.response.TransactionReceiptProcessor;

import static org.nervos.appchain.protocol.core.JsonRpc2_0Web3j.DEFAULT_BLOCK_TIME;

/**
 * Transaction manager abstraction for executing transactions with Ethereum client via
 * various mechanisms.
 */

///TODO this class includes ethereum methods: remove them later
public abstract class TransactionManager {

    public static final int DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH = 40;
    public static final long DEFAULT_POLLING_FREQUENCY = JsonRpc2_0Web3j.DEFAULT_BLOCK_TIME;

    private final TransactionReceiptProcessor transactionReceiptProcessor;
    private final String fromAddress;

    protected TransactionManager(
            TransactionReceiptProcessor transactionReceiptProcessor, String fromAddress) {
        this.transactionReceiptProcessor = transactionReceiptProcessor;
        this.fromAddress = fromAddress;
    }

    protected TransactionManager(Nervosj nervosj, String fromAddress) {
        this(new PollingTransactionReceiptProcessor(
                        nervosj, DEFAULT_POLLING_FREQUENCY,
                        DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH),
                fromAddress);
    }

    protected TransactionManager(
            Nervosj nervosj, int attempts, long sleepDuration, String fromAddress) {
        this(new PollingTransactionReceiptProcessor(
                nervosj, sleepDuration, attempts), fromAddress);
    }

    protected TransactionReceipt executeTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, String value)
            throws IOException, TransactionException {

        AppSendTransaction ethSendTransaction = sendTransaction(
                gasPrice, gasLimit, to, data, value);
        return processResponse(ethSendTransaction);
    }

    // adapt to cita
    protected TransactionReceipt executeTransaction(
            String to, String data, BigInteger quota,
            BigInteger nonce, BigInteger validUntilBlock,
            BigInteger version, int chainId, String value)
            throws IOException, TransactionException {
        AppSendTransaction ethSendTransaction = sendTransaction(
                to, data, quota, nonce, validUntilBlock, version, chainId, value);
        return processResponse(ethSendTransaction);
    }

    // adapt to cita, return empty EthSendTransaction default,
    // only CitaTransactionManager will override this method
    public AppSendTransaction sendTransaction(
            String to, String data, BigInteger quota,
            BigInteger nonce, BigInteger validUntilBlock,
            BigInteger version, int chainId, String value)
            throws IOException {
        return new AppSendTransaction();
    }

    public abstract AppSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, String value)
            throws IOException;

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
