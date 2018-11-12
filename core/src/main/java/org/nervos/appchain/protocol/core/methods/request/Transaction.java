package org.nervos.appchain.protocol.core.methods.request;

import java.math.BigInteger;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.abstractj.kalium.crypto.Hash;
import org.abstractj.kalium.keys.SigningKey;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.ECKeyPair;
import org.nervos.appchain.crypto.Keys;
import org.nervos.appchain.crypto.Sign;
import org.nervos.appchain.crypto.Signature;
import org.nervos.appchain.protobuf.Blockchain;
import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.utils.Numeric;

import static org.abstractj.kalium.encoders.Encoder.HEX;
import static org.nervos.appchain.utils.Numeric.cleanHexPrefix;

/**
 * Transaction request object used the below methods.
 * <ol>
 * <li>appCall</li>
 * <li>appSendTransaction</li>
 * </ol>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {

    private String to;
    private String nonce;  // nonce field is not present on eth_call/eth_estimateGas
    private long quota;  // gas
    private long validUntilBlock;
    private int version = 0;
    private String data;
    private String value;
    private int chainId;
    private final Hash hash = new Hash();
    private static final BigInteger MAX_VALUE
            = new BigInteger(
                    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);

    public Transaction(
            String to, String nonce, long quota, long validUntilBlock,
            int version, int chainId, String value, String data) {
        this.to = to;
        this.quota = quota;
        this.version = version;
        this.validUntilBlock = validUntilBlock;
        this.chainId = chainId;
        this.value = value;

        if (data != null) {
            this.data = Numeric.prependHexPrefix(data);
        }

        this.nonce = processNonce(nonce);
        this.value = processValue(value);
        this.to = processTo(to);
    }

    private static String processNonce(String nonce) {
        if (nonce == null || nonce.isEmpty()) {
            Random random = new Random(System.currentTimeMillis());
            return String.valueOf(Math.abs(random.nextLong()));
        }
        return nonce;
    }

    private static String processValue(String value) {
        String result = "";
        if (value == null || value.isEmpty()) {
            result = "0";
        } else if (value.matches("0[xX][0-9a-fA-F]+")) {
            result = value.substring(2);
        } else {
            result = new BigInteger(value).toString(16);
        }

        BigInteger valueBigInt = new BigInteger(result, 16);

        if (Transaction.MAX_VALUE.compareTo(valueBigInt) > 0) {
            return result;
        } else {
            System.out.println("Value you input is out of bound");
            throw new IllegalArgumentException(
                    "Value you input for the transaction is out of bound. "
                            + "\nThe upper bound of value is: " + MAX_VALUE.toString(16)
                            + " (" + MAX_VALUE + ")");
        }
    }

    private static String processTo(String to) {
        if (!Keys.verifyAddress(to)) {
            if (!to.matches("^(0x|0X)?")) {
                throw new IllegalArgumentException("Address is not in correct format.");
            }
        }
        return cleanHexPrefix(to).toLowerCase();
    }

    public static Transaction createContractTransaction(
            String nonce, long quota, long validUntilBlock,
            int version, int chainId, String value, String init) {
        return new Transaction("", nonce, quota, validUntilBlock, version, chainId, value, init);
    }

    public static Transaction createFunctionCallTransaction(
            String to, String nonce, long quota, long validUntilBlock,
            int version, int chainId, String value, String data) {
        return new Transaction(to, nonce, quota, validUntilBlock, version, chainId, value, data);
    }

    public static Transaction createFunctionCallTransaction(
            String to, String nonce, long quota, long validUntilBlock,
            int version, int chainId, String value,  byte[] data) {

        return new Transaction(
                to, nonce, quota, validUntilBlock, version, chainId, value, new String(data));
    }

    public String getTo() {
        return to;
    }

    public String getNonce() {
        return nonce;
    }

    public long getQuota() {
        return quota;
    }

    public long get_valid_until_block() {
        return validUntilBlock;
    }

    public int getVersion() {
        return version;
    }

    public String getData() {
        return data;
    }

    public int getChainId() {
        return chainId;
    }

    public String getValue() {
        return value;
    }

    /*
    * sign consists of 3 parts:
    * 1. serialize raw transaction
    * 2. get signature from transaction
    * 3. add serialized raw transaction and signature together
    * */
    public String sign(String privateKey, boolean isEd25519AndBlake2b, boolean isByteArray) {
        byte[] tx = this.serializeRawTransaction(isByteArray);
        byte[] sig = this.getSignature(privateKey, tx, isEd25519AndBlake2b);
        return this.serializeUnverifiedTransaction(sig, tx);
    }

    public String sign(String privateKey) {
        return sign(privateKey, false, false);
    }

    // just used to secp256k1
    public String sign(Credentials credentials) {
        byte[] tx = this.serializeRawTransaction(false);
        byte[] sig = this.getSignature(credentials, tx);
        return this.serializeUnverifiedTransaction(sig, tx);
    }

    public String sign(Signature signature) {
        byte[] tx = serializeRawTransaction(false);
        byte[] sig = signature.getSignature(tx);
        return serializeUnverifiedTransaction(sig, tx);
    }

    public byte[] serializeRawTransaction(boolean isByteArray) {
        Blockchain.Transaction.Builder builder = Blockchain.Transaction.newBuilder();
        byte[] strbyte;
        if (isByteArray) {
            strbyte = getData().getBytes();
        } else {
            strbyte = ConvertStrByte.hexStringToBytes(cleanHexPrefix(getData()));
        }
        ByteString bdata = ByteString.copyFrom(strbyte);

        byte[] byteValue = ConvertStrByte.hexStringToBytes(
                cleanHexPrefix(getValue()), 256);
        ByteString bvalue = ByteString.copyFrom(byteValue);

        builder.setData(bdata);
        builder.setNonce(getNonce());
        builder.setValidUntilBlock(get_valid_until_block());
        builder.setQuota(getQuota());
        builder.setValue(bvalue);
        builder.setVersion(getVersion());

        /*
        * version 0: cita 0.19
        * version 1: cita 0.20
        * */
        if (getVersion() == 0) {
            builder.setTo(getTo());
            builder.setChainId(getChainId());
        } else if (getVersion() == 1) {
            builder.setToV1(ByteString.copyFrom(
                    ConvertStrByte.hexStringToBytes(getTo())));
            builder.setChainIdV1(ByteString.copyFrom(
                    ConvertStrByte.hexStringToBytes(
                            cleanHexPrefix(Integer.toHexString(getChainId())), 256)));
        }

        return builder.build().toByteArray();
    }

    public byte[] getSignature(Credentials credentials, byte[] tx) {
        ECKeyPair keyPair = credentials.getEcKeyPair();
        Sign.SignatureData signatureData = Sign.signMessage(tx, keyPair);
        return signatureData.get_signature();
    }

    public byte[] getSignature(String privateKey, byte[] tx, boolean isEd25519AndBlake2b) {

        if (!Keys.verifyPrivateKey(privateKey)) {
            throw new IllegalArgumentException("private key is not in correct format.");
        }
        privateKey = privateKey.toLowerCase();

        Hash hash = new Hash();
        byte[] sig;

        if (isEd25519AndBlake2b) {
            byte[] message = hash.blake2(tx, "CryptapeCryptape".getBytes(), null, null);
            SigningKey key = new SigningKey(privateKey, HEX);
            byte[] pk = key.getVerifyKey().toBytes();
            byte[] signature = key.sign(message);
            sig = new byte[signature.length + pk.length];
            System.arraycopy(signature, 0, sig, 0, signature.length);
            System.arraycopy(pk, 0, sig, signature.length, pk.length);
        } else {
            Credentials credentials = Credentials.create(privateKey);
            ECKeyPair keyPair = credentials.getEcKeyPair();
            Sign.SignatureData signatureData = Sign.signMessage(tx, keyPair);
            sig = signatureData.get_signature();
        }
        return sig;
    }

    public String serializeUnverifiedTransaction(byte[] sig, byte[] tx) {
        Blockchain.UnverifiedTransaction utx = null;
        try {
            Blockchain.Transaction transaction = Blockchain.Transaction.parseFrom(tx);
            Blockchain.UnverifiedTransaction.Builder builder =
                    Blockchain.UnverifiedTransaction.newBuilder();
            builder.setTransaction(transaction);
            builder.setSignature(ByteString.copyFrom(sig));
            builder.setCrypto(Blockchain.Crypto.SECP);
            utx = builder.build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        String txStr = ConvertStrByte.bytesToHexString(utx.toByteArray());
        return Numeric.prependHexPrefix(txStr);
    }
}
