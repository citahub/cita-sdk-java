package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * eth_getCode.
 */
public class AppGetCode extends Response<String> {
    public boolean isEmpty() {
        return getResult() == null;
    }

    public String getCode() {
        return getResult();
    }
}
