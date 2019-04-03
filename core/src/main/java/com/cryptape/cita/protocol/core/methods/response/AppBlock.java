package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.ObjectMapperFactory;
import com.cryptape.cita.protocol.core.Response;
import com.cryptape.cita.utils.Numeric;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppBlock extends Response<AppBlock.Block> {

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
        public String commitAddress;
        public String commit;

        public TendermintCommit() {}

    }

    public static class Tendermint {
        public String proposal;
        public String height;
        public String round;
        public TendermintCommit[] tendermintCommits;

        public Tendermint() {}

    }

    public static class Bft {

        public String proposal;
        public int height;
        public int round;
        public Map<String, String> commits;

        public Bft() {}

    }


    public static class Proof {
        public Tendermint tendermint;
        @JsonProperty("Bft")
        public Bft bft;

        public Proof() {}

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

        @JsonDeserialize(using = TransactionResultDeserializer.class)
        public void setTransactions(List<TransactionObject> transactions) {
            this.transactions = transactions;
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

    }

    public interface TransactionResult<T> {
        T get();
    }

    public static class TransactionObject extends Transaction
            implements TransactionResult<Transaction> {
        public TransactionObject() {
        }

        public TransactionObject(String hash, String blockHash, String blockNumber,
                                 String content, String index, String from) {
            super(hash, blockHash, blockNumber, content, index, from);
        }

        @Override
        public Transaction get() {
            return this;
        }
    }

    public static class Hash implements TransactionResult<String> {
        private String value;

        public Hash() {
        }

        public Hash(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    public static class TransactionResultDeserializer
            extends JsonDeserializer<List<TransactionResult>> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public List<TransactionResult> deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext) throws IOException {

            List<TransactionResult> transactionResults = new ArrayList<>();
            JsonToken nextToken = jsonParser.nextToken();

            if (nextToken == JsonToken.START_OBJECT) {
                Iterator<TransactionObject> transactionObjectIterator =
                        objectReader.readValues(jsonParser, TransactionObject.class);
                while (transactionObjectIterator.hasNext()) {
                    transactionResults.add(transactionObjectIterator.next());
                }
            } else if (nextToken == JsonToken.VALUE_STRING) {
                jsonParser.getValueAsString();

                Iterator<Hash> transactionHashIterator =
                        objectReader.readValues(jsonParser, Hash.class);
                while (transactionHashIterator.hasNext()) {
                    transactionResults.add(transactionHashIterator.next());
                }
            }
            return transactionResults;
        }
    }

}
