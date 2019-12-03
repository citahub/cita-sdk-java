package com.citahub.cita.protocol.core.methods.response;

import java.util.List;

import com.citahub.cita.protocol.core.Response;

/**
 * eth_accounts.
 */
public class AppAccounts extends Response<List<String>> {
    public List<String> getAccounts() {
        return getResult();
    }
}
