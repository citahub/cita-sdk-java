package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.protocol.core.Response;

public class AppSign extends Response<String> {
    public String getSignature() {
        return getResult();
    }
}
