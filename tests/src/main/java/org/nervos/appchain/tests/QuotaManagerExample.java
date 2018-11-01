package org.nervos.appchain.tests;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.system.AppChainjSystemContract;

public class QuotaManagerExample {
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

    public static void main(String[] args) throws Exception {
        AppChainjSystemContract sysContract = new AppChainjSystemContract(service);
        int bql = sysContract.getBql(senderAddr);
        System.out.println("BQL(Block quota limit): " + bql + ". default one should be 1073741824");

        boolean bqlSuccess = sysContract.setBql(
                new BigInteger("1073741825"), adminPriavteKey, version, chainId);
        System.out.println("Is Bql set: " + bqlSuccess);

        System.out.println("Wait 3 seconds for transaction from pending to latest");
        TimeUnit.SECONDS.sleep(3);

        bql = sysContract.getBql(senderAddr);
        System.out.println("BQL(Block quota limit): " + bql + ". default one should be 1073741824");

        int defaultAql = sysContract.getDefaultAql(senderAddr);
        System.out.println(
                "Default AQL(Account quota limit): " + defaultAql
                        + ". Default one should be 268435456");

        boolean setDefaultAqlSucces = sysContract.setDefaultAql(
                new BigInteger("268435456"), adminPriavteKey, version, chainId);

        System.out.println("Is default Aql set: " + setDefaultAqlSucces);

        System.out.println("wait 5 seconds for status from pending to latest");
        TimeUnit.SECONDS.sleep(5);

        defaultAql = sysContract.getDefaultAql(senderAddr);
        System.out.println(
                "Default AQL(Account quota limit): " + defaultAql
                        + ". Default one should be 268435456");

        int aql = sysContract.getAql(senderAddr, senderAddr);
        System.out.println("AQL(Account quota limit) for addr " + senderAddr + " is: " + aql);

        boolean setAqlSuccess = sysContract.setAql(
                senderAddr, new BigInteger("1073741824"), adminPriavteKey, version, chainId);

        System.out.println("Is aql set: " + setAqlSuccess);
        System.out.println("wait 5 seconds for status from pending to latest");
        TimeUnit.SECONDS.sleep(5);

        aql = sysContract.getAql(senderAddr, senderAddr);
        System.out.println("AQL(Account quota limit) for addr " + senderAddr + " is: " + aql);

        List<String> accounts = sysContract.getAccounts(senderAddr);
        System.out.println("List of accounts.");
        accounts.forEach(System.out::println);

        List<BigInteger> quotas = sysContract.getQuotas(senderAddr);
        System.out.println("List of quotas");
        quotas.forEach(System.out::println);
    }
}
