package org.nervos.appchain.protocol.admin.methods.response;

import org.nervos.appchain.protocol.core.Response;

/**
 * Boolean response type.
 */
public class BooleanResponse extends Response<Boolean> {
    public boolean success() {
        return getResult();
    }
}
