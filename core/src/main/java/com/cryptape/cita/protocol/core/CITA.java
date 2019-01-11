package com.cryptape.cita.protocol.core;

import java.math.BigInteger;

import com.cryptape.cita.protocol.core.methods.request.Call;
import com.cryptape.cita.protocol.core.methods.response.AppAccounts;
import com.cryptape.cita.protocol.core.methods.response.AppBlock;
import com.cryptape.cita.protocol.core.methods.response.AppBlockNumber;
import com.cryptape.cita.protocol.core.methods.response.AppCall;
import com.cryptape.cita.protocol.core.methods.response.AppFilter;
import com.cryptape.cita.protocol.core.methods.response.AppGetAbi;
import com.cryptape.cita.protocol.core.methods.response.AppGetBalance;
import com.cryptape.cita.protocol.core.methods.response.AppGetCode;
import com.cryptape.cita.protocol.core.methods.response.AppGetTransactionCount;
import com.cryptape.cita.protocol.core.methods.response.AppGetTransactionReceipt;
import com.cryptape.cita.protocol.core.methods.response.AppLog;
import com.cryptape.cita.protocol.core.methods.response.AppMetaData;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.cryptape.cita.protocol.core.methods.response.AppSign;
import com.cryptape.cita.protocol.core.methods.response.AppTransaction;
import com.cryptape.cita.protocol.core.methods.response.AppUninstallFilter;
import com.cryptape.cita.protocol.core.methods.response.NetPeerCount;


/**
 * Core CITA JSON-RPC API.
 */
public interface CITA {

    Request<?, NetPeerCount> netPeerCount();

    Request<?, AppAccounts> appAccounts();

    Request<?, AppSign> appSign(String address, String sha3HashOfDataToSign);

    Request<?, AppBlockNumber> appBlockNumber();

    Request<?, AppMetaData> appMetaData(DefaultBlockParameter defaultBlockParameter);

    Request<?, AppGetBalance> appGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, AppGetAbi> appGetAbi(String contractAddress,
                                    DefaultBlockParameter defaultBlockParameter);

    Request<?, AppGetTransactionCount> appGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, AppGetCode> appGetCode(String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, AppSendTransaction> appSendRawTransaction(
            String signedTransactionData);

    Request<?, AppCall> appCall(
            Call transaction,
            DefaultBlockParameter defaultBlockParameter);

    Request<?, AppBlock> appGetBlockByHash(String blockHash, boolean returnFullTransactionObjects);

    Request<?, AppBlock> appGetBlockByNumber(
            DefaultBlockParameter defaultBlockParameter,
            boolean returnFullTransactionObjects);

    Request<?, AppTransaction> appGetTransactionByHash(String transactionHash);

    Request<?, AppGetTransactionReceipt> appGetTransactionReceipt(String transactionHash);

    Request<?, AppFilter> appNewFilter(
            com.cryptape.cita.protocol.core.methods.request.AppFilter ethFilter);

    Request<?, AppFilter> appNewBlockFilter();

    Request<?, AppFilter> appNewPendingTransactionFilter();

    Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId);

    Request<?, AppLog> appGetFilterChanges(BigInteger filterId);

    Request<?, AppLog> appGetFilterLogs(BigInteger filterId);

    Request<?, AppLog> appGetLogs(
            com.cryptape.cita.protocol.core.methods.request.AppFilter ethFilter);
}
