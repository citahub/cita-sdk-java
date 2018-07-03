package org.web3j.examples;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Call;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Random;
import com.fasterxml.jackson.*;

public class SendTransactionDemo {
    private final static int VERSION = 0;

    private static Web3j service;
    private static Random random;
    private static int chainId;
    private static String value;
    private static Properties props;
    private static String testNetIpAddr;

    static{
        loadConfig();
        testNetIpAddr = props.getProperty("TestNetIpAddr");
        chainId = Integer.parseInt(props.getProperty("ChainId"));
        HttpService.setDebug(true);
        service = Web3j.build(new HttpService(testNetIpAddr));
        random = new Random(System.currentTimeMillis());
        value = "0";
    }

    static void loadConfig(){
        try{
            props = new Properties();
            props.load(new FileInputStream("examples/src/main/resources/config.properties"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static long currentBlockNumber() throws Exception {
        return service.ethBlockNumber().send().getBlockNumber().longValue();
    }

    // Deploy contract, return transaction hash
    static String deployContract() throws Exception {
        // contract.bin
        String contractCode = "606060405234156100105760006000fd5b610015565b60e0806100236000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806360fe47b114604b5780636d4ce63c14606c576045565b60006000fd5b341560565760006000fd5b606a60048080359060200190919050506093565b005b341560775760006000fd5b607d60a3565b6040518082815260200191505060405180910390f35b8060006000508190909055505b50565b6000600060005054905060b1565b905600a165627a7a72305820942223976c6dd48a3aa1d4749f45ad270915cfacd9c0bf3583c018d4c86f9da20029";
        String privateKey = "0x2ee9f6e662c59e65b676f24c4975f0e7ca8f9ac2b61664f659d719b9e47b4005";
        long currentHeight = currentBlockNumber();
        // validUntilBlock should between currentHeight and currentHeight+100
        long validUntilBlock = currentHeight + 80;
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = 9999999;

        Transaction tx = Transaction.createContractTransaction(nonce, quota, validUntilBlock, VERSION, chainId, value, contractCode);
        String rawTx = tx.sign(privateKey, false, false);

        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    // Contract function call
    static String contractFunctionCall(String contractAddress) throws Exception {
        String functionCallData = "60fe47b10000000000000000000000000000000000000000000000000000000000000001";
        String privateKey = "0x2ee9f6e662c59e65b676f24c4975f0e7ca8f9ac2b61664f659d719b9e47b4005";
        long currentHeight = currentBlockNumber();
        // validUntilBlock should between currentHeight and currentHeight+100
        long validUntilBlock = currentHeight + 80;
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = 1000000;


        Transaction tx = Transaction.createFunctionCallTransaction(contractAddress, nonce, quota, validUntilBlock, VERSION, chainId, value, functionCallData);
        String rawTx = tx.sign(privateKey, false, false);

        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    // eth_call
    static String call(String from, String contractAddress, String callData) throws Exception {
        Call call = new Call(from, contractAddress, callData);
        return service.ethCall(call, DefaultBlockParameter.valueOf("latest")).send().getValue();
    }

    // Get transaction receipt
    static TransactionReceipt getTransactionReceipt(String txHash) throws Exception {
        return service.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
    }

    public static void main(String[] args) throws Exception {
        // deploy contract
        String deployContractTxHash = SendTransactionDemo.deployContract();
        System.out.println("wait to deploy contract");
        Thread.sleep(10000);

        // get contract address from receipt
        TransactionReceipt txReceipt = SendTransactionDemo.getTransactionReceipt(deployContractTxHash);
        String contractAddress = txReceipt.getContractAddress();

        System.out.println("contractAddress ... " + contractAddress);
        // call contract function
        SendTransactionDemo.contractFunctionCall(contractAddress);
        System.out.println("Contract address: " + contractAddress + ", wait to call contract function");
        Thread.sleep(10000);

        String from = "0x798b7c48775eb73359f4d10bad6acfb0e471d85d";
        String ethCallResult = call(from, contractAddress, "0x6d4ce63c");
        System.out.println("eth_call result: " + ethCallResult);
        System.out.println("complete");
    }
}