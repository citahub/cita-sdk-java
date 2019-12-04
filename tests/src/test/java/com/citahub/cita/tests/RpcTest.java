package com.citahub.cita.tests;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.citahub.cita.abi.FunctionEncoder;
import com.citahub.cita.abi.TypeReference;
import com.citahub.cita.abi.datatypes.Address;
import com.citahub.cita.abi.datatypes.Function;
import com.citahub.cita.abi.datatypes.Uint;
import com.citahub.cita.crypto.Credentials;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.DefaultBlockParameterName;
import com.citahub.cita.protocol.core.methods.request.Call;
import com.citahub.cita.protocol.core.methods.request.Transaction;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.AppBlockNumber;
import com.citahub.cita.protocol.core.methods.response.AppCall;
import com.citahub.cita.protocol.core.methods.response.AppGetBalance;
import com.citahub.cita.protocol.core.methods.response.AppGetCode;
import com.citahub.cita.protocol.core.methods.response.AppGetTransactionCount;
import com.citahub.cita.protocol.core.methods.response.AppGetTransactionReceipt;
import com.citahub.cita.protocol.core.methods.response.AppMetaData;
import com.citahub.cita.protocol.core.methods.response.AppSendTransaction;
import com.citahub.cita.protocol.core.methods.response.AppTransaction;
import com.citahub.cita.protocol.core.methods.response.AppVersion;
import com.citahub.cita.protocol.core.methods.response.NetPeerCount;
import com.citahub.cita.protocol.core.methods.response.NetPeersInfo;
import com.citahub.cita.protocol.core.methods.response.TransactionReceipt;
import com.citahub.cita.utils.Convert;
import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class RpcTest {

    private static int version;
    private static BigInteger chainId;
    private static CITAj service;
    private static String value;
    private static String privateKey;
    private static String adminPrivateKey;
    private static long quotaToDeploy;
    private static Transaction.CryptoTx cryptoTx;


    //because unsigned transaction is not supported in cita, there is no method sendTransaction.
    String code = "6060604052341561000f57600080fd5b600160a060020"
        + "a033316600090815260208190526040902061271090556101"
        + "df8061003b6000396000f3006060604052600436106100565"
        + "763ffffffff7c010000000000000000000000000000000000"
        + "000000000000000000000060003504166327e235e38114610"
        + "05b578063a9059cbb1461008c578063f8b2cb4f146100c257"
        + "5b600080fd5b341561006657600080fd5b61007a600160a06"
        + "0020a03600435166100e1565b604051908152602001604051"
        + "80910390f35b341561009757600080fd5b6100ae600160a06"
        + "0020a03600435166024356100f3565b604051901515815260"
        + "200160405180910390f35b34156100cd57600080fd5b61007"
        + "a600160a060020a0360043516610198565b60006020819052"
        + "908152604090205481565b600160a060020a0333166000908"
        + "1526020819052604081205482901080159061011c57506000"
        + "82115b1561018e57600160a060020a0333811660008181526"
        + "0208190526040808220805487900390559286168082529083"
        + "9020805486019055917fddf252ad1be2c89b69c2b068fc378"
        + "daa952ba7f163c4a11628f55a4df523b3ef90859051908152"
        + "60200160405180910390a3506001610192565b5060005b929"
        + "15050565b600160a060020a03166000908152602081905260"
        + "40902054905600a165627a7a72305820f59b7130870eee8f0"
        + "44b129f4a20345ffaff662707fc0758133cd16684bc3b160029";

    static {
        Config conf = new Config();
        conf.buildService(true);
        service = conf.service;
        privateKey = conf.primaryPrivKey;
        adminPrivateKey = conf.adminPrivateKey;
        quotaToDeploy = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    @Test
    public void testGetBlockByNumber() throws IOException {
        BigInteger number = BigInteger.valueOf(4L);
        AppBlock.Block block = service.appGetBlockByNumber(DefaultBlockParameter.valueOf(number), false).send().getBlock();
        assertNotNull(block.getHash());
        assertNotNull(block.getBody());
        System.out.println("block " + number.toString() + " is: " + new Gson().toJson(block));
    }

    @Test
    public void testGetBalance() throws Exception {
        Credentials c = Credentials.create(privateKey);
        String addr = c.getAddress();
        AppGetBalance appGetbalance = service.appGetBalance(
                addr, DefaultBlockParameterName.PENDING).send();
        assertNotNull(appGetbalance.getBalance());
    }

    @Test
    public void testMetaData() throws Exception {
        AppMetaData appMetaData = service.appMetaData(DefaultBlockParameterName.PENDING).send();
        assertNotNull(appMetaData.getAppMetaDataResult());
        System.out.println("AppMetaData: " + new Gson().toJson(appMetaData));
    }

    @Test
    public void testNetPeerCount() throws Exception {
        NetPeerCount netPeerCount = service.netPeerCount().send();
        assertNotNull(netPeerCount);
        System.out.println("net_peerCount:" + netPeerCount.getQuantity());
    }

    @Test
    public void testNetPeersInfo() throws Exception {
        NetPeersInfo netPeersInfo = service.netPeersInfo().send();
        Map<String, String> peers = netPeersInfo.getPeersInfo().peers;
        System.out.println("net_peersInfo amount:" + netPeersInfo.getPeersInfo().amount);
        for (Map.Entry<String, String> entry : peers.entrySet()) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            System.out.println("Address : " + entry.getKey() + " Node : " + entry.getValue());
        }
    }

    @Test
    public void testGetVersion() throws Exception {
        AppVersion appVersion = service.getVersion().send();

        if(appVersion.getError().equals(null)){
            assertNotNull(appVersion.getVersion());
        }else {
            assertTrue(appVersion.getError().getCode() == -32601);
        }
    }

    @Test
    public void testBlockNumber() throws Exception {
        AppBlockNumber appBlockNumber = service.appBlockNumber().send();
        BigInteger validBlockNumber = appBlockNumber.getBlockNumber();
        assertTrue(validBlockNumber.longValue()>0);
    }

    @Test
    public void testAppGetBlockByNumber()
            throws Exception {
        BigInteger validBlockNumber = BigInteger.valueOf(4L);
        boolean isfullTranobj = true;
        AppBlock appBlock = service.appGetBlockByNumber(
                DefaultBlockParameter.valueOf(validBlockNumber), isfullTranobj).send();
        AppBlock.Block block = appBlock.getBlock();
        assertNotNull(block.getBody());
    }

    @Test
    public void testAppGetBlockByHash()
            throws Exception {
        BigInteger validBlockNumber = BigInteger.valueOf(4L);
        AppBlock appBlock = service.appGetBlockByNumber(
            DefaultBlockParameter.valueOf(validBlockNumber), true).send();
        String validBlockHash = appBlock.getBlock().getHash();
        AppBlock appFindBlock = service
                .appGetBlockByHash(validBlockHash, true).send();
        String appBlockResult = new Gson().toJson(appBlock.getResult().getBody());
        String appBlockFindResult = new Gson().toJson(appFindBlock.getResult().getBody());
        assertThat(appBlockFindResult, equalTo(appBlockResult));
    }

    public AppSendTransaction appSendRawTransaction() throws IOException {
        String nonce = TestUtil.getNonce();
        BigInteger validUtil = TestUtil.getValidUtilBlock(service);
        Transaction rtx = Transaction.createContractTransaction(
            nonce, quotaToDeploy, validUtil.longValue(),
            version, chainId, value, code);
        String signedTransaction = rtx.sign(adminPrivateKey, cryptoTx, false);
        AppSendTransaction appSendTx = service
            .appSendRawTransaction(signedTransaction).send();
        return appSendTx;
    }

    @Test
    public void testAppSendRawTransaction( ) throws Exception {
        AppSendTransaction appSendTx = appSendRawTransaction();
        String hash = appSendTx.getSendTransactionResult().getHash();
        String status = appSendTx.getSendTransactionResult().getStatus();
        assertNotNull(hash);
        assertNotNull(status);
    }

    @Test
    public void testAppGetTransactionByHash( ) throws Exception {
        AppSendTransaction appSendTx = appSendRawTransaction();
        String hash = appSendTx.getSendTransactionResult().getHash();
        Thread.sleep(8000);
        AppTransaction appTransaction = service.appGetTransactionByHash(
                hash).send();
        assertNotNull(appTransaction.getTransaction());
        assertNull(appTransaction.getError());
    }

    @Test
    public void testAppGetTransactionCount() throws Exception {
        Credentials credentials = Credentials.create(privateKey);
        String validAccount = credentials.getAddress();
        DefaultBlockParameter param = DefaultBlockParameterName.PENDING;
        AppGetTransactionCount appGetTransactionCount = service.appGetTransactionCount(
                validAccount, param).send();
        assertTrue(appGetTransactionCount.getTransactionCount().longValue()>=0);
    }

    @Test
    public void testAppGetTransactionReceipt() throws Exception {
        AppSendTransaction appSendTx = appSendRawTransaction();
        String validTransactionHash = appSendTx.getSendTransactionResult().getHash();
        Thread.sleep(8000);
        AppGetTransactionReceipt appGetTransactionReceipt = service.appGetTransactionReceipt(
                validTransactionHash).send();
        TransactionReceipt transactionReceipt =
            appGetTransactionReceipt.getTransactionReceipt();
        assertNotNull(transactionReceipt.getContractAddress());
        assertNull(transactionReceipt.getErrorMessage());
    }

    @Test
    public void testAppGetCode() throws Exception {

        AppSendTransaction appSendTx = appSendRawTransaction();
        String validTransactionHash = appSendTx.getSendTransactionResult().getHash();
        Thread.sleep(8000);
        AppGetTransactionReceipt appGetTransactionReceipt = service.appGetTransactionReceipt(
            validTransactionHash).send();
        TransactionReceipt transactionReceipt =
            appGetTransactionReceipt.getTransactionReceipt();
        String validContractAddress = transactionReceipt.getContractAddress();
        AppGetCode appGetCode = service
                .appGetCode(validContractAddress, DefaultBlockParameterName.PENDING).send();
        assertThat(code,containsString(appGetCode.getCode().substring(2)));
    }

    @Test
    public void testAppCall() throws Exception {
        // init from address
        String fromAddress = Credentials.create(adminPrivateKey).getAddress();

        // init contract address
        AppSendTransaction appSendTx = appSendRawTransaction();
        String validTransactionHash = appSendTx.getSendTransactionResult().getHash();
        Thread.sleep(8000);
        AppGetTransactionReceipt appGetTransactionReceipt = service.appGetTransactionReceipt(
            validTransactionHash).send();
        TransactionReceipt transactionReceipt =
            appGetTransactionReceipt.getTransactionReceipt();
        String contractAddress = transactionReceipt.getContractAddress();

        // init funcCallData
        Function getBalanceFunc = new Function(
            "getBalance",
            Arrays.asList(new Address(fromAddress)),
            Arrays.asList(new TypeReference<Uint>() {
            })
        );
        String funcCallData = FunctionEncoder.encode(getBalanceFunc);

        // send call and check
        AppCall appCall = service.appCall(
                new Call(fromAddress, contractAddress, funcCallData),
            DefaultBlockParameterName.PENDING).send();
        assertNotNull(appCall.getValue());
    }

    @BeforeClass
    public static void initSenderBalanceBeforeClass() throws Exception {
        //initSenderBalance();
    }
}
