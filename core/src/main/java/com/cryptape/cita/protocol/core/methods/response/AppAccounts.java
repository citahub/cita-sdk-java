package com.cryptape.cita.protocol.core.methods.response;

import java.util.List;

import com.cryptape.cita.protocol.core.Response;

/**
 * eth_accounts.
 */
public class AppAccounts extends Response<List<String>> {
    public List<String> getAccounts() {
        return getResult();
    }
}
