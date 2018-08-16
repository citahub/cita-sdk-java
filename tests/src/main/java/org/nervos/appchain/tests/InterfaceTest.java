package org.nervos.appchain.tests;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.Uint;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjFactory;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.DefaultBlockParameterNumber;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.protocol.core.methods.response.AppBlockNumber;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.core.methods.response.AppGetBalance;
import org.nervos.appchain.protocol.core.methods.response.AppGetCode;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppTransaction;
import org.nervos.appchain.protocol.core.methods.response.NetPeerCount;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.http.HttpService;

public class InterfaceTest {

    private static String testNetAddr;
    private static int version;
    private static int chainId;
    private static Nervosj service;
    private static String value;
    private static String privateKey;
    private static Properties props;
    private static long quota;
    private static final String configPath = "tests/src/main/resources/config.properties";
    private static String validTransactionHash;

    static {
        try {
            props = Config.load(configPath);
        } catch (Exception e) {
            System.out.println("Failed to get props from config file");
            System.exit(1);
        }
        testNetAddr = props.getProperty(Config.TEST_NET_ADDR);
        HttpService.setDebug(false);
        service = NervosjFactory.build(new HttpService(testNetAddr));
        privateKey = props.getProperty(Config.SENDER_PRIVATE_KEY);
        quota = Long.parseLong(props.getProperty(Config.DEFAULT_QUOTA));
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        chainId = Integer.parseInt(props.getProperty(Config.CHAIN_ID));
        value = "0";
    }

    public static void main(String[] args) throws Exception {


        System.out.println("======================================");
        System.out.println("***  0.  getbalance             ***");
        testGetBalance();

        System.out.println("======================================");
        System.out.println("***  1.  getMetaData            ***");
        testMetaData();

        System.out.println("======================================");
        System.out.println("***  2.  net_peerCount          ***");
        testNetPeerCount();

        System.out.println("======================================");
        System.out.println("***  3.  blockNumber          ***");
        BigInteger validBlockNumber = testBlockNumber();

        System.out.println(validBlockNumber);
        System.out.println(validBlockNumber.toString(16));
        System.out.println(DefaultBlockParameterNumber.valueOf(validBlockNumber).getValue());

        System.out.println("======================================");
        System.out.println("***  4.  getBlockByNumber     ***");
        boolean returnFullTransactions = true;
        String validBlockHash = testEthGetBlockByNumber(
                validBlockNumber, returnFullTransactions);

        System.out.println("======================================");
        System.out.println("***  5.  getBlockByHash       ***");
        testEthGetBlockByHash(validBlockHash, returnFullTransactions);

        //because unsigned transaction is not supported in cita, there is no method sendTransaction.
        System.out.println("======================================");
        System.out.println("***  6.  sendRawTransaction      ***");
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
        BigInteger nonce = TestUtil.getNonce();
        BigInteger validUtil = TestUtil.getValidUtilBlock(service);

        Transaction rtx = Transaction.createContractTransaction(
                nonce, quota, validUtil.longValue(),
                version, chainId, value, code);
        String signedTx = rtx.sign(privateKey, false, false);

        validTransactionHash = testEthSendRawTransaction(signedTx);
        System.out.println("waiting for tx into chain ...");
        Thread.sleep(8000);

        System.out.println("======================================");
        System.out.println("***  7.  getTransactionByHash  ***");
        testEthGetTransactionByHash(validTransactionHash);

        System.out.println("======================================");
        System.out.println("***  8.  getTransactionCount   ***");
        Credentials credentials = Credentials.create(privateKey);
        String validAccount = credentials.getAddress();
        DefaultBlockParameter param = DefaultBlockParameterName.valueOf("latest");
        testEthGetTransactionCount(validAccount, param);

        System.out.println("======================================");
        System.out.println("***  9.  getTransactionReceipt ***");
        String validContractAddress = testEthGetTransactionReceipt(validTransactionHash);

        System.out.println("======================================");
        System.out.println("***  10.  getCode               ***");
        DefaultBlockParameter parameter = DefaultBlockParameterName.valueOf("latest");
        testEthGetCode(validContractAddress, parameter);

        System.out.println("======================================");
        System.out.println("***  11. eth_call                  ***");
        String fromAddr = Credentials.create(privateKey).getAddress();
        Function getBalanceFunc = new Function(
                "getBalance",
                Arrays.<Type>asList(new Address(fromAddr)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {
                })
        );
        String funcCallData = FunctionEncoder.encode(getBalanceFunc);

        testEthCall(fromAddr, validContractAddress, funcCallData, DefaultBlockParameterName.LATEST);
    }


