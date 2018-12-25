package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.protobuf.Blockchain;
import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.system.AppChainjSystemContract;

import static org.nervos.appchain.utils.Numeric.cleanHexPrefix;

public class StoreRecordExample {

    private static String payerKey;
    private static BigInteger chainId;
    private static int version;
    private static Transaction.CryptoTx cryptoTx;

    static AppChainj service;

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

        AppChainjSystemContract sysContract = new AppChainjSystemContract(service);

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
                .hexStringToBytes(cleanHexPrefix(serializedTx));

        Blockchain.UnverifiedTransaction unverifiedTransaction
                = Blockchain.UnverifiedTransaction
                .parseFrom(byteSerializedTx);

        System.out.println(unverifiedTransaction);
    }
}
