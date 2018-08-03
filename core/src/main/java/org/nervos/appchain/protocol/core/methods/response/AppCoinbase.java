package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.protocol.core.Response;

/**
 * eth_coinbase.
 */
public class AppCoinbase extends Response<String> {
    public String getAddress() {
        return getResult();
    }
}
