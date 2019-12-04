package com.citahub.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.citahub.cita.protocol.core.Response;
import com.citahub.cita.utils.Numeric;

/**
 * app_getTransactionCount.
 */
public class AppGetTransactionCount extends Response<String> {
    public boolean isEmpty() {
        return getResult() == null;
    }

    public BigInteger getTransactionCount() {
        return Numeric.decodeQuantity(getResult());
    }
}
