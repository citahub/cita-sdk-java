package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.RawTransaction;
import org.nervos.appchain.crypto.TransactionEncoder;
import org.nervos.appchain.protocol.Nervosj;
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

    private final Nervosj nervosj;
    final Credentials credentials;

    private final byte chainId;

    public RawTransactionManager(Nervosj nervosj, Credentials credentials, byte chainId) {
        super(nervosj, credentials.getAddress());

        this.nervosj = nervosj;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(
            Nervosj nervosj, Credentials credentials, byte chainId,
            TransactionReceiptProcessor transactionReceiptProcessor) {
        super(transactionReceiptProcessor, credentials.getAddress());

        this.nervosj = nervosj;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(
            Nervosj nervosj, Credentials credentials,
            byte chainId, int attempts, long sleepDuration) {
        super(nervosj, attempts, sleepDuration, credentials.getAddress());

        this.nervosj = nervosj;
        this.credentials = credentials;

        this.chainId = chainId;
    }

    public RawTransactionManager(Nervosj nervosj, Credentials credentials) {
        this(nervosj, credentials, ChainId.NONE);
    }

    public RawTransactionManager(
            Nervosj nervosj, Credentials credentials, int attempts, int sleepDuration) {
        this(nervosj, credentials, ChainId.NONE, attempts, sleepDuration);
    }

    protected BigInteger getNonce() throws IOException {
        AppGetTransactionCount ethGetTransactionCount = nervosj.appGetTransactionCount(
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

        return nervosj.appSendRawTransaction(hexValue).send();
    }
}
