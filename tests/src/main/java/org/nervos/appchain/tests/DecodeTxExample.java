package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
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
    private static BigInteger chainId;
    private static AppChainj service;
    private static String privateKey;
    private static long quotaToDeploy;
    private static String payeeAddr;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        privateKey = conf.primaryPrivKey;
        chainId = new BigInteger(conf.chainId);
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

        System.out.println(new Gson().toJson(tx.decodeContent()));
    }
}
