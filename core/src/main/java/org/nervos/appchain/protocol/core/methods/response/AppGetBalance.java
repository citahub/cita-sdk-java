package org.nervos.appchain.protocol.core.methods.response;

import java.math.BigInteger;

import org.nervos.appchain.protocol.core.Response;
import org.nervos.appchain.utils.Numeric;

/**
 * eth_getBalance.
 */
public class AppGetBalance extends Response<String> {
    public BigInteger getBalance() {
        return Numeric.decodeQuantity(getResult());
    }
}
