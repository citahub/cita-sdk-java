package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

public class AppGetAbi extends Response<String> {

    public boolean isEmpty() {
        return getResult() == null;
    }

    public String getAbi() {
        return getResult();
    }

}
