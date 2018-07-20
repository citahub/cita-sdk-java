package org.web3j.tests;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Call;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.TypeReference;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;

public class TokenTransactionTest {
    private static Properties props;
    private static String testNetIpAddr;
    private static int chainId;
    private static int version;
    private static String privateKey;
    private static String fromAddress;
    private static String toAddress;
    private static String binPath;

    private static final String configPath = "tests/src/main/resources/config.properties";

    private static Random random;
    private static BigInteger quota;
    private static String value;
    private static Web3j service;

    static {
        try{
            props = Config.load(configPath);
        }
        catch(Exception e){
            System.out.println("Failed to read properties from config file");
            e.printStackTrace();
        }

        chainId = Integer.parseInt(props.getProperty(Config.CHAIN_ID));
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        testNetIpAddr = props.getProperty(Config.TEST_NET_ADDR);
        privateKey = props.getProperty(Config.SENDER_PRIVATE_KEY);
        fromAddress = props.getProperty(Config.SENDER_ADDR);
        toAddress = props.getProperty(Config.TEST_ADDR_1);
        binPath= props.getProperty(Config.TOKEN_BIN);

        HttpService.setDebug(false);
        service = Web3j.build(new HttpService(testNetIpAddr));
        random = new Random(System.currentTimeMillis());
        quota = BigInteger.valueOf(1000000);
        value = "0";
    }

    static String loadContractCode(String binPath) throws Exception{
        return new String(Files.readAllBytes(Paths.get(binPath)));
    }

    static String deployContract(String contractCode) throws Exception{
        long currentHeight = service.ethBlockNumber().send().getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = 9999999;
        Transaction tx = Transaction.createContractTransaction(nonce, quota, validUntilBlock, version, chainId, value, contractCode);
        String rawTx = tx.sign(privateKey, false, false);
        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    static TransactionReceipt getTransactionReceipt(String txHash) throws Exception{
        return service.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
    }

    static String contractFunctionCall(String contractAddress, String funcCallData) throws Exception{
        long currentHeight = service.ethBlockNumber().send().getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = 1000000;

        Transaction tx = Transaction.createFunctionCallTransaction(contractAddress, nonce, quota, validUntilBlock, version, chainId, value, funcCallData);
        String rawTx = tx.sign(privateKey, false, false);

        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    static String transfer(String contractAddr, String toAddr, BigInteger value) throws Exception{
        Function transferFunc = new Function(
                "transfer",
                Arrays.asList(new Address(toAddr), new Uint256(value)),
                Collections.emptyList()
        );
        String funcCallData = FunctionEncoder.encode(transferFunc);
        return contractFunctionCall(contractAddr, funcCallData);
    }

    //eth_call
    static String call(String from, String contractAddress, String callData) throws Exception {
        Call call = new Call(from, contractAddress, callData);
        return service.ethCall(call, DefaultBlockParameter.valueOf("latest")).send().getValue();
    }

    static String getBalance(String fromAddr, String contractAddress) throws Exception {
        Function getBalanceFunc = new Function(
                "getBalance",
                Arrays.asList(new Address(fromAddr)),
                Arrays.asList(new TypeReference<Uint>(){})
        );
        String funcCallData = FunctionEncoder.encode(getBalanceFunc);
        String result = call(fromAddr, contractAddress, funcCallData);
        List<Type> resultTypes = FunctionReturnDecoder.decode(result, getBalanceFunc.getOutputParameters());
        return resultTypes.get(0).getValue().toString();
    }

    public static void main(String[] args) throws Exception {
        // deploy contract
        String contractCode = loadContractCode(binPath);
        System.out.println(contractCode);
        String deployContractTxHash = deployContract(contractCode);

        System.out.println("wait to deploy contract");
        Thread.sleep(10000);

        // get contract address from receipt
        TransactionReceipt txReceipt = getTransactionReceipt(deployContractTxHash);
        if(txReceipt.getErrorMessage() != null){
            System.out.println("There is something wrong in deployContractTxHash. Error: " + txReceipt.getErrorMessage());
            System.exit(1);
        }
        String contractAddress = txReceipt.getContractAddress();
        System.out.println("Contract deployed successfully. Contract address: " + contractAddress);

        // call contract function(eth_call)
        String balaneFrom = getBalance(fromAddress, contractAddress);
        String balanceTo = getBalance(toAddress, contractAddress);
        System.out.println(fromAddress + " has " + balaneFrom + " tokens.");
        System.out.println(toAddress + " has " + balanceTo + " tokens.");

        // call contract function
        String transferTxHash = transfer(contractAddress, toAddress, BigInteger.valueOf(1000));
        System.out.println("wait for transfer transaction.");
        Thread.sleep(10000);

        TransactionReceipt transferTxReceipt = getTransactionReceipt(transferTxHash);
        if(transferTxReceipt.getErrorMessage() != null){
            System.out.println("Failed to call transfer method in contract. Error: " + transferTxReceipt.getErrorMessage());
            System.exit(1);
        }
        System.out.println("call transfer method success and receipt is " + transferTxHash);

        balaneFrom = getBalance(fromAddress, contractAddress);
        balanceTo = getBalance(toAddress, contractAddress);
        System.out.println(fromAddress + " has " + balaneFrom + " tokens.");
        System.out.println(toAddress + " has " + balanceTo + " tokens.");

        System.out.println("Complete");
        System.exit(0);
    }
}
