package com.cryptape.cita.tests;

import static com.cryptape.cita.utils.Numeric.decodeQuantity;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.alibaba.fastjson.JSONObject;
import com.cryptape.cita.protobuf.ConvertStrByte;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.core.methods.response.AppTransaction;
import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/*
* This example shows how to decode transaction info from response.
* */

public class DecodeTxTest {
    private static int version;
    private static BigInteger chainId;
    private static CITAj service;
    private static String privateKey;
    private static long quotaToDeploy;
    private static String payeeAddr;
    private static Transaction.CryptoTx cryptoTx;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
        privateKey = conf.adminPrivateKey;
        chainId = new BigInteger(conf.chainId);
        payeeAddr = conf.auxAddr1;
        quotaToDeploy = Long.parseLong(conf.defaultQuotaDeployment);
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    private static String createSampleTransaction() throws IOException {
        String nonce = TestUtil.getNonce();
        String data = ConvertStrByte.stringToHexString("some message");
        long validUtilBlock = TestUtil.getValidUtilBlock(service).longValue();
        String value = decodeQuantity("0xa9b").toString();  // test value should be all-round, contains  at least one a-f

        Transaction transferTx = new Transaction(
                payeeAddr, nonce, quotaToDeploy, validUtilBlock, version, chainId, value , data);

        String rawTx = transferTx.sign(privateKey, cryptoTx, false);
        return service.appSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    @Test
    public void testDecodeTx( ) throws Exception {
        //create a sample transaction
        String hash = createSampleTransaction();
        System.out.println("Hash of the transaction: " + hash);

        //wait for 10s for transaction
        System.out.println("Waiting 10 seconds for the transaction.");
        TimeUnit.SECONDS.sleep(10);

        //get response transaction
        AppTransaction appTx = service.appGetTransactionByHash(hash).send();
        com.cryptape.cita.protocol.core.methods.response.Transaction tx = appTx.getTransaction();
        JSONObject contendJson = JSONObject.parseObject(new Gson().toJson(tx.decodeContent()));
        assertTrue(payeeAddr.equals("0x"+contendJson.getString("to")));
        System.out.println(new Gson().toJson(tx.decodeContent()));
    }
}
