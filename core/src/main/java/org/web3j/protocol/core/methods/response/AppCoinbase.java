package org.web3j.protocol.core.methods.response;

import org.web3j.protocol.core.Response;

/**
 * eth_coinbase.
 */
public class AppCoinbase extends Response<String> {
    public String getAddress() {
        return getResult();
    }
}
