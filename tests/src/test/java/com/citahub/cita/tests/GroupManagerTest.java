package com.citahub.cita.tests;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import com.citahub.cita.protocol.system.CITAjSystemContract;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * Group and Account Management
 * If you want to know more about the documentation, please click on the link below.
 * https://docs.citahub.com/zh-CN/cita/account-permission/account
 */
public class GroupManagerTest extends SystemContractTest {
    static String senderAddr;
    static String adminPrivateKey;
    static int version;
    static BigInteger chainId;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        senderAddr = conf.primaryAddr;
        adminPrivateKey = conf.adminPrivateKey;
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
    }

    @Test
    public void testGroupManager( ) throws Exception {
        CITAjSystemContract sysContract = new CITAjSystemContract(service);

        // New group
        List<String> addresses = new ArrayList<>();
        addresses.add("0x1c6eebf136ee234caff3a95e0d9d22e40c9ac4ca");
        String newGroupAddr = sysContract.newGroup("vlk1", addresses, adminPrivateKey, version, chainId);
        assertFalse("".equals(newGroupAddr));
        if ("".equals(newGroupAddr)) {
            System.out.println("New group failed！");
            return;
        }
        System.out.println("Address for new group: " + newGroupAddr);
        TimeUnit.SECONDS.sleep(10);

        // Update group name
        boolean updated = sysContract.updateGroupName(newGroupAddr, "TokenNew", adminPrivateKey, version, chainId);
        assertTrue(updated);
        System.out.println("Group updated: " + updated);
        TimeUnit.SECONDS.sleep(10);

        // Add accounts
        List<String> addressNeedBeAdded = new ArrayList<>();
        addressNeedBeAdded.add("0xbac68e5cb986ead0253e0632da1131a0a96efa18");
        updated = sysContract.addAccounts(newGroupAddr, addressNeedBeAdded, adminPrivateKey, version, chainId);
        assertTrue(updated);
        System.out.println("Account is added: " + updated);
        TimeUnit.SECONDS.sleep(10);

        // Delete accounts
        updated = sysContract.deleteAccounts(newGroupAddr, addressNeedBeAdded, adminPrivateKey, version, chainId);
        assertTrue(updated);
        System.out.println("Account is deleted: " + updated);
        TimeUnit.SECONDS.sleep(10);

        // Check scope
        updated = sysContract.checkScope(newGroupAddr, adminPrivateKey, version, chainId);
        assertTrue(updated);
        System.out.println("Check scope: " + updated);
        TimeUnit.SECONDS.sleep(10);

        // Query groups
        List<String> res = sysContract.queryGroups(senderAddr);
        if (res != null) {
            for (String s : res) {
                System.out.println("Group address: " + s);
            }
        }
        assertTrue(res.contains(newGroupAddr));
        TimeUnit.SECONDS.sleep(10);

        // Delete group
        updated = sysContract.deleteGroup(newGroupAddr, adminPrivateKey, version, chainId);
        assertTrue(updated);
        System.out.println("Group deleted: " + updated);
        TimeUnit.SECONDS.sleep(10);
    }
}
