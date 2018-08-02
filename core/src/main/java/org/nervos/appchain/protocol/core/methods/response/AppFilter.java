package org.nervos.appchain.protocol.core.methods.response;

import java.math.BigInteger;

import org.nervos.appchain.protocol.core.Response;
import org.nervos.appchain.utils.Numeric;

/**
 * eth_newFilter.
 */
public class AppFilter extends Response<String> {
    public BigInteger getFilterId() {
        return Numeric.decodeQuantity(getResult());
    }
}
