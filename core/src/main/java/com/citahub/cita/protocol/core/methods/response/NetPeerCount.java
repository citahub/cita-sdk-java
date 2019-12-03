package com.citahub.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.citahub.cita.protocol.core.Response;
import com.citahub.cita.utils.Numeric;

/**
 * net_peerCount.
 */
public class NetPeerCount extends Response<String> {

    public BigInteger getQuantity() {
        return Numeric.decodeQuantity(getResult());
    }
}
