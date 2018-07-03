package org.web3j.examples;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Random;


public class SendNOSDemo {
    private final static int VERSION = 0;

    private static Properties props;
    private static String testNetIpAddr;
    private static Web3j service;
    private static Random random;
    private static int chainId;
    private static String payerPrivateKey;
    private static String payerAddr;
    private static String payeeAddr;

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
        payerPrivateKey = "0x2c5c6c187d42e58a4c212a4aab0a3cfa4030256ed82bb3e05706706ab5be9641";
        payerAddr = "0x0438bfcabdda99c00acf0039e6c1f3f2d78edde5";
        payeeAddr = "0x01256621e13187316caccc61b401e805ccf3a831";
    }

    public static void main(String[] args) throws Exception{
        //check balance before transfer
        BigInteger payerBalance = getBalance(payerAddr);
        BigInteger payeeBalance = getBalance(payeeAddr);
        printInfo(payerAddr, payerBalance);
        printInfo(payeeAddr, payeeBalance);

        //transfer one NOS from payer to payee
        //please be attention that default unit of parameter value, like ethereum, is wei which is 10e-18 of NOS
        String value = "2";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();
        System.out.println(valueWei);
        transfer(payeeAddr, valueWei);

        //wait for block written into chain
        Thread.sleep(15000);

        //check balance after transfer
        payerBalance = getBalance(payerAddr);
        payeeBalance = getBalance(payeeAddr);
        printInfo(payerAddr, payerBalance);
        printInfo(payeeAddr, payeeBalance);

    }

    static BigInteger getBalance(String addr) throws Exception{
        EthGetBalance result = service.ethGetBalance(addr, DefaultBlockParameter.valueOf("latest")).send();
        BigInteger balance = result.getBalance();
        return balance;
    }

    static void printInfo(String addr, BigInteger balance){
        String balanceStr = balance.toString();
        System.out.println("Address: " + addr +
                " NOS: " + Convert.fromWei(balanceStr, Convert.Unit.ETHER) +
                " Balance: " + balance);
    }

    static long currentBlockNumber() throws Exception {
        return service.ethBlockNumber().send().getBlockNumber().longValue();
    }

    static void transfer(String payeeAddr, String value) throws Exception{
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = 9999;
        long currentHeight = currentBlockNumber();
        long validUntilBlock = currentHeight + 80;
        Transaction tx = new Transaction(payeeAddr, nonce, quota, validUntilBlock, VERSION, chainId, value, "");
        String rawTx = tx.sign(payerPrivateKey, false, false);
        service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
    }

}
