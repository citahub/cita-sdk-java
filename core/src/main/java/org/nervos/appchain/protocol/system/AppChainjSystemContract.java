package org.nervos.appchain.protocol.system;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Bool;
import org.nervos.appchain.abi.datatypes.DynamicArray;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.Uint;
import org.nervos.appchain.abi.datatypes.generated.Bytes32;
import org.nervos.appchain.abi.datatypes.generated.Bytes4;
import org.nervos.appchain.abi.datatypes.generated.Uint64;
import org.nervos.appchain.abi.datatypes.generated.Uint8;
import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.system.entities.QueryInfoResult;
import org.nervos.appchain.protocol.system.entities.QueryResourceResult;
import org.nervos.appchain.utils.Collection;
import org.nervos.appchain.utils.Convert;
import org.nervos.appchain.utils.Numeric;

import static org.nervos.appchain.protocol.system.Util.addUpTo64Hex;
import static org.nervos.appchain.protocol.system.Util.getNonce;
import static org.nervos.appchain.protocol.system.Util.getValidUtilBlock;
import static org.nervos.appchain.protocol.system.Util.hexToASCII;

public class AppChainjSystemContract implements AppChainSystemContract, AppChainSystemAddress {
    private AppChainj service;

    public AppChainjSystemContract(AppChainj service) {
        this.service = service;
    }

