package org.nervos.appchain.protocol.system;

import java.io.IOException;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.response.AppCall;

public class NervosjSysContract {
    private Nervosj service;

    public static final String QUOTA_PRICE_ADDR = "0xffffffffffffffffffffffffffffffffff020010";
    public static final String QUOTA_PRICE_DATA = "0x6bacc53f";
    public static final DefaultBlockParameter
            DEFAULT_BLOCK_PARAMETER = DefaultBlockParameter.valueOf("latest");

    public NervosjSysContract(Nervosj service) {
        this.service = service;
    }

    public AppCall getQuotaPrice(String from) throws IOException {
        return service.appCall(
                new Call(from, QUOTA_PRICE_ADDR, QUOTA_PRICE_DATA),
                DEFAULT_BLOCK_PARAMETER).send();
    }

}
