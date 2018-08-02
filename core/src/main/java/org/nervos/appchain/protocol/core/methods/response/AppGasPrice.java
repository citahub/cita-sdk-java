package org.nervos.appchain.protocol.core.methods.response;

import java.math.BigInteger;

import org.nervos.appchain.protocol.core.Response;
import org.nervos.appchain.utils.Numeric;

/**
 * eth_gasPrice.
 */
public class AppGasPrice extends Response<String> {
    public BigInteger getGasPrice() {
        return Numeric.decodeQuantity(getResult());
    }
}
