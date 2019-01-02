package org.nervos.appchain.tests;

import java.math.BigInteger;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetBalance;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.tx.response.PollingTransactionReceiptProcessor;
import org.nervos.appchain.utils.Convert;

public class SendTransactionSyncExample {

    private static String payerKey;
    private static String payerAddr;
    private static String payeeAddr1;
    private static String payeeAddr2;
    private static BigInteger chainId;
    private static int version;
    private static long quotaToTransfer;
    private static Transaction.CryptoTx cryptoTx;

    static AppChainj service;

    static {
        Config conf = new Config();
        conf.buildService(false);

        payerKey = conf.primaryPrivKey;
        payerAddr = conf.primaryAddr;
        payeeAddr1 = conf.auxAddr1;
        payeeAddr2 = conf.auxAddr2;
        quotaToTransfer = Long.parseLong(conf.defaultQuotaDeployment);
        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    private static BigInteger getBalance(String address) {
        BigInteger balance = null;
        try {
            AppGetBalance response = service
                    .appGetBalance(
                            address, DefaultBlockParameterName.PENDING).send();
            balance = response.getBalance();
        } catch (Exception e) {
            System.out.println("failed to get balance.");
            System.exit(1);
        }
        return balance;
    }

    //use PollingTransactionReceiptProcessor to wait for transaction receipt.
    private static TransactionReceipt transferSync(
            String payerKey, String payeeAddr, String value)
            throws Exception {
        PollingTransactionReceiptProcessor txProcessor = new PollingTransactionReceiptProcessor(
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
        AppSendTransaction appSendTrasnction = service.appSendRawTransaction(rawTx).send();

        TransactionReceipt txReceipt = txProcessor
                .waitForTransactionReceipt(
                        appSendTrasnction.getSendTransactionResult().getHash());
        return txReceipt;
    }

    public static void main(String[] args) throws Exception {

        System.out.println(payerAddr + ": "
                + Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
        System.out.println(payeeAddr1 + ": "
                + Convert.fromWei(getBalance(payeeAddr1).toString(), Convert.Unit.ETHER));
        System.out.println(payeeAddr2 + ": "
                + Convert.fromWei(getBalance(payeeAddr2).toString(), Convert.Unit.ETHER));

        String value = "1";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();

        TransactionReceipt txReceipt = transferSync(payerKey, payeeAddr1, valueWei);

        if (txReceipt.getErrorMessage() == null) {
            System.out.println(payerAddr + ": "
                    + Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
            System.out.println(payeeAddr1 + ": "
                    + Convert.fromWei(getBalance(payeeAddr1).toString(), Convert.Unit.ETHER));
        }

        TransactionReceipt txReceipt1 = transferSync(payerKey, payeeAddr2, valueWei);

        if (txReceipt1.getErrorMessage() == null) {
            System.out.println(payerAddr + ": "
                    + Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
            System.out.println(payeeAddr2 + ": "
                    + Convert.fromWei(getBalance(payeeAddr2).toString(), Convert.Unit.ETHER));
        }
    }
}
