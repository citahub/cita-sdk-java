package org.nervos.appchain.protocol.core.methods.response;

import java.util.List;

import org.nervos.appchain.protocol.core.Response;

/**
 * eth_accounts.
 */
public class AppAccounts extends Response<List<String>> {
    public List<String> getAccounts() {
        return getResult();
    }
}
