package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;

public class CitaTransactionManager extends TransactionManager {

    private final Nervosj nervosj;
    final Credentials credentials;

    public CitaTransactionManager(Nervosj nervosj, Credentials credentials) {
        super(nervosj, credentials.getAddress());
        this.nervosj = nervosj;
        this.credentials = credentials;

    }

    public CitaTransactionManager(
            Nervosj nervosj, Credentials credentials, int attempts, int sleepDuration) {
        super(nervosj, attempts, sleepDuration, credentials.getAddress());
        this.nervosj = nervosj;
        this.credentials = credentials;
    }

    BigInteger getNonce() throws IOException {
        AppGetTransactionCount ethGetTransactionCount = nervosj.appGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).send();

        return ethGetTransactionCount.getTransactionCount();
    }

    @Override
    public AppSendTransaction sendTransaction(
            BigInteger quota, BigInteger nonce, String to,
            String data, String value) throws IOException {
        return new AppSendTransaction();
    }

    // adapt to cita
    @Override
    public AppSendTransaction sendTransaction(
            String to, String data, BigInteger quota, BigInteger nonce,
            BigInteger validUntilBlock, BigInteger version, int chainId, String value)
            throws IOException {
        Transaction transaction = new Transaction(
                to, nonce, quota.longValue(), validUntilBlock.longValue(),
                version.intValue(), chainId, value, data);
        return nervosj.appSendRawTransaction(transaction.sign(credentials)).send();
    }

    // adapt to cita
    public CompletableFuture<AppSendTransaction> sendTransactionAsync(
            String to, String data, BigInteger quota, BigInteger nonce,
            BigInteger validUntilBlock, BigInteger version, int chainId, String value)
            throws IOException {
        Transaction transaction = new Transaction(
                to, nonce, quota.longValue(), validUntilBlock.longValue(),
                version.intValue(), chainId, value, data);
        return nervosj.appSendRawTransaction(transaction.sign(credentials)).sendAsync();
    }

    @Override
    public String getFromAddress() {
        return credentials.getAddress();
    }
}
