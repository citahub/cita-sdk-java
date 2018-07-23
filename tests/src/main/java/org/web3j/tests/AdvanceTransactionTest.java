package org.web3j.tests;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.request.Call;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;


import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;

public class AdvanceTransactionTest {
    static final String configPath = "tests/src/main/resources/config.properties";

    static Properties props;
    static Web3j service;
    static String senderPrivateKey;
    static int version;
    static int chainId;

    static {
        props = Config.load(configPath);
        service = Web3j.build(new HttpService(props.getProperty(Config.TEST_NET_ADDR)));
        senderPrivateKey = props.getProperty(Config.SENDER_PRIVATE_KEY);
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        chainId = Integer.parseInt(props.getProperty(Config.CHAIN_ID));
    }

    private Random random;
    private long quota = 50000;
    private long validUntilBlock;
    private int sendCount;
    private int threadCount;
    private boolean isEd25519AndBlake2b;
    private String contractAddress;
    private String value = "0";

    public AdvanceTransactionTest(int sdCount, int thdCount, boolean isEd25519AndBlake2b){
        try {
            random = new Random(System.currentTimeMillis());
            this.validUntilBlock = testUtil.getValidUtilBlock(service, 100).longValue();
            this.sendCount = sdCount;
            this.threadCount = thdCount;
            this.isEd25519AndBlake2b = isEd25519AndBlake2b;
            System.out.println("Initial block height: "+ testUtil.getCurrentHeight(service));
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Deploy contract, return transaction hash
    private String deployContract(boolean isEd25519AndBlake2b) throws Exception {
        // contract.bin
        String contractCode = "606060405260008055341561001357600080fd5b60de806100216000396000f30060606040526004361060525763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416634f2be91f811460575780636d4ce63c146069578063d826f88f14608b575b600080fd5b3415606157600080fd5b6067609b565b005b3415607357600080fd5b607960a6565b60405190815260200160405180910390f35b3415609557600080fd5b606760ac565b600080546001019055565b60005490565b600080555600a165627a7a723058201b541b114db7898755d2a3124410cef8903e8a119a5a0a59a119cb00412986220029";
        BigInteger nonce = BigInteger.valueOf(Math.abs(this.random.nextLong()));
        Transaction tx = Transaction.createContractTransaction(nonce, this.quota, this.validUntilBlock, version, chainId, value, contractCode);

        String rawTx = tx.sign(senderPrivateKey, isEd25519AndBlake2b,false);

        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    // Contract function reset call
    private String funcResetCall(String contractAddress, boolean isEd25519AndBlake2b) throws Exception {
        Function resetFunc = new Function(
                "reset",
                Collections.emptyList(),
                Collections.emptyList()
        );

        String resetFuncData = FunctionEncoder.encode(resetFunc);

        BigInteger nonce = testUtil.getNonce();
        Transaction tx = Transaction.createFunctionCallTransaction(
                contractAddress,
                nonce,
                this.quota,
                this.validUntilBlock,
                version,
                chainId,
                value,
                resetFuncData);
        String rawTx = tx.sign(senderPrivateKey, isEd25519AndBlake2b,false);

        return service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

    // Contract function add call
    public void funcAddCall(String contractAddress, boolean isEd25519AndBlake2b) throws Exception {
        Function addFunc = new Function(
                "add",
                Collections.emptyList(),
                Collections.emptyList()
        );
        String addFuncData = FunctionEncoder.encode(addFunc);

        BigInteger nonce = BigInteger.valueOf(Math.abs(this.random.nextLong()));
        Transaction tx = Transaction.createFunctionCallTransaction(
                contractAddress,
                nonce,
                this.quota,
                this.validUntilBlock,
                version,
                chainId,
                value,
                addFuncData);
        String rawTx = tx.sign(senderPrivateKey, isEd25519AndBlake2b,false);

        service.ethSendRawTransaction(rawTx).send();
    }

    // eth_call
    private String call(String from, String contractAddress, String callData) throws Exception {
        Call call = new Call(from, contractAddress, callData);
        return service.ethCall(call, DefaultBlockParameter.valueOf("latest")).send().getValue();
    }

    // Get transaction receipt
    private TransactionReceipt getTransactionReceipt(String txHash) throws Exception {
        return service.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
    }

    public void runContract() throws Exception{
        boolean isEd25519AndBlake2b = this.isEd25519AndBlake2b;
        long dealWithCount = 0;
        String ethCallResult;
        String from = "0x0dbd369a741319fa5107733e2c9db9929093e3c7";
        long endBlock = 0;
        long startBlock = testUtil.getCurrentHeight(service).longValue();
        long oldBlock = startBlock;
        long currentBlock = startBlock;

        // deploy contract
        String deployContractTxHash = deployContract(isEd25519AndBlake2b);
        System.out.println("wait to deploy contract , txHash: " + deployContractTxHash);
        // waiting for new block
        while (true) {
            endBlock = testUtil.getCurrentHeight(service).longValue();
            if (endBlock > startBlock+3){
                break;
            }else{
                Thread.sleep(2000);
            }
        }
        // get contract address from receipt
        TransactionReceipt txReceipt = getTransactionReceipt(deployContractTxHash);
        this.contractAddress = txReceipt.getContractAddress();

        String resetTxHash = funcResetCall(this.contractAddress, isEd25519AndBlake2b);

        //Here are 2 verifications:
        //1. Make sure TransactionReceipt is not null before fetch values from the receipt in case for null pointer exception.
        //2. Wait for at most 3 blocks for reset transaction written into block in case for infinite loop.
        startBlock = testUtil.getCurrentHeight(service).longValue();
        while(true){
            Optional<TransactionReceipt> receipt = service.ethGetTransactionReceipt(resetTxHash).send().getTransactionReceipt();
            if(receipt.isPresent()){
                TransactionReceipt resetTxReceipt = receipt.get();
                if(resetTxReceipt.getErrorMessage() == null){
                    System.out.println("Count is reset successfully.");
                    break;
                }else{
                    System.out.println("Failed to reset count.");
                    System.exit(1);
                }
            }else{
                currentBlock = testUtil.getCurrentHeight(service).longValue();
                if(currentBlock - startBlock > 3){
                    System.out.println("Failed to get receipt from reset: timeout.");
                    System.exit(1);
                    break;
                }
                System.out.println("Waiting to reset count....");
                Thread.sleep(2000);
            }
        }


        System.out.println("Contract address: " + this.contractAddress + ", start call add...");
        long startTime = System.currentTimeMillis();
        //start thread
        Thread[] threads = new Thread[this.threadCount];
        int count_per_thread = this.sendCount;
        for (int i = 0; i < this.threadCount; i++) {
            Thread t = new Thread(
                () -> {
                    try {
                        int j = 0;
                        while(j < count_per_thread){
                            funcAddCall(contractAddress, isEd25519AndBlake2b);
                            j++;
                        }
                    }catch(Exception e) {
                        System.out.println("Failed to call contract function.");
                        e.printStackTrace();
                        System.exit(1);
                    }
                });

            t.start();
            threads[i] = t;
        }

        for (int k = 0; k < this.threadCount; k++) {
            threads[k].join();
        }
        System.out.println("send tx use " + (System.currentTimeMillis() - startTime) + " ms");

        //get result
        while(true) {
            Thread.sleep(2000);
            currentBlock = testUtil.getCurrentHeight(service).longValue();
            if (currentBlock < oldBlock) {
                System.out.println("Error : Height is decreasing");
            }
            oldBlock = currentBlock;
            if (currentBlock >= this.validUntilBlock) {
                break;
            }

            Function getCall = new Function(
                    "get",
                    Collections.emptyList(),
                    Arrays.asList(new TypeReference<Uint>(){})
            );

            String getCallData = FunctionEncoder.encode(getCall);
            ethCallResult = call(from, contractAddress, getCallData);
            String ethCallResultReadable = FunctionReturnDecoder.decode(ethCallResult, getCall.getOutputParameters()).get(0).toString();
            System.out.println("ethCallResult: " + ethCallResultReadable);
            if (ethCallResult == null || ethCallResult.length() < 3){
                continue;
            }
            dealWithCount = Long.valueOf(ethCallResult.substring(2),16);
            System.out.println("eth_call result: " + dealWithCount);
            if(dealWithCount == this.sendCount * this.threadCount){
                break;
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.println("complete input count : " + this.sendCount * this.threadCount + ", actual count: " + dealWithCount);
        System.out.println("performance(transactioncount/s):");
        System.out.println("start time: " + startTime + " ms\r\n"+ "end   time: " + endTime + " ms");
        double tps =  dealWithCount / ((endTime - startTime) /1000.0);
        DecimalFormat df = new DecimalFormat("0.00000000");
        String outStr = "performance - send tx tese case ...... success , result : " +  df.format(tps) + " TPS";
        System.out.println(outStr);
    }

    public static void main(String[] args) throws Exception {
        int sendcount = 20;
        int threadcount = 1;
        boolean isEd25519AndBlake2b = false;

        System.out.println("sendCount: "+ sendcount+ " threadCount: " + threadcount + " isEd25519AndBlake2b: " + isEd25519AndBlake2b);
        AdvanceTransactionTest advanceTxTest  = new AdvanceTransactionTest(sendcount, threadcount, isEd25519AndBlake2b);
        advanceTxTest.runContract();
        System.out.println("Performance - test case complete");
    }
}
