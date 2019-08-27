package com.cryptape.cita.tests;


import java.io.IOException;
import java.util.List;

/**
 * Group and Account Management
 * If you want to know more about the documentation, please click on the link below.
 * https://docs.citahub.com/zh-CN/cita/account-permission/account
 */
public class GroupManagerExample extends SystemContractExample {
    public static void main(String[] args) throws IOException {
        // query groups
        List<String> groupAddress = sysContract.queryGroups("0xfFFfFFFFFffFFfffFFFFfffffFffffFFfF020009");
        System.out.println("groupAddress: " + groupAddress);
    }
}
