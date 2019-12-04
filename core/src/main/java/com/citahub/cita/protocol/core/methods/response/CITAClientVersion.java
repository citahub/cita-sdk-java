package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * CITA_clientVersion.
 */
public class CITAClientVersion extends Response<String> {

    public String getCitaClientVersion() {
        return getResult();
    }
}
