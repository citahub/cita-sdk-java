package com.citahub.cita.protocol.admin.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * Boolean response type.
 */
public class BooleanResponse extends Response<Boolean> {
    public boolean success() {
        return getResult();
    }
}
