package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

import java.util.Map;

/**
 * Created by duanyytop on 2019-04-09.
 * Copyright © 2018 Cryptape. All rights reserved.
 */
public class NetPeersInfo extends Response<NetPeersInfo.PeersInfo> {

    public PeersInfo getPeersInfo() {
        return getResult();
    }

    public static class PeersInfo {
        public long amount;
        public Map<String, String> peers;
    }

}
