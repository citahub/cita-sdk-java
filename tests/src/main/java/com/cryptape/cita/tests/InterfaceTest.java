package com.cryptape.cita.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import com.cryptape.cita.abi.FunctionEncoder;
import com.cryptape.cita.abi.TypeReference;
import com.cryptape.cita.abi.datatypes.Address;
import com.cryptape.cita.abi.datatypes.Function;
import com.cryptape.cita.abi.datatypes.Uint;
import com.cryptape.cita.crypto.Credentials;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.DefaultBlockParameter;
import com.cryptape.cita.protocol.core.DefaultBlockParameterName;
import com.cryptape.cita.protocol.core.methods.request.Call;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cryptape.cita.protocol.core.methods.response.AppBlock;
import com.cryptape.cita.protocol.core.methods.response.AppBlockNumber;
import com.cryptape.cita.protocol.core.methods.response.AppCall;
import com.cryptape.cita.protocol.core.methods.response.AppGetBalance;
import com.cryptape.cita.protocol.core.methods.response.AppGetCode;
import com.cryptape.cita.protocol.core.methods.response.AppGetTransactionCount;
import com.cryptape.cita.protocol.core.methods.response.AppGetTransactionReceipt;
import com.cryptape.cita.protocol.core.methods.response.AppMetaData;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.cryptape.cita.protocol.core.methods.response.AppTransaction;
import com.cryptape.cita.protocol.core.methods.response.NetPeerCount;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.google.gson.Gson;

public class InterfaceTest {

    private static int version;
    private static BigInteger chainId;
    private static CITAj service;
    private static String value;
    private static String privateKey;
    private static long quotaToDeploy;
    private static Transaction.CryptoTx cryptoTx;


