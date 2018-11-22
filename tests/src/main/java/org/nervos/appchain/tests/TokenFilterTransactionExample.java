package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.abi.EventEncoder;
import org.nervos.appchain.abi.EventValues;
import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Event;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.request.AppFilter;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.AppLog;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;

import static org.nervos.appchain.tx.Contract.staticExtractEventParameters;

public class TokenFilterTransactionExample {
    private static BigInteger chainId;
    private static int version;
    private static String privateKey;
    private static String toAddress;
    private static Random random;
    private static Long quota;
    private static String value;
    private static AppChainj service;

    static {
        Config config = new Config();
        config.buildService(true);

        privateKey = config.primaryPrivKey;
        toAddress = config.auxAddr1;
        service = config.service;
        random = new Random(System.currentTimeMillis());
        quota = Long.parseLong(config.defaultQuotaDeployment);
        value = "0";

        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
    }


    private static String deployContract(String contractCode) throws IOException {
        String txHash = "";
        long validUntilBlock = TestUtil.getValidUtilBlock(service).longValue();
        String nonce = TestUtil.getNonce();
        Transaction txToDeployContract = Transaction
                .createContractTransaction(
                        nonce, quota, validUntilBlock,
                        version, chainId, value, contractCode);
        String signedTx = txToDeployContract.sign(privateKey);
        AppSendTransaction appSendTransaction = service.appSendRawTransaction(signedTx).send();
        if (!appSendTransaction.hasError()) {
            System.out.println("tx sent successfully.");
            System.out.println("Get txHash from contract deployment tx");
            txHash = appSendTransaction.getSendTransactionResult().getHash();
            System.out.println("TxHash: " + txHash);
        }
        return txHash;
    }

    private static String getContractAddr(String txHash) throws IOException {
        AppGetTransactionReceipt transactionReceipt
                = service.appGetTransactionReceipt(txHash).send();
        TransactionReceipt receipt = transactionReceipt.getTransactionReceipt();
        if (receipt == null) {
            System.out.println("Failed to get tx receipt from hash: " + txHash);
            return null;
        }
        return receipt.getContractAddress();
    }

    private static Event createEvent() {
        return new Event("Transfer",
                Arrays.asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.asList(new TypeReference<Uint256>() {}));
    }

    //create a Request.AppFilter to be sent with service.
    private static AppFilter createNewFilter(Event event, String contractAddr) throws IOException {
        AppFilter filter = new AppFilter(DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST, contractAddr);
        // 1. Only indexed parameter can be added into topic.
        // 2. Data in topic must be 256-bit long.
        // 3. Parameters are in order.
        filter.addSingleTopic(EventEncoder.encode(event));
        filter.addNullTopic();
        filter.addSingleTopic("0x000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa18");
        return filter;
    }

    //this will call JSON RPC "newFilter" and get the filterId.
    private static String newFilter(AppFilter appFilter) throws IOException {
        org.nervos.appchain.protocol.core.methods.response.AppFilter responseFilter
                = service.appNewFilter(appFilter).send();
        return responseFilter.getFilterId().toString();
    }

