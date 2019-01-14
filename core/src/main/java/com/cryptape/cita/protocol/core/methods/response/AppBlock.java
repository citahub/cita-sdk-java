package com.cryptape.cita.protocol.core.methods.response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.cryptape.cita.protocol.core.Response;
import com.cryptape.cita.utils.Numeric;


public class AppBlock extends Response<AppBlock.Block> {

    @Override
    @JsonDeserialize(using = AppBlock.ResponseDeserialiser.class)
    public void setResult(Block result) {
        super.setResult(result);
    }

    public Block getBlock() {
        return getResult();
    }

    public boolean isEmpty() {
        return getResult() == null;
    }

    public static class TendermintCommit {
        private String commitAddress;
        private String commit;

        public TendermintCommit() {}

        public TendermintCommit(String commitAddress, String commit) {
            this.commitAddress = commitAddress;
            this.commit = commit;
        }

        public String getCommitAddress() {
            return this.commitAddress;
        }

        public void setCommitAddress(String commitAddress) {
            this.commitAddress = commitAddress;
        }

        public String getCommit() {
            return this.commit;
        }

        public void setCommit(String commit) {
            this.commit = commit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TendermintCommit)) {
                return false;
            }

            TendermintCommit commit = (TendermintCommit) o;

            if (getCommitAddress() != null
                    ? !getCommitAddress().equals(commit.getCommitAddress())
                    : commit.getCommitAddress() != null) {
                return false;
            }
            return (getCommit() != null
                    ? getCommit().equals(commit.getCommit()) : commit.getCommit() == null);
        }

        @Override
        public int hashCode() {
            int result = getCommitAddress() != null ? getCommitAddress().hashCode() : 0;
            result = 31 * result + (getCommit() != null ? getCommit().hashCode() : 0);
            return result;
        }
    }

    public static class Tendermint {
        private String proposal;
        private String height;
        private String round;
        private TendermintCommit[] tendermintCommits;

        public Tendermint() {}

        public Tendermint(String proposal, String height,
                   String round, TendermintCommit[] tendermintCommits) {
            this.proposal = proposal;
            this.height = height;
            this.round = round;
            this.tendermintCommits = tendermintCommits;
        }

        public String getProposal() {
            return proposal;
        }

        public void setProposal(String proposal) {
            this.proposal = proposal;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getRound() {
            return round;
        }

        public void setRound(String round) {
            this.round = round;
        }

        public TendermintCommit[] getTendermintCommits() {
            return this.tendermintCommits;
        }

        public void setTendermintCommits(
                TendermintCommit[] tendermintCommits) {
            this.tendermintCommits = tendermintCommits;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Tendermint)) {
                return false;
            }

            Tendermint tendermint = (Tendermint) o;

            if (getHeight() != null
                    ? !getHeight().equals(tendermint.getHeight())
                    : tendermint.getHeight() != null) {
                return false;
            }

            if (getProposal() != null
                    ? !getProposal().equals(tendermint.getProposal())
                    : tendermint.getProposal() != null) {
                return false;
            }

            return (getRound() != null
                    ? getRound().equals(tendermint.getRound())
                    : tendermint.getRound() == null);
        }

        @Override
        public int hashCode() {
            int result = getProposal() != null ? getProposal().hashCode() : 0;
            result = 31 * result + (getHeight() != null ? getHeight().hashCode() : 0);
            result = 31 * result + (getRound() != null ? getRound().hashCode() : 0);
            return result;
        }
    }


    public static class Proof {
        private Tendermint tendermint;

        public Proof() {
        }

        public Proof(Tendermint tendermint) {
            this.tendermint = tendermint;
        }

        public Tendermint getTendermint() {
            return this.tendermint;
        }

        public void setTendermint(Tendermint tendermint) {
            this.tendermint = tendermint;
        }

        @Override
        public int hashCode() {
            return getTendermint() != null
                    ? getTendermint().hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Proof)) {
                return false;
            }

            Proof proof = (Proof) o;

            return (getTendermint() != null
                    ? getTendermint().equals(proof.getTendermint())
                    : proof.getTendermint() == null);
        }
    }


    public static class Header {
        private Long timestamp;
        private String prevHash;
        private String number;
        private String stateRoot;
        private String transactionsRoot;
        private String receiptsRoot;
        private String gasUsed;
        private Proof proof;
        private String proposer;


        public Header() {
        }

        public Header(long timestamp, String prevHash, String number,
                      String stateRoot, String transactionsRoot, String receiptsRoot,
                      String gasUsed, Proof proof, String proposer) {
            this.timestamp = timestamp;
            this.prevHash = prevHash;
            this.number = number;
            this.stateRoot = stateRoot;
            this.transactionsRoot = transactionsRoot;
            this.receiptsRoot = receiptsRoot;
            this.gasUsed = gasUsed;
            this.proof = proof;
            this.proposer = proposer;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getPrevHash() {
            return prevHash;
        }

        public void setPrevHash(String prevHash) {
            this.prevHash = prevHash;
        }

        public BigInteger getNumberDec() {
            return Numeric.decodeQuantity(number);
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getStateRoot() {
            return stateRoot;
        }

        public void setStateRoot(String stateRoot) {
            this.stateRoot = stateRoot;
        }

        public String getTransactionsRoot() {
            return transactionsRoot;
        }

        public void setTransactionsRoot(String transactionsRoot) {
            this.transactionsRoot = transactionsRoot;
        }

        public String getReceiptsRoot() {
            return receiptsRoot;
        }

        public void setReceiptsRoot(String receiptsRoot) {
            this.receiptsRoot = receiptsRoot;
        }

        public BigInteger getGasUsedDec() {
            return Numeric.decodeQuantity(gasUsed);
        }

        public String getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public Proof getProof() {
            return proof;
        }

        public void setProof(Proof proof) {
            this.proof = proof;
        }

        public String getProposer() {
            return proposer;
        }

        public void setProposer(String proposer) {
            this.proposer = proposer;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + (getPrevHash() != null
                    ? getPrevHash().hashCode() : 0);
            result = 31 * result + (getNumber() != null
                    ? getNumber().hashCode() : 0);
            result = 31 * result + (getStateRoot() != null
                    ? getStateRoot().hashCode() : 0);
            result = 31 * result + (getTransactionsRoot() != null
                    ? getTransactionsRoot().hashCode() : 0);
            result = 31 * result + (getReceiptsRoot() != null
                    ? getReceiptsRoot().hashCode() : 0);
            result = 31 * result + (getGasUsed() != null
                    ? getGasUsed().hashCode() : 0);
            result = 31 * result + (getProof() != null
                    ? getProof().hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Header)) {
                return false;
            }

            Header header = (Header) o;

            if (getTimestamp() != null
                    ? !getTimestamp().equals(header.getTimestamp())
                    : header.getTimestamp() != null) {
                return false;
            }
            if (getPrevHash() != null
                    ? !getPrevHash().equals(header.getPrevHash())
                    : header.getPrevHash() != null) {
                return false;
            }
            if (getNumber() != null
                    ? !getNumber().equals(header.getNumber()) : header.getNumber() != null) {
                return false;
            }
            if (getStateRoot() != null
                    ? !getStateRoot().equals(header.getStateRoot())
                    : header.getStateRoot() != null) {
                return false;
            }
            if (getTransactionsRoot() != null
                    ? !getTransactionsRoot().equals(header.getTransactionsRoot())
                    : header.getTransactionsRoot() != null) {
                return false;
            }
            if (getReceiptsRoot() != null
                    ? !getReceiptsRoot().equals(header.getReceiptsRoot())
                    : header.getReceiptsRoot() != null) {
                return false;
            }
            if (getGasUsed() != null
                    ? !getGasUsed().equals(header.getGasUsed()) : header.getGasUsed() != null) {
                return false;
            }
            if (getReceiptsRoot() != null
                    ? !getReceiptsRoot().equals(header.getReceiptsRoot())
                    : header.getReceiptsRoot() != null) {
                return false;
            }
            return (getProof() != null
                    ? getProof().equals(header.getProof()) : header.getProof() == null);
        }
    }

    public static class Body {
        private List<TransactionObject> transactions;

        public Body() {
        }

        public Body(List<TransactionObject> transactions) {
            this.transactions = transactions;
        }

        public List<TransactionObject> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<TransactionObject> transactions) {
            this.transactions = transactions;
        }

        @Override
        public int hashCode() {
            int result = getTransactions() != null ? getTransactions().hashCode() : 0;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Body)) {
                return false;
            }

            Body body = (Body) o;
            return (getTransactions() != null
                    ? getTransactions().equals(body.getTransactions())
                    : body.getTransactions() == null);
        }
    }

    public static class Block {
        private String version;
        private String hash;
        private Header header;
        private Body body;

        public Block() {
        }

        public Block(String version, String hash, Header header, Body body) {
            this.version = version;
            this.hash = hash;
            this.header = header;
            this.body = body;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        public Body getBody() {
            return body;
        }

        public void setBody(Body body) {
            this.body = body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Block)) {
                return false;
            }

            Block block = (Block) o;

            if (getHeader() != null
                    ? !getHeader().equals(block.getHeader()) : block.getHeader() != null) {
                return false;
            }
            if (getBody() != null
                    ? !getBody().equals(block.getBody()) : block.getBody() != null) {
                return false;
            }
            if (getHash() != null
                    ? !getHash().equals(block.getHash()) : block.getHash() != null) {
                return false;
            }
            return (getVersion() != null
                    ? getVersion().equals(block.getVersion()) : block.getVersion() == null);
        }

        @Override
        public int hashCode() {
            int result = getVersion() != null ? getVersion().hashCode() : 0;
            result = 31 * result + (getHash() != null ? getHash().hashCode() : 0);
            result = 31 * result + (getHeader() != null ? getHeader().hashCode() : 0);
            result = 31 * result + (getBody() != null ? getBody().hashCode() : 0);
            return result;
        }
    }

    public interface TransactionResult<T> {
        T get();
    }

    public static class TransactionObject extends Transaction
            implements TransactionResult<Transaction> {
        public TransactionObject() {
        }

        public TransactionObject(String hash, String blockHash, String blockNumber,
                                 String content, String index) {
            super(hash, blockHash, blockNumber, content, index);
        }

        @Override
        public Transaction get() {
            return this;
        }
    }

    public static class ResponseDeserialiser extends JsonDeserializer<Block> {
        //block meta
        String blockVersion;
        String blockHash;

        //block header
        Long timeStamp;
        String prevHash;
        String number;
        String stateRoot;
        String transactionsRoot;
        String receiptsRoot;
        String quotaUsed;

        //proof tendermint
        String proposer;
        String proposal;
        String height;
        String round;

        @Override
        public Block deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext) throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                JsonNode node = jsonParser.getCodec().readTree(jsonParser);
                //block meta
                blockVersion = node.get("version").asText();
                blockHash = node.get("hash").asText();

                //block header
                JsonNode headerNode = node.get("header");
                timeStamp = headerNode.get("timestamp").asLong();
                prevHash = headerNode.get("prevHash").asText();
                number = headerNode.get("number").asText();
                stateRoot = headerNode.get("stateRoot").asText();
                transactionsRoot = headerNode.get("transactionsRoot").asText();
                receiptsRoot = headerNode.get("receiptsRoot").asText();

                //pre 0.20 gasUsed. 0.20 quotaUsed
                if (headerNode.get("gasUsed") != null) {
                    quotaUsed = headerNode.get("gasUsed").asText();
                } else {
                    quotaUsed = headerNode.get("quotaUsed").asText();
                }

                proposer = headerNode.get("proposer").asText();

                //proof tendermint
                JsonNode proofNode = node.get("header").get("proof").get("Bft");
                proposal = proofNode.get("proposal").asText();
                height = proofNode.get("height").asText();
                round = proofNode.get("round").asText();

                //proof tendermint commits
                List<TendermintCommit> tendermintCommits = new ArrayList<>();
                JsonNode commitsNode = node.get("header")
                        .get("proof").get("Bft").get("commits");
                Iterator<String> commitsAddress = commitsNode.fieldNames();
                while (commitsAddress.hasNext()) {
                    String commitAddress = commitsAddress.next();
                    String commit = commitsNode.get(commitAddress).asText();
                    tendermintCommits.add(new TendermintCommit(commitAddress, commit));
                }

                //body transactions
                List<TransactionObject> transactionObjs = new ArrayList<TransactionObject>();
                JsonNode transactionNode = node.get("body").get("transactions");
                Iterator<JsonNode> txNodes = transactionNode.elements();
                while (txNodes.hasNext()) {
                    JsonNode txNode = txNodes.next();
                    TransactionObject txToAdd = new TransactionObject();
                    if (txNode.get("hash") == null && txNode.get("content") == null) {
                        txToAdd.setHash(txNode.asText());
                    } else {
                        txToAdd.setHash(txNode.get("hash").asText());
                        txToAdd.setContent(txNode.get("content").asText());
                    }
                    transactionObjs.add(txToAdd);
                }

                Tendermint tendermint = new Tendermint(
                        proposal, height, round,
                        tendermintCommits.toArray(
                                new TendermintCommit[tendermintCommits.size()]));

                Header header = new Header(timeStamp, prevHash, number, stateRoot,
                        transactionsRoot, receiptsRoot, quotaUsed, new Proof(tendermint), proposer);
                Body body = new Body(transactionObjs);
                return new Block(blockVersion, blockHash, header, body);
            } else {
                return null;  // null is wrapped by Optional in above getter
            }
        }
    }
}
