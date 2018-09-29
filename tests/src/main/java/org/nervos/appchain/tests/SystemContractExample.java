package org.nervos.appchain.tests;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.system.NervosjSysContract;

public class SystemContractExample {
    static Nervosj service;
    static String senderAddr;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        senderAddr = conf.primaryAddr;
    }

    public static void main(String[] args) throws Exception {
        NervosjSysContract sysContract = new NervosjSysContract(service);
        AppCall appcall = sysContract.getQuotaPrice(senderAddr);

        if (appcall.getError() != null) {
            System.out.println("Failed to read quota price.");
            System.out.println("Error: " + appcall.getError().getMessage());
            System.exit(1);
        }

        System.out.println(appcall.getValue());
    }
}
