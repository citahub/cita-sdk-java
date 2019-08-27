package com.cryptape.cita.tests;


import com.cryptape.cita.protocol.core.methods.response.Log;
import com.cryptape.cita.protocol.system.CITASystemContract;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Group and Account Management
 * If you want to know more about the documentation, please click on the link below.
 * https://docs.citahub.com/zh-CN/cita/account-permission/account
 */
public class GroupManagerExample extends SystemContractExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        // query groups
        List<String> groupAddress = sysContract.queryGroups("0xfFFfFFFFFffFFfffFFFFfffffFffffFFfF020009");
        System.out.println("groupAddress: " + groupAddress);

        // create a group
        String transcationHash = sysContract.newGroup("0xfFFfFFFFFffFFfffFFFFfffffFffffFFfF020009",
                "newGroup",
                Arrays.asList("e1c4021742730ded647590a1686d5c4bfcbae0b0", "45a50f45cb81c8aedeab917ea0cd3c9178ebdcae"),
                adminPriavteKey,
                version,
                chainId);
        System.out.println("transcationHash: " + transcationHash);
        // get receipt by hash
        Log log = CITASystemContract.getReceiptLog(service, transcationHash, 0);
        System.out.println("groupAddress: " + (log == null ? "null" : log.getAddress()));
    }
}
