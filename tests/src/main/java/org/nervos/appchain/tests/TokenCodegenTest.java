package org.nervos.appchain.tests;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjFactory;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.http.HttpService;
import org.nervos.appchain.tx.CitaTransactionManager;
import org.nervos.appchain.tx.TransactionManager;

public class TokenCodegenTest {
    private static Properties props;
    private static String testNetIpAddr;
    private static int chainId;
    private static int version;
    private static String creatorPrivateKey;
    private static Credentials creator;
    private static ArrayList<Credentials> testAccounts = new ArrayList<>();
    private static Nervosj service;
    private static Random random;
    private static BigInteger quota;
    private static String value;
    private static Token token;

    private static final String configPath =
            "tests/src/main/resources/config.properties";

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
        creatorPrivateKey = props.getProperty(Config.SENDER_PRIVATE_KEY);
        creator = Credentials.create(creatorPrivateKey);
        loadAccounts();

        HttpService.setDebug(false);
        service = NervosjFactory.build(new HttpService(testNetIpAddr));
        random = new Random(System.currentTimeMillis());
        quota = BigInteger.valueOf(1000000);
        value = "0";
    }

    static void loadAccounts() {
        testAccounts.add(creator);

        List<String> accounts = new ArrayList<String>();
        String testAcct1 = props.getProperty(Config.TEST_PRIVATE_KEY_1);
        String testAcct2 = props.getProperty(Config.TEST_PRIVATE_KEY_2);
        accounts.add(testAcct1);
        accounts.add(testAcct2);
        for (String acct : accounts) {
            Credentials c = Credentials.create(acct);
            testAccounts.add(c);
        }
    }

    static BigInteger getCurrentHeight() throws Exception {
        return service.appBlockNumber().send().getBlockNumber();
    }

    static long getBalance(Credentials credentials) {
        long accountBalance = 0;
        try {
            Future<BigInteger> balanceFuture = token.getBalance(
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

    static void printBalanceInfo() {
        for (Credentials credentials : testAccounts) {
            System.out.println("Address: " + credentials.getAddress()
                    + " Balance: " + getBalance(credentials));
        }
    }

    /*
     * Waiting for tx complete.
     * Interact with chain to get the accounts' balances and then update the local state
     * */
    static void waitToGetToken() {
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
    static boolean isTokenTransferComplete() {
        Map<Credentials, Long> accountTokens = new HashMap<Credentials, Long>();
        for (Credentials c : testAccounts) {
            Long balance = getBalance(c);
            accountTokens.put(c, balance);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("failed to get balance.");
        }
        long totalToken = 0;
        for (Map.Entry<Credentials, Long> accountToken : accountTokens.entrySet()) {
            totalToken += accountToken.getValue();
        }
        return totalToken == 10000;
    }

    private void shuffle(Credentials credentials) {
        System.out.println("transfer all tokens to " + credentials.getAddress());
        for (Credentials testAccount : testAccounts) {
            if(testAccount != credentials) {
                TransferEvent event = new TransferEvent(testAccount, credentials, getBalance(testAccount));
                try {
                    Future<TransactionReceipt> receiptFuture = event.execute();
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
        }
    }

    private void randomTransferToken() {
        int accountNum = testAccounts.size();
        Random random = new Random();
        long shuffleThreshold = 10;

        while (true) {
            Random r = new Random();
            int n = accountNum;
            int i = r.nextInt() % n;
            int j = r.nextInt() % n;
            int[] pair = {i, j};
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

                Future<TransactionReceipt> receiptFuture = event.execute();
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

                for (Credentials testAccount : testAccounts) {
                    long accountBalance = getBalance(testAccount);
                    System.out.println(testAccount.getAddress() + ": " + accountBalance);
                }

                System.exit(1);
            }

            if (event.tokens < shuffleThreshold) {
                shuffle(testAccounts.get(0));
            }
        }
    }


    public static void main(String[] args) throws Exception {
        TransactionManager citaTxManager = new CitaTransactionManager(
                service, creator, 5, 3000);
        long currentheight = getCurrentHeight().longValue();
        long validUtilBlock = currentheight + 88;
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));

        Future<Token> tokenFuture = Token.deploy(
                service, citaTxManager, 1000000L, nonce, validUtilBlock,
                version, value, chainId).sendAsync();
        TokenCodegenTest tokenCodegenTest = new TokenCodegenTest();


        System.out.println("Wait 10s for contract to be deployed...");
        Thread.sleep(10000);
        Token token = tokenFuture.get();
        if(token != null) {
            System.out.println("contract deployment success. Contract address: "
                    + token.getContractAddress());
        } else {
            System.out.println("Failed to deploy the contract.");
            System.exit(1);
        }

        try {
            System.out.println("Contract initial state: ");
            printBalanceInfo();
            tokenCodegenTest.randomTransferToken();
        } catch (Exception e) {
            System.out.println("Failed to get accounts balances");
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);

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

        Future<TransactionReceipt> execute() throws Exception {
            Token tokenContract = new Token(token.getContractAddress(), service,
                    new CitaTransactionManager(service, from, 5, 3000));
            long currentHeight = TokenCodegenTest.this.getCurrentHeight().longValue();
            return tokenContract.transfer(
                    this.to.getAddress(), BigInteger.valueOf(tokens), 100000L,
                    BigInteger.valueOf(Math.abs(random.nextLong())),
                    currentHeight + 88,
                    0, chainId, value).sendAsync();
        }

        @Override
        public String toString() {
            return "TransferEvent(" + this.from.getAddress() + ", "
                    + this.to.getAddress() + ", " + this.tokens + ")";
        }
    }
}
