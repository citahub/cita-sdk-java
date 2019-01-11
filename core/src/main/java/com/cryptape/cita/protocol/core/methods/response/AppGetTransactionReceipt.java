package com.cryptape.cita.protocol.core.methods.response;

import java.io.IOException;

import com.cryptape.cita.protocol.ObjectMapperFactory;
import com.cryptape.cita.protocol.core.Response;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * eth_getTransactionReceipt.
 */
public class AppGetTransactionReceipt extends Response<TransactionReceipt> {

    public TransactionReceipt getTransactionReceipt() {
        return getResult();
    }

    public static class ResponseDeserialiser extends JsonDeserializer<TransactionReceipt> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public TransactionReceipt deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext) throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                return objectReader.readValue(jsonParser, TransactionReceipt.class);
            } else {
                return null;  // null is wrapped by Optional in above getter
            }
        }
    }
}
