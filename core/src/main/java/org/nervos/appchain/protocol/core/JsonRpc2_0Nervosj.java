package org.nervos.appchain.protocol.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjService;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.response.AppAccounts;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.protocol.core.methods.response.AppBlockNumber;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.core.methods.response.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppGetAbi;
import org.nervos.appchain.protocol.core.methods.response.AppGetBalance;
import org.nervos.appchain.protocol.core.methods.response.AppGetCode;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.AppLog;
import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppSign;
import org.nervos.appchain.protocol.core.methods.response.AppTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppUninstallFilter;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.NetPeerCount;
import org.nervos.appchain.protocol.core.methods.response.Transaction;
import org.nervos.appchain.protocol.core.methods.response.Web3ClientVersion;
import org.nervos.appchain.protocol.core.methods.response.Web3Sha3;
import org.nervos.appchain.protocol.rx.JsonRpc2_0Rx;
import org.nervos.appchain.utils.Async;
import org.nervos.appchain.utils.Numeric;
import rx.Observable;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0Nervosj implements Nervosj {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final NervosjService nervosjService;
    private final JsonRpc2_0Rx nervosjRx;
    private final long blockTime;

    public JsonRpc2_0Nervosj(NervosjService nervosjService) {
        this(nervosjService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0Nervosj(NervosjService nervosjService, long pollingInterval) {
        this(nervosjService, pollingInterval, Async.defaultExecutorService());
    }

    public JsonRpc2_0Nervosj(
            NervosjService nervosjService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        this.nervosjService = nervosjService;
        this.nervosjRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
    }

    @Override
    public Request<?, Web3ClientVersion> web3ClientVersion() {
        return new Request<>(
                "clientVersion",
                Collections.<String>emptyList(),
                nervosjService,
                Web3ClientVersion.class);
    }

    @Override
    public Request<?, Web3Sha3> web3Sha3(String data) {
        return new Request<>(
                "sha3",
                Arrays.asList(data),
                nervosjService,
                Web3Sha3.class);
    }


    @Override
    public Request<?, NetPeerCount> netPeerCount() {
        return new Request<>(
                "peerCount",
                Collections.<String>emptyList(),
                nervosjService,
                NetPeerCount.class);
    }


    //2 methods: appAccount, appSign are not used
    //keep them for wallet use in future.

    @Override
    public Request<?, AppAccounts> appAccounts() {
        return new Request<>(
                "accounts",
                Collections.<String>emptyList(),
                nervosjService,
                AppAccounts.class);
    }

    @Override
    public Request<?, AppSign> appSign(String address, String sha3HashOfDataToSign) {
        return new Request<>(
                "sign",
                Arrays.asList(address, sha3HashOfDataToSign),
                nervosjService,
                AppSign.class);
    }

    @Override
    public Request<?, AppMetaData> appMetaData(DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getMetaData",
                Arrays.asList(defaultBlockParameter.getValue()),
                nervosjService,
                AppMetaData.class);
    }

    @Override
    public Request<?, AppBlockNumber> appBlockNumber() {
        return new Request<>(
                "blockNumber",
                Collections.<String>emptyList(),
                nervosjService,
                AppBlockNumber.class);
    }

    @Override
    public Request<?, AppGetBalance> appGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getBalance",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                nervosjService,
                AppGetBalance.class);
    }

    @Override
    public Request<?, AppGetAbi> appGetAbi(
            String contractAddress, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getAbi",
                Arrays.asList(contractAddress, defaultBlockParameter.getValue()),
                nervosjService,
                AppGetAbi.class);
    }


    @Override
    public Request<?, AppGetTransactionCount> appGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getTransactionCount",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                nervosjService,
                AppGetTransactionCount.class);
    }


    @Override
    public Request<?, AppGetCode> appGetCode(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getCode",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                nervosjService,
                AppGetCode.class);
    }

    @Override
    public Request<?, AppSendTransaction>
            appSendRawTransaction(
            String signedTransactionData) {
        return new Request<>(
                "sendRawTransaction",
                Arrays.asList(signedTransactionData),
                nervosjService,
                AppSendTransaction.class);
    }

    @Override
    public Request<?, AppCall> appCall(
            Call call, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "call",
                Arrays.asList(call, defaultBlockParameter),
                nervosjService,
                AppCall.class);
    }


    @Override
    public Request<?, AppBlock> appGetBlockByHash(
            String blockHash, boolean returnFullTransactionObjects) {
        return new Request<>(
                "getBlockByHash",
                Arrays.asList(
                        blockHash,
                        returnFullTransactionObjects),
                nervosjService,
                AppBlock.class);
    }

    @Override
    public Request<?, AppBlock> appGetBlockByNumber(
            DefaultBlockParameter defaultBlockParameter,
            boolean returnFullTransactionObjects) {
        return new Request<>(
                "getBlockByNumber",
                Arrays.asList(
                        defaultBlockParameter.getValue(),
                        returnFullTransactionObjects),
                nervosjService,
                AppBlock.class);
    }

    @Override
    public Request<?, AppTransaction> appGetTransactionByHash(String transactionHash) {
        return new Request<>(
                "getTransaction",
                Arrays.asList(transactionHash),
                nervosjService,
                AppTransaction.class);
    }

    @Override
    public Request<?, AppGetTransactionReceipt> appGetTransactionReceipt(String transactionHash) {
        return new Request<>(
                "getTransactionReceipt",
                Arrays.asList(transactionHash),
                nervosjService,
                AppGetTransactionReceipt.class);
    }

    @Override
    public Request<?, AppFilter> appNewFilter(
            org.nervos.appchain.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "newFilter",
                Arrays.asList(appFilter),
                nervosjService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppFilter> appNewBlockFilter() {
        return new Request<>(
                "newBlockFilter",
                Collections.<String>emptyList(),
                nervosjService,
                AppFilter.class);
    }

    public Request<?, AppFilter> appNewPendingTransactionFilter() {
        return new Request<>(
                "newPendingTransactionFilter",
                Collections.<String>emptyList(),
                nervosjService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId) {
        return new Request<>(
                "uninstallFilter",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                nervosjService,
                AppUninstallFilter.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterChanges(BigInteger filterId) {
        return new Request<>(
                "getFilterChanges",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                nervosjService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterLogs(BigInteger filterId) {
        return new Request<>(
                "getFilterLogs",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                nervosjService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetLogs(
            org.nervos.appchain.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "getLogs",
                Arrays.asList(appFilter),
                nervosjService,
                AppLog.class);
    }

    @Override
    public Observable<String> appBlockHashObservable() {
        return nervosjRx.appBlockHashObservable(blockTime);
    }

    @Override
    public Observable<String> appPendingTransactionHashObservable() {
        return nervosjRx.appPendingTransactionHashObservable(blockTime);
    }

    @Override
    public Observable<Log> appLogObservable(
            org.nervos.appchain.protocol.core.methods.request.AppFilter appFilter) {
        return nervosjRx.appLogObservable(appFilter, blockTime);
    }

    @Override
    public Observable<Transaction>
            transactionObservable() {
        return nervosjRx.transactionObservable(blockTime);
    }

    @Override
    public Observable<Transaction>
            pendingTransactionObservable() {
        return nervosjRx.pendingTransactionObservable(blockTime);
    }

    @Override
    public Observable<AppBlock> blockObservable(boolean fullTransactionObjects) {
        return nervosjRx.blockObservable(fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<AppBlock> replayBlocksObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return nervosjRx.replayBlocksObservable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Observable<AppBlock> replayBlocksObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return nervosjRx.replayBlocksObservable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Observable<Transaction>
            replayTransactionsObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return nervosjRx.replayTransactionsObservable(startBlock, endBlock);
    }

    @Override
    public Observable<AppBlock> catchUpToLatestBlockObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Observable<AppBlock> onCompleteObservable) {
        return nervosjRx.catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, onCompleteObservable);
    }

    @Override
    public Observable<AppBlock> catchUpToLatestBlockObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return nervosjRx.catchUpToLatestBlockObservable(startBlock, fullTransactionObjects);
    }

    @Override
    public Observable<Transaction>
            catchUpToLatestTransactionObservable(DefaultBlockParameter startBlock) {
        return nervosjRx.catchUpToLatestTransactionObservable(startBlock);
    }

    @Override
    public Observable<AppBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return nervosjRx.catchUpToLatestAndSubscribeToNewBlocksObservable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<Transaction>
            catchUpToLatestAndSubscribeToNewTransactionsObservable(
            DefaultBlockParameter startBlock) {
        return nervosjRx.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                startBlock, blockTime);
    }
}
