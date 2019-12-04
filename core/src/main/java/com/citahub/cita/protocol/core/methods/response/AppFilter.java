package com.citahub.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.citahub.cita.protocol.core.Response;
import com.citahub.cita.utils.Numeric;

/**
 * eth_newFilter.
 */
public class AppFilter extends Response<String> {
    public BigInteger getFilterId() {
        return Numeric.decodeQuantity(getResult());
    }
}
