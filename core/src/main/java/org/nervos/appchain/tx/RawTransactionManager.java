package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.RawTransaction;
import org.nervos.appchain.crypto.TransactionEncoder;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.tx.response.TransactionReceiptProcessor;
import org.nervos.appchain.utils.Numeric;

/**
 * TransactionManager implementation using Ethereum wallet file to create and sign transactions
 * locally.
 *
 * <p>This transaction manager provides support for specifying the chain id for transactions as per
 * <a href="https://github.com/ethereum/EIPs/issues/155">EIP155</a>.
 */
public class RawTransactionManager extends TransactionManager {

    private final AppChainj appChainj;
    final Credentials credentials;

    private final byte chainId;

    public RawTransactionManager(AppChainj appChainj, Credentials credentials, byte chainId) {
        super(appChainj, credentials.getAddress());

        this.appChainj = appChainj;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(
            AppChainj appChainj, Credentials credentials, byte chainId,
            TransactionReceiptProcessor transactionReceiptProcessor) {
        super(transactionReceiptProcessor, credentials.getAddress());

        this.appChainj = appChainj;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(
            AppChainj appChainj, Credentials credentials,
            byte chainId, int attempts, long sleepDuration) {
        super(appChainj, attempts, sleepDuration, credentials.getAddress());

        this.appChainj = appChainj;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(AppChainj appChainj, Credentials credentials) {
        this(appChainj, credentials, ChainId.NONE);
    }

    public RawTransactionManager(
            AppChainj appChainj, Credentials credentials, int attempts, int sleepDuration) {
        this(appChainj, credentials, ChainId.NONE, attempts, sleepDuration);
    }

    protected BigInteger getNonce() throws IOException {
        AppGetTransactionCount ethGetTransactionCount = appChainj.appGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.PENDING).send();

        return ethGetTransactionCount.getTransactionCount();
    }

    @Override
    public AppSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, String value) throws IOException {

        BigInteger nonce = getNonce();

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value,
                data);

        return signAndSend(rawTransaction);
    }

    public AppSendTransaction signAndSend(RawTransaction rawTransaction)
            throws IOException {

        byte[] signedMessage;

        if (chainId > ChainId.NONE) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        String hexValue = Numeric.toHexString(signedMessage);

        return appChainj.appSendRawTransaction(hexValue).send();
    }
}
