package org.nervos.appchain.protocol.admin;

import org.nervos.appchain.protocol.NervosjService;

public class AdminFactory {
    public static Admin build(NervosjService web3jService) {
        return new JsonRpc2_0Admin(web3jService);
    }
}
