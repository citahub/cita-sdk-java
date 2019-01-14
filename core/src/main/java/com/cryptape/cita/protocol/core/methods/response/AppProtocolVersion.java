package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * eth_protocolVersion.
 */
public class AppProtocolVersion extends Response<String> {
    public String getProtocolVersion() {
        return getResult();
    }
}
