package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Future;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.Signature;
import org.nervos.appchain.crypto.sm2.SM2;
import org.nervos.appchain.crypto.sm2.SM2KeyPair;
import org.nervos.appchain.crypto.sm2.SM2Keys;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.utils.Numeric;

public class RawTransactionManager extends TransactionManager {

    private final AppChainj appChainj;
    private Credentials credentials;
    private Signature signature;
    private Transaction.CryptoTx cryptoTx = Transaction.CryptoTx.SECP256K1;
    private SM2KeyPair keyPair;

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

    private RawTransactionManager(
            AppChainj appChainj, SM2KeyPair keyPair, Transaction.CryptoTx cryptoTx) {
        super(appChainj, SM2Keys.getAddress(keyPair.getPublicKey()));
        this.appChainj = appChainj;
        this.cryptoTx = cryptoTx;
        this.keyPair = keyPair;
    }

    private RawTransactionManager(
            AppChainj appChainj,
            SM2KeyPair keyPair,
            Transaction.CryptoTx cryptoTx,
            int attempts,
            int sleepDuration) {
        super(appChainj, attempts, sleepDuration, SM2Keys.getAddress(keyPair.getPublicKey()));
        this.appChainj = appChainj;
        this.cryptoTx = cryptoTx;
        this.keyPair = keyPair;
    }

    public static RawTransactionManager createSM2Manager(AppChainj appChainj, SM2KeyPair keyPair) {
        Transaction.CryptoTx cryptoTx = Transaction.CryptoTx.SM2;
        return new RawTransactionManager(appChainj, keyPair, cryptoTx);
    }

    public static RawTransactionManager createSM2Manager(
            AppChainj appChainj, SM2KeyPair keyPair, int attempts, int sleepDuration) {
        Transaction.CryptoTx cryptoTx = Transaction.CryptoTx.SM2;
        return new RawTransactionManager(appChainj, keyPair, cryptoTx);
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
        if (this.cryptoTx == Transaction.CryptoTx.SM2) {
            rawTx = transaction.sign(
                    this.keyPair.getPrivateKey().toString(16), this.cryptoTx, false);
        } else if (this.credentials != null) {
            rawTx = transaction.sign(this.credentials);
        } else if (this.signature != null) {
            rawTx = transaction.sign(this.signature);
        }
        return appChainj.appSendRawTransaction(rawTx).send();
    }

    public Future<AppSendTransaction> sendTransactionAsync(
            String to, String data, long quota, String nonce,
            long validUntilBlock, int version, BigInteger chainId, String value)
            throws IOException {
        Transaction transaction = new Transaction(
                to, nonce, quota, validUntilBlock,
                version, chainId, value, data);
        String rawTx = null;
        if (this.cryptoTx == Transaction.CryptoTx.SM2) {
            rawTx = transaction.sign(
                    this.keyPair.getPrivateKey().toString(16), this.cryptoTx, false);
        } else if (this.credentials != null) {
            rawTx = transaction.sign(this.credentials);
        } else if (this.signature != null) {
            rawTx = transaction.sign(this.signature);
        }
        return appChainj.appSendRawTransaction(rawTx).sendAsync();
    }

    @Override
    public String getFromAddress() {
        if (cryptoTx == Transaction.CryptoTx.SECP256K1) {
            if (credentials != null) {
                return credentials.getAddress();
            } else {
                return signature.getAddress();
            }
        } else if (cryptoTx == Transaction.CryptoTx.SM2) {
            return Numeric.prependHexPrefix(SM2Keys.getAddress(keyPair.getPublicKey()));
        } else {
            return null;
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
