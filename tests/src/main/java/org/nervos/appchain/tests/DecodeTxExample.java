package org.nervos.appchain.tests;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.InvalidProtocolBufferException;
import org.nervos.appchain.crypto.ECKeyPair;
import org.nervos.appchain.protobuf.Blockchain;
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
        conf.buildService(false);
        service = conf.service;
        privateKey = conf.primaryPrivKey;
        chainId = Integer.parseInt(conf.chainId);
        payeeAddr = conf.auxAddr1;
        quotaToDeploy = Long.parseLong(conf.defaultQuotaDeployment);
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
    }

    private static String createSampleTransaction() throws IOException {
        String nonce = TestUtil.getNonce();
        String data = ConvertStrByte.stringToHexString("some message");
        long validUtilBlock = TestUtil.getValidUtilBlock(service).longValue();

        Transaction transferTx = new Transaction(
                payeeAddr, nonce, quotaToDeploy, validUtilBlock, version, chainId, "1", data);

        String rawTx = transferTx.sign(privateKey);
        return service.appSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    public static void main(String[] args) throws Exception {
        //create a sample transaction
        String hash = createSampleTransaction();
        System.out.println("Hash of the transaction: " + hash);

        //wait for 10s for transaction
        System.out.println("Waiting 10 seconds for the transaction.");
        TimeUnit.SECONDS.sleep(10);

        //get response transaction
        AppTransaction appTx = service.appGetTransactionByHash(hash).send();
        org.nervos.appchain.protocol.core.methods.response.Transaction tx
                = appTx.getTransaction();

<<<<<<< HEAD
        //decode from response transaction's content
        Transaction decodedTx = tx.decodeContent();
        System.out.println("version: " + decodedTx.getVersion());
        System.out.println("nonce: " + decodedTx.getNonce());
        System.out.println("quota: " + decodedTx.getQuota());
        System.out.println("ValidUntilBlock: " + decodedTx.get_valid_until_block());
        System.out.println("data: " + decodedTx.getData());
        System.out.println("value: " + decodedTx.getValue());
        System.out.println("chainId: " + decodedTx.getChainId());
        System.out.println("to: " + decodedTx.getTo());
=======
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
>>>>>>> Replace CompletableFuture with Future and delete Optional
    }
}
