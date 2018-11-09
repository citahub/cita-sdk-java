package org.nervos.appchain.tests;

//CHECKSTYLE:OFF
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import org.nervos.appchain.abi.EventEncoder;
import org.nervos.appchain.abi.EventValues;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Event;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.RemoteCall;
import org.nervos.appchain.protocol.core.methods.request.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.tx.Contract;
import org.nervos.appchain.tx.TransactionManager;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://github.com/cryptape/nervosj/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with appChainj version 0.18.
 */
public class Token extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b600160a060020a033316600090815260208190526040902061271090556101df8061003b6000396000f3006060604052600436106100565763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166327e235e3811461005b578063a9059cbb1461008c578063f8b2cb4f146100c2575b600080fd5b341561006657600080fd5b61007a600160a060020a03600435166100e1565b60405190815260200160405180910390f35b341561009757600080fd5b6100ae600160a060020a03600435166024356100f3565b604051901515815260200160405180910390f35b34156100cd57600080fd5b61007a600160a060020a0360043516610198565b60006020819052908152604090205481565b600160a060020a03331660009081526020819052604081205482901080159061011c5750600082115b1561018e57600160a060020a033381166000818152602081905260408082208054879003905592861680825290839020805486019055917fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a3506001610192565b5060005b92915050565b600160a060020a0316600090815260208190526040902054905600a165627a7a72305820f59b7130870eee8f044b129f4a20345ffaff662707fc0758133cd16684bc3b160029";

    protected Token(String contractAddress, AppChainj appChainj, TransactionManager transactionManager) {
        super(BINARY, contractAddress, appChainj, transactionManager);
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Transfer",
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Transfer",
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        AppFilter filter = new AppFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return appChainj.appLogFlowable(filter).map(new io.reactivex.functions.Function<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse apply(Log log) throws Exception {
                EventValues eventValues = extractEventParameters(event, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<BigInteger> balances(String param0) {
        Function function = new Function("balances",
                Arrays.<Type>asList(new org.nervos.appchain.abi.datatypes.Address(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> transfer(String _to, BigInteger _value, Long quota, String nonce, Long validUntilBlock, Integer version, BigInteger chainId, String value) {
        Function function = new Function(
                "transfer",
                Arrays.<Type>asList(new org.nervos.appchain.abi.datatypes.Address(_to),
                        new org.nervos.appchain.abi.datatypes.generated.Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, quota, nonce, validUntilBlock, version, chainId, value);
    }

    public RemoteCall<BigInteger> getBalance(String account) {
        Function function = new Function("getBalance",
                Arrays.<Type>asList(new org.nervos.appchain.abi.datatypes.Address(account)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static RemoteCall<Token> deploy(AppChainj appChainj, TransactionManager transactionManager, Long quota, String nonce, Long validUntilBlock, Integer version, String value, BigInteger chainId) {
        return deployRemoteCall(Token.class, appChainj, transactionManager, quota, nonce, validUntilBlock, version, chainId, value, BINARY, "");
    }

    public static Token load(String contractAddress, AppChainj appChainj, TransactionManager transactionManager) {
        return new Token(contractAddress, appChainj, transactionManager);
    }

    public static class TransferEventResponse {
        public String _from;

        public String _to;

        public BigInteger _value;
    }

}
//CHECKSTYLE:ON
