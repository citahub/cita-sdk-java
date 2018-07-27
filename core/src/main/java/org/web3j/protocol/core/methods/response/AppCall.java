package org.web3j.protocol.core.methods.response;

import org.web3j.protocol.core.Response;

/**
 * eth_call.
 */
public class AppCall extends Response<String> {
    public String getValue() {
        return getResult();
    }
}