    //this will call JSON RPC "getFilterChanges"
    private static List<TransferEventResponse> getFilterChanges(Event event, String filterId) throws IOException {
        List<TransferEventResponse> responseList = new ArrayList<>();
        Request<?, AppLog> req = service
                .appGetFilterChanges(BigInteger.valueOf(Long.parseLong(filterId)));
        AppLog appLog = req.send();
        List<AppLog.LogResult> logResults = appLog.getLogs();
        for (AppLog.LogResult logResult : logResults) {
            if (logResult instanceof AppLog.LogObject) {
                Log log = ((AppLog.LogObject) logResult).get();
                EventValues eventValues = staticExtractEventParameters(event, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.value = (BigInteger) eventValues
                        .getNonIndexedValues().get(0).getValue();
                responseList.add(typedResponse);
            }
        }
        System.out.println("Total number of changed logs: " + logResults.size());
        return responseList;
    }

    //this will call JSON RPC "getFilterLogs" to get logs
    private static List<TransferEventResponse> getFilterLogs(Event event, AppFilter appFilter) throws IOException {
        List<TransferEventResponse> responseList = new ArrayList<>();
        AppLog responseAppLog = service.appGetLogs(appFilter).send();
        List<AppLog.LogResult> logResults = responseAppLog.getLogs();
        for (AppLog.LogResult logResult : logResults) {
            if (logResult instanceof AppLog.LogObject) {
                Log log = ((AppLog.LogObject) logResult).get();
                EventValues eventValues = staticExtractEventParameters(event, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.value = (BigInteger) eventValues
                        .getNonIndexedValues().get(0).getValue();
                responseList.add(typedResponse);
            }
        }
        System.out.println("Total number of logs: " + logResults.size());
        return responseList;
    }

    private static void transfer(
            String contractAddr, String toAddr, BigInteger value) throws Exception {
        Function transferFunc = new Function(
                "transfer",
                Arrays.asList(new Address(toAddr), new Uint256(value)),
                Collections.emptyList()
        );
        String funcCallData = FunctionEncoder.encode(transferFunc);
        contractFunctionCall(contractAddr, funcCallData);
    }

    private static void contractFunctionCall(
            String contractAddress, String funcCallData) throws Exception {
        long currentHeight = service.appBlockNumber()
                .send().getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        String nonce = String.valueOf(Math.abs(random.nextLong()));
        long quota = 1000000;

        Transaction tx = Transaction.createFunctionCallTransaction(
                contractAddress, nonce, quota, validUntilBlock,
                version, chainId, value, funcCallData);
        String rawTx = tx.sign(privateKey, false, false);

        service.appSendRawTransaction(rawTx).send();
    }

    public static void main(String[] args) {
        String contractCode = "6060604052341561000f57600080fd5"
                + "b600160a060020a03331660009081526020819052604090206127109055610"
                + "1df8061003b6000396000f3006060604052600436106100565763ffffffff7"
                + "c0100000000000000000000000000000000000000000000000000000000600"
                + "03504166327e235e3811461005b578063a9059cbb1461008c578063f8b2cb4"
                + "f146100c2575b600080fd5b341561006657600080fd5b61007a600160a0600"
                + "20a03600435166100e1565b60405190815260200160405180910390f35b341"
                + "561009757600080fd5b6100ae600160a060020a03600435166024356100f35"
                + "65b604051901515815260200160405180910390f35b34156100cd57600080f"
                + "d5b61007a600160a060020a0360043516610198565b6000602081905290815"
                + "2604090205481565b600160a060020a0333166000908152602081905260408"
                + "1205482901080159061011c5750600082115b1561018e57600160a060020a0"
                + "33381166000818152602081905260408082208054879003905592861680825"
                + "290839020805486019055917fddf252ad1be2c89b69c2b068fc378daa952ba"
                + "7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a"
                + "3506001610192565b5060005b92915050565b600160a060020a03166000908"
                + "15260208190526040902054905600a165627a7a72305820f59b7130870eee8"
                + "f044b129f4a20345ffaff662707fc0758133cd16684bc3b160029";

        try {
            String txHash = deployContract(contractCode);
            System.out.println("wait for 8 secs for contract deployment");
            TimeUnit.SECONDS.sleep(8);
            String contractAddr = getContractAddr(txHash);
            System.out.println("contract deployed successfully. Contract address: " + contractAddr);

            System.out.println("create event...");
            Event event = createEvent();

            System.out.println("create request filter...");
            final AppFilter filter = createNewFilter(event, contractAddr);

            System.out.println("Send 3 transactions before installing the filter");
            transfer(contractAddr, toAddress, new BigInteger("1"));
            transfer(contractAddr, toAddress, new BigInteger("2"));
            transfer(contractAddr, toAddress, new BigInteger("3"));

            TimeUnit.SECONDS.sleep(15);

            System.out.println("create new filter...");
            String filterId = newFilter(filter);

            System.out.println("Filter Id for new filter: " + filterId);
            System.out.println("\nget history logs by \"getFilterLogs\"");
            List<TransferEventResponse> list = getFilterLogs(event, filter);
            for (TransferEventResponse response : list) {
                System.out.println("from: " + response.from
                        + " to: " + response.to
                        + " value: " + response.value);
            }

            System.out.println("add one more changes");
            transfer(contractAddr, toAddress, new BigInteger("4"));

            System.out.println("wait 6s for transfer.");
            TimeUnit.SECONDS.sleep(6);

            System.out.println("\nget change logs by \"getFilterChanges\"");
            List<TransferEventResponse> responses = getFilterChanges(event, filterId);
            for (TransferEventResponse response : responses) {
                System.out.println("from: " + response.from
                        + " to: " + response.to
                        + " value: " + response.value);
            }
            System.out.println("Filter Id " + filterId);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception: maybe tx is not deployed successfully");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static class TransferEventResponse {
        public String from;

        public String to;

        public BigInteger value;
    }
}
