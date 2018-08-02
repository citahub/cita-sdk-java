package org.nervos.appchain.protocol.core.methods.response;

import java.math.BigInteger;

import org.nervos.appchain.protocol.core.Response;
import org.nervos.appchain.utils.Numeric;

/**
 * net_peerCount.
 */
public class NetPeerCount extends Response<String> {

    public BigInteger getQuantity() {
        return Numeric.decodeQuantity(getResult());
    }
}
