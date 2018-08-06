package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.protocol.core.Response;

/**
 * eth_getCode.
 */
public class AppGetCode extends Response<String> {
    public boolean isEmpty() {
        return getResult() == null;
    }

    public String getCode() {
        return getResult();
    }
}
