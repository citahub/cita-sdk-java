package com.citahub.cita.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Future;

import com.citahub.cita.crypto.Credentials;
import com.citahub.cita.crypto.Signature;
import com.citahub.cita.crypto.sm2.SM2KeyPair;
import com.citahub.cita.crypto.sm2.SM2Keys;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameterName;
import com.citahub.cita.protocol.core.methods.request.Transaction;
import com.citahub.cita.protocol.core.methods.response.AppGetTransactionCount;
import com.citahub.cita.protocol.core.methods.response.AppSendTransaction;
import com.citahub.cita.utils.Numeric;
import io.reactivex.Flowable;

public class RawTransactionManager extends TransactionManager {

    private final CITAj citaj;
    private Credentials credentials;
    private Signature signature;
    private Transaction.CryptoTx cryptoTx = Transaction.CryptoTx.SECP256K1;
    private SM2KeyPair keyPair;

    public RawTransactionManager(CITAj citaj, Credentials credentials) {
        super(citaj, credentials.getAddress());
        this.citaj = citaj;
        this.credentials = credentials;
    }

    public RawTransactionManager(CITAj citaj, Signature signature) {
        super(citaj, signature.getAddress());
        this.citaj = citaj;
        this.signature = signature;
    }

    public RawTransactionManager(
            CITAj citaj, Credentials credentials, int attempts, int sleepDuration) {
        super(citaj, attempts, sleepDuration, credentials.getAddress());
        this.citaj = citaj;
        this.credentials = credentials;
    }

    public RawTransactionManager(
            CITAj citaj, Signature signature, int attempts, int sleepDuration) {
        super(citaj, attempts, sleepDuration, signature.getAddress());
        this.citaj = citaj;
        this.signature = signature;
    }

    private RawTransactionManager(
            CITAj citaj, SM2KeyPair keyPair, Transaction.CryptoTx cryptoTx) {
        super(citaj, SM2Keys.getAddress(keyPair.getPublicKey()));
        this.citaj = citaj;
        this.cryptoTx = cryptoTx;
        this.keyPair = keyPair;
    }

    private RawTransactionManager(
            CITAj citaj,
            SM2KeyPair keyPair,
            Transaction.CryptoTx cryptoTx,
            int attempts,
            int sleepDuration) {
        super(citaj, attempts, sleepDuration, SM2Keys.getAddress(keyPair.getPublicKey()));
        this.citaj = citaj;
        this.cryptoTx = cryptoTx;
        this.keyPair = keyPair;
    }

    public static RawTransactionManager createSM2Manager(CITAj citaj, SM2KeyPair keyPair) {
        Transaction.CryptoTx cryptoTx = Transaction.CryptoTx.SM2;
        return new RawTransactionManager(citaj, keyPair, cryptoTx);
    }

    public static RawTransactionManager createSM2Manager(
            CITAj citaj, SM2KeyPair keyPair, int attempts, int sleepDuration) {
        Transaction.CryptoTx cryptoTx = Transaction.CryptoTx.SM2;
        return new RawTransactionManager(citaj, keyPair, cryptoTx);
    }

    BigInteger getNonce() throws IOException {
        AppGetTransactionCount ethGetTransactionCount = citaj.appGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.PENDING).send();

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
        return citaj.appSendRawTransaction(rawTx).send();
    }

    public Flowable<AppSendTransaction> sendTransactionAsync(
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
        return citaj.appSendRawTransaction(rawTx).flowable();
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
