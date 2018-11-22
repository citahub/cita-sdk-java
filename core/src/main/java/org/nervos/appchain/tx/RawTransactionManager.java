package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Future;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.Signature;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;

public class RawTransactionManager extends TransactionManager {

    private final AppChainj appChainj;
    private Credentials credentials;
    private Signature signature;

    public RawTransactionManager(AppChainj appChainj, Credentials credentials) {
        super(appChainj, credentials.getAddress());
        this.appChainj = appChainj;
        this.credentials = credentials;

    }

    public RawTransactionManager(AppChainj appChainj, Signature signature) {
        super(appChainj, signature.getAddress());
        this.appChainj = appChainj;
        this.signature = signature;
    }

    public RawTransactionManager(
            AppChainj appChainj, Credentials credentials, int attempts, int sleepDuration) {
        super(appChainj, attempts, sleepDuration, credentials.getAddress());
        this.appChainj = appChainj;
        this.credentials = credentials;
    }

    public RawTransactionManager(
            AppChainj appChainj, Signature signature, int attempts, int sleepDuration) {
        super(appChainj, attempts, sleepDuration, signature.getAddress());
        this.appChainj = appChainj;
        this.signature = signature;
    }

    BigInteger getNonce() throws IOException {
        AppGetTransactionCount ethGetTransactionCount = appChainj.appGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).send();

        return ethGetTransactionCount.getTransactionCount();
    }


    @Override
    public AppSendTransaction sendTransaction(
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version, BigInteger chainId, String value)
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
        return appChainj.appSendRawTransaction(rawTx).send();
    }

    public Future<AppSendTransaction> sendTransactionAsync(
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version, BigInteger chainId, String value) {
        Transaction transaction = new Transaction(
                to, nonce, quota, validUntilBlock,
                version, chainId, value, data);
        String rawTx = null;
        if (this.credentials != null) {
            rawTx = transaction.sign(this.credentials);
        } else if (this.signature != null) {
            rawTx = transaction.sign(this.signature);
        }
        return appChainj.appSendRawTransaction(rawTx).sendAsync();
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
