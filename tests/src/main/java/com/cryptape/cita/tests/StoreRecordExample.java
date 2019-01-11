package com.cryptape.cita.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import com.cryptape.cita.protobuf.Blockchain;
import com.cryptape.cita.protobuf.ConvertStrByte;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.cryptape.cita.protocol.system.CITAjSystemContract;
import com.cryptape.cita.utils.Numeric;

public class StoreRecordExample {

    private static String payerKey;
    private static BigInteger chainId;
    private static int version;
    private static Transaction.CryptoTx cryptoTx;

    static CITAj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;

        payerKey = conf.primaryPrivKey;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    public static void main(String[] args)
            throws IOException, InterruptedException {

        String sampleDataToStore = ConvertStrByte
                .stringToHexString("SampleDataToStore");

        CITAjSystemContract sysContract = new CITAjSystemContract(service);

        Transaction txToStoreData = sysContract
                .constructStoreTransaction(sampleDataToStore, version, chainId);

        String signedTx = txToStoreData.sign(payerKey, cryptoTx, false);

        AppSendTransaction appSendTransaction
                = service.appSendRawTransaction(signedTx).send();

        if (appSendTransaction.getError() != null) {
            System.out.println(
                    "Failed to get hash of transaction. Error message: "
                            + appSendTransaction.getError().getMessage());
            System.exit(1);
        }

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
    }
}
