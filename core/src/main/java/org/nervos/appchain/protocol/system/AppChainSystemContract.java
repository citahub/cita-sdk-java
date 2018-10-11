package org.nervos.appchain.protocol.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.response.AppCall;

/**
 * Please check system smart contracts at
 * https://github.com/cryptape/cita/tree/develop/scripts/contracts/src
 */

public interface AppChainSystemContract {
    DefaultBlockParameter DEFAULT_BLOCK_PARAMETER
            = DefaultBlockParameter.valueOf("latest");

    Long DEFAULT_QUOTA = 10000000l;

    //Node manager
    String NODE_MANAGER_ADDR = "0xffffffffffffffffffffffffffffffffff020001";
    String NODE_MANAGER_LIST_NODE = "listNode";
    String NODE_MANAGER_GET_STATUS = "getStatus";
    String NODE_MANAGER_LIST_STAKE = "listStake";
    String NODE_MANAGER_STAKE_PERMILLAGE = "stakePermillage";
    String NODE_MANAGER_APPROVE_NODE = "approveNode";
    String NODE_MANAGER_DELETE_NODE = "deleteNode";
    String NODE_MANAGER_SET_STAKE = "setStake";

    //Quota manager
    String QUOTA_MANAGER_ADDR = "0xffffffffffffffffffffffffffffffffff020003";
    String QUOTA_MANAGER_SET_BQL = "setBQL";
    String QUOTA_MANAGER_SET_DEFAULT_AQL = "setDefaultAQL";
    String QUOTA_MANAGER_SET_AQL = "setAQL";
    String QUOTA_MANAGER_GET_BQL = "getBQL";
    String QUOTA_MANAGER_GET_DEFAULT_AQL = "getDefaultAQL";
    String QUOTA_MANAGER_GET_AQL = "getAQL";
    String QUOTA_MANAGER_GET_ACCOUNTS = "getAccounts";
    String QUOTA_MANAGER_GET_QUOTAS = "getQuotas";

    //Price Manager
    String PRICE_MANAGER_ADDR = "0xffffffffffffffffffffffffffffffffff020010";
    String PRICE_MANAGER_GET_QUOTA_PRICE = "getQuotaPrice";

    //Permission manager
    String PERMISSION_MANAGER_ADDR = "0xffffffffffffffffffffffffffffffffff020010";
    //Permission manager manipulation
    String PERMISSION_MANAGER_NEW_PERMISSION = "newPermission";
    String PERMISSION_MANAGER_DELETE_PERMISSION = "deletePermission";
    String PERMISSION_MANAGER_UPDATE_PERMISSION_NAME = "updatePermissionName";
    String PERMISSION_MANAGER_ADD_RESOURCES = "addResources";
    String PERMISSION_MANAGER_DELETE_RESOURCES = "deleteResources";
    String PERMISSION_MANAGER_SET_AUTHORIZATION = "setAuthorization";
    String PERMISSION_MANAGER_SET_AUTHORIZATIONS = "setAuthorizations";
    String PERMISSION_MANAGER_CANCEL_AUTHORIZATION = "cancelAuthorization";
    String PERMISSION_MANAGER_CANCEL_AUTHORIZATIONS = "cancelAuthorizations";
    String PERMISSION_MANAGER_CLEAR_AUTHORIZATION = "clearAuthorization";
    //Permission manager query
    String PERMISSION_MANAGER_QUERY_ALL_ACCOUNTS = "queryAllAccounts";
    String PERMISSION_MANAGER_QUERY_PERMISSION = "queryPermissions";
    String PERMISSION_MANAGER_QUERY_ACCOUNTS = "queryAccounts";
    String PERMISSION_MANAGER_CHECK_PERMISSON = "checkPermission";
    String PERMISSION_MANAGER_CHECK_RESOURCE = "checkResource";
    String PERMISSION_MANAGER_IN_PERMISSION = "inPermission";
    String PERMISSION_MANAGER_QUERY_INFO = "queryInfo";
    String PERMISSION_MANAGER_QUERY_NAME = "queryName";
    String PERMISSION_MANAGER_QUERY_RESOURCE = "queryResource";

    //User manager
    String USER_MANAGER_ADDR = "0xffffffffffffffffffffffffffffffffff020010";
    //User manager manipulation
    String USER_MANAGER_NEW_GROUP = "newGroup";
    String USER_MANAGER_DELETE_GROUP = "deleteGroup";
    String USER_MANAGER_UPDATE_GROUP_NAME = "updateGroupName";
    String USER_MANAGER_ADD_ACCOUNTS = "addAccounts";
    String USER_MANAGER_DELETE_ACCOUNTS = "deleteAccounts";
    //User manager query (within group)
    String USER_MANAGER_QUERY_INFO = "queryInfo";
    String USER_MANAGER_QUERY_NAME = "queryName";
    String USER_MANAGER_QUERY_ACCOUNTS = "queryAccounts";
    String USER_MANAGER_QUERY_CHILD = "queryChild";
    String USER_MANAGER_QUERY_CHILD_LENGHT = "queryChildLength";
    String USER_MANAGER_QUERY_PARENT = "queryParent";
    String USER_MANAGER_IN_GROUP = "inGroup";
    //User manager query (query group)
    String USER_MANAGER_CHECK_SCOPE = "checkScope";
    String USER_MANAGER_QUERY_GROUPS = "queryGroups";

    static List<TypeReference<Type>> convert(List<TypeReference<?>> input) {
        List<TypeReference<Type>> result = new ArrayList<>(input.size());
        result.addAll(input.stream()
                .map(typeReference -> (TypeReference<Type>) typeReference)
                .collect(Collectors.toList()));
        return result;
    }

    static String encodeCall(String methodName) {
        Function callFunc = new Function(
                methodName,
                Collections.emptyList(),
                Collections.emptyList());
        return FunctionEncoder.encode(callFunc);
    }

    static String encodeFunction(String methodName, List<Type> inputParameters) {
        Function func = new Function(
                methodName,
                inputParameters,
                Collections.emptyList()
        );
        return FunctionEncoder.encode(func);
    }

    static AppCall sendCall(String from, String addr,
                            String callData, Nervosj service)
            throws IOException {
        Call call = new Call(from, addr, callData);
        return service.appCall(call, DEFAULT_BLOCK_PARAMETER).send();
    }

    int getQuotaPrice(String from) throws IOException;
}
