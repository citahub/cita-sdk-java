package org.nervos.appchain.protocol.rx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Scheduler;

import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.DefaultBlockParameterNumber;
import org.nervos.appchain.protocol.core.filters.BlockFilter;
import org.nervos.appchain.protocol.core.filters.Callback;
import org.nervos.appchain.protocol.core.filters.Filter;
import org.nervos.appchain.protocol.core.filters.LogFilter;
import org.nervos.appchain.protocol.core.filters.PendingTransactionFilter;
import org.nervos.appchain.protocol.core.methods.request.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.protocol.core.methods.response.AppTransaction;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.Transaction;
import org.nervos.appchain.utils.Flowables;
import org.reactivestreams.Publisher;

/**
 * appChainj reactive API implementation.
 */
public class JsonRpc2_0Rx {

    private final AppChainj appChainj;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Scheduler scheduler;

    public JsonRpc2_0Rx(AppChainj appChainj, ScheduledExecutorService scheduledExecutorService) {
        this.appChainj = appChainj;
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduler = Schedulers.from(scheduledExecutorService);
    }

    public Flowable<String> appBlockHashFlowable(long pollingInterval) {
        return Flowable.create(subscriber -> {
            BlockFilter blockFilter = new BlockFilter(appChainj, new Callback<String>() {
                @Override
                public void onEvent(final String value) {
                    subscriber.onNext(value);
                }
            });
            JsonRpc2_0Rx.this.run(blockFilter, subscriber, pollingInterval);
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<String> appPendingTransactionHashFlowable(long pollingInterval) {
        return Flowable.create(subscriber -> {
            PendingTransactionFilter pendingTransactionFilter = new PendingTransactionFilter(
                    appChainj, subscriber::onNext);

            run(pendingTransactionFilter, subscriber, pollingInterval);
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<Log> appLogFlowable(AppFilter appFilter, long pollingInterval) {
        return Flowable.create(new FlowableOnSubscribe<Log>() {
            @Override
            public void subscribe(FlowableEmitter<Log> emitter) throws Exception {
                final LogFilter logFilter = new LogFilter(appChainj, new Callback<Log>() {
                    @Override
                    public void onEvent(Log value) {
                        emitter.onNext(value);
                    }
                }, appFilter);
                run(logFilter, emitter, pollingInterval);
            }
        }, BackpressureStrategy.BUFFER);
    }

    private <T> void run(
            Filter<T> filter, FlowableEmitter<? super T> emitter,
            long pollingInterval) {

        filter.run(scheduledExecutorService, pollingInterval);
        emitter.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                filter.cancel();
            }
        });
    }

    public Flowable<Transaction> transactionFlowable(long pollingInterval) {
        return blockFlowable(true, pollingInterval)
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<Transaction> pendingTransactionFlowable(long pollingInterval) {
        return appPendingTransactionHashFlowable(pollingInterval)
                .flatMap(new Function<String, Publisher<AppTransaction>>() {
                    @Override
                    public Publisher<AppTransaction> apply(String transactionHash) throws Exception {
                        return appChainj.appGetTransactionByHash(transactionHash).flowable();
                    }
                })
                .filter(new Predicate<AppTransaction>() {
                    @Override
                    public boolean test(AppTransaction ethTransaction) throws Exception {
                        return ethTransaction.getTransaction() != null;
                    }
                })
                .map(new Function<AppTransaction, Transaction>() {
                    @Override
                    public Transaction apply(AppTransaction ethTransaction) throws Exception {
                        return ethTransaction.getTransaction();
                    }
                });
    }

    public Flowable<AppBlock> blockFlowable(
            boolean fullTransactionObjects, long pollingInterval) {
        return appBlockHashFlowable(pollingInterval)
                .flatMap(blockHash ->
                        appChainj.appGetBlockByHash(
                                blockHash, fullTransactionObjects).flowable());
    }

    public Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksFlowable(startBlock, endBlock, fullTransactionObjects, true);
    }

    public Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        // We use a scheduler to ensure this Flowable runs asynchronously for users to be
        // consistent with the other Flowables
        return replayBlocksFlowableSync(startBlock, endBlock, fullTransactionObjects, ascending)
                .subscribeOn(scheduler);
    }

    private Flowable<AppBlock> replayBlocksFlowableSync(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksFlowableSync(startBlock, endBlock, fullTransactionObjects, true);
    }

    private Flowable<AppBlock> replayBlocksFlowableSync(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {

        BigInteger startBlockNumber = null;
        BigInteger endBlockNumber = null;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            endBlockNumber = getBlockNumber(endBlock);
        } catch (IOException e) {
            Flowable.error(e);
        }

        if (ascending) {
            return Flowables.range(startBlockNumber, endBlockNumber)
                    .flatMap(i -> appChainj.appGetBlockByNumber(
                            new DefaultBlockParameterNumber(i),
                            fullTransactionObjects).flowable());
        } else {
            return Flowables.range(startBlockNumber, endBlockNumber, false)
                    .flatMap(i -> appChainj.appGetBlockByNumber(
                            new DefaultBlockParameterNumber(i),
                            fullTransactionObjects).flowable());
        }
    }

    public Flowable<Transaction> replayTransactionsFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return replayBlocksFlowable(startBlock, endBlock, true)
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Flowable<AppBlock> onCompleteFlowable) {
        // We use a scheduler to ensure this Flowable runs asynchronously for users to be
        // consistent with the other Flowables
        return catchUpToLatestBlockFlowableSync(
                startBlock, fullTransactionObjects, onCompleteFlowable)
                .subscribeOn(scheduler);
    }

    public Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return catchUpToLatestBlockFlowable(
                startBlock, fullTransactionObjects, Flowable.empty());
    }

