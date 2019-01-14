package com.cryptape.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.cryptape.cita.protocol.core.Response;
import com.cryptape.cita.utils.Numeric;

/**
 * eth_getBalance.
 */
public class AppGetBalance extends Response<String> {
    public BigInteger getBalance() {
        return Numeric.decodeQuantity(getResult());
    }
}
