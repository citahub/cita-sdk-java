package com.cryptape.cita.protocol.system;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cryptape.cita.abi.FunctionEncoder;
import com.cryptape.cita.abi.TypeReference;
import com.cryptape.cita.abi.datatypes.generated.Bytes32;
import com.cryptape.cita.abi.datatypes.generated.Bytes4;
import com.cryptape.cita.abi.datatypes.generated.Uint64;
import com.cryptape.cita.abi.datatypes.generated.Uint8;
import com.cryptape.cita.protobuf.ConvertStrByte;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.core.methods.response.AppCall;
import com.cryptape.cita.protocol.core.methods.response.Log;
import com.cryptape.cita.protocol.system.entities.QueryInfoResult;
import com.cryptape.cita.protocol.system.entities.QueryResourceResult;
import com.cryptape.cita.utils.Numeric;
import com.cryptape.cita.abi.datatypes.Address;
import com.cryptape.cita.abi.datatypes.Bool;
import com.cryptape.cita.abi.datatypes.DynamicArray;
import com.cryptape.cita.abi.datatypes.Function;
import com.cryptape.cita.abi.datatypes.Type;
import com.cryptape.cita.abi.datatypes.Uint;

import static com.cryptape.cita.protocol.system.Util.getValidUtilBlock;

public class CITAjSystemContract implements CITASystemContract, CITASystemAddress {
    private CITAj service;

    public CITAjSystemContract(CITAj service) {
        this.service = service;
    }

