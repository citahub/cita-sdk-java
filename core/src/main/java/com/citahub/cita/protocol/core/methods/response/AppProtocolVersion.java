package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * eth_protocolVersion.
 */
public class AppProtocolVersion extends Response<String> {
    public String getProtocolVersion() {
        return getResult();
    }
}