    private Flowable<AppBlock> catchUpToLatestBlockFlowableSync(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Flowable<AppBlock> onCompleteFlowable) {

        BigInteger startBlockNumber;
        BigInteger latestBlockNumber;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            latestBlockNumber = getLatestBlockNumber();
        } catch (IOException e) {
            return Flowable.error(e);
        }

        if (startBlockNumber.compareTo(latestBlockNumber) > -1) {
            return onCompleteFlowable;
        } else {
            return Flowable.concat(
                    replayBlocksFlowableSync(
                            new DefaultBlockParameterNumber(startBlockNumber),
                            new DefaultBlockParameterNumber(latestBlockNumber),
                            fullTransactionObjects),
                    Flowable.defer(() -> catchUpToLatestBlockFlowableSync(
                            new DefaultBlockParameterNumber(latestBlockNumber.add(BigInteger.ONE)),
                            fullTransactionObjects,
                            onCompleteFlowable)));
        }
    }

    public Flowable<Transaction> catchUpToLatestTransactionFlowable(
            DefaultBlockParameter startBlock) {
        return catchUpToLatestBlockFlowable(
                startBlock, true, Flowable.empty())
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<AppBlock> catchUpToLatestAndSubscribeToNewBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            long pollingInterval) {

        return catchUpToLatestBlockFlowable(
                startBlock, fullTransactionObjects,
                blockFlowable(fullTransactionObjects, pollingInterval));
    }

    public Flowable<Transaction> catchUpToLatestAndSubscribeToNewTransactionsFlowable(
            DefaultBlockParameter startBlock, long pollingInterval) {
        return catchUpToLatestAndSubscribeToNewBlocksFlowable(
                startBlock, true, pollingInterval)
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    private BigInteger getLatestBlockNumber() throws IOException {
        return getBlockNumber(DefaultBlockParameterName.PENDING);
    }

    private BigInteger getBlockNumber(
            DefaultBlockParameter defaultBlockParameter) throws IOException {
        if (defaultBlockParameter instanceof DefaultBlockParameterNumber) {
            return ((DefaultBlockParameterNumber) defaultBlockParameter).getBlockNumber();
        } else {
            AppBlock latestEthBlock = appChainj.appGetBlockByNumber(
                    defaultBlockParameter, false).send();
            return latestEthBlock.getBlock().getHeader().getNumberDec();
        }
    }

    private static List<Transaction> toTransactions(AppBlock appBlock) {
        // If you ever see an exception thrown here, it's probably due to an incomplete chain in
        // Geth/Parity. You should resync to solve.
        List<AppBlock.TransactionObject> transactionResults = appBlock.getBlock().getBody().getTransactions();
        List<Transaction> transactions = new ArrayList<Transaction>(transactionResults.size());

        for (AppBlock.TransactionResult transactionResult : transactionResults) {
            transactions.add((Transaction) transactionResult.get());
        }
        return transactions;
    }
}
