package org.web3j.examples;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Call;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

public class DeployContractDemo {
    private final static int VERSION = 0;

    private static Properties props;
    private static String testNetIpAddr;
    private static Web3j service;
    private static Random random;
    private static int chainId;
    private static String privateKey;
    private static String from;
    private static String value;

    static void loadConfig(){
        try{
            props = new Properties();
            props.load(new FileInputStream("examples/src/main/resources/config.properties"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static {
        loadConfig();
        testNetIpAddr = props.getProperty("TestNetIpAddr");
        chainId = Integer.parseInt(props.getProperty("ChainId"));
        HttpService.setDebug(true);
        service = Web3j.build(new HttpService(testNetIpAddr));
        random = new Random(System.currentTimeMillis());
        privateKey = "0x2c5c6c187d42e58a4c212a4aab0a3cfa4030256ed82bb3e05706706ab5be9641";
        from = "0x0438bfcabdda99c00acf0039e6c1f3f2d78edde5";
        value = "0";
    }

    static String deployContract() throws Exception {
        String contractCode = "6060604052341561000f57600080fd5b60408051908101604052600b81527f68656c6c6f20776f726c640000000000000000000000000000000000000000006020820152600090805161005692916020019061005c565b506100f7565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009d57805160ff19168380011785556100ca565b828001600101855582156100ca579182015b828111156100ca5782518255916020019190600101906100af565b506100d69291506100da565b5090565b6100f491905b808211156100d657600081556001016100e0565b90565b6102c3806101066000396000f30060606040526004361061004b5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166320965255811461005057806393a09352146100da575b600080fd5b341561005b57600080fd5b61006361012d565b60405160208082528190810183818151815260200191508051906020019080838360005b8381101561009f578082015183820152602001610087565b50505050905090810190601f1680156100cc5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156100e557600080fd5b61012b60046024813581810190830135806020601f820181900481020160405190810160405281815292919060208401838380828437509496506101d695505050505050565b005b6101356101ed565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156101cb5780601f106101a0576101008083540402835291602001916101cb565b820191906000526020600020905b8154815290600101906020018083116101ae57829003601f168201915b505050505090505b90565b60008180516101e99291602001906101ff565b5050565b60206040519081016040526000815290565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061024057805160ff191683800117855561026d565b8280016001018555821561026d579182015b8281111561026d578251825591602001919060010190610252565b5061027992915061027d565b5090565b6101d391905b8082111561027957600081556001016102835600a165627a7a72305820086c5bf2a74080392d819fbd84ddb063f16da81bfcfa3e0d9d095fc2b99588220029";
        long currentHeight = service.ethBlockNumber().send().getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = 9999999;
        Transaction tx = Transaction.createContractTransaction(nonce, quota, validUntilBlock, VERSION, chainId, value, contractCode);
        String rawTx = tx.sign(privateKey, false, false);
        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    static String sendFuncCallTx(String contractAddress, String functionCallData) throws Exception {
        long currentHeight = service.ethBlockNumber().send().getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = 1000000;

        Transaction tx = Transaction.createFunctionCallTransaction(contractAddress, nonce, quota, validUntilBlock, VERSION, chainId, value, functionCallData);
        String rawTx = tx.sign(privateKey, false, false);

        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    static String call(String from, String contractAddress, String callData) throws Exception {
        Call call = new Call(from, contractAddress, callData);
        return service.ethCall(call, DefaultBlockParameter.valueOf("latest")).send().getValue();
    }

    static TransactionReceipt getTransactionReceipt(String txHash) throws Exception{
        return service.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
    }

    public static void main(String[] args) throws Exception {
        //deploy the contract.
        String contractHash = deployContract();
        System.out.println("Waiting for contract deployment...");
        Thread.sleep(10000);

        //get receipt for contract deployment transaction.
        TransactionReceipt txReceipt = getTransactionReceipt(contractHash);
        String error = txReceipt.getErrorMessage();

        if(error != null){
            System.out.println("There is something wrong in contract deployment. Error: " + error);
            return;
        }

        //get deployed contract address
        String contractAddress = txReceipt.getContractAddress();
        System.out.println("Contract deployed successfully. Contract address: " + contractAddress);

        //construct setValue function call data
        String date = LocalDateTime.now().toString();
        Function setValueFunc = new Function(
                "setValue",
                Arrays.asList(new Utf8String("Modified at " + date)),
                Collections.emptyList()
        );
        String setValueFuncCallData = FunctionEncoder.encode(setValueFunc);

        //send setValue function call transaction
        String setValueReceiptHash = sendFuncCallTx(contractAddress, setValueFuncCallData);
        System.out.println("Waiting for function call....");
        Thread.sleep(10000);

        //get receipt for setValue function call
        TransactionReceipt setValueTxReceipt = SendTransactionDemo.getTransactionReceipt(setValueReceiptHash);
        String setValueError = setValueTxReceipt.getErrorMessage();
        if(setValueError != null){
            System.out.println("There is something in contract method call. Error: " + setValueError);
            return;
        }

        //construct function call transaction for getValue.
        Function getValueFunc = new Function(
                "getValue",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Utf8String>(){})
        );
        String getValueCallData = FunctionEncoder.encode(getValueFunc);

        //send eth_call for function getValue
        String result = call(from, contractAddress, getValueCallData);

        //decode the result for getValue
        List<Type> resultTypes = FunctionReturnDecoder.decode(result, getValueFunc.getOutputParameters());
        System.out.println("Value of the contract is " + resultTypes.get(0).getValue());
    }
}
