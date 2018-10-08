package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.Signature;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;

public class CitaTransactionManager extends TransactionManager {

    private final Nervosj nervosj;
    private Credentials credentials;
    private Signature signature;

    public CitaTransactionManager(Nervosj nervosj, Credentials credentials) {
        super(nervosj, credentials.getAddress());
        this.nervosj = nervosj;
        this.credentials = credentials;

    }

    public CitaTransactionManager(Nervosj nervosj, Signature signature) {
        super(nervosj, signature.getAddress());
        this.nervosj = nervosj;
        this.signature = signature;
    }

    public CitaTransactionManager(
            Nervosj nervosj, Credentials credentials, int attempts, int sleepDuration) {
        super(nervosj, attempts, sleepDuration, credentials.getAddress());
        this.nervosj = nervosj;
        this.credentials = credentials;
    }

    public CitaTransactionManager(
            Nervosj nervosj, Signature signature, int attempts, int sleepDuration) {
        super(nervosj, attempts, sleepDuration, signature.getAddress());
        this.nervosj = nervosj;
        this.signature = signature;
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
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version, int chainId, String value)
            throws IOException {
        Transaction transaction = new Transaction(
                to, nonce, quota, validUntilBlock,
                version, chainId, value, data);
        String rawTx = null;
        if (this.credentials != null) {
            rawTx = transaction.sign(this.credentials);
        } else if (this.signature != null) {
            rawTx = transaction.sign(this.signature);
        }
        return nervosj.appSendRawTransaction(rawTx).send();
    }

    // adapt to cita
    public CompletableFuture<AppSendTransaction> sendTransactionAsync(
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version, int chainId, String value) {
        Transaction transaction = new Transaction(
                to, nonce, quota, validUntilBlock,
                version, chainId, value, data);
        String rawTx = null;
        if (this.credentials != null) {
            rawTx = transaction.sign(this.credentials);
        } else if (this.signature != null) {
            rawTx = transaction.sign(this.signature);
        }
        return nervosj.appSendRawTransaction(rawTx).sendAsync();
    }

    @Override
    public String getFromAddress() {
        if (credentials != null) {
            return credentials.getAddress();
        } else {
            return signature.getAddress();
        }
    }

    public String getFromAddress(boolean isCredential) {
        if (isCredential) {
            return credentials.getAddress();
        } else {
            return signature.getAddress();
        }
    }
}
