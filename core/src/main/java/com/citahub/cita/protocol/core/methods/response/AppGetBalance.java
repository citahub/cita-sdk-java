package com.citahub.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.citahub.cita.protocol.core.Response;
import com.citahub.cita.utils.Numeric;

/**
 * eth_getBalance.
 */
public class AppGetBalance extends Response<String> {
    public BigInteger getBalance() {
        return Numeric.decodeQuantity(getResult());
    }
}
