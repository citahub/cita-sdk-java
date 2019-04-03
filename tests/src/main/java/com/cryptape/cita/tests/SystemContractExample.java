package com.cryptape.cita.tests;

import java.math.BigInteger;
import java.util.List;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.system.CITAjSystemContract;

public class SystemContractExample {

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

    public static void main(String[] args) throws Exception {

        CITAjSystemContract sysContract = new CITAjSystemContract(service);
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
        if (approved) {
            System.out.println(
                    "Node(0xe2066149012e6c1505e3549d103068bd0f2f0577) "
                            + "is added as a consensus node successfully");
        }

        //test for delete node
        boolean deleted = sysContract.deleteNode(
                "0xe2066149012e6c1505e3549d103068bd0f2f0577", adminPriavteKey, version, chainId);
        if (deleted) {
            System.out.println(
                    "Node(0xe2066149012e6c1505e3549d103068bd0f2f0577) "
                            + "is deleted from consensus nodes successfully.");
        }

        /**Don't try this**/
        //boolean setStake = sysContract.setStake(senderAddr, 2, adminPrivateKey);
        //if (setStake) {
        //    System.out.println("success");
        //}
    }
}
