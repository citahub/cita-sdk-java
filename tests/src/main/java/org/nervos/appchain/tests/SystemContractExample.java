package org.nervos.appchain.tests;
import java.math.BigInteger;
import java.util.List;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.system.AppChainjSystemContract;
public class SystemContractExample {
    static Nervosj service;
    static String senderAddr;
    static String adminPriavteKey;
    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        senderAddr = conf.primaryAddr;
        adminPriavteKey = conf.adminPrivateKey;
    }
    public static void main(String[] args) throws Exception {
        AppChainjSystemContract sysContract = new AppChainjSystemContract(service);
        int quotaPrice = sysContract.getQuotaPrice(senderAddr);
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
                "0xe2066149012e6c1505e3549d103068bd0f2f0577", adminPriavteKey);
        if (approved) {
            System.out.println(
                    "Node(0xe2066149012e6c1505e3549d103068bd0f2f0577) "
                            + "is added as a consensus node successfully");
        }
        //test for delete node
        boolean deleted = sysContract.deleteNode(
                "0xe2066149012e6c1505e3549d103068bd0f2f0577", adminPriavteKey);
        if (deleted) {
            System.out.println(
                    "Node(0xe2066149012e6c1505e3549d103068bd0f2f0577) "
                            + "is deleted from consensus nodes successfully.");
        }
        /**Don't try this**/
        //boolean setStake = sysContract.setStake(senderAddr, 2, adminPriavteKey);
        //if (setStake) {
        //    System.out.println("success");
        //}
    }
}
