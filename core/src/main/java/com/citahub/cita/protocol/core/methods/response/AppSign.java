package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

public class AppSign extends Response<String> {
    public String getSignature() {
        return getResult();
    }
}
