package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.tx.response.TransactionReceiptProcessor;

/**
 * TransactionManager implementation for using an Ethereum node to transact.
 *
 * <p><b>Note</b>: accounts must be unlocked on the node for transactions to be successful.
 */
public class ClientTransactionManager extends TransactionManager {

    private final AppChainj appChainj;

    public ClientTransactionManager(
            AppChainj appChainj, String fromAddress) {
        super(appChainj, fromAddress);
        this.appChainj = appChainj;
    }

    public ClientTransactionManager(
            AppChainj appChainj, String fromAddress, int attempts, int sleepDuration) {
        super(appChainj, attempts, sleepDuration, fromAddress);
        this.appChainj = appChainj;
    }

    public ClientTransactionManager(
            AppChainj appChainj, String fromAddress,
            TransactionReceiptProcessor transactionReceiptProcessor) {
        super(transactionReceiptProcessor, fromAddress);
        this.appChainj = appChainj;
    }

    @Override
    public AppSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, String value)
            throws IOException {

        // Note: useless for use, just for compile
        Transaction transaction = new Transaction(
                to, "1", 1000000, 99, 0, 1, "0", data);

        //there is no method sendTransaction in cita so remote this
        return appChainj.appSendRawTransaction("fake data")
                .send();
    }
}
