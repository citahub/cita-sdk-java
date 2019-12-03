package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * eth_call.
 */
public class AppCall extends Response<String> {
    public String getValue() {
        return getResult();
    }
}
