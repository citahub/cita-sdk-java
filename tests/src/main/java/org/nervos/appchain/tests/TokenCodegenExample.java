package org.nervos.appchain.tests;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.tx.CitaTransactionManager;
import org.nervos.appchain.tx.TransactionManager;

public class TokenCodegenExample {
    private static Config conf;
    private static int chainId;
    private static int version;
    private static String creatorPrivateKey;
    private static Credentials creator;
    private static ArrayList<Credentials> testAccounts = new ArrayList<>();
    private static AppChainj service;
    private static Long quotaDeployment;
    private static String value;
    private static Token token;

    static {
        conf = new Config();
        conf.buildService(false);

        creatorPrivateKey = conf.primaryPrivKey;
        creator = Credentials.create(creatorPrivateKey);
        loadAccounts();

        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        quotaDeployment = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
    }

    static void loadAccounts() {
        testAccounts.add(creator);

        List<String> accounts = new ArrayList<String>();
        String testAcct1 = conf.auxPrivKey1;
        String testAcct2 = conf.auxPrivKey2;
        accounts.add(testAcct1);
        accounts.add(testAcct2);
        accounts.stream().map(Credentials::create).forEach(c -> testAccounts.add(c));
    }

    private static long getBalance(Credentials credentials) {
        long accountBalance = 0;
        try {
            CompletableFuture<BigInteger> balanceFuture = token.getBalance(
                    credentials.getAddress()).sendAsync();
            accountBalance = balanceFuture.get(8, TimeUnit.SECONDS).longValue();
        } catch (Exception e) {
            System.out.println("Failed to get balance of account: "
                    + credentials.getAddress());
            e.printStackTrace();
            System.exit(1);
        }
        return accountBalance;
    }

    private static void printBalanceInfo() {
        for (Credentials credentials : testAccounts) {
            System.out.println("Address: " + credentials.getAddress()
                    + " Balance: " + getBalance(credentials));
        }
    }

    /*
     * Waiting for tx complete.
     * Interact with chain to get the accounts' balances and then update the local state
     * */
    private static void waitToGetToken() {
        try {
            while (!isTokenTransferComplete()) {
                System.out.println("wait to get account token");
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            System.out.println("thread is interrupted accidently");
            System.exit(1);
        }
    }

    /**
     * check chain to see if the total token keeps the same.
     * In the contract, the initial supply is 10000,
     * the total token should be always 10000.
     * As state changes before publish the tx in contract,
     * it is way verifying tx in the chain
     * by checking if the total token keeps the same as initial token.
     */
    private static boolean isTokenTransferComplete() {
        Map<Credentials, Long> accountTokens =
                testAccounts.stream().collect(
                        Collectors.toMap(Function.identity(),
                                TokenCodegenExample::getBalance));
        long tokens = 0;
        long totalToken = accountTokens.values().stream().reduce(tokens, (x, y) -> x + y);
        return totalToken == 10000;
    }

    private void shuffle(Credentials credentials) {
        System.out.println("transfer all tokens to " + credentials.getAddress());
        testAccounts.forEach(c -> {
            if (c != credentials) {
                TransferEvent event = new TransferEvent(c, credentials, getBalance(c));
                try {
                    CompletableFuture<TransactionReceipt> receiptFuture = event.execute();
                    TransactionReceipt receipt = receiptFuture.get(12, TimeUnit.SECONDS);
                    if (receipt.getErrorMessage() == null) {
                        System.out.println(event.toString() + " execute success");
                    } else {
                        System.out.println(event.toString()
                                + " execute failed. Error: " + receipt.getErrorMessage());
                    }
                } catch (InterruptedException
                        | ExecutionException
                        | TimeoutException e) {
                    System.out.println(
                            "Failed to get receipt from receiptFuture. Failed to shuffle.");
                    System.exit(1);
                } catch (Exception e) {
                    System.out.println("Event execute failed. Failed to Shuffle");
                    System.exit(1);
                }
            }
        });
    }

    private void randomTransferToken() {
        int accountNum = testAccounts.size();
        Random random = new Random();
        long shuffleThreshold = 10;

        while (true) {
            int[] pair = random.ints(0, accountNum).limit(2).toArray();
            Credentials from;
            Credentials to;
            if (getBalance(testAccounts.get(pair[0])) > getBalance(testAccounts.get(pair[1]))) {
                from = testAccounts.get(pair[0]);
                to = testAccounts.get(pair[1]);
            } else {
                from = testAccounts.get(pair[1]);
                to = testAccounts.get(pair[0]);
            }

            long balanceOfFrom = getBalance(from);
            long balanceOfTo = getBalance(to);
            if (balanceOfFrom == balanceOfTo) {
                continue;
            }

            long transfer = ThreadLocalRandom.current()
                    .nextLong(0, balanceOfFrom - balanceOfTo);
            TransferEvent event = new TransferEvent(from, to, transfer);
            try {
                System.out.println("\nStart transfer with current Balance: ");
                printBalanceInfo();
                System.out.println(event.toString() + " executing..");

                CompletableFuture<TransactionReceipt> receiptFuture = event.execute();
                TransactionReceipt receipt = receiptFuture.get(12, TimeUnit.SECONDS);
                if (receipt.getErrorMessage() == null) {
                    System.out.println(event.toString()
                            + " execute success");
                } else {
                    System.out.println(event.toString()
                            + " execute failed, " + receipt.getErrorMessage());
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.out.println("Transaction " + event + ", get receipt failed, " + e);
                waitToGetToken();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!isTokenTransferComplete()) {
                System.out.println("token test failed, account tokens: ");
                testAccounts.forEach((account) -> {
                    long accountBalance = getBalance(account);
                    System.out.println(account.getAddress() + ": " + accountBalance);
                });
                System.exit(1);
            }

            if (event.tokens < shuffleThreshold) {
                shuffle(testAccounts.get(0));
            }
        }
    }


    public static void main(String[] args) {
        TransactionManager citaTxManager = new CitaTransactionManager(
                service, creator, 5, 3000);

        String nonce = TestUtil.getNonce();

        CompletableFuture<Token> tokenFuture = Token.deploy(
                service, citaTxManager, quotaDeployment, nonce,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, value, chainId).sendAsync();
        TokenCodegenExample tokenCodegenExample = new TokenCodegenExample();

        tokenFuture.whenCompleteAsync((contract, exception) -> {
            if (exception != null) {
                System.out.println("Failed to deploy the contract. Exception: "
                        + exception);
                exception.printStackTrace();
                System.exit(1);
            }
            token = contract;
            System.out.println("Contract deployment success. Contract address: "
                    + contract.getContractAddress());
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Interrupted when waiting blocks written in.");
                System.exit(1);
            }

            try {
                System.out.println("Contract initial state: ");
                printBalanceInfo();
                tokenCodegenExample.randomTransferToken();
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

        CompletableFuture<TransactionReceipt> execute() throws Exception {
            Token tokenContract = new Token(token.getContractAddress(), service,
                    new CitaTransactionManager(service, from, 5, 3000));

            return tokenContract.transfer(
                    this.to.getAddress(), BigInteger.valueOf(tokens), quotaDeployment,
                    TestUtil.getNonce(),
                    TestUtil.getValidUtilBlock(service).longValue(),
                    version, chainId, value).sendAsync();
        }

        @Override
        public String toString() {
            return "TransferEvent(" + this.from.getAddress() + ", "
                    + this.to.getAddress() + ", " + this.tokens + ")";
        }
    }
}