    public List<String> listNode(String from) throws IOException {
        String callData = CITASystemContract.encodeCall(NODE_MANAGER_LIST_NODE);
        AppCall callResult = CITASystemContract.sendCall(
                from, NODE_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<DynamicArray<Address>>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParamters);
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
        AppCall callResult = CITASystemContract.sendCall(
                from, NODE_MANAGER_ADDR,
                FunctionEncoder.encode(callFunc), service);

        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<Uint8>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParamters);
        if (resultTypes.get(0) == null) {
            throw new NullPointerException(
                    "No info for address: " + from + ". Maybe it is not in correct format.");
        }
        return Integer.parseInt(resultTypes.get(0).getValue().toString());
    }

    public List<BigInteger> listStake(String from) throws IOException {
        String callData = CITASystemContract.encodeCall(NODE_MANAGER_LIST_STAKE);
        AppCall callResult = CITASystemContract.sendCall(
                from, NODE_MANAGER_ADDR, callData, service);

        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<DynamicArray<Uint64>>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParamters);
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
        AppCall callResult = CITASystemContract.sendCall(
                from, NODE_MANAGER_ADDR,
                FunctionEncoder.encode(callFunc), service);
        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<Uint64>() {});

        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParamters);
        return Integer.parseInt(resultTypes.get(0).getValue().toString());
    }

    public long getQuotaPrice(String from) throws IOException {
        String callData = CITASystemContract.encodeCall(PRICE_MANAGER_GET_QUOTA_PRICE);
        AppCall callResult = CITASystemContract.sendCall(
                from, PRICE_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters
                = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);
        return Long.parseLong(
                resultTypes.get(0).getValue().toString());
    }

    public boolean approveNode(
            String nodeAddr, String adminPrivateKey, int version, BigInteger chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Address(nodeAddr));
        String funcData = CITASystemContract.encodeFunction(
                NODE_MANAGER_APPROVE_NODE, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                NODE_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);

        return log != null && log.getTopics().contains(Util.addUpTo64Hex(nodeAddr));
    }

    public boolean deleteNode(
            String nodeAddr, String adminPrivateKey, int version, BigInteger chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Address(nodeAddr));
        String funcData = CITASystemContract.encodeFunction(
                NODE_MANAGER_DELETE_NODE, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                NODE_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);
        return log != null && log.getTopics().contains(Util.addUpTo64Hex(nodeAddr));
    }

    public boolean setStake(
            String nodeAddr, int stake, String adminPrivateKey, int version, BigInteger chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Arrays.asList(new Address(nodeAddr), new Uint64(stake));
        String funcData = CITASystemContract.encodeFunction(NODE_MANAGER_SET_STAKE, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                NODE_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);
        return log != null && log.getTopics().contains(Util.addUpTo64Hex(nodeAddr));
    }

    public boolean setBql(
            BigInteger bqlToSet, String adminPrivateKey, int version, BigInteger chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Uint(bqlToSet));
        String funcData = CITASystemContract.encodeFunction(QUOTA_MANAGER_SET_BQL, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                QUOTA_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);
        return log != null
                && log.getTopics().contains(Util.addUpTo64Hex(bqlToSet.toString(16)));
    }

    public boolean setDefaultAql(
            BigInteger defaultAqlToSet, String adminPrivateKey, int version, BigInteger chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Uint(defaultAqlToSet));
        String funcData = CITASystemContract.encodeFunction(QUOTA_MANAGER_SET_DEFAULT_AQL, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                QUOTA_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);
        return log != null
                && log.getTopics().contains(Util.addUpTo64Hex(defaultAqlToSet.toString(16)));
    }

    public boolean setAql(
            String addrToSet, BigInteger aqlToSet, String adminPrivateKey, int version, BigInteger chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Arrays.asList(new Address(addrToSet), new Uint(aqlToSet));
        String funcData = CITASystemContract.encodeFunction(QUOTA_MANAGER_SET_AQL, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                QUOTA_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);
        return log != null && log.getTopics().contains(Util.addUpTo64Hex(addrToSet));
    }

    public int getBql(String from) throws IOException {
        String callData = CITASystemContract.encodeCall(QUOTA_MANAGER_GET_BQL);
        AppCall callResult = CITASystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public int getDefaultAql(String from) throws IOException {
        String callData = CITASystemContract.encodeCall(QUOTA_MANAGER_GET_DEFAULT_AQL);
        AppCall callResult = CITASystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public int getAql(String from, String addressToQuery) throws IOException {
        Function callFunc = new Function(
                QUOTA_MANAGER_GET_AQL,
                Arrays.asList(new Address(addressToQuery)),
                Collections.emptyList());

        String callData = FunctionEncoder.encode(callFunc);

        AppCall callResult = CITASystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);

        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Uint>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);
        return Integer.parseInt(
                resultTypes.get(0).getValue().toString());
    }

    public List<String> getAccounts(String from) throws IOException {
        String callData = CITASystemContract.encodeCall(QUOTA_MANAGER_GET_ACCOUNTS);
        AppCall callResult = CITASystemContract.sendCall(
                from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(
                new TypeReference<DynamicArray<Address>>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);
        ArrayList<Address> results =
                (ArrayList<Address>) resultTypes.get(0).getValue();

        List<String> list = new ArrayList<>();
        for (Address address : results) {
            list.add(address.getValue());
        }
        return list;
    }

    public List<BigInteger> getQuotas(String from) throws IOException {
        String callData = CITASystemContract.encodeCall(QUOTA_MANAGER_GET_QUOTAS);
        AppCall callResult = CITASystemContract.sendCall(from, QUOTA_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<DynamicArray<Uint>>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);
        ArrayList<Uint> results = (ArrayList<Uint>) resultTypes.get(0).getValue();
        List<BigInteger> resultList = new ArrayList<>();
        for (Uint uint : results) {
            resultList.add(uint.getValue());
        }
        return resultList;
    }

    // the name param length,  Chinese should not exceed 16 and English should not exceed 32
    public String newPermission(
            String name,
            List<String> addrs,
            List<String> funcs,
            String adminPrivateKey,
            int version,
            BigInteger chainId)
            throws IOException, InterruptedException {

        String nameHex = Util.addUpTo64Hex(ConvertStrByte.stringToHexString(name));
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

        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_NEW_PERMISSION, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);

        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);
        return log == null ? "" : log.getAddress();
    }

    public boolean deletePermission(String permissionAddr, String adminKey, int version, BigInteger chainId)
            throws Exception {

        List<Type> inputParameter = Collections.singletonList(new Address(permissionAddr));

        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_DELETE_PERMISSION, inputParameter);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean addResources(
            List<String> addrs,
            List<String> funcs,
            String permissionAddr,
            String adminKey,
            int version,
            BigInteger chainId)
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

        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_ADD_RESOURCES, inputParameter);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean deleteResources(
            List<String> addrs,
            List<String> funcs,
            String permissionAddr,
            String adminKey,
            int version,
            BigInteger chainId)
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

        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_DELETE_RESOURCES, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean setAuthorization(
            String addr,
            String permissionAddr,
            String adminKey,
            int version,
            BigInteger chainId)
            throws IOException, InterruptedException {

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new Address(permissionAddr));
        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_SET_AUTHORIZATION, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean setAuthorizations(
            String addr,
            List<String> permissionAddrs,
            String adminKey,
            int version,
            BigInteger chainId) throws IOException, InterruptedException {

        List<Address> permissionsToAdd = new ArrayList<>();
        for (String str : permissionAddrs) {
            permissionsToAdd.add(new Address(str));
        }

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new DynamicArray<Address>(permissionsToAdd));

        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_SET_AUTHORIZATIONS, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean cancelAuthorization(
            String addr,
            String permissionAddr,
            String adminKey,
            int version,
            BigInteger chainId)
            throws IOException, InterruptedException {

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new Address(permissionAddr));

        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_CANCEL_AUTHORIZATION, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean cancelAuthorizations(
            String addr,
            List<String> permissionAddrs,
            String adminKey,
            int version,
            BigInteger chainId) throws IOException, InterruptedException {

        List<Address> permissionToCancel = new ArrayList<>();
        for (String str : permissionAddrs) {
            permissionToCancel.add(new Address(str));
        }

        List<Type> inputParameters = Arrays.asList(
                new Address(addr),
                new DynamicArray<Address>(permissionToCancel));

        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_CANCEL_AUTHORIZATIONS, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean clearAuthorization(
            String addr,
            String adminKey,
            int version,
            BigInteger chainId)
            throws IOException, InterruptedException {
        List<Type> inputParameters = Collections.singletonList(new Address(addr));
        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_CLEAR_AUTHORIZATION, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }


    public boolean updatePermissionName(
            String permissionAddr, String newPermissionName, String adminKey, int version, BigInteger chainId)
            throws Exception {

        String nameHex = Util.addUpTo64Hex(ConvertStrByte.stringToHexString(newPermissionName));
        byte[] nameBytes = ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(nameHex));

        List<Type> inputParameters = Arrays.asList(new Address(permissionAddr), new Bytes32(nameBytes));
        String funcData = CITASystemContract.encodeFunction(
                PERMISSION_MANAGER_UPDATE_PERMISSION_NAME, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                PERMISSION_MANAGER_ADDR, service, adminKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean inPermission(String fromAddr, String permissionAddr, String contractAddr, String func) throws IOException {
        byte[] funcBytes = ConvertStrByte.hexStringToBytes(
                Numeric.cleanHexPrefix(Util.generateFunSig(func)));

        List<Type> inputParameters = Arrays.asList(
                new Address(contractAddr),
                new Bytes4(funcBytes));
        String callData = CITASystemContract.encodeFunction(PERMISSION_MANAGER_IN_PERMISSION, inputParameters);

        AppCall callResult = CITASystemContract.sendCall(fromAddr, permissionAddr, callData, service);
        List<TypeReference<?>> outputParameter = Collections.singletonList(new TypeReference<Bool>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameter);

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

        AppCall callResult = CITASystemContract.sendCall(
                fromAddr, permissionAddr, callData, service);
        List<TypeReference<?>> outputParameters = Arrays.asList(
                new TypeReference<Bytes32>() {},
                new TypeReference<DynamicArray<Address>>() {},
                new TypeReference<DynamicArray<Bytes4>>() {}
        );

        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);

        Bytes32 name = (Bytes32) resultTypes.get(0);
        String nameResult = Util.hexToASCII(Numeric.toHexStringNoPrefix(name.getValue()));

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

        AppCall callResult = CITASystemContract.sendCall(fromAddr, permissionAddr, callData, service);

        List<TypeReference<?>> outputParameters = Collections.singletonList(new TypeReference<Bytes32>() {});

        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);
        Bytes32 name = (Bytes32) resultTypes.get(0);
        return Util.hexToASCII(Numeric.toHexStringNoPrefix(name.getValue()));
    }

    public QueryResourceResult queryResource(String fromAddr, String permissionAddr)
            throws IOException {
        String callData = Util.generateFunSig(PERMISSION_MANAGER_QUERY_RESOURCE);
        AppCall callResult = CITASystemContract.sendCall(fromAddr, permissionAddr, callData, service);

        List<TypeReference<?>> outputParameters = Arrays.asList(
                new TypeReference<DynamicArray<Address>>() {},
                new TypeReference<DynamicArray<Bytes4>>() {});

        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParameters);

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

    public boolean deleteGroup(String groupAddr, String adminPrivateKey, int version, BigInteger chainId) throws Exception {
        List<Type> inputParameter = Arrays.asList(new Address(INTRA_GROUP_USER_MANAGEMENT_ADDR),
                new Address(groupAddr));
        String funcData = CITASystemContract.encodeFunction(
                USER_MANAGER_DELETE_GROUP, inputParameter);
        String txHash = CITASystemContract.sendTxAndGetHash(
                USER_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean updateGroupName(String groupAddr, String newGroupName, String adminPrivateKey, int version, BigInteger chainId) throws Exception {
        String nameHex = Util.addUpTo64Hex(ConvertStrByte.stringToHexString(newGroupName));
        byte[] nameBytes = ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(nameHex));

        List<Type> inputParameters = Arrays.asList(new Address(INTRA_GROUP_USER_MANAGEMENT_ADDR),
                new Address(groupAddr), new Bytes32(nameBytes));
        String funcData = CITASystemContract.encodeFunction(
                USER_MANAGER_UPDATE_GROUP_NAME, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                USER_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean addAccounts(String groupAddr, List<String> accounts, String adminPrivateKey, int version, BigInteger chainId) throws Exception {
        List<Address> addrsToAdd = new ArrayList<>();
        for (String str : accounts) {
            addrsToAdd.add(new Address(str));
        }
        List<Type> inputParameters = Arrays.asList(
                new Address(INTRA_GROUP_USER_MANAGEMENT_ADDR),
                new Address(groupAddr),
                new DynamicArray<Address>(addrsToAdd));

        String funcData = CITASystemContract.encodeFunction(
                USER_MANAGER_ADD_ACCOUNTS, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                USER_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean deleteAccounts(String groupAddr, List<String> accounts, String adminPrivateKey, int version, BigInteger chainId) throws Exception {
        List<Address> addrsToAdd = new ArrayList<>();
        for (String str : accounts) {
            addrsToAdd.add(new Address(str));
        }
        List<Type> inputParameters = Arrays.asList(
                new Address(INTRA_GROUP_USER_MANAGEMENT_ADDR),
                new Address(groupAddr),
                new DynamicArray<Address>(addrsToAdd));

        String funcData = CITASystemContract.encodeFunction(
                USER_MANAGER_DELETE_ACCOUNTS, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                USER_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }

    public boolean checkScope(String groupAddr, String adminPrivateKey, int version, BigInteger chainId) throws Exception {
        List<Type> inputParameters = Arrays.asList(
                new Address(INTRA_GROUP_USER_MANAGEMENT_ADDR),
                new Address(groupAddr));

        String funcData = CITASystemContract.encodeFunction(
                USER_MANAGER_CHECK_SCOPE, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                USER_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);
        return CITASystemContract.checkReceipt(service, txHash);
    }


    /**
     * query all groups
     * @param senderAddress sender address
     * @return list addresses of all groups
     * @throws IOException
     */
    public List<String> queryGroups(String senderAddress) throws IOException {
        String callData = CITASystemContract.encodeCall(USER_MANAGER_QUERY_GROUPS);
        AppCall callResult = CITASystemContract.sendCall(
                senderAddress, USER_MANAGER_ADDR, callData, service);
        List<TypeReference<?>> outputParamters
                = Collections.singletonList(new TypeReference<DynamicArray<Address>>() {});
        List<Type> resultTypes = CITASystemContract.decodeCallResult(callResult, outputParamters);
        if (resultTypes.isEmpty())
            return null;
        List<String> list = new ArrayList<>();
        ArrayList<Address> results = (ArrayList<Address>) resultTypes.get(0).getValue();
        for (Address address : results) {
            if (INTRA_GROUP_USER_MANAGEMENT_ADDR.equalsIgnoreCase(address.getValue()))
                continue;
            list.add(address.getValue());
        }
        return list;
    }

    /**
     * new group
     * @see <a href="https://docs.citahub.com/zh-CN/cita/sys-contract-interface/interface#newgroup">newGroup</a>
     * @param groupName the name of group to be created
     * @param accounts accounts added to the group
     * @param adminPrivateKey the private key of super_admin
     * @param version
     * @param chainId
     * @return the transaction hash for creating group
     * @throws IOException
     */
    public String newGroup(String groupName, List<String> accounts, String adminPrivateKey, int version, BigInteger chainId) throws Exception {
        String nameHex = Util.addUpTo64Hex(ConvertStrByte.stringToHexString(groupName));
        byte[] nameBytes = ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(nameHex));
        List<Address> addrsToAdd = new ArrayList<>();
        for (String str : accounts) {
            addrsToAdd.add(new Address(str));
        }
        List<Type> inputParameters = Arrays.asList(
                new Address(INTRA_GROUP_USER_MANAGEMENT_ADDR),
                new Bytes32(nameBytes),
                new DynamicArray<Address>(addrsToAdd));

        String funcData = CITASystemContract.encodeFunction(
                USER_MANAGER_NEW_GROUP, inputParameters);
        String txHash = CITASystemContract.sendTxAndGetHash(
                USER_MANAGER_ADDR, service, adminPrivateKey, funcData, version, chainId);

        Log log = CITASystemContract.getReceiptLog(service, txHash, 0);
        return log == null ? "" : log.getAddress();
    }

    public Transaction constructStoreTransaction(String data, int version, BigInteger chainId) {
        return new Transaction(
                STORE_ADDR, Util.getNonce(), DEFAULT_QUOTA,
                Util.getValidUtilBlock(service).longValue(),
                version, chainId, "0", data);
    }
}
