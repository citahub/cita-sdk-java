package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

public class AppSign extends Response<String> {
    public String getSignature() {
        return getResult();
    }
}
