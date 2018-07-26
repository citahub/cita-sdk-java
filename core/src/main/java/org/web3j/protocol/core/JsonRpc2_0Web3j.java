package org.web3j.protocol.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import rx.Observable;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.request.Call;
import org.web3j.protocol.core.methods.response.AppAccounts;
import org.web3j.protocol.core.methods.response.AppBlock;
import org.web3j.protocol.core.methods.response.AppBlockNumber;
import org.web3j.protocol.core.methods.response.AppFilter;
import org.web3j.protocol.core.methods.response.AppGetAbi;
import org.web3j.protocol.core.methods.response.AppGetBalance;
import org.web3j.protocol.core.methods.response.AppGetCode;
import org.web3j.protocol.core.methods.response.AppGetTransactionCount;
import org.web3j.protocol.core.methods.response.AppGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.AppLog;
import org.web3j.protocol.core.methods.response.AppMetaData;
import org.web3j.protocol.core.methods.response.AppSign;
import org.web3j.protocol.core.methods.response.AppTransaction;
import org.web3j.protocol.core.methods.response.AppUninstallFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.NetPeerCount;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.core.methods.response.Web3Sha3;
import org.web3j.protocol.rx.JsonRpc2_0Rx;
import org.web3j.utils.Async;
import org.web3j.utils.Numeric;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0Web3j implements Web3j {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final Web3jService web3jService;
    private final JsonRpc2_0Rx web3jRx;
    private final long blockTime;

    public JsonRpc2_0Web3j(Web3jService web3jService) {
        this(web3jService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0Web3j(Web3jService web3jService, long pollingInterval) {
        this(web3jService, pollingInterval, Async.defaultExecutorService());
    }

    public JsonRpc2_0Web3j(
            Web3jService web3jService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        this.web3jService = web3jService;
        this.web3jRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
    }

    @Override
    public Request<?, Web3ClientVersion> web3ClientVersion() {
        return new Request<>(
                "web3_clientVersion",
                Collections.<String>emptyList(),
                web3jService,
                Web3ClientVersion.class);
    }

    @Override
    public Request<?, Web3Sha3> web3Sha3(String data) {
        return new Request<>(
                "web3_sha3",
                Arrays.asList(data),
                web3jService,
                Web3Sha3.class);
    }


    @Override
    public Request<?, NetPeerCount> netPeerCount() {
        return new Request<>(
                "peerCount",
                Collections.<String>emptyList(),
                web3jService,
                NetPeerCount.class);
    }


    //2 methods: appAccount, appSign are not used
    //keep them for wallet use in future.

    @Override
    public Request<?, AppAccounts> appAccounts() {
        return new Request<>(
                "eth_accounts",
                Collections.<String>emptyList(),
                web3jService,
                AppAccounts.class);
    }

    @Override
    public Request<?, AppSign> appSign(String address, String sha3HashOfDataToSign) {
        return new Request<>(
                "eth_sign",
                Arrays.asList(address, sha3HashOfDataToSign),
                web3jService,
                AppSign.class);
    }

    @Override
    public Request<?, AppMetaData> appMetaData(DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getMetaData",
                Arrays.asList(defaultBlockParameter.getValue()),
                web3jService,
                AppMetaData.class);
    }

    @Override
    public Request<?, AppBlockNumber> appBlockNumber() {
        return new Request<>(
                "blockNumber",
                Collections.<String>emptyList(),
                web3jService,
                AppBlockNumber.class);
    }

    @Override
    public Request<?, AppGetBalance> appGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getBalance",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                web3jService,
                AppGetBalance.class);
    }

    @Override
    public Request<?, AppGetAbi> appGetAbi(
            String contractAddress, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getAbi",
                Arrays.asList(contractAddress, defaultBlockParameter.getValue()),
                web3jService,
                AppGetAbi.class);
    }


    @Override
    public Request<?, AppGetTransactionCount> appGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getTransactionCount",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                web3jService,
                AppGetTransactionCount.class);
    }


    @Override
    public Request<?, AppGetCode> appGetCode(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getCode",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                web3jService,
                AppGetCode.class);
    }


    //    @Override
    //    public Request<?, org.web3j.protocol.core.methods.response.EthSendTransaction>
    //            ethSendTransaction(
    //            Transaction transaction) {
    //        return new Request<>(
    //                "eth_sendTransaction",
    //                Arrays.asList(transaction),
    //                web3jService,
    //                org.web3j.protocol.core.methods.response.EthSendTransaction.class);
    //    }

    @Override
    public Request<?, org.web3j.protocol.core.methods.response.AppSendTransaction>
            appSendRawTransaction(
            String signedTransactionData) {
        return new Request<>(
                "sendRawTransaction",
                Arrays.asList(signedTransactionData),
                web3jService,
                org.web3j.protocol.core.methods.response.AppSendTransaction.class);
    }

    @Override
    public Request<?, org.web3j.protocol.core.methods.response.AppCall> appCall(
            Call call, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "call",
                Arrays.asList(call, defaultBlockParameter),
                web3jService,
                org.web3j.protocol.core.methods.response.AppCall.class);
    }


    @Override
    public Request<?, AppBlock> appGetBlockByHash(
            String blockHash, boolean returnFullTransactionObjects) {
        return new Request<>(
                "getBlockByHash",
                Arrays.asList(
                        blockHash,
                        returnFullTransactionObjects),
                web3jService,
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
                web3jService,
                AppBlock.class);
    }

    @Override
    public Request<?, AppTransaction> appGetTransactionByHash(String transactionHash) {
        return new Request<>(
                "getTransaction",
                Arrays.asList(transactionHash),
                web3jService,
                AppTransaction.class);
    }

    @Override
    public Request<?, AppGetTransactionReceipt> appGetTransactionReceipt(String transactionHash) {
        return new Request<>(
                "getTransactionReceipt",
                Arrays.asList(transactionHash),
                web3jService,
                AppGetTransactionReceipt.class);
    }

    @Override
    public Request<?, AppFilter> appNewFilter(
            org.web3j.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "newFilter",
                Arrays.asList(appFilter),
                web3jService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppFilter> appNewBlockFilter() {
        return new Request<>(
                "newBlockFilter",
                Collections.<String>emptyList(),
                web3jService,
                AppFilter.class);
    }

    public Request<?, AppFilter> appNewPendingTransactionFilter() {
        return new Request<>(
                "eth_newPendingTransactionFilter",
                Collections.<String>emptyList(),
                web3jService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId) {
        return new Request<>(
                "uninstallFilter",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                web3jService,
                AppUninstallFilter.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterChanges(BigInteger filterId) {
        return new Request<>(
                "getFilterChanges",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                web3jService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterLogs(BigInteger filterId) {
        return new Request<>(
                "getFilterLogs",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                web3jService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetLogs(
            org.web3j.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "getLogs",
                Arrays.asList(appFilter),
                web3jService,
                AppLog.class);
    }

    @Override
    public Observable<String> appBlockHashObservable() {
        return web3jRx.appBlockHashObservable(blockTime);
    }

    @Override
    public Observable<String> appPendingTransactionHashObservable() {
        return web3jRx.appPendingTransactionHashObservable(blockTime);
    }

    @Override
    public Observable<Log> appLogObservable(
            org.web3j.protocol.core.methods.request.AppFilter appFilter) {
        return web3jRx.appLogObservable(appFilter, blockTime);
    }

    @Override
    public Observable<org.web3j.protocol.core.methods.response.Transaction>
            transactionObservable() {
        return web3jRx.transactionObservable(blockTime);
    }

    @Override
    public Observable<org.web3j.protocol.core.methods.response.Transaction>
            pendingTransactionObservable() {
        return web3jRx.pendingTransactionObservable(blockTime);
    }

    @Override
    public Observable<AppBlock> blockObservable(boolean fullTransactionObjects) {
        return web3jRx.blockObservable(fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<AppBlock> replayBlocksObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return web3jRx.replayBlocksObservable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Observable<AppBlock> replayBlocksObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return web3jRx.replayBlocksObservable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Observable<org.web3j.protocol.core.methods.response.Transaction>
            replayTransactionsObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return web3jRx.replayTransactionsObservable(startBlock, endBlock);
    }

    @Override
    public Observable<AppBlock> catchUpToLatestBlockObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Observable<AppBlock> onCompleteObservable) {
        return web3jRx.catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, onCompleteObservable);
    }

    @Override
    public Observable<AppBlock> catchUpToLatestBlockObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return web3jRx.catchUpToLatestBlockObservable(startBlock, fullTransactionObjects);
    }

    @Override
    public Observable<org.web3j.protocol.core.methods.response.Transaction>
            catchUpToLatestTransactionObservable(DefaultBlockParameter startBlock) {
        return web3jRx.catchUpToLatestTransactionObservable(startBlock);
    }

    @Override
    public Observable<AppBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return web3jRx.catchUpToLatestAndSubscribeToNewBlocksObservable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<org.web3j.protocol.core.methods.response.Transaction>
            catchUpToLatestAndSubscribeToNewTransactionsObservable(
            DefaultBlockParameter startBlock) {
        return web3jRx.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                startBlock, blockTime);
    }
}
