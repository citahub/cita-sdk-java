package org.nervos.appchain.protocol.core.methods.request;

import java.math.BigInteger;
import java.util.Objects;
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
import org.nervos.appchain.utils.Strings;

import static org.abstractj.kalium.encoders.Encoder.HEX;
import static org.nervos.appchain.utils.Numeric.cleanHexPrefix;
import static org.nervos.appchain.utils.Numeric.prependHexPrefix;

/**
 * Transaction request object used the below methods.
 * <ol>
 * <li>appCall</li>
 * <li>appSendTransaction</li>
 * </ol>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {
    private static final BigInteger MAX_VALUE = BigInteger.valueOf(2).pow(256).subtract(BigInteger.ONE);
    private String to;
    private String nonce;  // nonce field is not present on eth_call/eth_estimateGas
    private long quota;  // gas
    private long validUntilBlock;
    private int version = 0;
    private String data;
    private String value;
    private BigInteger chainId;

    public Transaction(String to, String nonce, long quota, long validUntilBlock, int version,
                       BigInteger chainId, String value, String data) {
        this.to = to;
        this.quota = quota;
        this.version = version;
        this.validUntilBlock = validUntilBlock;
        this.chainId = chainId;
        this.value = value;
        this.data = data != null ? prependHexPrefix(data) : "";
        this.nonce = processNonce(nonce);
        this.value = processValue(value);
        this.to = processTo(to);
    }



    public static Transaction createContractTransaction(String nonce, long quota, long validUntilBlock,
            int version, BigInteger chainId, String value, String init) {
        return new Transaction("", nonce, quota, validUntilBlock, version, chainId, value, init);
    }

    public static Transaction createContractTransaction(String nonce, long quota, long validUntilBlock,
            int version, BigInteger chainId, String value, String contractCode, String constructorCode) {
        String init = contractCode + cleanHexPrefix(constructorCode);
        return new Transaction("", nonce, quota, validUntilBlock, version, chainId, value, init);
    }

    public static Transaction createFunctionCallTransaction(String to, String nonce, long quota, long validUntilBlock,
            int version, BigInteger chainId, String value, String data) {
        return new Transaction(to, nonce, quota, validUntilBlock, version, chainId, value, data);
    }

    public static Transaction createFunctionCallTransaction(String to, String nonce, long quota, long validUntilBlock,
            int version, BigInteger chainId, String value,  byte[] data) {
        return new Transaction(to, nonce, quota, validUntilBlock, version, chainId, value, new String(data));
    }

    /*
    * sign consists of 3 parts:
    * 1. serialize raw transaction
    * 2. get signature from transaction
    * 3. add serialized raw transaction and signature together
    * */
    public String sign(String privateKey, boolean isEd25519AndBlake2b, boolean isByteArray) {
        byte[] tx = serializeRawTransaction(isByteArray);
        return serializeUnverifiedTransaction(getSignature(privateKey, tx, isEd25519AndBlake2b), tx);
    }

    public String sign(String privateKey) {
        return sign(privateKey, false, false);
    }

    // just used to secp256k1
    public String sign(Credentials credentials) {
        return sign(credentials.getEcKeyPair().getPrivateKey().toString(16), false, false);
    }

    public String sign(Signature signature) {
        byte[] tx = serializeRawTransaction(false);
        return serializeUnverifiedTransaction(signature.getSignature(tx), tx);
    }

    public byte[] serializeRawTransaction(boolean isByteArray) {
        Blockchain.Transaction.Builder builder = Blockchain.Transaction.newBuilder();
        ByteString byteData = ByteString.copyFrom(isByteArray ? data.getBytes() : ConvertStrByte.hexStringToBytes(cleanHexPrefix(data)));
        ByteString byteValue = ByteString.copyFrom(ConvertStrByte.hexStringToBytes(cleanHexPrefix(value), 256));

        builder.setData(byteData).setNonce(nonce).setValidUntilBlock(validUntilBlock)
                .setQuota(quota).setValue(byteValue).setVersion(version);


        /*
        * version 0: cita 0.19
        * version 1: cita 0.20
        * */
        if (version == 0) {
            builder.setTo(to).setChainId(chainId.intValue());
        } else if (version == 1) {
            builder.setToV1(ByteString.copyFrom(ConvertStrByte.hexStringToBytes(to)))
                .setChainIdV1(ByteString.copyFrom(ConvertStrByte.hexStringToBytes(Numeric.toHexStringNoPrefix(chainId), 256)));
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
            utx = Blockchain.UnverifiedTransaction.newBuilder()
                    .setTransaction(Blockchain.Transaction.parseFrom(tx))
                    .setSignature(ByteString.copyFrom(sig))
                    .setCrypto(Blockchain.Crypto.SECP)
                    .build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        String txStr = ConvertStrByte.bytesToHexString(Objects.requireNonNull(utx).toByteArray());
        return prependHexPrefix(txStr);
    }


    private static String processNonce(String nonce) {
        if (nonce == null || nonce.isEmpty()) {
            return String.valueOf(Math.abs(new Random(System.currentTimeMillis()).nextLong()));
        }
        return nonce;
    }

    private static String processValue(String value) {
        if (Strings.isEmpty(value)) {
            return "0";
        } else {
            BigInteger valueBigInt = value.matches("0[xX][0-9a-fA-F]+") ? Numeric.toBigInt(value) : new BigInteger(value);
            if (Transaction.MAX_VALUE.compareTo(valueBigInt) > 0) {
                return valueBigInt.toString(16);
            } else {
                System.out.println("Value you input is out of bound");
                throw new IllegalArgumentException(
                        "Value you input for the transaction is out of bound. "
                                + "\nThe upper bound of value is: " + MAX_VALUE.toString(16)
                                + " (" + MAX_VALUE + ")");
            }
        }
    }

    private static String processTo(String to) {
        if (!Keys.verifyAddress(to) && to.matches("^(0x|0X)?")) {
            throw new IllegalArgumentException("Address is not in correct format.");
        }
        return cleanHexPrefix(to).toLowerCase();
    }
}