    //0.  getBalance
    static void testGetBalance() throws Exception {
        Credentials c = Credentials.create(privateKey);
        String addr = c.getAddress();
        AppGetBalance appGetbalance = service.appGetBalance(
                addr, DefaultBlockParameterName.LATEST).send();
        if (appGetbalance == null) {
            System.out.println("the result is null");
        } else {
            BigInteger balance = appGetbalance.getBalance();
            System.out.println("Balance for addr " + addr + "is " + balance);
        }
    }

    //1.  getMetaData
    static void testMetaData() throws Exception {
        AppMetaData appMetaData = service.appMetaData(DefaultBlockParameterName.LATEST).send();
        if (appMetaData == null) {
            System.out.println("the result is null");
        } else {
            System.out.println("BasicToken: "
                    + appMetaData.getEthMetaDataResult().basicToken);
            System.out.println("ChainName: "
                    + appMetaData.getEthMetaDataResult().chainName);
            System.out.println("Genesis TS: "
                    + appMetaData.getEthMetaDataResult().genesisTimestamp);
            System.out.println("Operator: "
                    + appMetaData.getEthMetaDataResult().operator);
            System.out.println("Website: "
                    + appMetaData.getEthMetaDataResult().website);
            System.out.println("Block Interval: "
                    + appMetaData.getEthMetaDataResult().blockInterval);
            System.out.println("Chain Id: "
                    + appMetaData.getEthMetaDataResult().chainId);
            System.out.println("Validators: ");

            for (Address validator : appMetaData.getEthMetaDataResult().validators) {
                System.out.println("Address: " + validator.toString());
            }
        }
    }

    //1.  net_peerCount
    static void testNetPeerCount() throws Exception {
        NetPeerCount netPeerCount = service.netPeerCount().send();
        System.out.println("net_peerCount:" + netPeerCount.getQuantity());
    }

