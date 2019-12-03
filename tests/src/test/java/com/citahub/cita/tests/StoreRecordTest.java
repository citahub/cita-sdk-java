package com.citahub.cita.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.citahub.cita.protobuf.Blockchain;
import com.citahub.cita.protobuf.ConvertStrByte;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.methods.request.Transaction;
import com.citahub.cita.protocol.core.methods.response.AppSendTransaction;
import com.citahub.cita.protocol.system.CITAjSystemContract;
import com.citahub.cita.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class StoreRecordTest {

    private static String payerKey;
    private static BigInteger chainId;
    private static int version;
    private static Transaction.CryptoTx cryptoTx;

    static CITAj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;

        payerKey = conf.adminPrivateKey;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    @Test
    public void testStoreRecord()
            throws IOException, InterruptedException {

        String sampleDataToStore = ConvertStrByte
                .stringToHexString("SampleDataToStore");

        CITAjSystemContract sysContract = new CITAjSystemContract(service);

        Transaction txToStoreData = sysContract
                .constructStoreTransaction(sampleDataToStore, version, chainId);

        String signedTx = txToStoreData.sign(payerKey, cryptoTx, false);

        AppSendTransaction appSendTransaction
                = service.appSendRawTransaction(signedTx).send();

        String hash = appSendTransaction.getSendTransactionResult().getHash();

        System.out.println("Wait 6s for transaction written into block.");
        TimeUnit.SECONDS.sleep(6);

        String serializedTx = service
                .appGetTransactionByHash(hash).send()
                .getTransaction().getContent();

        System.out.println(
                "Unverified transaction(Content transaction): \n"
                        + serializedTx);

        byte[] byteSerializedTx = ConvertStrByte
                .hexStringToBytes(Numeric.cleanHexPrefix(serializedTx));

        Blockchain.UnverifiedTransaction unverifiedTransaction
                = Blockchain.UnverifiedTransaction
                .parseFrom(byteSerializedTx);

        System.out.println(unverifiedTransaction);
        String dataStored = unverifiedTransaction.getTransaction().getData().toStringUtf8();
        assertThat(dataStored,equalTo("SampleDataToStore"));
    }
}
