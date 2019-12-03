package com.citahub.cita.tests;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.system.CITAjSystemContract;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class QuotaManagerTest {
    static CITAj service;
    static String senderAddr;
    static String adminPriavteKey;
    static int version;
    static BigInteger chainId;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        senderAddr = conf.primaryAddr;
        adminPriavteKey = conf.adminPrivateKey;
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
    }

    @Test
    public void testQuotaManager() throws Exception {
        CITAjSystemContract sysContract = new CITAjSystemContract(service);
        int bql = sysContract.getBql(senderAddr);
        System.out.println("BQL(Block quota limit): " + bql + ". default one should be 1073741824");

        int setBql = bql+1;
        boolean bqlSuccess = sysContract.setBql(
                new BigInteger(""+setBql), adminPriavteKey, version, chainId);
        System.out.println("Is Bql set: " + bqlSuccess);

        System.out.println("Wait 3 seconds for transaction from pending to latest");
        TimeUnit.SECONDS.sleep(3);

        bql = sysContract.getBql(senderAddr);
        assertThat(bql,equalTo(setBql));
        System.out.println("BQL(Block quota limit): " + bql + ". default one should be 1073741824");

        int defaultAql = sysContract.getDefaultAql(senderAddr);
        System.out.println(
                "Default AQL(Account quota limit): " + defaultAql
                        + ". Default one should be 268435456");

        int setAql = defaultAql+1;
        boolean setDefaultAqlSucces = sysContract.setDefaultAql(
                new BigInteger(""+setAql), adminPriavteKey, version, chainId);

        System.out.println("Is default Aql set: " + setDefaultAqlSucces);

        System.out.println("wait 5 seconds for status from pending to latest");
        TimeUnit.SECONDS.sleep(5);

        defaultAql = sysContract.getDefaultAql(senderAddr);
        assertThat(defaultAql,equalTo(setAql));
        System.out.println(
                "Default AQL(Account quota limit): " + defaultAql
                        + ". Default one should be 268435456");

        int aql = sysContract.getAql(senderAddr, senderAddr);
        System.out.println("AQL(Account quota limit) for addr " + senderAddr + " is: " + aql);

        setAql = aql+1;
        boolean setAqlSuccess = sysContract.setAql(
                senderAddr, new BigInteger(""+setAql), adminPriavteKey, version, chainId);

        System.out.println("Is aql set: " + setAqlSuccess);
        System.out.println("wait 5 seconds for status from pending to latest");
        TimeUnit.SECONDS.sleep(5);

        aql = sysContract.getAql(senderAddr, senderAddr);
        assertThat(aql,equalTo(setAql));
        System.out.println("AQL(Account quota limit) for addr " + senderAddr + " is: " + aql);

        List<String> accounts = sysContract.getAccounts(senderAddr);
        System.out.println("List of accounts.");
        accounts.forEach(System.out::println);

        List<BigInteger> quotas = sysContract.getQuotas(senderAddr);
        System.out.println("List of quotas");
        quotas.forEach(System.out::println);
    }
}
