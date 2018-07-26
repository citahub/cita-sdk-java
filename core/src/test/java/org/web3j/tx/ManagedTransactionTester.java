package org.web3j.tx;

import java.io.IOException;

import org.junit.Before;

import org.web3j.crypto.SampleKeys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.AppGetTransactionCount;
import org.web3j.protocol.core.methods.response.AppGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.AppSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class ManagedTransactionTester {

    static final String ADDRESS = "0x3d6cb163f7c72d20b0fcd6baae5889329d138a4a";
    static final String TRANSACTION_HASH = "0xHASH";
    protected Web3j web3j;

    @Before
    public void setUp() throws Exception {
        web3j = mock(Web3j.class);
    }

    void prepareTransaction(TransactionReceipt transactionReceipt) throws IOException {
        prepareNonceRequest();
        prepareTransactionRequest();
        prepareTransactionReceipt(transactionReceipt);
    }

    @SuppressWarnings("unchecked")
    void prepareNonceRequest() throws IOException {
        AppGetTransactionCount ethGetTransactionCount = new AppGetTransactionCount();
        ethGetTransactionCount.setResult("0x1");

        Request<?, AppGetTransactionCount> transactionCountRequest = mock(Request.class);
        when(transactionCountRequest.send())
                .thenReturn(ethGetTransactionCount);
        when(web3j.appGetTransactionCount(SampleKeys.ADDRESS, DefaultBlockParameterName.PENDING))
                .thenReturn((Request) transactionCountRequest);
    }

    @SuppressWarnings("unchecked")
    void prepareTransactionRequest() throws IOException {
        AppSendTransaction ethSendTransaction = new AppSendTransaction();
        AppSendTransaction.SendTransactionResult sendTransactionResult =
                new AppSendTransaction.SendTransactionResult();
        ethSendTransaction.setResult(sendTransactionResult);
        ethSendTransaction.getSendTransactionResult().setHash(TRANSACTION_HASH);

        Request<?, AppSendTransaction> rawTransactionRequest = mock(Request.class);
        when(rawTransactionRequest.send()).thenReturn(ethSendTransaction);
        when(web3j.appSendRawTransaction(any(String.class)))
                .thenReturn((Request) rawTransactionRequest);
    }

    @SuppressWarnings("unchecked")
    void prepareTransactionReceipt(TransactionReceipt transactionReceipt) throws IOException {
        AppGetTransactionReceipt ethGetTransactionReceipt = new AppGetTransactionReceipt();
        ethGetTransactionReceipt.setResult(transactionReceipt);

        Request<?, AppGetTransactionReceipt> getTransactionReceiptRequest = mock(Request.class);
        when(getTransactionReceiptRequest.send())
                .thenReturn(ethGetTransactionReceipt);
        when(web3j.appGetTransactionReceipt(TRANSACTION_HASH))
                .thenReturn((Request) getTransactionReceiptRequest);
    }
}
