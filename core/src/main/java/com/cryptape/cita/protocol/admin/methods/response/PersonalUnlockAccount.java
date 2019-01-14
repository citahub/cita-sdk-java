package com.cryptape.cita.protocol.admin.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * personal_unlockAccount.
 */
public class PersonalUnlockAccount extends Response<Boolean> {
    public Boolean accountUnlocked() {
        return getResult();
    }
}
