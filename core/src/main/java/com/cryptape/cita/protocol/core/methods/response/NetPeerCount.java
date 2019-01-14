package com.cryptape.cita.protocol.core.methods.response;

import java.math.BigInteger;

import com.cryptape.cita.protocol.core.Response;
import com.cryptape.cita.utils.Numeric;

/**
 * net_peerCount.
 */
public class NetPeerCount extends Response<String> {

    public BigInteger getQuantity() {
        return Numeric.decodeQuantity(getResult());
    }
}
