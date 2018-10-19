package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
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
import org.nervos.appchain.protocol.core.methods.request.AppFilter;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;

import rx.Observable;

import static org.nervos.appchain.tx.Contract.staticExtractEventParameters;

public class TokenFilterObservableExample {
    private static int chainId;
    private static int version;
    private static String privateKey;
    private static String toAddress;
    private static Long quota;
    private static String value;
    private static AppChainj service;

    static {
        Config conf = new Config();
        conf.buildService(false);

        chainId = Integer.parseInt(conf.chainId);
        version = Integer.parseInt(conf.version);
        privateKey = conf.primaryPrivKey;
        toAddress = conf.auxAddr1;
        service = conf.service;
        quota = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
    }

    public static String deployContract(String contractCode) throws IOException {
        String txHash = "";
        long validUntilBlock = TestUtil.getValidUtilBlock(service).longValue();
        String nonce = TestUtil.getNonce();
        Transaction txToDeployContract = Transaction
                .createContractTransaction(
                        nonce, quota, validUntilBlock,
                        version, chainId, value, contractCode);
        String signedTx = txToDeployContract.sign(privateKey);
        AppSendTransaction appSendTransaction = service
                .appSendRawTransaction(signedTx).send();
        if (!appSendTransaction.hasError()) {
            System.out.println("tx sent successfully.");
            System.out.println("Get txHash from contract deployment tx");
            txHash = appSendTransaction.getSendTransactionResult().getHash();
            System.out.println("TxHash: " + txHash);
        }
        return txHash;
    }

    private static String getContractAddr(String txHash) throws IOException {
        AppGetTransactionReceipt transactionReceipt =
                service.appGetTransactionReceipt(txHash).send();
        Optional<TransactionReceipt> receipt = transactionReceipt.getTransactionReceipt();
        if (!receipt.isPresent()) {
            System.out.println("Failed to get tx receipt from hash: " + txHash);
            return null;
        }
        return receipt.get().getContractAddress();
    }


    //create a Request.AppFilter to be sent with service.
    private static AppFilter createNewFilter(Event event, String contractAddr) {
        AppFilter filter = new AppFilter(DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST, contractAddr);
        filter.addSingleTopic(EventEncoder.encode(event));
        return filter;
    }

    //this will call JSON RPC "newFilter" and get the filterId.
    private static String newFilter(AppFilter appFilter) throws IOException {
        org.nervos.appchain.protocol.core.methods.response.AppFilter responseFilter
                = service.appNewFilter(appFilter).send();
        return responseFilter.getFilterId().toString();
    }

    private static Event createEvent() {
        return new Event("Transfer",
                Arrays.asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.asList(new TypeReference<Uint256>() {}));
    }

    static String transfer(
            String contractAddr, String toAddr, BigInteger value) throws Exception {
        Function transferFunc = new Function(
                "transfer",
                Arrays.asList(new Address(toAddr), new Uint256(value)),
                Collections.emptyList()
        );
        String funcCallData = FunctionEncoder.encode(transferFunc);
        return contractFunctionCall(contractAddr, funcCallData);
    }

    private static String contractFunctionCall(
            String contractAddress, String funcCallData) throws Exception {
        long currentHeight = service.appBlockNumber()
                .send().getBlockNumber().longValue();
        long validUntilBlock = currentHeight + 80;
        String nonce = TestUtil.getNonce();

        long quota = 1000000;

        Transaction tx = Transaction.createFunctionCallTransaction(
                contractAddress, nonce, quota, validUntilBlock,
                version, chainId, value, funcCallData);
        String rawTx = tx.sign(privateKey, false, false);

        return service.appSendRawTransaction(rawTx)
                .send().getSendTransactionResult().getHash();
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
            AppFilter filter = createNewFilter(event, contractAddr);
            String filterId = newFilter(filter);
            System.out.println("Filter created successfully: filter ID: " + filterId);

            Observable appLogObservable = service.appLogObservable(filter);
            Observable<TransferObj> reponse = appLogObservable.map(
                    (log) -> {
                        EventValues eventValues = staticExtractEventParameters(event, (Log)log);
                        TransferObj typedResponse = new TransferObj();
                        typedResponse.from =
                                (String) eventValues.getIndexedValues().get(0).getValue();
                        typedResponse.to =
                                (String) eventValues.getIndexedValues().get(1).getValue();
                        typedResponse.value =
                                (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                        return typedResponse;
                    }
                    );

            reponse.subscribe(x ->
                    System.out.println(
                        "(Transfer Object)From: " + x.from
                        + " To: " + x.to + " Value: " + x.value)
            );


            System.out.println("Send 3 transactions to be detected by event filter");
            transfer(contractAddr, toAddress, new BigInteger("1"));
            transfer(contractAddr, toAddress, new BigInteger("2"));
            transfer(contractAddr, toAddress, new BigInteger("3"));

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception: maybe tx is not deployed successfully");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static class TransferObj {
        public String from;

        public String to;

        public BigInteger value;
    }
}
