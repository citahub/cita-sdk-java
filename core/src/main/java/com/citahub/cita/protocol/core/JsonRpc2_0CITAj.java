package com.citahub.cita.protocol.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import com.citahub.cita.protocol.core.methods.request.Call;
import com.citahub.cita.protocol.core.methods.response.*;
import io.reactivex.Flowable;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.CITAjService;
import com.citahub.cita.protocol.rx.JsonRpc2_0Rx;
import com.citahub.cita.utils.Async;
import com.citahub.cita.utils.Numeric;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0CITAj implements CITAj {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final CITAjService CITAjService;
    private final JsonRpc2_0Rx citajRx;
    private final long blockTime;

    public JsonRpc2_0CITAj(CITAjService CITAjService) {
        this(CITAjService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0CITAj(CITAjService CITAjService, long pollingInterval) {
        this(CITAjService, pollingInterval, Async.defaultExecutorService());
    }

    public JsonRpc2_0CITAj(
            CITAjService CITAjService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        this.CITAjService = CITAjService;
        this.citajRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
    }

    @Override
    public Request<?, NetPeerCount> netPeerCount() {
        return new Request<>(
                "peerCount",
                Collections.<String>emptyList(),
                CITAjService,
                NetPeerCount.class);
    }

    @Override
    public Request<?, NetPeersInfo> netPeersInfo() {
        return new Request<>(
                "peersInfo",
                Collections.<String>emptyList(),
                CITAjService,
                NetPeersInfo.class);
    }

    @Override
    public Request<?, AppVersion> getVersion() {
        return new Request<>(
                "getVersion",
                Collections.<String>emptyList(),
                CITAjService,
                AppVersion.class);
    }


    //2 methods: appAccount, appSign are not used
    //keep them for wallet use in future.

    @Override
    public Request<?, AppAccounts> appAccounts() {
        return new Request<>(
                "accounts",
                Collections.<String>emptyList(),
                CITAjService,
                AppAccounts.class);
    }

    @Override
    public Request<?, AppSign> appSign(String address, String sha3HashOfDataToSign) {
        return new Request<>(
                "sign",
                Arrays.asList(address, sha3HashOfDataToSign),
                CITAjService,
                AppSign.class);
    }

    @Override
    public Request<?, AppMetaData> appMetaData(DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getMetaData",
                Arrays.asList(defaultBlockParameter.getValue()),
                CITAjService,
                AppMetaData.class);
    }

    @Override
    public Request<?, AppBlockNumber> appBlockNumber() {
        return new Request<>(
                "blockNumber",
                Collections.<String>emptyList(),
                CITAjService,
                AppBlockNumber.class);
    }

    @Override
    public Request<?, AppGetBalance> appGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getBalance",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                CITAjService,
                AppGetBalance.class);
    }

    @Override
    public Request<?, AppGetAbi> appGetAbi(
            String contractAddress, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getAbi",
                Arrays.asList(contractAddress, defaultBlockParameter.getValue()),
                CITAjService,
                AppGetAbi.class);
    }


    @Override
    public Request<?, AppGetTransactionCount> appGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getTransactionCount",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                CITAjService,
                AppGetTransactionCount.class);
    }


    @Override
    public Request<?, AppGetCode> appGetCode(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getCode",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                CITAjService,
                AppGetCode.class);
    }

    @Override
    public Request<?, AppSendTransaction> appSendRawTransaction(String signedTransactionData) {
        return new Request<>(
                "sendRawTransaction",
                Arrays.asList(signedTransactionData),
                CITAjService,
                AppSendTransaction.class);
    }

    @Override
    public Request<?, AppCall> appCall(Call call, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "call",
                Arrays.asList(call, defaultBlockParameter),
                CITAjService,
                AppCall.class);
    }


    @Override
    public Request<?, AppBlock> appGetBlockByHash(String blockHash, boolean returnFullTransactionObjects) {
        return new Request<>(
                "getBlockByHash",
                Arrays.asList(
                        blockHash,
                        returnFullTransactionObjects),
                CITAjService,
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
                CITAjService,
                AppBlock.class);
    }

    @Override
    public Request<?, AppTransaction> appGetTransactionByHash(String transactionHash) {
        return new Request<>(
                "getTransaction",
                Arrays.asList(transactionHash),
                CITAjService,
                AppTransaction.class);
    }

    @Override
    public Request<?, AppGetTransactionReceipt> appGetTransactionReceipt(String transactionHash) {
        return new Request<>(
                "getTransactionReceipt",
                Arrays.asList(transactionHash),
                CITAjService,
                AppGetTransactionReceipt.class);
    }

    @Override
    public Request<?, AppFilter> appNewFilter(
            com.citahub.cita.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "newFilter",
                Arrays.asList(appFilter),
                CITAjService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppFilter> appNewBlockFilter() {
        return new Request<>(
                "newBlockFilter",
                Collections.<String>emptyList(),
                CITAjService,
                AppFilter.class);
    }

    public Request<?, AppFilter> appNewPendingTransactionFilter() {
        return new Request<>(
                "newPendingTransactionFilter",
                Collections.<String>emptyList(),
                CITAjService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId) {
        return new Request<>(
                "uninstallFilter",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                CITAjService,
                AppUninstallFilter.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterChanges(BigInteger filterId) {
        return new Request<>(
                "getFilterChanges",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                CITAjService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterLogs(BigInteger filterId) {
        return new Request<>(
                "getFilterLogs",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                CITAjService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetLogs(
            com.citahub.cita.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "getLogs",
                Arrays.asList(appFilter),
                CITAjService,
                AppLog.class);
    }

    @Override
    public Flowable<String> appBlockHashFlowable() {
        return citajRx.appBlockHashFlowable(blockTime);
    }

    @Override
    public Flowable<String> appPendingTransactionHashFlowable() {
        return citajRx.appPendingTransactionHashFlowable(blockTime);
    }

    @Override
    public Flowable<Log> appLogFlowable(
            com.citahub.cita.protocol.core.methods.request.AppFilter appFilter) {
        return citajRx.appLogFlowable(appFilter, blockTime);
    }

    @Override
    public Flowable<Transaction> transactionFlowable() {
        return citajRx.transactionFlowable(blockTime);
    }

    @Override
    public Flowable<Transaction> pendingTransactionFlowable() {
        return citajRx.pendingTransactionFlowable(blockTime);
    }

    @Override
    public Flowable<AppBlock> blockFlowable(boolean fullTransactionObjects) {
        return citajRx.blockFlowable(fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return citajRx.replayBlocksFlowable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return citajRx.replayBlocksFlowable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Flowable<Transaction> replayTransactionsFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return citajRx.replayTransactionsFlowable(startBlock, endBlock);
    }

    @Override
    public Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Flowable<AppBlock> onCompleteFlowable) {
        return citajRx.catchUpToLatestBlockFlowable(
                startBlock, fullTransactionObjects, onCompleteFlowable);
    }

    @Override
    public Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return citajRx.catchUpToLatestBlockFlowable(startBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<Transaction> catchUpToLatestTransactionFlowable(DefaultBlockParameter startBlock) {
        return citajRx.catchUpToLatestTransactionFlowable(startBlock);
    }

    @Override
    public Flowable<AppBlock> catchUpToLatestAndSubscribeToNewBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return citajRx.catchUpToLatestAndSubscribeToNewBlocksFlowable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<Transaction> catchUpToLatestAndSubscribeToNewTransactionsFlowable(
            DefaultBlockParameter startBlock) {
        return citajRx.catchUpToLatestAndSubscribeToNewTransactionsFlowable(
                startBlock, blockTime);
    }
}
