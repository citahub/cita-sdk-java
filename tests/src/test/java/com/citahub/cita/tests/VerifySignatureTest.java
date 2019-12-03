package com.citahub.cita.tests;

import static org.junit.Assert.assertTrue;

import com.citahub.cita.protobuf.ConvertStrByte;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.methods.request.Transaction;
import com.citahub.cita.protocol.core.methods.response.AppSendTransaction;
import com.citahub.cita.protocol.core.methods.response.AppTransaction;
import com.citahub.cita.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class VerifySignatureTest {
    private static String privateKey;
    private static String primaryAddr;
    private static BigInteger chainId;
    private static int version;
    private static CITAj service;
    private static String to;
    private static Transaction.CryptoTx cryptoTx;

    static {
        Config conf = new Config();
        conf.buildService(false);

        privateKey = conf.adminPrivateKey;
        primaryAddr = conf.adminAddress;
        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        to = conf.auxAddr1;
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
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

    private static com.citahub.cita.protocol.core.methods.response.Transaction
            getResponseTransaction(String hash) throws IOException {
        AppTransaction appTransaction = service.appGetTransactionByHash(hash).send();
        if (appTransaction.getTransaction() != null) {
            return appTransaction.getTransaction();
        } else {
            return null;
        }
    }

    @Test
    public void testVerifySignatureTest() throws Exception {
        //create a sample transaction
        Transaction tx = createSampleTransaction();

        //sign the sample transaction
        String signedTx = tx.sign(privateKey, cryptoTx, false);

        //get sent transaction hash
        String txHash = sendSampleTransaction(signedTx);
        System.out.println("transaction hash is: " + txHash);

        //wait 10s for transaction in the block
        System.out.println("wait for 10s for transaction");
        TimeUnit.SECONDS.sleep(10);

        //get response transaction
        com.citahub.cita.protocol.core.methods.response.Transaction
                respTx = getResponseTransaction(txHash);

        //verify signature in response transaction
        boolean valid = respTx.verifySignature(primaryAddr);
        System.out.println("Verification: " + valid);
        assertTrue(valid);
    }
}
