package com.cryptape.cita.tests;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.DefaultBlockParameterName;
import com.cryptape.cita.protocol.core.RemoteCall;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.core.methods.response.AppGetBalance;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.cryptape.cita.tx.response.PollingTransactionReceiptProcessor;
import com.cryptape.cita.utils.Convert;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;

public class SendTransactionTest {
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
        payerKey = conf.adminPrivateKey;
        payerAddr = conf.adminAddress;
        payeeAddr = conf.auxAddr1;
        quotaToTransfer = Long.parseLong(conf.defaultQuotaTransfer);
        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    public static BigInteger getBalance(String address) {
        BigInteger balance = null;
        try {
            AppGetBalance response = service.appGetBalance(
                    address, DefaultBlockParameterName.PENDING).send();
            balance = response.getBalance();
        } catch (Exception e) {
            System.out.println("failed to get balance.");
            //System.exit(1);
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

    public static Future<TransactionReceipt> transferAsync(
            String payerKey, String payeeAddr, String value) {
        return new RemoteCall<>(
                () -> transferSync(payerKey, payeeAddr, value)).sendAsync();
    }

    @Test
    public void testSendTransaction( ) throws ExecutionException, InterruptedException {
        BigDecimal payeeBalance =  Convert.fromWei(getBalance(payeeAddr).toString(), Convert.Unit.ETHER);
        String value = "1";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();
        Future<TransactionReceipt> receiptFuture =
            transferAsync(payerKey, payeeAddr, valueWei);
        TransactionReceipt receipt = receiptFuture.get();
        assertNull(receipt.getErrorMessage());
        BigDecimal payeeBalanceNow =  Convert.fromWei(getBalance(payeeAddr).toString(), Convert.Unit.ETHER);
        assertThat(payeeBalanceNow.subtract(payeeBalance).intValue(), equalTo(Integer.parseInt(value)));
    }
}
