package org.nervos.appchain.tests;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.system.AppChainjSystemContract;
import org.nervos.appchain.protocol.system.entities.QueryInfoResult;

import java.io.IOException;
import java.util.ArrayList;

public class PermissionManagerExample {
    static AppChainj service;
    static String senderAddr;
    static String adminPriavteKey;
    static int version;
    static int chainId;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        senderAddr = conf.primaryAddr;
        adminPriavteKey = conf.adminPrivateKey;
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        AppChainjSystemContract sysContract = new AppChainjSystemContract(service);

        ArrayList<String> addrs = new ArrayList<>();
        addrs.add("0xb9ebc94e655b07fcad32224798dc5b433cb32b05");

        ArrayList<String> funcs = new ArrayList<>();
        funcs.add("Token");
        boolean newPermission = sysContract.newPermission(
                "somePermission1", addrs, funcs, adminPriavteKey, version, chainId);

        System.out.println(newPermission);

    }
}
