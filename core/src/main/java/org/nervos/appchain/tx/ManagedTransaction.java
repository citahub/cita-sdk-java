package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.exceptions.TransactionException;


/**
 * Generic transaction manager.
 */

/// TODO this includes ethereum methods like gasLimit gasPrice. Remove them
public abstract class ManagedTransaction {

    public static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);

    protected Nervosj nervosj;

    protected TransactionManager transactionManager;


    protected ManagedTransaction(Nervosj nervosj, TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.nervosj = nervosj;
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
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version , int chainId, String value)
            throws IOException, TransactionException {
        return transactionManager.executeTransaction(
                to, data, quota, nonce, validUntilBlock, version, chainId, value);
    }
}
