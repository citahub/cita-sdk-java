package org.nervos.appchain.protocol.system;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.FunctionReturnDecoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.DynamicArray;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.Uint;
import org.nervos.appchain.abi.datatypes.generated.Uint64;
import org.nervos.appchain.abi.datatypes.generated.Uint8;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.utils.Convert;

import static org.nervos.appchain.protocol.system.Util.addUpTo64Hex;
import static org.nervos.appchain.protocol.system.Util.getNonce;
import static org.nervos.appchain.protocol.system.Util.getValidUtilBlock;

/**
 * TODO: code refactor
 * messy, refactor it later.
 * **/

public class AppChainjSystemContract implements AppChainSystemContract, AppChainSystemAddress {
    private AppChainj service;

    public AppChainjSystemContract(AppChainj service) {
        this.service = service;
    }

    public List<String> listNode(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(NODE_MANAGER_LIST_NODE);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR, callData, service);
        List<Type> resultTypes = FunctionReturnDecoder.decode(
                callResult.getValue(),
                AppChainSystemContract.convert(
                        Arrays.asList(new TypeReference<DynamicArray<Address>>() {})
                )
        );
        ArrayList<Address> results =
                (ArrayList<Address>) resultTypes.get(0).getValue();
        return results.stream()
                .map(Address::getValue)
                .collect(Collectors.toList());
    }

    public int getStatus(String from) throws IOException {
        Function callFunc = new Function(
                NODE_MANAGER_GET_STATUS,
                Arrays.asList(new Address(from)),
                Collections.emptyList());
        FunctionEncoder.encode(callFunc);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR,
                FunctionEncoder.encode(callFunc), service);
        List<Type> resultTypes = FunctionReturnDecoder.decode(
                callResult.getValue(),
                AppChainSystemContract.convert(
                        Arrays.asList(new TypeReference<Uint8>() {})
                ));
        if (resultTypes.get(0) == null) {
            throw new NullPointerException(
                    "No info for address: " + from
                            + ". Maybe it is not in correct format.");
        }
        return Integer.parseInt(resultTypes.get(0).getValue().toString());
    }

    public List<BigInteger> listStake(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(NODE_MANAGER_LIST_STAKE);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR, callData, service);
        List<Type> resultTypes = FunctionReturnDecoder.decode(
                callResult.getValue(),
                AppChainSystemContract.convert(
                        Arrays.asList(
                                new TypeReference<DynamicArray<Uint64>>() {})
                )
        );
        ArrayList<Uint64> results =
                (ArrayList<Uint64>) resultTypes.get(0).getValue();
        return results.stream()
                .map(Uint64::getValue)
                .collect(Collectors.toList());
    }

    public int stakePermillage(String from) throws IOException {
        Function callFunc = new Function(
                NODE_MANAGER_STAKE_PERMILLAGE,
                Arrays.asList(new Address(from)),
                Collections.emptyList());
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR,
                FunctionEncoder.encode(callFunc), service);
        List<Type> resultTypes = FunctionReturnDecoder.decode(callResult.getValue(),
                AppChainSystemContract.convert(
                        Arrays.asList(new TypeReference<Uint64>() {})
                ));
        return Integer.parseInt(resultTypes.get(0).getValue().toString());
    }

    public int getQuotaPrice(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(PRICE_MANAGER_GET_QUOTA_PRICE);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, PRICE_MANAGER_ADDR, callData, service);
        List<Type> resultTypes =
                FunctionReturnDecoder.decode(callResult.getValue(),
                        AppChainSystemContract.convert(
                                Arrays.asList(new TypeReference<Uint>() {})
                        )
                );
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public boolean approveNode(
            String nodeAddr, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        Function func = new Function(
                NODE_MANAGER_APPROVE_NODE,
                Arrays.asList(new Address(nodeAddr)),
                Collections.emptyList());
        String funcData = FunctionEncoder.encode(func);
        Long validUtilBlock = getValidUtilBlock(service).longValue();

        //send tx to approve node
        Transaction tx = new Transaction(
                NODE_MANAGER_ADDR, getNonce(), 10000000,
                validUtilBlock, version, chainId, "0", funcData);
        String signedTx = tx.sign(adminPrivatekey);
        AppSendTransaction appTx = service.appSendRawTransaction(signedTx).send();

        //1. check tx validated
        if (appTx.getError() != null) {
            String message = appTx.getError().getMessage();
            System.out.println(
                    "Failed to add approve node(" + nodeAddr
                            + "). Error message: " + message);
            return false;
        }
        String txHash = appTx.getSendTransactionResult().getHash();

        //2. check receipt error and logs
        int count = 0;
        while (true) {
            Optional<TransactionReceipt> receipt = service
                    .appGetTransactionReceipt(txHash)
                    .send().getTransactionReceipt();
            if (receipt.isPresent()) {
                TransactionReceipt txReceipt = receipt.get();
                if (txReceipt.getErrorMessage() != null) {
                    return false;
                }
                List<Log> logs = txReceipt.getLogs();
                String toCompare = addUpTo64Hex(nodeAddr);
                return logs.get(0).getTopics().contains(toCompare);
            } else {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count > 5) {
                    break;
                }
            }
        }
        return false;
    }

    public boolean deleteNode(
            String nodeAddr, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {

        Function func = new Function(
                NODE_MANAGER_DELETE_NODE,
                Arrays.asList(new Address(nodeAddr)),
                Collections.emptyList());

        String funcData = FunctionEncoder.encode(func);

        Long validUtilBlock = getValidUtilBlock(service).longValue();
        Transaction tx = new Transaction(
                NODE_MANAGER_ADDR, getNonce(), 10000000, validUtilBlock,
                version, chainId, "0", funcData);
        String signedTx = tx.sign(adminPrivatekey);

        //send tx to delete node.
        AppSendTransaction appSendTransaction =
                service.appSendRawTransaction(signedTx).send();
        if (appSendTransaction.getError() != null) {
            String message = appSendTransaction.getError().getMessage();
            System.out.println(
                    "Failed to add delete node(" + nodeAddr
                            + "). Error message: " + message);
            return false;
        }
        String txHash = appSendTransaction
                .getSendTransactionResult().getHash();
        int count = 0;
        while (true) {
            Optional<TransactionReceipt> receipt = service
                    .appGetTransactionReceipt(txHash)
                    .send().getTransactionReceipt();
            if (receipt.isPresent()) {
                TransactionReceipt txReceipt = receipt.get();
                if (txReceipt.getErrorMessage() != null) {
                    return false;
                }
                List<Log> logs = txReceipt.getLogs();
                String toCompare = addUpTo64Hex(nodeAddr);
                return logs.get(0).getTopics().contains(toCompare);
            } else {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count > 5) {
                    break;
                }
            }
        }
        return false;
    }

    public boolean setStake(
            String nodeAddr, int stake, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {

        Function func = new Function(
                NODE_MANAGER_SET_STAKE,
                Arrays.asList(new Address(nodeAddr), new Uint64(stake)),
                Collections.emptyList());
        String funcData = FunctionEncoder.encode(func);

        Long validUtilBlock = getValidUtilBlock(service).longValue();
        Transaction tx = new Transaction(
                NODE_MANAGER_ADDR, getNonce(), 10000000, validUtilBlock,
                version, chainId, "0", funcData);
        String rawTx = tx.sign(adminPrivatekey);

        AppSendTransaction appSendTransaction =
                service.appSendRawTransaction(rawTx).send();

        if (appSendTransaction.getError() != null) {
            String message = appSendTransaction.getError().getMessage();
            System.out.println(
                    "Failed to set stake " + stake + " for node("
                            + nodeAddr + "). Error message: " + message);
            return false;
        }

        String txHash = appSendTransaction.getSendTransactionResult().getHash();

        int count = 0;
        while (true) {
            Optional<TransactionReceipt> receipt = service
                    .appGetTransactionReceipt(txHash)
                    .send().getTransactionReceipt();
            if (receipt.isPresent()) {
                TransactionReceipt txReceipt = receipt.get();
                if (txReceipt.getErrorMessage() != null) {
                    return false;
                }
                List<Log> logs = txReceipt.getLogs();
                String nodeAddrToCompare = addUpTo64Hex(nodeAddr);
                return logs.get(0).getTopics().contains(nodeAddrToCompare);
            } else {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count > 5) {
                    break;
                }
            }
        }
        return false;
    }

    public boolean setBql(
            BigInteger bqlToSet, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        Function func = new Function(
                QUOTA_MANAGER_SET_BQL,
                Arrays.asList(new Uint(bqlToSet)),
                Collections.emptyList());

        String funcData = FunctionEncoder.encode(func);

        Long validUtilBlock = getValidUtilBlock(service).longValue();
        Transaction tx = new Transaction(
                QUOTA_MANAGER_ADDR, getNonce(), 10000000, validUtilBlock,
                version, chainId, "0", funcData);

        String rawTx = tx.sign(adminPrivatekey);

        AppSendTransaction appSendTransaction =
                service.appSendRawTransaction(rawTx).send();

        if (appSendTransaction.getError() != null) {
            String message = appSendTransaction.getError().getMessage();
            System.out.println(
                    "Failed to set bql " + bqlToSet + ". Error message: " + message);
            return false;
        }
        String txHash = appSendTransaction.getSendTransactionResult().getHash();

        int count = 0;

        while (true) {
            Optional<TransactionReceipt> receipt = service
                    .appGetTransactionReceipt(txHash)
                    .send().getTransactionReceipt();
            if (receipt.isPresent()) {
                TransactionReceipt txReceipt = receipt.get();
                if (txReceipt.getErrorMessage() != null) {
                    return false;
                }
                List<Log> logs = txReceipt.getLogs();
                String bqlToCompare = bqlToSet.toString(16);
                bqlToCompare = addUpTo64Hex(bqlToCompare);
                return logs.get(0).getTopics().contains(bqlToCompare);
            } else {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count > 5) {
                    break;
                }
            }
        }
        return false;
    }

    public boolean setDefaultAql(
            BigInteger defaultAqlToSet, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        Function func = new Function(
                QUOTA_MANAGER_SET_DEFAULT_AQL,
                Arrays.asList(new Uint(defaultAqlToSet)),
                Collections.emptyList());

        String funcData = FunctionEncoder.encode(func);

        Long validUtilBlock = getValidUtilBlock(service).longValue();
        Transaction tx = new Transaction(
                QUOTA_MANAGER_ADDR, getNonce(), 10000000, validUtilBlock,
                version, chainId, "0", funcData);

        String rawTx = tx.sign(adminPrivatekey);

        AppSendTransaction appSendTransaction =
                service.appSendRawTransaction(rawTx).send();

        if (appSendTransaction.getError() != null) {
            String message = appSendTransaction.getError().getMessage();
            System.out.println(
                    "Failed to set default aql " + defaultAqlToSet + ". Error message: " + message);
            return false;
        }
        String txHash = appSendTransaction.getSendTransactionResult().getHash();

        int count = 0;

        while (true) {
            Optional<TransactionReceipt> receipt = service
                    .appGetTransactionReceipt(txHash)
                    .send().getTransactionReceipt();
            if (receipt.isPresent()) {
                TransactionReceipt txReceipt = receipt.get();
                if (txReceipt.getErrorMessage() != null) {
                    return false;
                }
                List<Log> logs = txReceipt.getLogs();
                String defaultAqlToCompare = defaultAqlToSet.toString(16);
                defaultAqlToCompare = addUpTo64Hex(defaultAqlToCompare);
                return logs.get(0).getTopics().contains(defaultAqlToCompare);
            } else {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count > 5) {
                    break;
                }
            }
        }
        return false;
    }

    public boolean setAql(
            String addrToSet, BigInteger aqlToSet, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        Function func = new Function(
                QUOTA_MANAGER_SET_AQL,
                Arrays.asList(new Address(addrToSet), new Uint(aqlToSet)),
                Collections.emptyList());

        String funcData = FunctionEncoder.encode(func);

        Long validUtilBlock = getValidUtilBlock(service).longValue();
        Transaction tx = new Transaction(
                QUOTA_MANAGER_ADDR, getNonce(), 10000000, validUtilBlock,
                version, chainId, "0", funcData);

        String rawTx = tx.sign(adminPrivatekey);

        AppSendTransaction appSendTransaction =
                service.appSendRawTransaction(rawTx).send();

        if (appSendTransaction.getError() != null) {
            String message = appSendTransaction.getError().getMessage();
            System.out.println(
                    "Failed to set aql " + aqlToSet
                            + " for addr " + addrToSet + ". Error message: " + message);
            return false;
        }
        String txHash = appSendTransaction.getSendTransactionResult().getHash();

        int count = 0;

        while (true) {
            Optional<TransactionReceipt> receipt = service
                    .appGetTransactionReceipt(txHash)
                    .send().getTransactionReceipt();
            if (receipt.isPresent()) {
                TransactionReceipt txReceipt = receipt.get();
                if (txReceipt.getErrorMessage() != null) {
                    return false;
                }
                List<Log> logs = txReceipt.getLogs();
                String addrToCompare = addUpTo64Hex(addrToSet);
                return logs.get(0).getTopics().contains(addrToCompare);
            } else {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count > 5) {
                    break;
                }
            }
        }
        return false;
    }

    public int getBql(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_BQL);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<Type> resultTypes =
                FunctionReturnDecoder.decode(callResult.getValue(),
                        AppChainSystemContract.convert(
                                Arrays.asList(new TypeReference<Uint>() {})
                        )
                );
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public int getDefaultAql(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_DEFAULT_AQL);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<Type> resultTypes =
                FunctionReturnDecoder.decode(callResult.getValue(),
                        AppChainSystemContract.convert(
                                Arrays.asList(new TypeReference<Uint>() {})
                        )
                );
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public int getAql(String from, String addressToQuery) throws IOException {
        Function callFunc = new Function(
                QUOTA_MANAGER_GET_AQL,
                Arrays.asList(new Address(addressToQuery)),
                Collections.emptyList());

        String callData = FunctionEncoder.encode(callFunc);

        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);

        List<Type> resultTypes =
                FunctionReturnDecoder.decode(callResult.getValue(),
                        AppChainSystemContract.convert(
                                Arrays.asList(new TypeReference<Uint>() {})
                        )
                );
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public List<String> getAccounts(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_ACCOUNTS);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<Type> resultTypes = FunctionReturnDecoder.decode(
                callResult.getValue(),
                AppChainSystemContract.convert(
                        Arrays.asList(new TypeReference<DynamicArray<Address>>() {})
                )
        );
        ArrayList<Address> results =
                (ArrayList<Address>) resultTypes.get(0).getValue();
        return results.stream()
                .map(Address::getValue)
                .collect(Collectors.toList());
    }

    public List<BigInteger> getQuotas(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_QUOTAS);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<Type> resultTypes = FunctionReturnDecoder.decode(
                callResult.getValue(),
                AppChainSystemContract.convert(
                        Arrays.asList(new TypeReference<DynamicArray<Uint>>() {})
                )
        );
        ArrayList<Uint> results =
                (ArrayList<Uint>) resultTypes.get(0).getValue();
        return results.stream()
                .map(Uint::getValue)
                .collect(Collectors.toList());
    }


    public Transaction constructStoreTransaction(
            String data, int version, int chainId) {
        Transaction tx = new Transaction(
                STORE_ADDR, getNonce(), DEFAULT_QUOTA,
                getValidUtilBlock(service).longValue(),
                version, chainId, "0", data);
        return tx;
    }
}
