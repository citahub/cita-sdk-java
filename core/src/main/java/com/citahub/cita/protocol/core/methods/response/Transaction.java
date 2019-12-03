package com.citahub.cita.protocol.core.methods.response;

import java.math.BigInteger;
import java.security.SignatureException;

import com.citahub.cita.utils.Numeric;
import com.citahub.cita.utils.TransactionUtil;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Transaction object used by both {@link AppTransaction} and {@link AppBlock}.
 */
public class Transaction {
    private String hash;
    private String blockHash;
    private String blockNumber;
    private String content;
    private String index;
    private String from;

    public Transaction() {
    }

    public Transaction(String hash, String blockHash, String blockNumber,
                        String content, String index, String from) {
        this.hash = hash;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.content = content;
        this.index = index;
        this.from = from;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean verifySignature(String addr)
            throws InvalidProtocolBufferException, SignatureException {
        return TransactionUtil.verifySignature(addr, content);
    }

    public com.citahub.cita.protocol.core.methods.request.Transaction
            decodeContent() throws InvalidProtocolBufferException {
        return TransactionUtil.decodeContent(content);
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

        if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
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
        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        return result;
    }
}