    static {
        Config conf = new Config();
        conf.buildService(true);
        service = conf.service;
        privateKey = conf.primaryPrivKey;
        quotaToDeploy = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
        version = TestUtil.getVersion(service);
        chainId = TestUtil.getChainId(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    public static void main(String[] args) throws Exception {

        testGetBlockByNumber(BigInteger.valueOf(47));

        testGetBalance();

        testMetaData();

        testNetPeerCount();

        BigInteger validBlockNumber = testBlockNumber();

        System.out.println(validBlockNumber.toString(10));

        String blockByNumberHash = testAppGetBlockByNumber(
                validBlockNumber, true);

        testAppGetBlockByHash(blockByNumberHash, true);

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

        String nonce = TestUtil.getNonce();
        BigInteger validUtil = TestUtil.getValidUtilBlock(service);

        Transaction rtx = Transaction.createContractTransaction(
                nonce, quotaToDeploy, validUtil.longValue(),
                version, chainId, value, code);
        String signedTransaction = rtx.sign(privateKey, cryptoTx, false);
        String transactionHash = testAppSendRawTransaction(signedTransaction);

        System.out.println("waiting for tx into chain ...");
        Thread.sleep(8000);

        testAppGetTransactionByHash(transactionHash);

        Credentials credentials = Credentials.create(privateKey);
        String validAccount = credentials.getAddress();
        testAppGetTransactionCount(validAccount, DefaultBlockParameterName.PENDING);

        String validContractAddress = "";
        String contractAddr = testAppGetTransactionReceipt(transactionHash);
        if (contractAddr != null) {
            validContractAddress = contractAddr;
        } else {
            System.out.println("Failed to get address from tx receipt");
            System.exit(1);
        }

        testAppGetCode(validContractAddress, DefaultBlockParameterName.PENDING);

        String fromAddress = Credentials.create(privateKey).getAddress();
        Function getBalanceFunc = new Function(
                "getBalance",
                Arrays.asList(new Address(fromAddress)),
                Arrays.asList(new TypeReference<Uint>() {
                })
        );
        String funcCallData = FunctionEncoder.encode(getBalanceFunc);

        testAppCall(fromAddress, validContractAddress, funcCallData, DefaultBlockParameterName.PENDING);
    }


    private static void testGetBlockByNumber(BigInteger number) throws IOException {
        AppBlock.Block block = service.appGetBlockByNumber(DefaultBlockParameter.valueOf(number), false).send().getBlock();
        System.out.println("block " + number.toString() + " is: " + new Gson().toJson(block));
    }

    private static void testGetBalance() throws Exception {
        Credentials c = Credentials.create(privateKey);
        String addr = c.getAddress();
        AppGetBalance appGetbalance = service.appGetBalance(
                addr, DefaultBlockParameterName.PENDING).send();
        if (appGetbalance == null) {
            System.out.println("the result is null");
        } else {
            BigInteger balance = appGetbalance.getBalance();
            System.out.println("Balance for address " + addr + "is " + balance);
        }
    }

    private static void testMetaData() throws Exception {
        AppMetaData appMetaData = service.appMetaData(DefaultBlockParameterName.PENDING).send();
        System.out.println("AppMetaData: " + new Gson().toJson(appMetaData));
    }

    private static void testNetPeerCount() throws Exception {
        NetPeerCount netPeerCount = service.netPeerCount().send();
        System.out.println("net_peerCount:" + netPeerCount.getQuantity());
    }

    private static BigInteger testBlockNumber() throws Exception {

        AppBlockNumber appBlockNumber = service.appBlockNumber().send();

        BigInteger validBlockNumber = BigInteger.valueOf(Long.MAX_VALUE);

        if (appBlockNumber.isEmpty()) {
            System.out.println("the result is null");
        } else {
            validBlockNumber = appBlockNumber.getBlockNumber();
            System.out.println("blockNumber:" + validBlockNumber);
        }
        return validBlockNumber;
    }

    private static String testAppGetBlockByNumber(
            BigInteger validBlockNumber, boolean isfullTranobj)
            throws Exception {
        AppBlock appBlock = service.appGetBlockByNumber(
                DefaultBlockParameter.valueOf(validBlockNumber), isfullTranobj).send();

        if (appBlock.isEmpty()) {
            System.out.println("the result is null");
            return null;
        } else {
            AppBlock.Block block = appBlock.getBlock();
            System.out.println("Block: " + new Gson().toJson(block));
            return block.getHash();
        }
    }

    private static void testAppGetBlockByHash(
            String validBlockHash, boolean isfullTran)
            throws Exception {
        AppBlock appBlock = service
                .appGetBlockByHash(validBlockHash, isfullTran).send();

        if (appBlock.isEmpty()) {
            System.out.println("the result is null");
        } else {
            AppBlock.Block block = appBlock.getBlock();
            System.out.println("Block: " + new Gson().toJson(block));
        }
    }


    private static String testAppSendRawTransaction(
            String rawData) throws Exception {
        AppSendTransaction appSendTx = service
                .appSendRawTransaction(rawData).send();

        if (appSendTx.isEmpty()) {
            System.out.println("the result is null");
            return null;
        } else {
            String hash = appSendTx.getSendTransactionResult().getHash();
            System.out.println("hash(Transaction):" + hash);
            System.out.println("status:"
                    + appSendTx.getSendTransactionResult().getStatus());
            return hash;
        }
    }


    private static void testAppGetTransactionByHash(
            String validTransactionHash) throws Exception {
        AppTransaction appTransaction = service.appGetTransactionByHash(
                validTransactionHash).send();

        if (appTransaction.getTransaction() == null) {
            System.out.println("the result is null");
        } else {
            com.cryptape.cita.protocol.core.methods.response.Transaction transaction
                    = appTransaction.getTransaction();
            System.out.println("Transaction: " + new Gson().toJson(transaction));
        }
    }


    private static void testAppGetTransactionCount(
            String validAccount, DefaultBlockParameter param) throws Exception {
        AppGetTransactionCount appGetTransactionCount = service.appGetTransactionCount(
                validAccount, param).send();

        if (appGetTransactionCount.isEmpty()) {
            System.out.println("the result is null");
        } else {
            System.out.println("TransactionCount:"
                    + appGetTransactionCount.getTransactionCount());
        }
    }

    private static String testAppGetTransactionReceipt(
            String validTransactionHash) throws Exception {
        AppGetTransactionReceipt appGetTransactionReceipt = service.appGetTransactionReceipt(
                validTransactionHash).send();

        if (appGetTransactionReceipt.getTransactionReceipt() == null) {
            System.out.println("the result is null");
            return null;
        } else {
            //is option_value is null return NoSuchElementException, else return option_value
            TransactionReceipt transactionReceipt =
                    appGetTransactionReceipt.getTransactionReceipt();
            printTransactionReceiptInfo(transactionReceipt);
            if (transactionReceipt.getErrorMessage() != null) {
                System.out.println("Transaction failed.");
                System.out.println("Error Message: " + transactionReceipt.getErrorMessage());
                System.exit(1);
            }
            return transactionReceipt.getContractAddress();
        }

    }

    private static void printTransactionReceiptInfo(
            TransactionReceipt transactionReceipt) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(transactionReceipt);
            System.out.println(result);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to process object to json. Exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testAppGetCode(
            String validContractAddress, DefaultBlockParameter param)
            throws Exception {
        AppGetCode appGetCode = service
                .appGetCode(validContractAddress, param).send();

        if (appGetCode.isEmpty()) {
            System.out.println("the result is null");
        } else {
            System.out.println("contract code:" + appGetCode.getCode());
        }
    }


    private static void testAppCall(
            String fromAddress, String contractAddress, String encodedFunction,
            DefaultBlockParameter param) throws Exception {
        AppCall appCall = service.appCall(
                new Call(fromAddress, contractAddress, encodedFunction),
                param).send();

        System.out.println("call result:" + appCall.getValue());
    }

}
