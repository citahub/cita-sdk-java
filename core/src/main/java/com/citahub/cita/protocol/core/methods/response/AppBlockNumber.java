package com.citahub.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.citahub.cita.protocol.core.Response;
import com.citahub.cita.utils.Numeric;

/**
 * eth_blockNumber.
 */
public class AppBlockNumber extends Response<String> {
    public boolean isEmpty() {
        return getResult() == null;
    }

    public BigInteger getBlockNumber() {
        return Numeric.decodeQuantity(getResult());
    }
}