    //2.  blockNumber
    static BigInteger testBlockNumber() throws Exception {

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

    //3.  getBlockByNumber
    public static String testEthGetBlockByNumber(
            BigInteger validBlockNumber, boolean isfullTranobj)
            throws Exception {
        AppBlock appBlock = service.appGetBlockByNumber(
                DefaultBlockParameterNumber.valueOf(validBlockNumber), isfullTranobj).send();

        if (appBlock.isEmpty()) {
            System.out.println("the result is null");
            return null;
        } else {
            AppBlock.Block block = appBlock.getBlock();
            printBlock(block);
            return block.getHash();
        }
    }

    //4.  cita_getBlockByHash
    public static String testEthGetBlockByHash(
            String validBlockHash, boolean isfullTran)
            throws Exception {
        AppBlock appBlock = service
                .appGetBlockByHash(validBlockHash, isfullTran).send();

        if (appBlock.isEmpty()) {
            System.out.println("the result is null");
            return null;
        } else {
            AppBlock.Block block = appBlock.getBlock();
            printBlock(block);
            return block.getHash();
        }
    }


    //5.  sendRawTransaction
    public static String testEthSendRawTransaction(
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


    //6.  getTransactionByHash
    public static void testEthGetTransactionByHash(
            String validTransactionHash) throws Exception {
        AppTransaction appTransaction = service.appGetTransactionByHash(
                validTransactionHash).send();

        if (appTransaction.getTransaction() != null) {
            System.out.println("the result is null");
        } else {
            org.nervos.appchain.protocol.core.methods.response.Transaction transaction
                    = appTransaction.getTransaction();
            System.out.println("hash(Transaction):" + transaction.getHash());
            System.out.println("content:" + transaction.getContent());
            System.out.println("blockNumber(dec):" + transaction.getBlockNumber());
            System.out.println("blockHash:" + transaction.getBlockHash());
            System.out.println("index:" + transaction.getIndex());
        }
    }


    //7.  getTransactionCount
    public static void testEthGetTransactionCount(
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

    //8.  getTransactionReceipt
    public static String testEthGetTransactionReceipt(
            String validTransactionHash) throws Exception {
        AppGetTransactionReceipt appGetTransactionReceipt = service.appGetTransactionReceipt(
                validTransactionHash).send();

        if (appGetTransactionReceipt.getTransactionReceipt() != null) {
            System.out.println("the result is null");
            return null;
        } else {
            //is option_value is null return NoSuchElementException, else return option_value
            TransactionReceipt transactionReceipt =
                    appGetTransactionReceipt.getTransactionReceipt();
            printTransactionReceiptInfo(transactionReceipt);
            return transactionReceipt.getContractAddress();
        }

    }

    public static void printTransactionReceiptInfo(
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

    //9.  eth_getCode
    public static void testEthGetCode(
            String validContractAddress, DefaultBlockParameter param)
            throws Exception {
        AppGetCode appGetCode = service
                .appGetCode(validContractAddress, param).send();

        if (appGetCode.isEmpty()) {
            System.out.println("the result is null");
        } else {
            System.out.println("contractcode:" + appGetCode.getCode());
        }
    }


    public Function totalSupply() {
        return new Function(
                "get",
                Collections.<Type>emptyList(),
                Collections.<TypeReference<?>>singletonList(new TypeReference<Uint256>() {
                }));
    }

    //10.  eth_call
    public static void testEthCall(
            String fromaddress, String contractAddress, String encodedFunction,
            DefaultBlockParameter param) throws Exception {
        AppCall appCall = service.appCall(
                new Call(fromaddress, contractAddress, encodedFunction),
                param).send();

        System.out.println("call result:" + appCall.getValue());
    }

    private static void printBlock(AppBlock.Block block) {
        System.out.println("hash(blockhash):"
                + block.getHash());
        System.out.println("version:"
                + block.getVersion());
        System.out.println("header.timestamp:"
                + block.getHeader().getTimestamp());
        System.out.println("header.prevHash:"
                + block.getHeader().getPrevHash());
        System.out.println("header.number(hex):"
                + block.getHeader().getNumber());
        System.out.println("header.number(dec):"
                + block.getHeader().getNumberDec());
        System.out.println("header.stateRoot:"
                + block.getHeader().getStateRoot());
        System.out.println("header.transactionsRoot:"
                + block.getHeader().getTransactionsRoot());
        System.out.println("header.receiptsRoot:"
                + block.getHeader().getReceiptsRoot());
        System.out.println("header.gasUsed:"
                + block.getHeader().getGasUsed());
        System.out.println("header.proof.proposal:"
                + block.getHeader().getProof().getTendermint().getProposal());
        System.out.println("header.proof.height:"
                + block.getHeader().getProof().getTendermint().getHeight());
        System.out.println("header.proof.round:"
                + block.getHeader().getProof().getTendermint().getRound());

        if (!block.getBody().getTransactions().isEmpty()) {
            System.out.println("number of transaction:"
                    + block.getBody().getTransactions().size());

            for (int i = 0; i < block.getBody().getTransactions().size(); i++) {
                org.nervos.appchain.protocol.core.methods.response.Transaction tx =
                        (org.nervos.appchain.protocol.core.methods.response.Transaction)
                                block.getBody().getTransactions().get(i).get();
                System.out.println("body.transactions.tranhash:" + tx.getHash());
                System.out.println("body.transactions.content:" + tx.getContent());
            }
        } else {
            System.out.println("the block transactions is null");
        }
    }
}
