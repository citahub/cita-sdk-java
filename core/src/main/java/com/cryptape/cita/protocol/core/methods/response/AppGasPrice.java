package com.cryptape.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.cryptape.cita.protocol.core.Response;
import com.cryptape.cita.utils.Numeric;

/**
 * eth_gasPrice.
 */
public class AppGasPrice extends Response<String> {
    public BigInteger getGasPrice() {
        return Numeric.decodeQuantity(getResult());
    }
}
