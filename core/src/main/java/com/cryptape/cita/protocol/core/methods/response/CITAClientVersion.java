package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * CITA_clientVersion.
 */
public class CITAClientVersion extends Response<String> {

    public String getCitaClientVersion() {
        return getResult();
    }
}
