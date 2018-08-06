package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.protocol.core.Response;

/**
 * eth_sendRawTransaction.
 */
public class AppSendRawTransaction extends Response<String> {
    public String getTransactionHash() {
        return getResult();
    }
}
