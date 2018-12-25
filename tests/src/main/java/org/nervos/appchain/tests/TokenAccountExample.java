package org.nervos.appchain.tests;

import java.io.File;
import java.math.BigInteger;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.account.Account;
import org.nervos.appchain.protocol.account.CompiledContract;
import org.nervos.appchain.protocol.core.methods.response.AbiDefinition;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;

public class TokenAccountExample {

    private static BigInteger chainId;
    private static int version;
    private static String privateKey;
    private static String fromAddress;
    private static String toAddress;
    private static String solPath;

    private static long quota;
    private static String value;
    private static AppChainj service;

    private Account account;
    private CompiledContract tokenContract;
    private String contractAddress;

    static {
        Config conf = new Config();
        conf.buildService(false);

        privateKey = conf.primaryPrivKey;
        fromAddress = conf.primaryAddr;
        toAddress = conf.auxAddr1;
        solPath = conf.tokenSolidity;
        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        quota = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
    }

    private static TransactionReceipt waitToGetReceipt(
            String hash) throws Exception {
        Thread.sleep(10_000);
        return service.appGetTransactionReceipt(hash)
                .send().getTransactionReceipt();
    }

    public TokenAccountExample() throws Exception {
        account = new Account(privateKey, service);
        tokenContract = new CompiledContract(new File(solPath));

    }

    public String deployContract(String path) throws Exception {
        AppSendTransaction ethSendTransaction = account.deploy(
                new File(path), TestUtil.getNonce(), quota, version, chainId, value);
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
        AppSendTransaction ethSendTransaction = (AppSendTransaction)
                account.callContract(
                        contractAddress, transfer, TestUtil.getNonce(),
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
                contractAddress, getBalance, TestUtil.getNonce(),
                quota, version, chainId, value, address);
        System.out.println(address + " has "
                + object.toString() + " tokens");
    }

    public void transferRemote(String toAddress, BigInteger amount) throws Exception {
        AppSendTransaction ethSendTransaction = (AppSendTransaction) account.callContract(
                contractAddress, "transfer", TestUtil.getNonce(),
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
                contractAddress, "getBalance", TestUtil.getNonce(),
                quota, version, chainId, value, address);
        System.out.println(address + " has " + object.toString() + " tokens");
    }

    public void storeAbiToBlockchain() throws Exception {
        AppSendTransaction ethSendTransaction =
                (AppSendTransaction) account.uploadAbi(
                        contractAddress, tokenContract.getAbi(),
                        TestUtil.getNonce(), quota, version, chainId, value);
        TransactionReceipt receipt = waitToGetReceipt(
                ethSendTransaction.getSendTransactionResult().getHash());
        if (receipt.getErrorMessage() != null) {
            System.out.println("call upload abi method failed because of "
                    + receipt.getErrorMessage());
            System.exit(1);
        } else {
            System.out.println("call upload abi method success. Receipt " + receipt);
        }
        System.out.println("call upload abi method success and receipt is "
                + receipt.getTransactionHash());
    }

    public void getAbi() throws Exception {
        System.out.println("Get Abi from address: " + contractAddress);
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
        TokenAccountExample tokenAccountExample = new TokenAccountExample();
        String contractAddr = tokenAccountExample.deployContract(solPath);
        tokenAccountExample.getBalance(fromAddress);
        tokenAccountExample.getBalance(toAddress);
        tokenAccountExample.transfer(toAddress, BigInteger.valueOf(1200));
        tokenAccountExample.getBalance(fromAddress);
        tokenAccountExample.getBalance(toAddress);
        tokenAccountExample.storeAbiToBlockchain();
        tokenAccountExample.getAbi();
        return contractAddr;
    }
    //CHECKSTYLE:ON

    private static void callContractMethodFromRemoteAbi(String contractAddress)
            throws Exception {
        TokenAccountExample tokenAccountExample = new TokenAccountExample();
        tokenAccountExample.contractAddress = contractAddress;
        tokenAccountExample.transferRemote(toAddress, BigInteger.valueOf(500));
        tokenAccountExample.getBalanceRemote(fromAddress);
        tokenAccountExample.getBalanceRemote(toAddress);
    }
}
