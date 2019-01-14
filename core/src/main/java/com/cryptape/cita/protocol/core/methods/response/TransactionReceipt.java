package com.cryptape.cita.protocol.core.methods.response;

import java.math.BigInteger;
import java.util.List;

import com.cryptape.cita.utils.Numeric;

/**
 * TransactionReceipt object used by {@link AppGetTransactionReceipt}.
 */
public class TransactionReceipt {
    private String transactionHash;
    private String transactionIndex;
    private String blockHash;
    private String blockNumber;
    //gas rather than quota is used as of 0.20
    private String cumulativeGasUsed;
    private String cumulativeQuotaUsed;
    private String gasUsed;
    private String quotaUsed;
    private String contractAddress;
    private String root;
    private String status;
    private String from;
    private String to;
    private List<Log> logs;
    private String logsBloom;
    private String errorMessage;

    public TransactionReceipt() {
    }

    public TransactionReceipt(String transactionHash, String transactionIndex,
                              String blockHash, String blockNumber, String cumulativeGasUsed,
                              String cumulativeQuotaUsed, String gasUsed, String quotaUsed,
                              String contractAddress, String root, String status, String from,
                              String to, List<Log> logs, String logsBloom, String errorMessage) {
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.cumulativeQuotaUsed = cumulativeQuotaUsed;
        this.gasUsed = gasUsed;
        this.quotaUsed = quotaUsed;
        this.contractAddress = contractAddress;
        this.root = root;
        this.status = status;
        this.from = from;
        this.to = to;
        this.logs = logs;
        this.logsBloom = logsBloom;
        this.errorMessage = errorMessage;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public BigInteger getTransactionIndex() {
        return Numeric.decodeQuantity(transactionIndex);
    }

    public String getTransactionIndexRaw() {
        return transactionIndex;
    }

    public void setTransactionIndex(String transactionIndex) {
        this.transactionIndex = transactionIndex;
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

    public BigInteger getCumulativeGasUsed() {
        if (this.cumulativeGasUsed != null) {
            return Numeric.decodeQuantity(cumulativeGasUsed);
        } else {
            return null;
        }
    }

    public String getCumulativeGasUsedRaw() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(String cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public BigInteger getCumulativeQuotaUsed() {
        if (this.cumulativeQuotaUsed != null) {
            return Numeric.decodeQuantity(cumulativeQuotaUsed);
        } else {
            return null;
        }
    }

    public String getCumulativeQuotaUsedRaw() {
        return cumulativeQuotaUsed;
    }

    public void setCumulativeQuotaUsed(String cumulativeQuotaUsed) {
        this.cumulativeQuotaUsed = cumulativeQuotaUsed;
    }

    public BigInteger getGasUsed() {
        if (this.gasUsed != null) {
            return Numeric.decodeQuantity(gasUsed);
        } else {
            return null;
        }
    }

    public String getGasUsedRaw() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public BigInteger getQuotaUsed() {
        if (this.quotaUsed != null) {
            return Numeric.decodeQuantity(quotaUsed);
        } else {
            return null;
        }
    }

    public String getQuotaUsedRaw() {
        return quotaUsed;
    }

    public void setQuotaUsed(String quotaUsed) {
        this.quotaUsed = quotaUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionReceipt)) {
            return false;
        }

        TransactionReceipt that = (TransactionReceipt) o;

        if (getTransactionHash() != null
                ? !getTransactionHash().equals(that.getTransactionHash())
                : that.getTransactionHash() != null) {
            return false;
        }
        if (transactionIndex != null
                ? !transactionIndex.equals(that.transactionIndex)
                : that.transactionIndex != null) {
            return false;
        }
        if (getBlockHash() != null
                ? !getBlockHash().equals(that.getBlockHash())
                : that.getBlockHash() != null) {
            return false;
        }
        if (blockNumber != null
                ? !blockNumber.equals(that.blockNumber) : that.blockNumber != null) {
            return false;
        }
        if (cumulativeQuotaUsed != null
                ? !cumulativeQuotaUsed.equals(that.cumulativeQuotaUsed)
                : that.cumulativeQuotaUsed != null) {
            return false;
        }
        if (cumulativeGasUsed != null
                ? !cumulativeGasUsed.equals(that.cumulativeGasUsed)
                : that.cumulativeGasUsed != null) {
            return false;
        }
        if (quotaUsed != null ? !quotaUsed.equals(that.quotaUsed) : that.quotaUsed != null) {
            return false;
        }
        if (gasUsed != null ? !gasUsed.equals(that.gasUsed) : that.gasUsed != null) {
            return false;
        }
        if (getContractAddress() != null
                ? !getContractAddress().equals(that.getContractAddress())
                : that.getContractAddress() != null) {
            return false;
        }
        if (getRoot() != null
                ? !getRoot().equals(that.getRoot()) : that.getRoot() != null) {
            return false;
        }
        if (getStatus() != null
                ? !getStatus().equals(that.getStatus()) : that.getStatus() != null) {
            return false;
        }
        if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
            return false;
        }
        if (getTo() != null ? !getTo().equals(that.getTo()) : that.getTo() != null) {
            return false;
        }
        if (getLogs() != null ? !getLogs().equals(that.getLogs()) : that.getLogs() != null) {
            return false;
        }
        if (getLogsBloom() != null
                ? !getLogsBloom().equals(that.getLogsBloom()) : that.getLogsBloom() != null) {
            return false;
        }
        return getErrorMessage() != null
                ? getErrorMessage().equals(that.getErrorMessage()) : that.getErrorMessage() == null;
    }

    @Override
    public int hashCode() {
        int result = getTransactionHash() != null ? getTransactionHash().hashCode() : 0;
        result = 31 * result + (transactionIndex != null ? transactionIndex.hashCode() : 0);
        result = 31 * result + (getBlockHash() != null ? getBlockHash().hashCode() : 0);
        result = 31 * result + (blockNumber != null ? blockNumber.hashCode() : 0);
        result = 31 * result + (cumulativeQuotaUsed != null ? cumulativeQuotaUsed.hashCode() : 0);
        result = 31 * result + (cumulativeGasUsed != null ? cumulativeGasUsed.hashCode() : 0);
        result = 31 * result + (quotaUsed != null ? quotaUsed.hashCode() : 0);
        result = 31 * result + (gasUsed != null ? gasUsed.hashCode() : 0);
        result = 31 * result + (getContractAddress() != null ? getContractAddress().hashCode() : 0);
        result = 31 * result + (getRoot() != null ? getRoot().hashCode() : 0);
        result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        result = 31 * result + (getLogs() != null ? getLogs().hashCode() : 0);
        result = 31 * result + (getLogsBloom() != null ? getLogsBloom().hashCode() : 0);
        result = 31 * result + (getErrorMessage() != null ? getErrorMessage().hashCode() : 0);
        return result;
    }
}
