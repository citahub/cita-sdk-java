package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * eth_call.
 */
public class AppCall extends Response<String> {
    public String getValue() {
        return getResult();
    }
}
