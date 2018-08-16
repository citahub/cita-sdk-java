package org.nervos.appchain.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import org.nervos.appchain.abi.EventValues;
import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Event;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.Utf8String;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.SampleKeys;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.RemoteCall;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.Response;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.core.methods.response.AppGetCode;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.exceptions.TransactionException;
import org.nervos.appchain.utils.Async;
import org.nervos.appchain.utils.Numeric;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContractTest extends ManagedTransactionTester {

    private static final String TEST_CONTRACT_BINARY = "12345";

    private TestContract contract;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        contract = new TestContract(
                ADDRESS, nervosj, SampleKeys.CREDENTIALS,
                Contract.GAS_PRICE, Contract.GAS_LIMIT);
    }

    @Test
    public void testGetContractAddress() {
        assertThat(contract.getContractAddress(), is(ADDRESS));
    }

    @Test
    public void testGetContractTransactionReceipt() {
        assertNull(contract.getTransactionReceipt());
    }

    @Test
    public void testDeploy() throws Exception {
        TransactionReceipt transactionReceipt = createTransactionReceipt();
        Contract deployedContract = deployContract(transactionReceipt);

        assertThat(deployedContract.getContractAddress(), is(ADDRESS));
        assertNotNull(deployedContract.getTransactionReceipt());
        assertThat(deployedContract.getTransactionReceipt(), equalTo(transactionReceipt));
    }

    private TransactionReceipt createTransactionReceipt() {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);
        transactionReceipt.setContractAddress(ADDRESS);
        return transactionReceipt;
    }

    @Test
    public void testIsValid() throws Exception {
        prepareEthGetCode(TEST_CONTRACT_BINARY);

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    public void testIsValidDifferentCode() throws Exception {
        prepareEthGetCode(TEST_CONTRACT_BINARY + "0");

        Contract contract = deployContract(createTransactionReceipt());
        assertFalse(contract.isValid());
    }

    @Test
    public void testIsValidEmptyCode() throws Exception {
        prepareEthGetCode("");

        Contract contract = deployContract(createTransactionReceipt());
        assertFalse(contract.isValid());
    }

    @Test(expected = RuntimeException.class)
    public void testDeployInvalidContractAddress() throws Throwable {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);

        prepareTransaction(transactionReceipt);

        String encodedConstructor = FunctionEncoder.encodeConstructor(
                Arrays.<Type>asList(new Uint256(BigInteger.TEN)));

        try {
            TestContract.deployRemoteCall(
                    TestContract.class,
                    nervosj, SampleKeys.CREDENTIALS,
                    BigInteger.valueOf(100L), BigInteger.valueOf(100L),
                    "0xcafed00d", encodedConstructor, "0").send();
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testCallSingleValue() throws Exception {
        // Example taken from FunctionReturnDecoderTest

        AppCall appCall = new AppCall();
        appCall.setResult("0x0000000000000000000000000000000000000000000000000000000000000020"
                + "0000000000000000000000000000000000000000000000000000000000000000");
        prepareCall(appCall);

        assertThat(contract.callSingleValue().send(), equalTo(new Utf8String("")));
    }

    @Test
    public void testCallSingleValueEmpty() throws Exception {
        // Example taken from FunctionReturnDecoderTest

        AppCall appCall = new AppCall();
        appCall.setResult("0x");
        prepareCall(appCall);

        assertNull(contract.callSingleValue().send());
    }

    @Test
    public void testCallMultipleValue() throws Exception {
        AppCall appCall = new AppCall();
        appCall.setResult("0x0000000000000000000000000000000000000000000000000000000000000037"
                + "0000000000000000000000000000000000000000000000000000000000000007");
        prepareCall(appCall);

        assertThat(contract.callMultipleValue().send(),
                equalTo(Arrays.<Type>asList(
                        new Uint256(BigInteger.valueOf(55)),
                        new Uint256(BigInteger.valueOf(7)))));
    }

    @Test
    public void testCallMultipleValueEmpty() throws Exception {
        AppCall appCall = new AppCall();
        appCall.setResult("0x");
        prepareCall(appCall);

        assertThat(contract.callMultipleValue().send(),
                equalTo(Collections.<Type>emptyList()));
    }

    @SuppressWarnings("unchecked")
    private void prepareCall(AppCall ethCall) throws IOException {
        Request<?, AppCall> request = mock(Request.class);
        when(request.send()).thenReturn(ethCall);

        when(nervosj.appCall(any(Call.class), eq(DefaultBlockParameterName.LATEST)))
                .thenReturn((Request) request);
    }

    @Test
    public void testTransaction() throws Exception {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);

        prepareTransaction(transactionReceipt);

        assertThat(contract.performTransaction(
                new Address(BigInteger.TEN), new Uint256(BigInteger.ONE)).send(),
                is(transactionReceipt));
    }

    @Test
    public void testProcessEvent() {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        Log log = new Log();
        log.setTopics(Arrays.asList(
                // encoded function
                "0xfceb437c298f40d64702ac26411b2316e79f3c28ffa60edfc891ad4fc8ab82ca",
                // indexed value
                "0000000000000000000000003d6cb163f7c72d20b0fcd6baae5889329d138a4a"));
        // non-indexed value
        log.setData("0000000000000000000000000000000000000000000000000000000000000001");

        transactionReceipt.setLogs(Arrays.asList(log));

        EventValues eventValues = contract.processEvent(transactionReceipt).get(0);

        assertThat(eventValues.getIndexedValues(),
                equalTo(Collections.<Type>singletonList(
                        new Address("0x3d6cb163f7c72d20b0fcd6baae5889329d138a4a"))));
        assertThat(eventValues.getNonIndexedValues(),
                equalTo(Collections.<Type>singletonList(new Uint256(BigInteger.ONE))));
    }

    @Test(expected = TransactionException.class)
    public void testTimeout() throws Throwable {
        prepareTransaction(null);

        TransactionManager transactionManager = new RawTransactionManager(
                nervosj, SampleKeys.CREDENTIALS, 1, 1);

        contract = new TestContract(
                ADDRESS, nervosj, transactionManager,
                Contract.GAS_PRICE, Contract.GAS_LIMIT);

        testErrorScenario();
    }

    @Test(expected = RuntimeException.class)
    @SuppressWarnings("unchecked")
    public void testInvalidTransactionResponse() throws Throwable {
        prepareNonceRequest();

        final AppSendTransaction appSendTransaction = new AppSendTransaction();
        appSendTransaction.setError(new Response.Error(1, "Invalid transaction"));

        Request<?, AppSendTransaction> rawTransactionRequest = mock(Request.class);
        when(rawTransactionRequest.sendAsync()).thenReturn(Async.run(new Callable<AppSendTransaction>() {
            @Override
            public AppSendTransaction call() throws Exception {
                return appSendTransaction;
            }
        }));
        when(nervosj.appSendRawTransaction(any(String.class)))
                .thenReturn((Request) rawTransactionRequest);

        testErrorScenario();
    }

    @Test
    public void testSetGetAddresses() throws Exception {
        assertNull(contract.getDeployedAddress("1"));
        contract.setDeployedAddress("1", "0x000000000000add0e00000000000");
        assertNotNull(contract.getDeployedAddress("1"));
        contract.setDeployedAddress("2", "0x000000000000add0e00000000000");
        assertNotNull(contract.getDeployedAddress("2"));
    }

    @Test(expected = RuntimeException.class)
    @SuppressWarnings("unchecked")
    public void testInvalidTransactionReceipt() throws Throwable {
        prepareNonceRequest();
        prepareTransactionRequest();

        final AppGetTransactionReceipt appGetTransactionReceipt = new AppGetTransactionReceipt();
        appGetTransactionReceipt.setError(new Response.Error(1, "Invalid transaction receipt"));

        Request<?, AppGetTransactionReceipt> getTransactionReceiptRequest = mock(Request.class);
        when(getTransactionReceiptRequest.sendAsync())
                .thenReturn(Async.run(new Callable<AppGetTransactionReceipt>() {
                    @Override
                    public AppGetTransactionReceipt call() throws Exception {
                        return appGetTransactionReceipt;
                    }
                }));
        when(nervosj.appGetTransactionReceipt(TRANSACTION_HASH))
                .thenReturn((Request) getTransactionReceiptRequest);

        testErrorScenario();
    }

    void testErrorScenario() throws Throwable {
        try {
            contract.performTransaction(
                    new Address(BigInteger.TEN), new Uint256(BigInteger.ONE)).send();
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    private Contract deployContract(TransactionReceipt transactionReceipt)
            throws Exception {

        prepareTransaction(transactionReceipt);

        String encodedConstructor = FunctionEncoder.encodeConstructor(
                Arrays.<Type>asList(new Uint256(BigInteger.TEN)));

        return TestContract.deployRemoteCall(
                TestContract.class,
                nervosj, SampleKeys.CREDENTIALS,
                BigInteger.valueOf(100L), BigInteger.valueOf(100L),
                "0xcafed00d", encodedConstructor, "0").send();
    }

    @SuppressWarnings("unchecked")
    private void prepareEthGetCode(String binary) throws IOException {
        AppGetCode ethGetCode = new AppGetCode();
        ethGetCode.setResult(Numeric.prependHexPrefix(binary));

        Request<?, AppGetCode> ethGetCodeRequest = mock(Request.class);
        when(ethGetCodeRequest.send())
                .thenReturn(ethGetCode);
        when(nervosj.appGetCode(ADDRESS, DefaultBlockParameterName.LATEST))
                .thenReturn((Request) ethGetCodeRequest);
    }

    private static class TestContract extends Contract {
        public TestContract(
                String contractAddress, Nervosj nervosj, Credentials credentials,
                BigInteger gasPrice, BigInteger gasLimit) {
            super(TEST_CONTRACT_BINARY, contractAddress, nervosj, credentials, gasPrice, gasLimit);
        }

        public TestContract(
                String contractAddress,
                Nervosj nervosj, TransactionManager transactionManager,
                BigInteger gasPrice, BigInteger gasLimit) {
            super(TEST_CONTRACT_BINARY, contractAddress, nervosj, transactionManager, gasPrice,
                    gasLimit);
        }

        public RemoteCall<Utf8String> callSingleValue() {
            Function function = new Function("call",
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                    }));
            return executeRemoteCallSingleValueReturn(function);
        }

        public RemoteCall<List<Type>> callMultipleValue()
                throws ExecutionException, InterruptedException {
            Function function = new Function("call",
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(
                            new TypeReference<Uint256>() { },
                            new TypeReference<Uint256>() { }));
            return executeRemoteCallMultipleValueReturn(function);
        }

        public RemoteCall<TransactionReceipt> performTransaction(
                Address address, Uint256 amount) {
            Function function = new Function("approve",
                    Arrays.<Type>asList(address, amount),
                    Collections.<TypeReference<?>>emptyList());
            return executeRemoteCallTransaction(function);
        }

        public List<EventValues> processEvent(TransactionReceipt transactionReceipt) {
            Event event = new Event("Event",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Address>() { }),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() { }));
            return extractEventParameters(event, transactionReceipt);
        }
    }
}
