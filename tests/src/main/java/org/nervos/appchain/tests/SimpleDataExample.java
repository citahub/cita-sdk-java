package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.FunctionReturnDecoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.DynamicBytes;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.Uint;
import org.nervos.appchain.abi.datatypes.Utf8String;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;

/*
* The example shows how to deploy a contract with constructor parameters
* */

public class SimpleDataExample {
    private static BigInteger chainId;
    private static int version;
    private static String privateKey;
    private static String fromAddress;
    private static String toAddress;
    private static String binPath;
    private static Long quota;
    private static String value;
    private static AppChainj service;
    private static String contractAddr;

    static {
        Config conf = new Config();
        conf.buildService(false);

        privateKey = conf.primaryPrivKey;
        fromAddress = conf.primaryAddr;
        toAddress = conf.auxAddr1;
        binPath = conf.simpleBin;

        service = conf.service;
        quota = Long.parseLong(conf.defaultQuotaDeployment);
        value = "0";
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
    }

    private static String deploySampleContract(
            int userId, String userName, String userDesc, String userAddress)
            throws Exception {
        String contractData = loadContractCode(binPath);
        String constructorData = buildConstructor(userId, userName, userDesc, userAddress);

        Transaction tx = Transaction.createContractTransaction(
                TestUtil.getNonce(), quota,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId, value, contractData, constructorData);

        String signedTx = tx.sign(privateKey);
        AppSendTransaction appSendTransaction = service.appSendRawTransaction(signedTx).send();

        return appSendTransaction.getSendTransactionResult().getHash();
    }

    private static void getTransactionReceipt(String hash) throws IOException {
        AppGetTransactionReceipt txReceipt
                = service.appGetTransactionReceipt(hash).send();
        if (txReceipt.getTransactionReceipt() != null) {
            if (txReceipt.getTransactionReceipt().getErrorMessage() == null) {
                contractAddr = txReceipt.getTransactionReceipt().getContractAddress();
            }
        } else {
            throw new IOException("Cannot get receipt for hash " + hash);
        }
    }

    private static String buildConstructor(int number, String name, String desc, String address) {
        List<Type> constructorList = Arrays.asList(
                new Uint256(number),
                new DynamicBytes(name.getBytes()),
                new Utf8String(desc),
                new Address(address));

        return FunctionEncoder.encodeConstructor(constructorList);
    }

    private static Type getAppCallResult(String addr, Function func)
            throws IOException {
        String funcData = FunctionEncoder.encode(func);
        Call call = new Call(fromAddress, addr, funcData);
        String result = service.appCall(call, DefaultBlockParameter.valueOf("latest"))
                .send().getValue();
        return FunctionReturnDecoder.decode(result, func.getOutputParameters()).get(0);
    }

    private static BigInteger getUserId() throws Exception {
        Function func = new Function(
                "userId",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint>(){}));

        Uint uint = (Uint) getAppCallResult(contractAddr, func);
        return uint.getValue();
    }

    private static String getName() throws Exception {
        Function func = new Function(
                "userName",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<DynamicBytes>() {}));

        DynamicBytes dynamicBytes = (DynamicBytes) getAppCallResult(contractAddr, func);
        String hexString = ConvertStrByte.bytesToHexString(dynamicBytes.getValue());
        return ConvertStrByte.hexStringToString(hexString);
    }

    private static String getUserDesc() throws Exception {
        Function func = new Function(
                "userDesc",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Utf8String>() {}));

        Utf8String utfStr = (Utf8String) getAppCallResult(contractAddr, func);
        return utfStr.getValue();
    }

    private static String getUserAddr() throws Exception {
        Function func = new Function(
                "userAddr",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Address>() {}));

        Address addr = (Address) getAppCallResult(contractAddr, func);
        return addr.toString();
    }

    private static String loadContractCode(String binPath) throws Exception {
        return new String(Files.readAllBytes(Paths.get(binPath)));
    }

    public static void main(String[] args) throws Exception {
        //deploy the contract with sample parameters
        String deploymentHash = deploySampleContract(1, "userName", "userDesc", toAddress);
        System.out.println("Hash of contract deployment: " + deploymentHash);

        //wait for contract deployment
        System.out.println("Wait 10s for contract deployment. ");
        TimeUnit.SECONDS.sleep(10);

        //check if attributes as expected
        getTransactionReceipt(deploymentHash);
        System.out.println("user ID: " + getUserId());
        System.out.println("user name: " + getName());
        System.out.println("user desc: " + getUserDesc());
        System.out.println("user addr: " + getUserAddr());
    }
}
