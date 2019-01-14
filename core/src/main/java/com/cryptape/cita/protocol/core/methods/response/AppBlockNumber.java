package com.cryptape.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.cryptape.cita.protocol.core.Response;
import com.cryptape.cita.utils.Numeric;

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
