package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * eth_coinbase.
 */
public class AppCoinbase extends Response<String> {
    public String getAddress() {
        return getResult();
    }
}
