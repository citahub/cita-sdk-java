package org.nervos.appchain.tests;

import java.util.Properties;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjFactory;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.http.HttpService;
import org.nervos.appchain.protocol.system.NervosjSysContract;

public class SystemContractExample {

    static Properties props;
    static String testNetAddr;
    static Nervosj service;
    static String senderAddr;

    static {
        try {
            props = Config.load();
        } catch (Exception e) {
            System.out.println("Failed to get props from config file");
            System.exit(1);
        }
        testNetAddr = props.getProperty(Config.TEST_NET_ADDR);
        HttpService.setDebug(false);
        service = NervosjFactory.build(new HttpService(testNetAddr));
        senderAddr = props.getProperty(Config.SENDER_ADDR);

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
