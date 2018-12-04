package org.nervos.appchain.tests;

import java.math.BigInteger;

import org.nervos.appchain.crypto.sm2.SM2;
import org.nervos.appchain.crypto.sm2.SM2KeyPair;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;

/**
 * This example is to use SM2 to generate 100 pairs of keys
 * and 100 signed transactions per private key.
 * The test is just verify if the transaction can be validated by node.
 * **/

public class SM2ValidationTest {
    private static BigInteger chainId;
    private static int version;
    private static Long quota;
    private static AppChainj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        quota = Long.parseLong(conf.defaultQuotaDeployment);
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
    }

    private static SM2KeyPair generateKeyPair() {
        SM2 sm2 = new SM2();
        return sm2.generateKeyPair();
    }

    private static String verifyTx(String contractCode, String privateKey) {
        Transaction txToDeploy = new Transaction(
                "", TestUtil.getNonce(), quota,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId, "0", contractCode);
        try {
            String rawTx = txToDeploy.sign(privateKey, Transaction.CryptoTx.SM2, false);
            AppSendTransaction testTx = service.appSendRawTransaction(rawTx).send();
            if (testTx.getError() != null) {
                System.out.println(testTx.getError().getMessage());
            }
            return testTx.getSendTransactionResult().getHash();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public static void main(String[] args) {
        // CHECKSTYLE:OFF
        String contractCode = "6060604052341561000f57600080fd5b600160a060020a033316600090815260208190526040902061271090556101df8061003b6000396000f3006060604052600436106100565763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166327e235e3811461005b578063a9059cbb1461008c578063f8b2cb4f146100c2575b600080fd5b341561006657600080fd5b61007a600160a060020a03600435166100e1565b60405190815260200160405180910390f35b341561009757600080fd5b6100ae600160a060020a03600435166024356100f3565b604051901515815260200160405180910390f35b34156100cd57600080fd5b61007a600160a060020a0360043516610198565b60006020819052908152604090205481565b600160a060020a03331660009081526020819052604081205482901080159061011c5750600082115b1561018e57600160a060020a033381166000818152602081905260408082208054879003905592861680825290839020805486019055917fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a3506001610192565b5060005b92915050565b600160a060020a0316600090815260208190526040902054905600a165627a7a72305820f59b7130870eee8f044b129f4a20345ffaff662707fc0758133cd16684bc3b160029";
        // CHECKSTYLE:ON
        SM2KeyPair keyPair = generateKeyPair();

        for (int i = 0; i < 100; i++) {
            String privKey = keyPair.getPrivateKey().toString(16);
            System.out.println("test for private key: " + privKey);
            for (int j = 0; j < 30; j++) {
                System.out.println("Private key Index: " + i
                        + "\nSignature Index: " + j
                        + "************************************************************");
                String contractAddress = verifyTx(contractCode, privKey);
                System.out.println("deploy success: " + contractAddress + "\n");
            }
        }
    }

}
