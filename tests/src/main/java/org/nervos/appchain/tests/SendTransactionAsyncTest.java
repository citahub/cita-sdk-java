package org.nervos.appchain.tests;

import java.math.BigInteger;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjFactory;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.RemoteCall;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetBalance;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.http.HttpService;
import org.nervos.appchain.tx.response.PollingTransactionReceiptProcessor;
import org.nervos.appchain.utils.Convert;
import rx.Completable;

public class SendTransactionAsyncTest {
    static String testNetAddr;
    static String payerKey;
    static String payeeKey;
    static String payeeAddr;
    static int chainId;
    static int version;
    static Properties props;
    static long quota;

    static Nervosj service;

    static {
        props = Config.load();
        testNetAddr = props.getProperty(Config.TEST_NET_ADDR);
        payerKey = props.getProperty(Config.SENDER_PRIVATE_KEY);
        payeeKey = props.getProperty(Config.TEST_PRIVATE_KEY_1);
        payeeAddr = props.getProperty(Config.TEST_ADDR_1);
        chainId = Integer.parseInt(props.getProperty(Config.CHAIN_ID));
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        quota = Long.parseLong(props.getProperty(Config.DEFAULT_QUOTA));

        HttpService.setDebug(false);
        service = NervosjFactory.build(new HttpService(testNetAddr));
    }

    static BigInteger getBalance(String address) {
        BigInteger balance = null;
        try {
            AppGetBalance response = service.appGetBalance(
                    address, DefaultBlockParameterName.LATEST).send();
            balance = response.getBalance();
        } catch (Exception e) {
            System.out.println("failed to get balance.");
            System.exit(1);
        }
        return balance;
    }

    static TransactionReceipt transferSync(
            String payerKey, String payeeAddr, String value)
            throws Exception {
        PollingTransactionReceiptProcessor txProcessor =
                new PollingTransactionReceiptProcessor(
                        service,
                        15 * 1000,
                        40);

        Transaction tx = new Transaction(payeeAddr,
                TestUtil.getNonce(),
                quota,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId,
                value,
                "");

        String rawTx = tx.sign(payerKey, false, false);
        AppSendTransaction appSendTrasnction = service
                .appSendRawTransaction(rawTx).send();

        TransactionReceipt txReceipt = txProcessor
                .waitForTransactionReceipt(
                        appSendTrasnction.getSendTransactionResult().getHash());

        return txReceipt;
    }

    static RemoteCall<TransactionReceipt> transferAsync(
            final String payerKey, final String payeeAddr, final String value) {
        return new RemoteCall<TransactionReceipt>(new Callable<TransactionReceipt>() {
            @Override
            public TransactionReceipt call() {
                TransactionReceipt receipt = null;
                try {
                    transferSync(payerKey, payeeAddr, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return receipt;
            }
        });
    }

    public static void main(String[] args) {
        Credentials payerCredential = Credentials.create(payerKey);
        String payerAddr = payerCredential.getAddress();
        System.out.println(Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
        System.out.println(Convert.fromWei(getBalance(payeeAddr).toString(), Convert.Unit.ETHER));

        String value = "1";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();

        Future<TransactionReceipt> receiptFuture =
                transferAsync(payerKey, payeeAddr, valueWei).sendAsync();

        try {
            System.out.println("Waiting 10s to get receipt...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            TransactionReceipt receipt = receiptFuture.get();
            if (receipt.getErrorMessage() == null) {
                System.out.println(
                        Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
                System.out.println(
                        Convert.fromWei(getBalance(payeeAddr).toString(), Convert.Unit.ETHER));
            } else {
                System.out.println("Error get receipt: " + receipt.getErrorMessage());
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
