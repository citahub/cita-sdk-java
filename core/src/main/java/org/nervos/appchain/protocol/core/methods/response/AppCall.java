package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.protocol.core.Response;

/**
 * eth_call.
 */
public class AppCall extends Response<String> {
    public String getValue() {
        return getResult();
    }
}
