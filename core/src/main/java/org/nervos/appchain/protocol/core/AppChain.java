package org.nervos.appchain.protocol.core;

import java.math.BigInteger;

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
import org.nervos.appchain.protocol.core.methods.response.NetPeerCount;
import org.nervos.appchain.protocol.core.methods.response.Web3ClientVersion;
import org.nervos.appchain.protocol.core.methods.response.Web3Sha3;


/**
 * Core Ethereum JSON-RPC API.
 */
public interface AppChain {
    Request<?, Web3ClientVersion> web3ClientVersion();

    Request<?, Web3Sha3> web3Sha3(String data);

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
            org.nervos.appchain.protocol.core.methods.request.AppFilter ethFilter);

    Request<?, AppFilter> appNewBlockFilter();

    Request<?, AppFilter> appNewPendingTransactionFilter();

    Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId);

    Request<?, AppLog> appGetFilterChanges(BigInteger filterId);

    Request<?, AppLog> appGetFilterLogs(BigInteger filterId);

    Request<?, AppLog> appGetLogs(
            org.nervos.appchain.protocol.core.methods.request.AppFilter ethFilter);
}
