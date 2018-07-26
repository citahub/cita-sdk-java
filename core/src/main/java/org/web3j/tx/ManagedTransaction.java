package org.web3j.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;


/**
 * Generic transaction manager.
 */

/// TODO this includes ethereum methods like gasLimit gasPrice. Remove them
public abstract class ManagedTransaction {

    public static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);

    protected Web3j web3j;

    protected TransactionManager transactionManager;


    protected ManagedTransaction(Web3j web3j, TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.web3j = web3j;
    }

    protected TransactionReceipt send(
            String to, String data, String value,
            BigInteger gasPrice, BigInteger gasLimit)
            throws IOException, TransactionException {

        return transactionManager.executeTransaction(
                gasPrice, gasLimit, to, data, value);
    }

    // adapt to cita
    protected TransactionReceipt sendAdaptToCita(
            String to, String data, BigInteger quota, BigInteger nonce,
            BigInteger validUntilBlock, BigInteger version , int chainId, String value)
            throws IOException, TransactionException {
        return transactionManager.executeTransaction(
                to, data, quota, nonce, validUntilBlock, version, chainId, value);
    }
}
