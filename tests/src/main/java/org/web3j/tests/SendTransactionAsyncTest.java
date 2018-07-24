package org.web3j.tests;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class SendTransactionAsyncTest {
    static String testNetAddr;
    static String payerKey;
    static String payeeKey;
    static String payeeAddr;
    static int chainId;
    static int version;
    static Properties props;
    static long quota;

    static Web3j service;

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
        service = Web3j.build(new HttpService(testNetAddr));
    }

    static BigInteger getBalance(String address){
        BigInteger balance = null;
        try{
            EthGetBalance response = service.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            balance = response.getBalance();
        }catch (Exception e){
            System.out.println("failed to get balance.");
            System.exit(1);
        }
        return balance;
    }

    static TransactionReceipt transferSync(String payerKey, String payeeAddr, String value) throws Exception{
        PollingTransactionReceiptProcessor txProcessor = new PollingTransactionReceiptProcessor(
                service,
                15 * 1000,
                40);

        Transaction tx = new Transaction(payeeAddr,
                testUtil.getNonce(),
                quota,
                testUtil.getValidUtilBlock(service).longValue(),
                version,chainId,
                value,
                "");

        String rawTx = tx.sign(payerKey, false, false);
        EthSendTransaction ethSendTrasnction = service.ethSendRawTransaction(rawTx).send();

        TransactionReceipt txReceipt = txProcessor
                .waitForTransactionReceipt(
                        ethSendTrasnction.getSendTransactionResult().getHash());

        return txReceipt;
    }

    static CompletableFuture<TransactionReceipt> transferAsync(String payerKey, String payeeAddr, String value) throws Exception{
        return new RemoteCall<>(() -> transferSync(payerKey, payeeAddr, value)).sendAsync();
    }

    public static void main(String[] args) throws Exception{
        Credentials payerCredential = Credentials.create(payerKey);
        String payerAddr = payerCredential.getAddress();
        System.out.println(getBalance(payerAddr));
        System.out.println(getBalance(payeeAddr));

        String value = "1";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();

        CompletableFuture<TransactionReceipt> receiptFuture = transferAsync(payerKey, payeeAddr, valueWei);
        receiptFuture.whenCompleteAsync((data, exception)->{
            if(exception == null){
                if(data.getErrorMessage() == null){
                    System.out.println(getBalance(payerAddr));
                    System.out.println(getBalance(payeeAddr));
                }else{
                    System.out.println("Error get receipt: " + data.getErrorMessage());
                }
            }else{
                System.out.println("Exception happens: " + exception);
            }
        });
    }
}
