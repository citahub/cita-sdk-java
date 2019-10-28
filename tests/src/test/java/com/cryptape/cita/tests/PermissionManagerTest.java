package com.cryptape.cita.tests;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import com.cryptape.cita.abi.FunctionEncoder;
import com.cryptape.cita.abi.datatypes.Function;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.system.CITAjSystemContract;
import com.cryptape.cita.protocol.system.entities.QueryInfoResult;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class PermissionManagerTest {
    static CITAj service;
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

    private static void printQueryInfo(QueryInfoResult result) {
        System.out.println("Permission name: " + result.name);
        System.out.println("Permission contracts: ");
        result.contractAddrs.forEach(System.out::println);
        System.out.println("Permission functions: ");
        result.funcs.forEach(System.out::println);
    }

    public String getEncodeOfFunc(String funcName){
        Function func = new Function(funcName, Collections.emptyList(), Collections.emptyList());
        return FunctionEncoder.encode(func).substring(2);
    }

    @Test
    public void testPermissionManager( ) throws Exception {

        CITAjSystemContract sysContract = new CITAjSystemContract(service);

        //create a permission
        ArrayList<String> addrs = new ArrayList<>(Collections.singletonList(senderAddr));
        ArrayList<String> funcs = new ArrayList<>(Collections.singletonList("Token"));
        String newPermissionAddr = sysContract.newPermission(
                "somePermission1", addrs, funcs, adminPrivateKey, version, chainId);
        assertTrue(newPermissionAddr.contains("0x"));
        assertTrue(newPermissionAddr.length()==42);
        System.out.println("Address for new permission: " + newPermissionAddr);
        TimeUnit.SECONDS.sleep(10);

        boolean updated = sysContract.updatePermissionName(newPermissionAddr, "TokenNew", adminPrivateKey, version, chainId);
        assertTrue(updated);
        System.out.println("Permission updated: " + updated);
        TimeUnit.SECONDS.sleep(10);

        //check if the permission updated
        QueryInfoResult queryInforesult = sysContract.queryInfo(senderAddr, newPermissionAddr);
        String name = queryInforesult.name;
        assertTrue(queryInforesult.name.contains("TokenNew"));
        printQueryInfo(queryInforesult);

        //add a new resource
        addrs = new ArrayList<>(Collections.singletonList(senderAddr));
        funcs = new ArrayList<>(Collections.singletonList("Token1"));
        String funcEncode =  getEncodeOfFunc(funcs.get(0));
        boolean addResource = sysContract.addResources(addrs, funcs, newPermissionAddr, adminPrivateKey, version, chainId);
        assertTrue(addResource);
        System.out.println("Resource is added: " + addResource);
        TimeUnit.SECONDS.sleep(10);

        //check if the resources added
        queryInforesult = sysContract.queryInfo(senderAddr, newPermissionAddr);
        assertTrue(queryInforesult.funcs.contains(funcEncode));
        printQueryInfo(queryInforesult);

        boolean inPermission = sysContract.inPermission(senderAddr, newPermissionAddr, senderAddr, "Token1");
        assertTrue(inPermission);
        System.out.println("The resource is in permission: " + inPermission);


        //delete a existing resource
        addrs = new ArrayList<>(Collections.singletonList(senderAddr));
        funcs = new ArrayList<>(Collections.singletonList("Token1"));
        boolean deleteResource = sysContract.deleteResources(addrs, funcs, newPermissionAddr, adminPrivateKey, version, chainId);
        assertTrue(deleteResource);
        System.out.println("Resource is deleted: " + deleteResource);
        TimeUnit.SECONDS.sleep(10);

        //check if the resources deleted
        queryInforesult = sysContract.queryInfo(senderAddr, newPermissionAddr);
        assertFalse(queryInforesult.funcs.contains(funcEncode));
        printQueryInfo(queryInforesult);

        //delete permission
        boolean deleted = sysContract.deletePermission(newPermissionAddr, adminPrivateKey, version, chainId);
        assertTrue(deleted);
        System.out.println("Permission deleted: " + deleted);
    }
}
