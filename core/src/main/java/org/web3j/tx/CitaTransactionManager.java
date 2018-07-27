package org.web3j.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.AppGetTransactionCount;
import org.web3j.protocol.core.methods.response.AppSendTransaction;

public class CitaTransactionManager extends TransactionManager {

    private final Web3j web3j;
    final Credentials credentials;

    public CitaTransactionManager(Web3j web3j, Credentials credentials) {
        super(web3j, credentials.getAddress());
        this.web3j = web3j;
        this.credentials = credentials;

    }

    public CitaTransactionManager(
            Web3j web3j, Credentials credentials, int attempts, int sleepDuration) {
        super(web3j, attempts, sleepDuration, credentials.getAddress());
        this.web3j = web3j;
        this.credentials = credentials;
    }

    BigInteger getNonce() throws IOException {
        AppGetTransactionCount ethGetTransactionCount = web3j.appGetTransactionCount(
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
        return web3j.appSendRawTransaction(transaction.sign(credentials)).send();
    }

    // adapt to cita
    public CompletableFuture<AppSendTransaction> sendTransactionAsync(
            String to, String data, BigInteger quota, BigInteger nonce,
            BigInteger validUntilBlock, BigInteger version, int chainId, String value)
            throws IOException {
        Transaction transaction = new Transaction(
                to, nonce, quota.longValue(), validUntilBlock.longValue(),
                version.intValue(), chainId, value, data);
        return web3j.appSendRawTransaction(transaction.sign(credentials)).sendAsync();
    }

    @Override
    public String getFromAddress() {
        return credentials.getAddress();
    }
}
