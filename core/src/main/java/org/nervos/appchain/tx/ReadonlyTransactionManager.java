package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;

/**
 * Transaction manager implementation for read-only operations on smart contracts.
 */
public class ReadonlyTransactionManager extends TransactionManager {

    public ReadonlyTransactionManager(Nervosj nervosj, String fromAddress) {
        super(nervosj, fromAddress);
    }

    @Override
    public AppSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to, String data, String value)
            throws IOException {
        throw new UnsupportedOperationException(
                "Only read operations are supported by this transaction manager");
    }
}
