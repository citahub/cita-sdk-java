package com.cryptape.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.cryptape.cita.protocol.core.Response;
import com.cryptape.cita.utils.Numeric;

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
