package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * eth_coinbase.
 */
public class AppCoinbase extends Response<String> {
    public String getAddress() {
        return getResult();
    }
}
