package org.nervos.appchain.tests;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.FunctionReturnDecoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.Uint;
import org.nervos.appchain.abi.datatypes.generated.Uint256;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;


public class TokenTransactionExample {
    private static BigInteger chainId;
    private static int version;
    private static String privateKey;
    private static String fromAddress;
    private static String toAddress;
    private static String binPath;
    private static Long quota;
    private static String value;
    private static AppChainj service;
    private static Transaction.CryptoTx cryptoTx;

    static {
        Config conf = new Config();
        conf.buildService(false);

        privateKey = conf.primaryPrivKey;
        fromAddress = conf.primaryAddr;
        toAddress = conf.auxAddr1;
        binPath = conf.tokenBin;

        service = conf.service;
        quota = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        cryptoTx = Transaction.CryptoTx.valueOf(conf.cryptoTx);
    }

    private static String loadContractCode(String binPath) throws Exception {
        return new String(Files.readAllBytes(Paths.get(binPath)));
    }

    private static String deployContract(String contractCode) throws Exception {
        long currentHeight = service.appBlockNumber().send()
                .getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        String nonce = TestUtil.getNonce();

        Transaction tx = Transaction.createContractTransaction(
                nonce, quota, validUntilBlock,
                version, chainId, value, contractCode);
        String rawTx = tx.sign(privateKey, cryptoTx, false);
        return service.appSendRawTransaction(rawTx)
                .send().getSendTransactionResult().getHash();
    }

    private static TransactionReceipt getTransactionReceipt(String txHash)
            throws Exception {
        return service.appGetTransactionReceipt(txHash)
                .send().getTransactionReceipt();
    }

    static String contractFunctionCall(
            String contractAddress, String funcCallData) throws Exception {
        long currentHeight = service.appBlockNumber()
                .send().getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        String nonce = TestUtil.getNonce();

        Transaction tx = Transaction.createFunctionCallTransaction(
                contractAddress, nonce, quota, validUntilBlock,
                version, chainId, value, funcCallData);
        String rawTx = tx.sign(privateKey, cryptoTx, false);

        return service.appSendRawTransaction(rawTx)
                .send().getSendTransactionResult().getHash();
    }

    private static String transfer(
            String contractAddr, String toAddr, BigInteger value) throws Exception {
        Function transferFunc = new Function(
                "transfer",
                Arrays.asList(new Address(toAddr), new Uint256(value)),
                Collections.emptyList()
        );
        String funcCallData = FunctionEncoder.encode(transferFunc);
        return contractFunctionCall(contractAddr, funcCallData);
    }

    //eth_call
    private static String call(
            String from, String contractAddress, String callData)
            throws Exception {
        Call call = new Call(from, contractAddress, callData);
        return service.appCall(call, DefaultBlockParameter.valueOf("latest")).send().getValue();
    }

    private static String getBalance(String fromAddr, String contractAddress) throws Exception {
        Function getBalanceFunc = new Function(
                "getBalance",
                Arrays.asList(new Address(fromAddr)),
                Arrays.asList(new TypeReference<Uint>() {
                })
        );
        String funcCallData = FunctionEncoder.encode(getBalanceFunc);
        String result = call(fromAddr, contractAddress, funcCallData);
        List<Type> resultTypes =
                FunctionReturnDecoder.decode(result, getBalanceFunc.getOutputParameters());
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
        if (txReceipt.getErrorMessage() != null) {
            System.out.println("There is something wrong in deployContractTxHash. Error: "
                    + txReceipt.getErrorMessage());
            System.exit(1);
        }
        String contractAddress = txReceipt.getContractAddress();
        System.out.println("Contract deployed successfully. Contract address: "
                + contractAddress);

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
        if (transferTxReceipt.getErrorMessage() != null) {
            System.out.println("Failed to call transfer method in contract. Error: "
                    + transferTxReceipt.getErrorMessage());
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
