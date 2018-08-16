package org.nervos.appchain.protocol.rx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.nervos.appchain.protocol.Nervosj;
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
import org.nervos.appchain.utils.Observables;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * nervosj reactive API implementation.
 */
public class JsonRpc2_0Rx {

    private final Nervosj nervosj;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Scheduler scheduler;

    public JsonRpc2_0Rx(Nervosj nervosj, ScheduledExecutorService scheduledExecutorService) {
        this.nervosj = nervosj;
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduler = Schedulers.from(scheduledExecutorService);
    }

    public Observable<String> appBlockHashObservable(final long pollingInterval) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                BlockFilter blockFilter = new BlockFilter(
                        nervosj, new Callback<String>() {
                    @Override
                    public void onEvent(String value) {
                        subscriber.onNext(value);
                    }
                });
                JsonRpc2_0Rx.this.run(blockFilter, subscriber, pollingInterval);
            }
        });
    }

    public Observable<String> appPendingTransactionHashObservable(final long pollingInterval) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                PendingTransactionFilter pendingTransactionFilter = new PendingTransactionFilter(
                        nervosj, new Callback<String>() {
                    @Override
                    public void onEvent(String value) {
                        subscriber.onNext(value);
                    }
                });
                JsonRpc2_0Rx.this.run(pendingTransactionFilter, subscriber, pollingInterval);
            }
        });
    }

    public Observable<Log> appLogObservable(
            final AppFilter appFilter, final long pollingInterval) {
        return Observable.create(new Observable.OnSubscribe<Log>() {
            @Override
            public void call(final Subscriber<? super Log> subscriber) {
                LogFilter logFilter = new LogFilter(
                        nervosj, new Callback<Log>() {
                    @Override
                    public void onEvent(Log value) {
                        subscriber.onNext(value);
                    }
                }, appFilter);

                run(logFilter, subscriber, pollingInterval);
            }
        });
    }

    private <T> void run(
            final Filter<T> filter, Subscriber<? super T> subscriber,
            final long pollingInterval) {

        filter.run(scheduledExecutorService, pollingInterval);
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                filter.cancel();
            }
        }));
    }

    public Observable<Transaction> transactionObservable(final long pollingInterval) {
        return blockObservable(true, pollingInterval)
                .flatMapIterable(new Func1<AppBlock, Iterable<? extends Transaction>>() {
                    @Override
                    public Iterable<? extends Transaction> call(final AppBlock appBlock) {
                        return JsonRpc2_0Rx.this.toTransactions(appBlock);
                    }
                });
    }

    public Observable<Transaction> pendingTransactionObservable(long pollingInterval) {
        return appPendingTransactionHashObservable(pollingInterval)
                .flatMap(new Func1<String, Observable<AppTransaction>>() {
                    @Override
                    public Observable<AppTransaction> call(final String transactionHash) {
                        return nervosj.appGetTransactionByHash(transactionHash).observable();
                    }
                })
                .map(new Func1<AppTransaction, Transaction>() {
                    @Override
                    public Transaction call(AppTransaction appTransaction) {
                        return appTransaction.getTransaction();
                    }
                });
    }

    public Observable<AppBlock> blockObservable(
            final boolean fullTransactionObjects, long pollingInterval) {
        return appBlockHashObservable(pollingInterval)
                .flatMap(new Func1<String, Observable<? extends AppBlock>>() {
                    @Override
                    public Observable<? extends AppBlock> call(final String blockHash) {
                        return nervosj.appGetBlockByHash(blockHash, fullTransactionObjects).observable();
                    }
                });
    }

    public Observable<AppBlock> replayBlocksObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksObservable(startBlock, endBlock, fullTransactionObjects, true);
    }

    public Observable<AppBlock> replayBlocksObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        // We use a scheduler to ensure this Observable runs asynchronously for users to be
        // consistent with the other Observables
        return replayBlocksObservableSync(startBlock, endBlock, fullTransactionObjects, ascending)
                .subscribeOn(scheduler);
    }

    private Observable<AppBlock> replayBlocksObservableSync(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksObservableSync(startBlock, endBlock, fullTransactionObjects, true);
    }

    private Observable<AppBlock> replayBlocksObservableSync(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            final boolean fullTransactionObjects, boolean ascending) {

        BigInteger startBlockNumber = null;
        BigInteger endBlockNumber = null;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            endBlockNumber = getBlockNumber(endBlock);
        } catch (IOException e) {
            Observable.error(e);
        }

        if (ascending) {
            return Observables.range(startBlockNumber, endBlockNumber)
                    .flatMap(new Func1<BigInteger, Observable<? extends AppBlock>>() {
                        @Override
                        public Observable<? extends AppBlock> call(BigInteger i) {
                            return nervosj.appGetBlockByNumber(
                                    new DefaultBlockParameterNumber(i),
                                    fullTransactionObjects).observable();
                        }
                    });
        } else {
            return Observables.range(startBlockNumber, endBlockNumber, false)
                    .flatMap(new Func1<BigInteger, Observable<? extends AppBlock>>() {
                        @Override
                        public Observable<? extends AppBlock> call(BigInteger i) {
                            return nervosj.appGetBlockByNumber(
                                    new DefaultBlockParameterNumber(i),
                                    fullTransactionObjects).observable();
                        }
                    });
        }
    }

    public Observable<Transaction> replayTransactionsObservable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return replayBlocksObservable(startBlock, endBlock, true)
                .flatMapIterable(new Func1<AppBlock, Iterable<? extends Transaction>>() {
                    @Override
                    public Iterable<? extends Transaction> call(AppBlock appBlock) {
                        return toTransactions(appBlock);
                    }
                });
    }

    public Observable<AppBlock> catchUpToLatestBlockObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Observable<AppBlock> onCompleteObservable) {
        // We use a scheduler to ensure this Observable runs asynchronously for users to be
        // consistent with the other Observables
        return catchUpToLatestBlockObservableSync(
                startBlock, fullTransactionObjects, onCompleteObservable)
                .subscribeOn(scheduler);
    }

    public Observable<AppBlock> catchUpToLatestBlockObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, Observable.<AppBlock>empty());
    }

    private Observable<AppBlock> catchUpToLatestBlockObservableSync(
            DefaultBlockParameter startBlock, final boolean fullTransactionObjects,
            final Observable<AppBlock> onCompleteObservable) {

        BigInteger startBlockNumber;
        final BigInteger latestBlockNumber;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            latestBlockNumber = getLatestBlockNumber();
        } catch (IOException e) {
            return Observable.error(e);
        }

        if (startBlockNumber.compareTo(latestBlockNumber) > -1) {
            return onCompleteObservable;
        } else {
            return Observable.concat(
                    replayBlocksObservableSync(
                            new DefaultBlockParameterNumber(startBlockNumber),
                            new DefaultBlockParameterNumber(latestBlockNumber),
                            fullTransactionObjects),
                    Observable.defer(new Func0<Observable<AppBlock>>() {
                        @Override
                        public Observable<AppBlock> call() {
                            return JsonRpc2_0Rx.this.catchUpToLatestBlockObservableSync(
                                    new DefaultBlockParameterNumber(latestBlockNumber.add(BigInteger.ONE)),
                                    fullTransactionObjects,
                                    onCompleteObservable
                            );
                        }
                    }));
        }
    }

    public Observable<Transaction> catchUpToLatestTransactionObservable(
            final DefaultBlockParameter startBlock) {
        return catchUpToLatestBlockObservable(
                startBlock, true, Observable.<AppBlock>empty())
                .flatMapIterable(new Func1<AppBlock, Iterable<? extends Transaction>>() {
                    @Override
                    public Iterable<? extends Transaction> call(AppBlock appBlock) {
                        return toTransactions(appBlock);
                    }
                });
    }

    public Observable<AppBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            long pollingInterval) {

        return catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects,
                blockObservable(fullTransactionObjects, pollingInterval));
    }

    public Observable<Transaction> catchUpToLatestAndSubscribeToNewTransactionsObservable(
            DefaultBlockParameter startBlock, long pollingInterval) {
        return catchUpToLatestAndSubscribeToNewBlocksObservable(
                startBlock, true, pollingInterval)
                .flatMapIterable(new Func1<AppBlock, Iterable<? extends Transaction>>() {
                    @Override
                    public Iterable<? extends Transaction> call(AppBlock appBlock) {
                        return toTransactions(appBlock);
                    }
                });
    }

    private BigInteger getLatestBlockNumber() throws IOException {
        return getBlockNumber(DefaultBlockParameterName.LATEST);
    }

    private BigInteger getBlockNumber(
            DefaultBlockParameter defaultBlockParameter) throws IOException {
        if (defaultBlockParameter instanceof DefaultBlockParameterNumber) {
            return ((DefaultBlockParameterNumber) defaultBlockParameter).getBlockNumber();
        } else {
            AppBlock latestEthBlock = nervosj.appGetBlockByNumber(
                    defaultBlockParameter, false).send();
            return latestEthBlock.getBlock().getHeader().getNumberDec();
        }
    }

    private static List<Transaction> toTransactions(AppBlock appBlock) {
        // If you ever see an exception thrown here, it's probably due to an incomplete chain in
        // Geth/Parity. You should resync to solve.
        List<AppBlock.TransactionObject> transactionResults = appBlock.getBlock().getBody().getTransactions();
        List<Transaction> transactions = new ArrayList<Transaction>(transactionResults.size());
        for (AppBlock.TransactionObject transactionResult: transactionResults) {
            transactions.add((Transaction) transactionResult.get());
        }
        return transactions;
    }
}
