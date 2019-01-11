package com.cryptape.cita.tx;

import java.io.IOException;
import java.math.BigInteger;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.cryptape.cita.protocol.exceptions.TransactionException;


/**
 * Generic transaction manager.
 */

public abstract class ManagedTransaction {

    protected CITAj citaj;

    protected TransactionManager transactionManager;


    protected ManagedTransaction(CITAj citaj, TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.citaj = citaj;
    }

    protected TransactionReceipt send(
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version , BigInteger chainId, String value)
            throws IOException, TransactionException {
        return transactionManager.executeTransaction(
                to, data, quota, nonce, validUntilBlock, version, chainId, value);
    }
}
