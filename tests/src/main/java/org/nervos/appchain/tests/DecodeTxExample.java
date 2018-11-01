package org.nervos.appchain.tests;

import java.util.concurrent.TimeUnit;

import org.nervos.appchain.crypto.ECKeyPair;
import org.nervos.appchain.protobuf.Blockchain.UnverifiedTransaction;
import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppTransaction;

public class DecodeTxExample {
    private static int version;
    private static int chainId;
    private static AppChainj service;
    private static String privateKey;
    private static long quotaToDeploy;
    private static String payeeAddr;

    static {
        Config conf = new Config();
        conf.buildService(true);
        service = conf.service;
        privateKey = conf.primaryPrivKey;
        chainId = Integer.parseInt(conf.chainId);
        payeeAddr = conf.auxAddr1;
        quotaToDeploy = Long.parseLong(conf.defaultQuotaDeployment);
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
    }

    public static void main(String[] args) throws Exception {
        Transaction transferTx = new Transaction(
                payeeAddr, TestUtil.getNonce(), quotaToDeploy,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId, "1", "");

        String rawTx = transferTx.sign(privateKey);
        AppSendTransaction appSendTransaction = service.appSendRawTransaction(rawTx).send();

        try {
            System.out.println("waiting for 5 seconds to send the transaction.");
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (appSendTransaction.getError() != null) {
            System.exit(1);
        } else {
            System.out.println("Hash of the transaction: "
                    + appSendTransaction.getSendTransactionResult().getHash());
        }

        System.out.println(
                "Waiting 5 seconds for the transaction from \"pending\" to \"latest\".");
        TimeUnit.SECONDS.sleep(5);

        AppTransaction appTx = service.appGetTransactionByHash(
                appSendTransaction.getSendTransactionResult().getHash()).send();


        org.nervos.appchain.protocol.core.methods.response.Transaction tx
                = appTx.getTransaction().get();

        UnverifiedTransaction unverifiedTransaction
                = UnverifiedTransaction.parseFrom(
                        TestUtil.convertHexToBytes(tx.getContent()));


        System.out.println("version:"
                + unverifiedTransaction.getTransaction().getVersion());

        System.out.println("validUntilBlock: "
                + unverifiedTransaction.getTransaction().getValidUntilBlock());

        System.out.println("nonce:"
                + unverifiedTransaction.getTransaction().getNonce());

        System.out.println("chainId: "
                + unverifiedTransaction.getTransaction().getChainId());

        System.out.println("to:"
                + unverifiedTransaction.getTransaction().getTo());

        System.out.println("chainId: "
                + ConvertStrByte.bytesToHexString(
                        unverifiedTransaction.getTransaction().getChainIdV1().toByteArray()));

        System.out.println("to:"
                + ConvertStrByte.bytesToHexString(
                        unverifiedTransaction.getTransaction().getToV1().toByteArray()));

        System.out.println("Content: " + tx.getContent());

        //verify signature of transaction fetched from chain by hash
        ECKeyPair pair = ECKeyPair.create(TestUtil.convertHexToBytes(privateKey));
        boolean isValid = VerifySignatureExample.verfiySig(
                unverifiedTransaction, pair.getPublicKey().toString(16));
        System.out.println("Verified or not: " + isValid);
    }
}
