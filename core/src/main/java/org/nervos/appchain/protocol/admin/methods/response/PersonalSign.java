package org.nervos.appchain.protocol.admin.methods.response;

import org.nervos.appchain.protocol.core.Response;

/**
 * personal_sign
 * parity_signMessage.
 */
public class PersonalSign extends Response<String> {
    public String getSignedMessage() {
        return getResult();
    }
}
