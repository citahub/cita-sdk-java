package org.nervos.appchain.protocol.core.methods.response;

import java.math.BigInteger;

import org.nervos.appchain.protocol.core.Response;
import org.nervos.appchain.utils.Numeric;

/**
 * eth_getTransactionCount.
 */
public class AppGetTransactionCount extends Response<String> {
    public boolean isEmpty() {
        return getResult() == null;
    }

    public BigInteger getTransactionCount() {
        return Numeric.decodeQuantity(getResult());
    }
}
