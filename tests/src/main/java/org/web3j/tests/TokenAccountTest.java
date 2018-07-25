package org.web3j.tests;

import java.io.File;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Random;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.account.Account;
import org.web3j.protocol.account.CompiledContract;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;


public class TokenAccountTest {

    private static Properties props;
    private static String testNetIpAddr;
    private static int chainId;
    private static int version;
    private static String privateKey;
    private static String fromAddress;
    private static String toAddress;
    private static String solPath;

    private static final String configPath = "tests/src/main/resources/config.properties";

    private static Random random;
    private static BigInteger quota;
    private static String value;
    private static Web3j service;

    private Account account;
    private CompiledContract tokenContract;
    private String contractAddress;

    static {
        try {
            props = Config.load(configPath);
        } catch (Exception e) {
            System.out.println("Failed to read properties from config file");
            e.printStackTrace();
        }

        chainId = Integer.parseInt(props.getProperty(Config.CHAIN_ID));
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        testNetIpAddr = props.getProperty(Config.TEST_NET_ADDR);
        privateKey = props.getProperty(Config.SENDER_PRIVATE_KEY);
        fromAddress = props.getProperty(Config.SENDER_ADDR);
        toAddress = props.getProperty(Config.TEST_ADDR_1);
        solPath = props.getProperty(Config.TOKEN_SOLIDITY);

        HttpService.setDebug(false);
        service = Web3j.build(new HttpService(testNetIpAddr));
        random = new Random(System.currentTimeMillis());
        quota = BigInteger.valueOf(1000000);
        value = "0";
    }

    private static BigInteger randomNonce() {
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }

    private static TransactionReceipt waitToGetReceipt(
            String hash) throws Exception {
        Thread.sleep(10_000);
        return service.ethGetTransactionReceipt(hash)
                .send().getTransactionReceipt().get();
    }

    public TokenAccountTest() throws Exception {
        account = new Account(privateKey, service);
        tokenContract = new CompiledContract(new File(solPath));

    }

    public String deployContract(String path) throws Exception {
        EthSendTransaction ethSendTransaction = account.deploy(
                new File(path), randomNonce(), quota, version, chainId, value);
        TransactionReceipt receipt = waitToGetReceipt(
                ethSendTransaction.getSendTransactionResult().getHash());
        if (receipt.getErrorMessage() != null) {
            System.out.println("deploy contract failed because of "
                    + receipt.getErrorMessage());
            System.exit(1);
        }
        contractAddress = receipt.getContractAddress();
        System.out.println("deploy contract success and contract address is "
                + receipt.getContractAddress());
        return contractAddress;
    }

    public void transfer(String toAddress, BigInteger amount)
            throws Exception {
        AbiDefinition transfer = tokenContract.getFunctionAbi("transfer", 2);
        EthSendTransaction ethSendTransaction = (EthSendTransaction)
                account.callContract(
                        contractAddress, transfer, randomNonce(),
                        quota, version, chainId, value, toAddress, amount);
        TransactionReceipt receipt = waitToGetReceipt(
                ethSendTransaction.getSendTransactionResult().getHash());
        if (receipt.getErrorMessage() != null) {
            System.out.println("call transfer method failed because of "
                    + receipt.getErrorMessage());
            System.exit(1);
        }
        System.out.println("call transfer method success and receipt is "
                + receipt.getTransactionHash());
    }

    public void getBalance(String address) throws Exception {
        AbiDefinition getBalance = tokenContract.getFunctionAbi("getBalance", 1);
        Object object = account.callContract(
                contractAddress, getBalance, randomNonce(),
                quota, version, chainId, value, address);
        System.out.println(address + " has "
                + object.toString() + " tokens");
    }

    public void transferRemote(String toAddress, BigInteger amount) throws Exception {
        EthSendTransaction ethSendTransaction = (EthSendTransaction) account.callContract(
                contractAddress, "transfer", randomNonce(),
                quota, version, chainId, value, toAddress, amount);
        TransactionReceipt receipt = waitToGetReceipt(
                ethSendTransaction.getSendTransactionResult().getHash());
        if (receipt.getErrorMessage() != null) {
            System.out.println("call transfer method failed because of "
                    + receipt.getErrorMessage());
            System.exit(1);
        }
        System.out.println("call transfer method success and receipt is "
                + receipt.getTransactionHash());
    }

    public void getBalanceRemote(String address) throws Exception {
        Object object = account.callContract(
                contractAddress, "getBalance", randomNonce(),
                quota, version, chainId, value, address);
        System.out.println(address + " has " + object.toString() + " tokens");
    }

    public void storeAbiToBlockchain() throws Exception {
        EthSendTransaction ethSendTransaction =
                (EthSendTransaction) account.uploadAbi(
                        contractAddress, tokenContract.getAbi(),
                        randomNonce(), quota, version, chainId, value);
        TransactionReceipt receipt = waitToGetReceipt(
                ethSendTransaction.getSendTransactionResult().getHash());
        if (receipt.getErrorMessage() != null) {
            System.out.println("call upload abi method failed because of "
                    + receipt.getErrorMessage());
            System.exit(1);
        }
        System.out.println("call upload abi method success and receipt is "
                + receipt.getTransactionHash());
    }

    public void getAbi() throws Exception {
        String abi = account.getAbi(contractAddress);
        System.out.println("abi: " + abi);
    }

    public static void main(String[] args) throws Exception {

        // deploy contract with smart contract solidity file
        // and call method "transfer" with generated Abi
        String contractAddr = deployContractAndCallMethodFromSolidity();

        // get abi from deployed smart contract
        // and call method "transfer"
        callContractMethodFromRemoteAbi(contractAddr);

        System.exit(0);
    }


    //CHECKSTYLE:OFF
    private static String deployContractAndCallMethodFromSolidity()
            throws Exception {
        TokenAccountTest tokenAccountTest = new TokenAccountTest();
        String contractAddr = tokenAccountTest.deployContract(solPath);
        tokenAccountTest.getBalance(fromAddress);
        tokenAccountTest.getBalance(toAddress);
        tokenAccountTest.transfer(toAddress, BigInteger.valueOf(1200));
        tokenAccountTest.getBalance(fromAddress);
        tokenAccountTest.getBalance(toAddress);
        tokenAccountTest.storeAbiToBlockchain();
        tokenAccountTest.getAbi();
        return contractAddr;
    }
    //CHECKSTYLE:ON

    private static void callContractMethodFromRemoteAbi(String contractAddress)
            throws Exception {
        TokenAccountTest tokenAccountTest = new TokenAccountTest();
        tokenAccountTest.contractAddress = contractAddress;
        tokenAccountTest.transferRemote(toAddress, BigInteger.valueOf(500));
        tokenAccountTest.getBalanceRemote(fromAddress);
        tokenAccountTest.getBalanceRemote(toAddress);
    }
}
