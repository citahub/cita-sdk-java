package com.citahub.cita.tests;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameterName;
import com.citahub.cita.protocol.core.methods.request.Transaction;
import com.citahub.cita.protocol.core.methods.response.AppGetBalance;
import com.citahub.cita.protocol.core.methods.response.AppSendTransaction;
import com.citahub.cita.protocol.core.methods.response.TransactionReceipt;
import com.citahub.cita.tx.response.PollingTransactionReceiptProcessor;
import com.citahub.cita.utils.Convert;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

public class SendTransactionSyncTest {

    private static String adminKey;
    private static String adminKeyAddr;
    private static String payerKey;
    private static String payerAddr;
    private static String payeeAddr1;
    private static String payeeAddr2;
    private static BigInteger chainId;
    private static int version;
    private static long quotaToTransfer;
    private static Transaction.CryptoTx cryptoTx;

    static CITAj service;

    static {
        Config conf = new Config();
        conf.buildService(false);

        adminKey = conf.adminPrivateKey;
        adminKeyAddr = conf.adminAddress;
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
            //System.exit(1);
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

    @Test
    public void testSendTransactionSync() throws Exception {

        BigDecimal  payeeBalanceEther1 = Convert.fromWei(getBalance(payeeAddr1).toString(), Convert.Unit.ETHER);
        BigDecimal  payeeBalanceEther2 = Convert.fromWei(getBalance(payeeAddr2).toString(), Convert.Unit.ETHER);
        System.out.println(payeeAddr1 + ": " + payeeBalanceEther1);
        System.out.println(payeeAddr2 + ": " + payeeBalanceEther2);

        String value = "1";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();
        TransactionReceipt transactionReceipt1 = transferSync(adminKey, payeeAddr1, valueWei);
        TransactionReceipt transactionReceipt2 = transferSync(adminKey, payeeAddr2, valueWei);
        assertNull(transactionReceipt1.getErrorMessage());
        assertNull(transactionReceipt2.getErrorMessage());
        BigDecimal  payeeBalanceEtherNow1 = Convert.fromWei(getBalance(payeeAddr1).toString(), Convert.Unit.ETHER);
        BigDecimal  payeeBalanceEtherNow2 = Convert.fromWei(getBalance(payeeAddr2).toString(), Convert.Unit.ETHER);

        System.out.println(payeeAddr1 + ": " + payeeBalanceEtherNow1);
        System.out.println(payeeAddr2 + ": " + payeeBalanceEtherNow2);
        assertThat(payeeBalanceEtherNow1.subtract(payeeBalanceEther1).intValue()+"", equalTo(value));
        assertThat(payeeBalanceEtherNow2.subtract(payeeBalanceEther2).intValue()+"", equalTo((value)));
    }


    public static void initSenderBalance () throws Exception {

        BigDecimal  adminBalanceEther = Convert.fromWei(getBalance(adminKeyAddr).toString(), Convert.Unit.ETHER);
        BigDecimal  payerBalanceEther = Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER);
        System.out.println(adminKeyAddr + ": " + adminBalanceEther);
        System.out.println(payerAddr + ": " + payerBalanceEther);

        String value = "40";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();
        TransactionReceipt transactionReceipt1 = transferSync(adminKey, payerAddr, valueWei);
        assertNull(transactionReceipt1.getErrorMessage());

        BigDecimal  payerBalanceEtherNow = Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER);
        System.out.println(payerAddr + ": " + payerBalanceEtherNow);

        String addEther = payerBalanceEtherNow.subtract(payerBalanceEther)+"";
        assertThat(addEther, equalTo(value));
    }

}
