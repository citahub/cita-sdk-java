package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * eth_sendRawTransaction.
 */
public class AppSendRawTransaction extends Response<String> {
    public String getTransactionHash() {
        return getResult();
    }
}
