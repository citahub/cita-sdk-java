package com.cryptape.cita.protocol.admin.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * personal_sign
 * parity_signMessage.
 */
public class PersonalSign extends Response<String> {
    public String getSignedMessage() {
        return getResult();
    }
}
