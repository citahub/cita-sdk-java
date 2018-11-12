package org.nervos.appchain.protocol.core.methods.response;

import java.math.BigInteger;
import java.security.SignatureException;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.nervos.appchain.crypto.Keys;
import org.nervos.appchain.crypto.Sign;
import org.nervos.appchain.protobuf.Blockchain;
import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.utils.Numeric;

/**
 * Transaction object used by both {@link AppTransaction} and {@link AppBlock}.
 */
public class Transaction {
    private String hash;
    private String blockHash;
    private String blockNumber;
    private String content;
    private String index;

    public Transaction() {
    }

    public Transaction(String hash, String blockHash, String blockNumber,
                        String content, String index) {
        this.hash = hash;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.content = content;
        this.index = index;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public BigInteger getBlockNumber() {
        return Numeric.decodeQuantity(blockNumber);
    }

    public String getBlockNumberRaw() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public boolean verifySignature(String addr)
            throws InvalidProtocolBufferException, SignatureException {

        if (!addr.matches("0[xX][0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Address should be in hex.");
        }

        if (addr.length() != 42) {
            throw new IllegalArgumentException("Length of address should be 20 bytes");
        }

        byte[] contentBytes = ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(content));
        Blockchain.UnverifiedTransaction unverifiedTx
                = Blockchain.UnverifiedTransaction.parseFrom(contentBytes);

        //signature = r (32 byte) + s (32 byte) + v (1 byte)
        ByteString sigByteString = unverifiedTx.getSignature();
        byte[] sigBytes = sigByteString.toByteArray();
        String sig = ConvertStrByte.bytesToHexString(sigBytes);

        if (sig.length() != 130) {
            throw new IllegalArgumentException("Transaction signature is not in correct format");
        }

        String r = sig.substring(0, 64);
        String s = sig.substring(64, 128);
        String v = sig.substring(128);

        byte[] bytesR = ConvertStrByte.hexStringToBytes(r);
        byte[] bytesS = ConvertStrByte.hexStringToBytes(s);
        byte[] bytesV = ConvertStrByte.hexStringToBytes(v);
        byte byteV = bytesV[0];

        String recoveredPubKey = Sign.signedMessageToKey(
                unverifiedTx.getTransaction().toByteArray(),
                new Sign.SignatureData(byteV, bytesR, bytesS)).toString(16);

        String recoveredAddr = Keys.getAddress(recoveredPubKey);

        return recoveredAddr.equals(Numeric.cleanHexPrefix(addr));
    }

    public org.nervos.appchain.protocol.core.methods.request.Transaction
            decodeContent() throws InvalidProtocolBufferException {

        Blockchain.UnverifiedTransaction unverifiedTx
                = Blockchain.UnverifiedTransaction.parseFrom(
                        ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(content)));

        Blockchain.Transaction blockChainTx = unverifiedTx.getTransaction();

        int version = blockChainTx.getVersion();
        String nonce = blockChainTx.getNonce();
        long quota = blockChainTx.getQuota();
        long validUntilBlock = blockChainTx.getValidUntilBlock();
        String data = bytestringToString(blockChainTx.getData());
        String value = bytestringToString(blockChainTx.getValue());

        String to = null;
        Integer chainId = null;

        //version 0: cita 0.19
        //version 1: cita 0.20
        if (version == 0) {
            to = blockChainTx.getTo();
            chainId = blockChainTx.getChainId();
        } else if (version == 1) {
            to = bytestringToString(blockChainTx.getToV1());
            chainId = Integer.parseInt(bytestringToString(blockChainTx.getChainIdV1()));
        }

        if (chainId == null || to == null) {
            throw new NullPointerException("Cannot get chainId or to from chain.");
        }

        return new org.nervos.appchain.protocol.core.methods.request.Transaction(
                to, nonce, quota, validUntilBlock, version, chainId, value, data);
    }

    private static String bytestringToString(ByteString byteStr) {
        return ConvertStrByte.bytesToHexString(byteStr.toByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction)) {
            return false;
        }

        Transaction that = (Transaction) o;

        if (getHash() != null ? !getHash().equals(that.getHash()) : that.getHash() != null) {
            return false;
        }

        if (getBlockHash() != null
                ? !getBlockHash().equals(that.getBlockHash()) : that.getBlockHash() != null) {
            return false;
        }

        if (getBlockNumberRaw() != null
                ? !getBlockNumberRaw().equals(that.getBlockNumberRaw())
                : that.getBlockNumberRaw() != null) {
            return false;
        }

        if (getIndex() != null ? !getIndex().equals(that.getIndex()) : that.getIndex() != null) {
            return false;
        }

        return getContent() != null
                ? getContent().equals(that.getContent()) : that.getContent() == null;
    }

    @Override
    public int hashCode() {
        int result = getHash() != null ? getHash().hashCode() : 0;
        result = 31 * result + (getBlockHash() != null ? getBlockHash().hashCode() : 0);
        result = 31 * result + (getBlockNumberRaw() != null ? getBlockNumberRaw().hashCode() : 0);
        result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
        result = 31 * result + (getIndex() != null ? getIndex().hashCode() : 0);
        return result;
    }
}
