package com.cryptape.cita.protocol.admin.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * Boolean response type.
 */
public class BooleanResponse extends Response<Boolean> {
    public boolean success() {
        return getResult();
    }
}