    public List<String> listNode(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(NODE_MANAGER_LIST_NODE);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<DynamicArray<Address>>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParamters);
        ArrayList<Address> results = (ArrayList<Address>) resultTypes.get(0).getValue();
        List<String> list = new ArrayList<>();
        for (Address address : results) {
            list.add(address.getValue());
        }
        return list;
    }

    public int getStatus(String from) throws IOException {
        Function callFunc = new Function(
                NODE_MANAGER_GET_STATUS,
                Collections.singletonList(new Address(from)),
                Collections.emptyList());
        FunctionEncoder.encode(callFunc);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR,
                FunctionEncoder.encode(callFunc), service);

        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<Uint8>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParamters);
        if (resultTypes.get(0) == null) {
            throw new NullPointerException(
                    "No info for address: " + from + ". Maybe it is not in correct format.");
        }
        return Integer.parseInt(resultTypes.get(0).getValue().toString());
    }

    public List<BigInteger> listStake(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(NODE_MANAGER_LIST_STAKE);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR, callData, service);

        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<DynamicArray<Uint64>>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParamters);
        ArrayList<Uint64> results =
                (ArrayList<Uint64>) resultTypes.get(0).getValue();
        List<BigInteger> list = new ArrayList<>();
        for (Uint64 uint64 : results) {
            list.add(uint64.getValue());
        }
        return list;
    }

    public int stakePermillage(String from) throws IOException {
        Function callFunc = new Function(
                NODE_MANAGER_STAKE_PERMILLAGE,
                Collections.singletonList(new Address(from)),
                Collections.emptyList());
        AppCall callResult = AppChainSystemContract.sendCall(
                from, NODE_MANAGER_ADDR,
                FunctionEncoder.encode(callFunc), service);
        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<Uint64>() {});

        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParamters);
        return Integer.parseInt(resultTypes.get(0).getValue().toString());
    }

    public int getQuotaPrice(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(PRICE_MANAGER_GET_QUOTA_PRICE);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, PRICE_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParamters);
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public boolean approveNode(
            String nodeAddr, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Address(nodeAddr));
        String funcData = AppChainSystemContract.encodeFunction(
                NODE_MANAGER_APPROVE_NODE, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                NODE_MANAGER_ADDR, service, adminPrivatekey, funcData, version, chainId);
        Log log = AppChainSystemContract.getReceiptLog(service, txHash, 0);

        return log != null && log.getTopics().contains(addUpTo64Hex(nodeAddr));
    }

    public boolean deleteNode(
            String nodeAddr, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Address(nodeAddr));
        String funcData = AppChainSystemContract.encodeFunction(
                NODE_MANAGER_DELETE_NODE, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                NODE_MANAGER_ADDR, service, adminPrivatekey, funcData, version, chainId);
        Log log = AppChainSystemContract.getReceiptLog(service, txHash, 0);
        return log != null && log.getTopics().contains(addUpTo64Hex(nodeAddr));
    }

    public boolean setStake(
            String nodeAddr, int stake, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Arrays.asList(new Address(nodeAddr), new Uint64(stake));
        String funcData = AppChainSystemContract.encodeFunction(NODE_MANAGER_SET_STAKE, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                NODE_MANAGER_ADDR, service, adminPrivatekey, funcData, version, chainId);
        Log log = AppChainSystemContract.getReceiptLog(service, txHash, 0);
        return log != null && log.getTopics().contains(addUpTo64Hex(nodeAddr));
    }

    public boolean setBql(
            BigInteger bqlToSet, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Uint(bqlToSet));
        String funcData = AppChainSystemContract.encodeFunction(QUOTA_MANAGER_SET_BQL, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                QUOTA_MANAGER_ADDR, service, adminPrivatekey, funcData, version, chainId);
        Log log = AppChainSystemContract.getReceiptLog(service, txHash, 0);
        return log != null
                && log.getTopics().contains(addUpTo64Hex(bqlToSet.toString(16)));
    }

    public boolean setDefaultAql(
            BigInteger defaultAqlToSet, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Uint(defaultAqlToSet));
        String funcData = AppChainSystemContract.encodeFunction(QUOTA_MANAGER_SET_DEFAULT_AQL, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                QUOTA_MANAGER_ADDR, service, adminPrivatekey, funcData, version, chainId);
        Log log = AppChainSystemContract.getReceiptLog(service, txHash, 0);
        return log != null
                && log.getTopics().contains(addUpTo64Hex(defaultAqlToSet.toString(16)));
    }

    public boolean setAql(
            String addrToSet, BigInteger aqlToSet, String adminPrivatekey, int version, int chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Arrays.asList(new Address(addrToSet), new Uint(aqlToSet));
        String funcData = AppChainSystemContract.encodeFunction(QUOTA_MANAGER_SET_AQL, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                QUOTA_MANAGER_ADDR, service, adminPrivatekey, funcData, version, chainId);
        Log log = AppChainSystemContract.getReceiptLog(service, txHash, 0);
        return log != null && log.getTopics().contains(addUpTo64Hex(addrToSet));
    }

    public int getBql(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_BQL);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public int getDefaultAql(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_DEFAULT_AQL);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);
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

        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public List<String> getAccounts(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_ACCOUNTS);
        AppCall callResult = AppChainSystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(
                new TypeReference<DynamicArray<Address>>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);
        ArrayList<Address> results =
                (ArrayList<Address>) resultTypes.get(0).getValue();

        List<String> list = new ArrayList<>();
        for (Address address : results) {
            list.add(address.getValue());
        }
        return list;
    }

    public List<BigInteger> getQuotas(String from) throws IOException {
        String callData = AppChainSystemContract.encodeCall(QUOTA_MANAGER_GET_QUOTAS);
        AppCall callResult = AppChainSystemContract.sendCall(from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<DynamicArray<Uint>>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);
        ArrayList<Uint> results = (ArrayList<Uint>) resultTypes.get(0).getValue();
        List<BigInteger> resultList = new ArrayList<>();
        for (Uint uint : results) {
            resultList.add(uint.getValue());
        }
        return resultList;
    }

    public String newPermission(
            String name,
            List<String> addrs,
            List<String> funcs,
            String adminPrivatekey,
            int version,
            int chainId)
            throws IOException, InterruptedException {

        String nameHex = addUpTo64Hex(ConvertStrByte.stringToHexString(name));
        byte[] nameBytes = ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(nameHex));

        List<Address> addrsToAdd = new ArrayList<>();
        for (String str : addrs) {
            addrsToAdd.add(new Address(str));
        }

        List<Bytes4> funcToAdd = new ArrayList<>();
        for (String str : funcs) {
            funcToAdd.add(new Bytes4(ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(Util.generateFunSig(str)))));
        }

        List<Type> inputParameters = Arrays.asList(
                new Bytes32(nameBytes),
                new DynamicArray<Address>(addrsToAdd),
                new DynamicArray<Bytes4>(funcToAdd));

        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_NEW_PERMISSION, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminPrivatekey, funcData, version, chainId);

        Log log = AppChainSystemContract.getReceiptLog(service, txHash, 0);
        return log == null ? "" : log.getAddress();
    }

    public boolean deletePermission(String permissionAddr, String adminKey, int version, int chainId)
            throws Exception {

        List<Type> inputParameter = Collections.singletonList(new Address(permissionAddr));

        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_DELETE_PERMISSION, inputParameter);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean addResources(
            List<String> addrs,
            List<String> funcs,
            String permissionAddr,
            String adminKey,
            int version,
            int chainId)
            throws IOException, InterruptedException {

        List<Address> addrsToAdd = new ArrayList<>();
        for (String str : addrs) {
            addrsToAdd.add(new Address(str));
        }

        List<Bytes4> funcToAdd = new ArrayList<>();
        for (String str : funcs) {
            funcToAdd.add(new Bytes4(ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(Util.generateFunSig(str)))));
        }

        List<Type> inputParameter = Arrays.asList(
                new Address(permissionAddr),
                new DynamicArray<Address>(addrsToAdd),
                new DynamicArray<Bytes4>(funcToAdd)
        );

        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_ADD_RESOURCES, inputParameter);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean deleteResources(
            List<String> addrs,
            List<String> funcs,
            String permissionAddr,
            String adminKey,
            int version,
            int chainId)
            throws IOException, InterruptedException {

        List<Address> addrsToAdd = new ArrayList<>();
        for (String str : addrs) {
            addrsToAdd.add(new Address(str));
        }

        List<Bytes4> funcToAdd = new ArrayList<>();
        for (String str : funcs) {
            funcToAdd.add(new Bytes4(ConvertStrByte.hexStringToBytes(
                    Numeric.cleanHexPrefix(Util.generateFunSig(str)))));
        }

        List<Type> inputParameters = Arrays.asList(
                new Address(permissionAddr),
                new DynamicArray<Address>(addrsToAdd),
                new DynamicArray<Bytes4>(funcToAdd));

        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_DELETE_RESOURCES, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean setAuthorization(
            String addr,
            String permissionAddr,
            String adminKey,
            int version,
            int chainId)
            throws IOException, InterruptedException {

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new Address(permissionAddr));
        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_SET_AUTHORIZATION, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean setAuthorizations(
            String addr,
            List<String> permissionAddrs,
            String adminKey,
            int version,
            int chainId) throws IOException, InterruptedException {

        List<Address> permissionsToAdd = new ArrayList<>();
        for (String str : permissionAddrs) {
            permissionsToAdd.add(new Address(str));
        }

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new DynamicArray<Address>(permissionsToAdd));

        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_SET_AUTHORIZATIONS, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean cancelAuthorization(
            String addr,
            String permissionAddr,
            String adminKey,
            int version,
            int chainId)
            throws IOException, InterruptedException {

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new Address(permissionAddr));

        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_CANCEL_AUTHORIZATION, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean cancelAuthorizations(
            String addr,
            List<String> permissionAddrs,
            String adminKey,
            int version,
            int chainId) throws IOException, InterruptedException {

        List<Address> permissionToCancel = new ArrayList<>();
        for (String str : permissionAddrs) {
            permissionToCancel.add(new Address(str));
        }

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new DynamicArray<Address>(permissionToCancel));

        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_CANCEL_AUTHORIZATIONS, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean clearAuthorization(
            String addr,
            String adminKey,
            int version,
            int chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Address(addr));
        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_CLEAR_AUTHORIZATION, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }


    public boolean updatePermissionName(
            String permissionAddr, String newPermissionName, String adminKey, int version, int chainId)
            throws Exception {

        String nameHex = addUpTo64Hex(ConvertStrByte.stringToHexString(newPermissionName));
        byte[] nameBytes = ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(nameHex));

        List<Type> inputParameters = Arrays.asList(new Address(permissionAddr), new Bytes32(nameBytes));
        String funcData = AppChainSystemContract.encodeFunction(
                PERMISSION_MANAGER_UPDATE_PERMISSION_NAME, inputParameters);
        String txHash = AppChainSystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return AppChainSystemContract.checkReceipt(service, txHash);
    }

    public boolean inPermission(String fromAddr, String permissionAddr, String contractAddr, String func) throws IOException {
        byte[] funcBytes = ConvertStrByte.hexStringToBytes(
                Numeric.cleanHexPrefix(Util.generateFunSig(func)));

        List<Type> inputParameters = Arrays.asList(
                new Address(contractAddr),
                new Bytes4(funcBytes));
        String callData = AppChainSystemContract.encodeFunction(PERMISSION_MANAGER_IN_PERMISSION, inputParameters);

        AppCall callResult = AppChainSystemContract.sendCall(fromAddr, permissionAddr, callData, service);
        List<TypeReference<?>> outputParameter = Collections.singletonList(new TypeReference<Bool>() {});
        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameter);

        return ((Bool) resultTypes.get(0)).getValue();
    }

    public QueryInfoResult queryInfo(String fromAddr, String permissionAddr)
            throws IOException {

        Function callFunc = new Function(
                PERMISSION_MANAGER_QUERY_INFO,
                Collections.emptyList(),
                Collections.emptyList()
        );

        String callData = FunctionEncoder.encode(callFunc);

        AppCall callResult = AppChainSystemContract.sendCall(
                fromAddr, permissionAddr, callData, service);
        List<TypeReference<?>> outputParameters = Arrays.asList(
                new TypeReference<Bytes32>() {},
                new TypeReference<DynamicArray<Address>>() {},
                new TypeReference<DynamicArray<Bytes4>>() {}
        );

        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);

        Bytes32 name = (Bytes32) resultTypes.get(0);
        String nameResult = hexToASCII(Numeric.toHexStringNoPrefix(name.getValue()));

        DynamicArray<Address> contractAddrs = (DynamicArray<Address>) resultTypes.get(1);
        List<Address> contractList = contractAddrs.getValue();

        DynamicArray<Bytes4> funcs = (DynamicArray<Bytes4>) resultTypes.get(2);
        List<Bytes4> funcList = funcs.getValue();

        List<String> contracts = new ArrayList<>();
        for (Address addr : contractList) {
            contracts.add(addr.toString());
        }

        List<String> functions = new ArrayList<>();
        for (Bytes4 bytes4 : funcList) {
            functions.add(ConvertStrByte.bytesToHexString(bytes4.getValue()));
        }

        return new QueryInfoResult(nameResult, contracts, functions);
    }


    public String queryName(String fromAddr, String permissionAddr) throws IOException {
        String callData = Util.generateFunSig(PERMISSION_MANAGER_QUERY_NAME);

        AppCall callResult = AppChainSystemContract.sendCall(fromAddr, permissionAddr, callData, service);

        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Bytes32>() {});

        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);
        Bytes32 name = (Bytes32) resultTypes.get(0);
        return hexToASCII(Numeric.toHexStringNoPrefix(name.getValue()));
    }

    public QueryResourceResult queryResource(String fromAddr, String permissionAddr)
            throws IOException {
        String callData = Util.generateFunSig(PERMISSION_MANAGER_QUERY_RESOURCE);
        AppCall callResult = AppChainSystemContract.sendCall(fromAddr, permissionAddr, callData, service);

        List<TypeReference<?>> outputParameters = Arrays.asList(
                new TypeReference<DynamicArray<Address>>() {},
                new TypeReference<DynamicArray<Bytes4>>() {});

        List<Type> resultTypes = AppChainSystemContract.decodeCallResult(callResult, outputParameters);

        DynamicArray<Address> scontractAddrs = (DynamicArray<Address>) resultTypes.get(0);
        List<Address> contractList = scontractAddrs.getValue();

        DynamicArray<Bytes4> funcs = (DynamicArray<Bytes4>) resultTypes.get(1);
        List<Bytes4> funcList = funcs.getValue();

        List<String> contracts = new ArrayList<>();
        for (Address addr : contractList) {
            contracts.add(addr.toString());
        }

        List<String> functions = new ArrayList<>();
        for (Bytes4 bytes4 : funcList) {
            functions.add(ConvertStrByte.bytesToHexString(bytes4.getValue()));
        }

        return new QueryResourceResult(contracts, functions);
    }

    public Transaction constructStoreTransaction(
            String data, int version, int chainId) {
        return new Transaction(
                STORE_ADDR, getNonce(), DEFAULT_QUOTA,
                getValidUtilBlock(service).longValue(),
                version, chainId, "0", data);
    }
}