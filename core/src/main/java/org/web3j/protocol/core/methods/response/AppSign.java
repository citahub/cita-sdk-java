package org.web3j.protocol.core.methods.response;

import org.web3j.protocol.core.Response;

public class AppSign extends Response<String> {
    public String getSignature() {
        return getResult();
    }
}
