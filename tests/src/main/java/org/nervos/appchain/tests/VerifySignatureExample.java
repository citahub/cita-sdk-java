package org.nervos.appchain.tests;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppTransaction;
import org.nervos.appchain.utils.Numeric;

public class VerifySignatureExample {
    private static String privateKey;
    private static String primaryAddr;
    private static int chainId;
    private static int version;
    private static AppChainj service;
    private static String to;

    static {
        Config conf = new Config();
        conf.buildService(false);

        privateKey = conf.primaryPrivKey;
        primaryAddr = conf.primaryAddr;
        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        to = conf.auxAddr1;
    }


    public static Transaction createSampleTransaction() {
        String message = Numeric.cleanHexPrefix(ConvertStrByte.stringToHexString("somthing"));
        Transaction tx = new Transaction(
                to, TestUtil.getNonce(), 21000,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId, "1", message);
        return tx;
    }

    private static String sendSampleTransaction(String signedTx)
            throws IOException {
        AppSendTransaction appSendTransaction
                = service.appSendRawTransaction(signedTx).send();
        if (appSendTransaction.hasError()) {
            return null;
        } else {
            return appSendTransaction.getSendTransactionResult().getHash();
        }
    }

    private static org.nervos.appchain.protocol.core.methods.response.Transaction
            getResponseTransaction(String hash) throws IOException {
        AppTransaction appTransaction = service.appGetTransactionByHash(hash).send();
        if (appTransaction.getTransaction().isPresent()) {
            return appTransaction.getTransaction().get();
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        //create a sample transaction
        Transaction tx = createSampleTransaction();

        //sign the sample transaction
        String signedTx = tx.sign(privateKey);

        //get sent transaction hash
        String txHash = sendSampleTransaction(signedTx);
        System.out.println("transaction hash is: " + txHash);

        //wait 10s for transaction in the block
        System.out.println("wait for 10s for transaction");
        TimeUnit.SECONDS.sleep(10);

        //get response transaction
        org.nervos.appchain.protocol.core.methods.response.Transaction
                respTx = getResponseTransaction(txHash);

        //verify signature in response transaction
        boolean valid = respTx.verifySignature(primaryAddr);
        System.out.println("Verification: " + valid);
    }
}
