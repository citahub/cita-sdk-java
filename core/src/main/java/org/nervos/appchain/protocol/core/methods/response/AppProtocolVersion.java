package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.protocol.core.Response;

/**
 * eth_protocolVersion.
 */
public class AppProtocolVersion extends Response<String> {
    public String getProtocolVersion() {
        return getResult();
    }
}
