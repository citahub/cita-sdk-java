package com.citahub.cita.tests;

import static org.junit.Assert.assertTrue;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.system.CITAjSystemContract;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;

public class SystemContractTest {

    static CITAj service;
    static String senderAddr;
    static String adminPriavteKey;
    static int version;
    static BigInteger chainId;
    static CITAjSystemContract sysContract;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        senderAddr = conf.primaryAddr;
        adminPriavteKey = conf.adminPrivateKey;
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
        sysContract = new CITAjSystemContract(service);
    }

    @Test
    public void testSystemContract() throws Exception {

        long quotaPrice = sysContract.getQuotaPrice(senderAddr);
        System.out.println("Quota price is: " + quotaPrice);

        List<String> nodes = sysContract.listNode(senderAddr);
        System.out.println("Consensus nodes: ");
        nodes.forEach(System.out::println);

        List<BigInteger> stakes = sysContract.listStake(senderAddr);
        System.out.println("Consensus nodes stakes: ");
        stakes.forEach(System.out::println);

        int status = sysContract.getStatus(senderAddr);
        System.out.println("Status of addr(" + senderAddr + "): " + status);

        int stakePermillige = sysContract.stakePermillage(senderAddr);
        System.out.println("Stake Permillage of addr(" + senderAddr
                + "): " + stakePermillige);

        //test approve node
        boolean approved = sysContract.approveNode(
                "0xe2066149012e6c1505e3549d103068bd0f2f0577", adminPriavteKey, version, chainId);
        List<String> nodesNow = sysContract.listNode(senderAddr);
        assertTrue(nodesNow.contains("0xe2066149012e6c1505e3549d103068bd0f2f0577"));

        //test for delete node
        boolean deleted = sysContract.deleteNode(
                "0xe2066149012e6c1505e3549d103068bd0f2f0577", adminPriavteKey, version, chainId);
        assertTrue(deleted);

    }
}
