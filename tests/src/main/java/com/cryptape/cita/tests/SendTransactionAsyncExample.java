package com.cryptape.cita.tests;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.DefaultBlockParameterName;
import com.cryptape.cita.protocol.core.RemoteCall;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.core.methods.response.AppGetBalance;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.cryptape.cita.tx.response.PollingTransactionReceiptProcessor;
import com.cryptape.cita.utils.Convert;

public class SendTransactionAsyncExample {
    private static String payerKey;
    private static String payerAddr;
    private static String payeeAddr;
    private static BigInteger chainId;
    private static int version;
    private static long quotaToTransfer;
    private static Transaction.CryptoTx cryptoTx;

    static CITAj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        payerKey = conf.primaryPrivKey;
        payerAddr = conf.primaryAddr;
        payeeAddr = conf.auxAddr1;
        quotaToTransfer = Long.parseLong(conf.defaultQuotaTransfer);
        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    private static BigInteger getBalance(String address) {
        BigInteger balance = null;
        try {
            AppGetBalance response = service.appGetBalance(
                    address, DefaultBlockParameterName.PENDING).send();
            balance = response.getBalance();
        } catch (Exception e) {
            System.out.println("failed to get balance.");
            System.exit(1);
        }
        return balance;
    }

    private static TransactionReceipt transferSync(
            String payerKey, String payeeAddr, String value)
            throws Exception {
        PollingTransactionReceiptProcessor txProcessor =
                new PollingTransactionReceiptProcessor(
                        service,
                        15 * 1000,
                        40);

        Transaction tx = new Transaction(payeeAddr,
                TestUtil.getNonce(),
                quotaToTransfer,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId,
                value,
                "");

        String rawTx = tx.sign(payerKey, cryptoTx, false);
        AppSendTransaction ethSendTrasnction = service
                .appSendRawTransaction(rawTx).send();

        TransactionReceipt txReceipt = txProcessor
                .waitForTransactionReceipt(
                        ethSendTrasnction.getSendTransactionResult().getHash());

        return txReceipt;
    }

    private static Future<TransactionReceipt> transferAsync(
            String payerKey, String payeeAddr, String value) {
        return new RemoteCall<>(
                () -> transferSync(payerKey, payeeAddr, value)).sendAsync();
    }

    public static void main(String[] args) {
        System.out.println(Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
        System.out.println(Convert.fromWei(getBalance(payeeAddr).toString(), Convert.Unit.ETHER));

        String value = "1";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();

        Future<TransactionReceipt> receiptFuture =
                transferAsync(payerKey, payeeAddr, valueWei);
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
