package org.nervos.appchain.tx;

import java.io.IOException;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.exceptions.TransactionException;


/**
 * Generic transaction manager.
 */

public abstract class ManagedTransaction {

    protected AppChainj appChainj;

    protected TransactionManager transactionManager;


    protected ManagedTransaction(AppChainj appChainj, TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.appChainj = appChainj;
    }

    protected TransactionReceipt send(
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version , BigInteger chainId, String value)
            throws IOException, TransactionException {
        return transactionManager.executeTransaction(
                to, data, quota, nonce, validUntilBlock, version, chainId, value);
    }
}
