package org.nervos.appchain.tests;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.tx.CitaTransactionManager;
import org.nervos.appchain.tx.TransactionManager;

public class TokenFilterCodeGenExample {
    private static int chainId;
    private static int version;
    private static String payerPrivateKey;
    private static String payeePrivateKey;
    private static AppChainj service;
    private static long quota;
    private static String value;
    private static Token token;

    static {

        Config conf = new Config();
        conf.buildService(false);

        payerPrivateKey = conf.primaryPrivKey;
        payeePrivateKey = conf.auxPrivKey1;

        service = conf.service;
        quota = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
    }

    static long getBalance(Credentials credentials) {
        long accountBalance = 0;
        try {
            CompletableFuture<BigInteger> balanceFuture =
                    token.getBalance(credentials.getAddress()).sendAsync();
            accountBalance = balanceFuture.get(8, TimeUnit.SECONDS).longValue();
        } catch (Exception e) {
            System.out.println("Failed to get balance of account: " + credentials.getAddress());
            e.printStackTrace();
            System.exit(1);
        }
        return accountBalance;
    }

    private void eventObserve() {
        rx.Observable<Token.TransferEventResponse> observable =
                token.transferEventObservable(
                        DefaultBlockParameter.valueOf(BigInteger.ONE),
                        DefaultBlockParameterName.LATEST);
        observable.subscribe(
                event -> System.out.println(
                        "Observable, TransferEvent(" + event._from + ", "
                                + event._to + ", " + event._value.longValue() + ")"));
    }

    private void randomTransferToken() {
        new Thread(this::eventObserve).start();

        Credentials fromCredential = Credentials.create(payerPrivateKey);
        Credentials toCredential = Credentials.create(payeePrivateKey);

        for (int i = 0; i < 20; i++) {
            System.out.println("Transfer " + i);
            long fromBalance = getBalance(fromCredential);
            long transferAmount = ThreadLocalRandom
                    .current().nextLong(0, fromBalance);
            TransferEvent event = new TransferEvent(fromCredential, toCredential, transferAmount);
            System.out.println("Transaction " + event.toString() + " is being executing.");
            event.execute();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception e) {
                System.out.println("Thread interrupted.");
            }
        }
    }


    public static void main(String[] args) {
        TransactionManager citaTxManager = new CitaTransactionManager(
                service, Credentials.create(payerPrivateKey), 5, 3000);
        long validUtilBlock = TestUtil.getValidUtilBlock(service).longValue();
        String nonce = TestUtil.getNonce();

        CompletableFuture<Token> tokenFuture = Token.deploy(
                service, citaTxManager, 1000000L, nonce,
                validUtilBlock, version,
                value, chainId).sendAsync();
        TokenFilterCodeGenExample tokenFilterCodeGenExample = new TokenFilterCodeGenExample();

        tokenFuture.whenCompleteAsync((contract, exception) -> {
            if (exception != null) {
                System.out.println("Failed to deploy the contract. Exception: " + exception);
                exception.printStackTrace();
                System.exit(1);
            }
            token = contract;
            System.out.println("Contract deployment success. Contract address: "
                    + contract.getContractAddress());

            //in cita 0.20, it seems that contract is not ready even if address is returned.
            try {
                System.out.println("waiting for transaction in the block");
                TimeUnit.SECONDS.sleep(4);
            } catch (Exception e) {
                System.out.println("interrupted when waiting for transactions written into block");
                e.printStackTrace();
                System.exit(1);
            }

            try {
                System.out.println("Contract initial state: ");
                tokenFilterCodeGenExample.randomTransferToken();
            } catch (Exception e) {
                System.out.println("Failed to get accounts balances");
                e.printStackTrace();
                System.exit(1);
            }
            System.exit(0);
        });
    }

    private class TransferEvent {
        Credentials from;
        Credentials to;
        long tokens;

        TransferEvent(Credentials from, Credentials to, long tokens) {
            this.from = from;
            this.to = to;
            this.tokens = tokens;
        }

        void execute() {
            Token tokenContract = TokenFilterCodeGenExample.this.token;
            long validUtilBlock = TestUtil.getValidUtilBlock(
                    TokenFilterCodeGenExample.this.service).longValue();

            String nonce = TestUtil.getNonce();
            tokenContract.transfer(
                    this.to.getAddress(), BigInteger.valueOf(tokens),
                    TokenFilterCodeGenExample.quota,
                    nonce, validUtilBlock, version, chainId, value).sendAsync();
        }

        @Override
        public String toString() {
            return "TransferEvent(" + this.from.getAddress()
                    + ", " + this.to.getAddress() + ", " + this.tokens + ")";
        }
    }
}
