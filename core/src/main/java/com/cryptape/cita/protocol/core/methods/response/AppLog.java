package com.cryptape.cita.protocol.core.methods.response;

import java.util.List;

import com.cryptape.cita.protocol.core.Response;

public class AppLog extends Response<List<AppLog.LogResult>> {

    @Override
    public void setResult(List<LogResult> result) {
        super.setResult(result);
    }

    public List<LogResult> getLogs() {
        return getResult();
    }

    public interface LogResult<T> {
        T get();
    }

    public static class LogObject extends Log implements LogResult<Log> {

        public LogObject() {
        }

        public LogObject(boolean removed, String logIndex, String transactionIndex,
                         String transactionHash, String blockHash, String blockNumber,
                         String address, String data, String transactionLogIndex,
                         List<String> topics) {
            super(removed, logIndex, transactionIndex, transactionHash, blockHash, blockNumber,
                    address, data, transactionLogIndex, topics);
        }

        @Override
        public Log get() {
            return this;
        }
    }

    public static class Hash implements LogResult<String> {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Hash)) {
                return false;
            }

            Hash hash = (Hash) o;

            return value != null ? value.equals(hash.value) : hash.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

}
