package org.nervos.appchain.protocol.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import io.reactivex.Flowable;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.AppChainjService;
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
import org.nervos.appchain.protocol.rx.JsonRpc2_0Rx;
import org.nervos.appchain.utils.Async;
import org.nervos.appchain.utils.Numeric;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0AppChainj implements AppChainj {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final AppChainjService appChainjService;
    private final JsonRpc2_0Rx appChainjRx;
    private final long blockTime;

    public JsonRpc2_0AppChainj(AppChainjService appChainjService) {
        this(appChainjService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0AppChainj(AppChainjService appChainjService, long pollingInterval) {
        this(appChainjService, pollingInterval, Async.defaultExecutorService());
    }

    public JsonRpc2_0AppChainj(
            AppChainjService appChainjService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        this.appChainjService = appChainjService;
        this.appChainjRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
    }

    @Override
    public Request<?, NetPeerCount> netPeerCount() {
        return new Request<>(
                "peerCount",
                Collections.<String>emptyList(),
                appChainjService,
                NetPeerCount.class);
    }


    //2 methods: appAccount, appSign are not used
    //keep them for wallet use in future.

    @Override
    public Request<?, AppAccounts> appAccounts() {
        return new Request<>(
                "accounts",
                Collections.<String>emptyList(),
                appChainjService,
                AppAccounts.class);
    }

    @Override
    public Request<?, AppSign> appSign(String address, String sha3HashOfDataToSign) {
        return new Request<>(
                "sign",
                Arrays.asList(address, sha3HashOfDataToSign),
                appChainjService,
                AppSign.class);
    }

    @Override
    public Request<?, AppMetaData> appMetaData(DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getMetaData",
                Arrays.asList(defaultBlockParameter.getValue()),
                appChainjService,
                AppMetaData.class);
    }

    @Override
    public Request<?, AppBlockNumber> appBlockNumber() {
        return new Request<>(
                "blockNumber",
                Collections.<String>emptyList(),
                appChainjService,
                AppBlockNumber.class);
    }

    @Override
    public Request<?, AppGetBalance> appGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getBalance",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                appChainjService,
                AppGetBalance.class);
    }

    @Override
    public Request<?, AppGetAbi> appGetAbi(
            String contractAddress, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getAbi",
                Arrays.asList(contractAddress, defaultBlockParameter.getValue()),
                appChainjService,
                AppGetAbi.class);
    }


    @Override
    public Request<?, AppGetTransactionCount> appGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getTransactionCount",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                appChainjService,
                AppGetTransactionCount.class);
    }


    @Override
    public Request<?, AppGetCode> appGetCode(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getCode",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                appChainjService,
                AppGetCode.class);
    }

    @Override
    public Request<?, AppSendTransaction>
    appSendRawTransaction(
            String signedTransactionData) {
        return new Request<>(
                "sendRawTransaction",
                Arrays.asList(signedTransactionData),
                appChainjService,
                AppSendTransaction.class);
    }

    @Override
    public Request<?, AppCall> appCall(
            Call call, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "call",
                Arrays.asList(call, defaultBlockParameter),
                appChainjService,
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
                appChainjService,
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
                appChainjService,
                AppBlock.class);
    }

    @Override
    public Request<?, AppTransaction> appGetTransactionByHash(String transactionHash) {
        return new Request<>(
                "getTransaction",
                Arrays.asList(transactionHash),
                appChainjService,
                AppTransaction.class);
    }

    @Override
    public Request<?, AppGetTransactionReceipt> appGetTransactionReceipt(String transactionHash) {
        return new Request<>(
                "getTransactionReceipt",
                Arrays.asList(transactionHash),
                appChainjService,
                AppGetTransactionReceipt.class);
    }

    @Override
    public Request<?, AppFilter> appNewFilter(
            org.nervos.appchain.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "newFilter",
                Arrays.asList(appFilter),
                appChainjService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppFilter> appNewBlockFilter() {
        return new Request<>(
                "newBlockFilter",
                Collections.<String>emptyList(),
                appChainjService,
                AppFilter.class);
    }

    public Request<?, AppFilter> appNewPendingTransactionFilter() {
        return new Request<>(
                "newPendingTransactionFilter",
                Collections.<String>emptyList(),
                appChainjService,
                AppFilter.class);
    }

    @Override
    public Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId) {
        return new Request<>(
                "uninstallFilter",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                appChainjService,
                AppUninstallFilter.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterChanges(BigInteger filterId) {
        return new Request<>(
                "getFilterChanges",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                appChainjService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetFilterLogs(BigInteger filterId) {
        return new Request<>(
                "getFilterLogs",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                appChainjService,
                AppLog.class);
    }

    @Override
    public Request<?, AppLog> appGetLogs(
            org.nervos.appchain.protocol.core.methods.request.AppFilter appFilter) {
        return new Request<>(
                "getLogs",
                Arrays.asList(appFilter),
                appChainjService,
                AppLog.class);
    }

    @Override
    public Flowable<String> appBlockHashFlowable() {
        return appChainjRx.appBlockHashFlowable(blockTime);
    }

    @Override
    public Flowable<String> appPendingTransactionHashFlowable() {
        return appChainjRx.appPendingTransactionHashFlowable(blockTime);
    }

    @Override
    public Flowable<Log> appLogFlowable(
            org.nervos.appchain.protocol.core.methods.request.AppFilter appFilter) {
        return appChainjRx.appLogFlowable(appFilter, blockTime);
    }

    @Override
    public Flowable<Transaction>
    transactionFlowable() {
        return appChainjRx.transactionFlowable(blockTime);
    }

    @Override
    public Flowable<Transaction>
    pendingTransactionFlowable() {
        return appChainjRx.pendingTransactionFlowable(blockTime);
    }

    @Override
    public Flowable<AppBlock> blockFlowable(boolean fullTransactionObjects) {
        return appChainjRx.blockFlowable(fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return appChainjRx.replayBlocksFlowable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return appChainjRx.replayBlocksFlowable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Flowable<Transaction>
    replayTransactionsFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return appChainjRx.replayTransactionsFlowable(startBlock, endBlock);
    }

    @Override
    public Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Flowable<AppBlock> onCompleteFlowable) {
        return appChainjRx.catchUpToLatestBlockFlowable(
                startBlock, fullTransactionObjects, onCompleteFlowable);
    }

    @Override
    public Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return appChainjRx.catchUpToLatestBlockFlowable(startBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<Transaction>
    catchUpToLatestTransactionFlowable(DefaultBlockParameter startBlock) {
        return appChainjRx.catchUpToLatestTransactionFlowable(startBlock);
    }

    @Override
    public Flowable<AppBlock> catchUpToLatestAndSubscribeToNewBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return appChainjRx.catchUpToLatestAndSubscribeToNewBlocksFlowable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<Transaction>
    catchUpToLatestAndSubscribeToNewTransactionsFlowable(
            DefaultBlockParameter startBlock) {
        return appChainjRx.catchUpToLatestAndSubscribeToNewTransactionsFlowable(
                startBlock, blockTime);
    }
}
