package com.citahub.cita.protocol.rx;

import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.methods.request.AppFilter;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.Log;
import io.reactivex.Flowable;
import com.citahub.cita.protocol.core.methods.response.Transaction;

/**
 * The Flowables JSON-RPC client event API.
 */
public interface CITAjRx {

    /**
     * Create an Flowable to filter for specific log events on the blockchain.
     *
     * @param ethFilter filter criteria
     * @return Flowable that emits all Log events matching the filter
     */
    Flowable<Log> appLogFlowable(AppFilter ethFilter);

    /**
     * Create an Flowable to emit block hashes.
     *
     * @return Flowable that emits all new block hashes as new blocks are created on the
     *         blockchain
     */
    Flowable<String> appBlockHashFlowable();

    /**
     * Create an Flowable to emit pending transactions, i.e. those transactions that have been
     * submitted by a node, but don't yet form part of a block (haven't been mined yet).
     *
     * @return Flowable to emit pending transaction hashes.
     */
    Flowable<String> appPendingTransactionHashFlowable();

    /**
     * Create an Flowable to emit all new transactions as they are confirmed on the blockchain.
     * i.e. they have been mined and are incorporated into a block.
     *
     * @return Flowable to emit new transactions on the blockchain
     */
    Flowable<Transaction> transactionFlowable();

    /**
     * Create an Flowable to emit all pending transactions that have yet to be placed into a
     * block on the blockchain.
     *
     * @return Flowable to emit pending transactions
     */
    Flowable<Transaction> pendingTransactionFlowable();

    /**
     * Create an Flowable that emits newly created blocks on the blockchain.
     *
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *                              transaction hashes
     * @return Flowable that emits all new blocks as they are added to the blockchain
     */
    Flowable<AppBlock> blockFlowable(boolean fullTransactionObjects);

    /**
     * Create an Flowable that emits all blocks from the blockchain contained within the
     * requested range.
     *
     * @param startBlock block number to commence with
     * @param endBlock block number to finish with
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *                               transaction hashes
     * @return Flowable to emit these blocks
     */
    Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects);

    /**
     * Create an Flowable that emits all blocks from the blockchain contained within the
     * requested range.
     *
     * @param startBlock block number to commence with
     * @param endBlock block number to finish with
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *                               transaction hashes
     * @param ascending if true, emits blocks in ascending order between range, otherwise
     *                  in descending order
     * @return Flowable to emit these blocks
     */
    Flowable<AppBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending);

    /**
     * Create an Flowable that emits all transactions from the blockchain contained within the
     * requested range.
     *
     * @param startBlock block number to commence with
     * @param endBlock block number to finish with
     * @return Flowable to emit these transactions in the order they appear in the blocks
     */
    Flowable<Transaction> replayTransactionsFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock);

    /**
     * Create an Flowable that emits all transactions from the blockchain starting with a
     * provided block number. Once it has replayed up to the most current block, the provided
     * Flowable is invoked.
     *
     * <p>To automatically subscribe to new blocks, use
     * {@link #catchUpToLatestAndSubscribeToNewBlocksFlowable(DefaultBlockParameter, boolean)}.
     *
     * @param startBlock the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *                              in the {@link AppBlock} responses
     * @param onCompleteFlowable a subsequent Flowable that we wish to run once we are caught
     *                             up with the latest block
     * @return Flowable to emit all requested blocks
     */
    Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Flowable<AppBlock> onCompleteFlowable);

    /**
     * Creates an Flowable that emits all blocks from the requested block number to the most
     * current. Once it has emitted the most current block, onComplete is called.
     *
     * @param startBlock the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *                               in the {@link AppBlock} responses
     * @return Flowable to emit all requested blocks
     */
    Flowable<AppBlock> catchUpToLatestBlockFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects);

    /**
     * Creates an Flowable that emits all transactions from the requested block number to the most
     * current. Once it has emitted the most current block's transactions, onComplete is called.
     *
     * @param startBlock the block number we wish to request from
     * @return Flowable to emit all requested transactions
     */
    Flowable<Transaction> catchUpToLatestTransactionFlowable(
            DefaultBlockParameter startBlock);

    /**
     * Creates an Flowable that emits all blocks from the requested block number to the most
     * current. Once it has emitted the most current block, it starts emitting new blocks as they
     * are created.
     *
     * @param startBlock the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *                               in the {@link AppBlock} responses
     * @return Flowable to emit all requested blocks and future
     */
    Flowable<AppBlock> catchUpToLatestAndSubscribeToNewBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects);

    /**
     * As per
     * {@link #catchUpToLatestAndSubscribeToNewBlocksFlowable(DefaultBlockParameter, boolean)},
     * except that all transactions contained within the blocks are emitted.
     *
     * @param startBlock the block number we wish to request from
     * @return Flowable to emit all requested transactions and future
     */
    Flowable<Transaction> catchUpToLatestAndSubscribeToNewTransactionsFlowable(
            DefaultBlockParameter startBlock);
}
