package org.nervos.appchain.codegen;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;

import org.nervos.appchain.TempFileProvider;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Bool;
import org.nervos.appchain.abi.datatypes.DynamicArray;
import org.nervos.appchain.abi.datatypes.DynamicBytes;
import org.nervos.appchain.abi.datatypes.StaticArray;
import org.nervos.appchain.abi.datatypes.Utf8String;
import org.nervos.appchain.abi.datatypes.generated.Bytes32;
import org.nervos.appchain.abi.datatypes.generated.Int256;
import org.nervos.appchain.abi.datatypes.generated.StaticArray10;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.abi.datatypes.generated.Uint64;
import org.nervos.appchain.protocol.core.methods.response.AbiDefinition;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.nervos.appchain.codegen.SolidityFunctionWrapper.buildTypeName;
import static org.nervos.appchain.codegen.SolidityFunctionWrapper.createValidParamName;
import static org.nervos.appchain.codegen.SolidityFunctionWrapper.getEventNativeType;
import static org.nervos.appchain.codegen.SolidityFunctionWrapper.getNativeType;


public class SolidityFunctionWrapperTest extends TempFileProvider {

    private SolidityFunctionWrapper solidityFunctionWrapper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solidityFunctionWrapper = new SolidityFunctionWrapper(true);
    }

    @Test
    public void testCreateValidParamName() {
        assertThat(createValidParamName("param", 1), is("param"));
        assertThat(createValidParamName("", 1), is("param1"));
    }

    @Test
    public void testBuildTypeName() {
        assertThat(buildTypeName("uint256"),
                is(ClassName.get(Uint256.class)));
        assertThat(buildTypeName("uint64"),
                is(ClassName.get(Uint64.class)));
        assertThat(buildTypeName("string"),
                is(ClassName.get(Utf8String.class)));

        assertThat(buildTypeName("uint256[]"),
                is(ParameterizedTypeName.get(DynamicArray.class, Uint256.class)));

        assertThat(buildTypeName("uint256[] storage"),
                is(ParameterizedTypeName.get(DynamicArray.class, Uint256.class)));

        assertThat(buildTypeName("uint256[] memory"),
                is(ParameterizedTypeName.get(DynamicArray.class, Uint256.class)));

        assertThat(buildTypeName("uint256[10]"),
                is(ParameterizedTypeName.get(StaticArray10.class, Uint256.class)));

        assertThat(buildTypeName("uint256[33]"),
                is(ParameterizedTypeName.get(StaticArray.class, Uint256.class)));
    }

    @Test
    public void testGetNativeType() {
        assertThat(getNativeType(TypeName.get(Address.class)),
                equalTo(TypeName.get(String.class)));
        assertThat(getNativeType(TypeName.get(Uint256.class)),
                equalTo(TypeName.get(BigInteger.class)));
        assertThat(getNativeType(TypeName.get(Int256.class)),
                equalTo(TypeName.get(BigInteger.class)));
        assertThat(getNativeType(TypeName.get(Utf8String.class)),
                equalTo(TypeName.get(String.class)));
        assertThat(getNativeType(TypeName.get(Bool.class)),
                equalTo(TypeName.get(Boolean.class)));
        assertThat(getNativeType(TypeName.get(Bytes32.class)),
                equalTo(TypeName.get(byte[].class)));
        assertThat(getNativeType(TypeName.get(DynamicBytes.class)),
                equalTo(TypeName.get(byte[].class)));
    }

    @Test
    public void testGetNativeTypeParameterized() {
        assertThat(getNativeType(
                ParameterizedTypeName.get(
                        ClassName.get(DynamicArray.class), TypeName.get(Address.class))),
                equalTo(ParameterizedTypeName.get(
                        ClassName.get(List.class), TypeName.get(String.class))));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNativeTypeInvalid() {
        getNativeType(TypeName.get(BigInteger.class));
    }

    @Test
    public void testGetEventNativeType() {
        assertThat(getEventNativeType(TypeName.get(Utf8String.class)),
                equalTo(TypeName.get(byte[].class)));
    }

    @Test
    public void testGetEventNativeTypeParameterized() {
        assertThat(getEventNativeType(
                ParameterizedTypeName.get(
                        ClassName.get(DynamicArray.class), TypeName.get(Address.class))),
                equalTo(TypeName.get(byte[].class)));
    }

    @Test
    public void testBuildFunctionTransaction() throws Exception {
        AbiDefinition functionDefinition = new AbiDefinition(
                false,
                Arrays.asList(
                        new AbiDefinition.NamedType("param", "uint8")),
                "functionName",
                Collections.emptyList(),
                "type",
                false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        //CHECKSTYLE:OFF
        String expected =
                "public org.nervos.appchain.protocol.core.RemoteCall<org.nervos.appchain.protocol.core.methods.response.TransactionReceipt> functionName(java.math.BigInteger param, java.lang.Long quota, java.lang.String nonce, java.lang.Long validUntilBlock, java.lang.Integer version, java.math.BigInteger chainId, java.lang.String value) {\n"
                        + "  org.nervos.appchain.abi.datatypes.Function function = new org.nervos.appchain.abi.datatypes.Function(\n"
                        + "      \"functionName\", \n"
                        + "      java.util.Arrays.<org.nervos.appchain.abi.datatypes.Type>asList(new org.nervos.appchain.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Collections.<org.nervos.appchain.abi.TypeReference<?>>emptyList());\n"
                        + "  return executeRemoteCallTransaction(function, quota, nonce, validUntilBlock, version, chainId, value);\n"
                        + "}\n";
        //CHECKSTYLE:ON

        assertThat(methodSpec.toString(), is(expected));
    }

    @Test
    public void testBuildPayabelFunctionTransaction() throws Exception {
        AbiDefinition functionDefinition = new AbiDefinition(
                false,
                Arrays.asList(
                        new AbiDefinition.NamedType("param", "uint8")),
                "functionName",
                Collections.emptyList(),
                "type",
                true);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        //CHECKSTYLE:OFF
        String expected =
                "public org.nervos.appchain.protocol.core.RemoteCall<org.nervos.appchain.protocol.core.methods.response.TransactionReceipt> functionName(java.math.BigInteger param, java.math.BigInteger weiValue, java.lang.Long quota, java.lang.String nonce, java.lang.Long validUntilBlock, java.lang.Integer version, java.math.BigInteger chainId, java.lang.String value) {\n"
                        + "  org.nervos.appchain.abi.datatypes.Function function = new org.nervos.appchain.abi.datatypes.Function(\n"
                        + "      \"functionName\", \n"
                        + "      java.util.Arrays.<org.nervos.appchain.abi.datatypes.Type>asList(new org.nervos.appchain.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Collections.<org.nervos.appchain.abi.TypeReference<?>>emptyList());\n"
                        + "  return executeRemoteCallTransaction(function, weiValue, quota, nonce, validUntilBlock, version, chainId, value);\n"
                        + "}\n";
        //CHECKSTYLE:ON

        assertThat(methodSpec.toString(), is(expected));
    }

    @Test
    public void testBuildFunctionConstantSingleValueReturn() throws Exception {
        AbiDefinition functionDefinition = new AbiDefinition(
                true,
                Arrays.asList(
                        new AbiDefinition.NamedType("param", "uint8")),
                "functionName",
                Arrays.asList(
                        new AbiDefinition.NamedType("result", "int8")),
                "type",
                false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        //CHECKSTYLE:OFF
        String expected =
                "public org.nervos.appchain.protocol.core.RemoteCall<java.math.BigInteger> functionName(java.math.BigInteger param) {\n"
                        + "  org.nervos.appchain.abi.datatypes.Function function = new org.nervos.appchain.abi.datatypes.Function(\"functionName\", \n"
                        + "      java.util.Arrays.<org.nervos.appchain.abi.datatypes.Type>asList(new org.nervos.appchain.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Arrays.<org.nervos.appchain.abi.TypeReference<?>>asList(new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.generated.Int8>() {}));\n"
                        + "  return executeRemoteCallSingleValueReturn(function, java.math.BigInteger.class);\n"
                        + "}\n";
        //CHECKSTYLE:ON

        assertThat(methodSpec.toString(), is(expected));
    }

    @Test(expected = RuntimeException.class)
    public void testBuildFunctionConstantInvalid() throws Exception {
        AbiDefinition functionDefinition = new AbiDefinition(
                true,
                Arrays.asList(
                        new AbiDefinition.NamedType("param", "uint8")),
                "functionName",
                Collections.emptyList(),
                "type",
                false);

        solidityFunctionWrapper.buildFunction(functionDefinition);
    }

    @Test
    public void testBuildFunctionConstantMultipleValueReturn() throws Exception {

        AbiDefinition functionDefinition = new AbiDefinition(
                true,
                Arrays.asList(
                        new AbiDefinition.NamedType("param1", "uint8"),
                        new AbiDefinition.NamedType("param2", "uint32")),
                "functionName",
                Arrays.asList(
                        new AbiDefinition.NamedType("result1", "int8"),
                        new AbiDefinition.NamedType("result2", "int32")),
                "type",
                false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        //CHECKSTYLE:OFF
        String expected = "public org.nervos.appchain.protocol.core.RemoteCall<org.nervos.appchain.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>> functionName(java.math.BigInteger param1, java.math.BigInteger param2) {\n"
                + "  final org.nervos.appchain.abi.datatypes.Function function = new org.nervos.appchain.abi.datatypes.Function(\"functionName\", \n"
                + "      java.util.Arrays.<org.nervos.appchain.abi.datatypes.Type>asList(new org.nervos.appchain.abi.datatypes.generated.Uint8(param1), \n"
                + "      new org.nervos.appchain.abi.datatypes.generated.Uint32(param2)), \n"
                + "      java.util.Arrays.<org.nervos.appchain.abi.TypeReference<?>>asList(new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.generated.Int8>() {}, new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.generated.Int32>() {}));\n"
                + "  return new org.nervos.appchain.protocol.core.RemoteCall<org.nervos.appchain.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>>(\n"
                + "      new java.util.concurrent.Callable<org.nervos.appchain.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>>() {\n"
                + "        @java.lang.Override\n"
                + "        public org.nervos.appchain.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger> call() throws java.lang.Exception {\n"
                + "          java.util.List<org.nervos.appchain.abi.datatypes.Type> results = executeCallMultipleValueReturn(function);;\n"
                + "          return new org.nervos.appchain.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>(\n"
                + "              (java.math.BigInteger) results.get(0).getValue(), \n"
                + "              (java.math.BigInteger) results.get(1).getValue());\n"
                + "        }\n"
                + "      });\n"
                + "}\n";
        //CHECKSTYLE:ON

        assertThat(methodSpec.toString(), is(expected));
    }

    @Test
    public void testBuildEventConstantMultipleValueReturn() throws Exception {

        AbiDefinition.NamedType id = new AbiDefinition.NamedType("id", "string", true);
        AbiDefinition.NamedType fromAddress = new AbiDefinition.NamedType("from", "address");
        AbiDefinition.NamedType toAddress = new AbiDefinition.NamedType("to", "address");
        AbiDefinition.NamedType value = new AbiDefinition.NamedType("value", "uint256");
        AbiDefinition.NamedType message = new AbiDefinition.NamedType("message", "string");
        fromAddress.setIndexed(true);
        toAddress.setIndexed(true);

        AbiDefinition functionDefinition = new AbiDefinition(
                false,
                Arrays.asList(id, fromAddress, toAddress, value, message),
                "Transfer",
                new ArrayList<>(),
                "event",
                false);
        TypeSpec.Builder builder = TypeSpec.classBuilder("testClass");

        solidityFunctionWrapper.buildEventFunctions(functionDefinition, builder);

        //CHECKSTYLE:OFF
        String expected =
                "class testClass {\n"
                        + "  public java.util.List<TransferEventResponse> getTransferEvents(org.nervos.appchain.protocol.core.methods.response.TransactionReceipt transactionReceipt) {\n"
                        + "    final org.nervos.appchain.abi.datatypes.Event event = new org.nervos.appchain.abi.datatypes.Event(\"Transfer\", \n"
                        + "        java.util.Arrays.<org.nervos.appchain.abi.TypeReference<?>>asList(new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Utf8String>() {}, new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Address>() {}, new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Address>() {}),\n"
                        + "        java.util.Arrays.<org.nervos.appchain.abi.TypeReference<?>>asList(new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.generated.Uint256>() {}, new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Utf8String>() {}));\n"
                        + "    java.util.List<org.nervos.appchain.abi.EventValues> valueList = extractEventParameters(event, transactionReceipt);\n"
                        + "    java.util.ArrayList<TransferEventResponse> responses = new java.util.ArrayList<TransferEventResponse>(valueList.size());\n"
                        + "    for (org.nervos.appchain.abi.EventValues eventValues : valueList) {\n"
                        + "      TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "      typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.from = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n"
                        + "      typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n"
                        + "      typedResponse.value = (java.math.BigInteger) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "      responses.add(typedResponse);\n"
                        + "    }\n"
                        + "    return responses;\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(org.nervos.appchain.protocol.core.DefaultBlockParameter startBlock, org.nervos.appchain.protocol.core.DefaultBlockParameter endBlock) {\n"
                        + "    final org.nervos.appchain.abi.datatypes.Event event = new org.nervos.appchain.abi.datatypes.Event(\"Transfer\", \n"
                        + "        java.util.Arrays.<org.nervos.appchain.abi.TypeReference<?>>asList(new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Utf8String>() {}, new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Address>() {}, new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Address>() {}),\n"
                        + "        java.util.Arrays.<org.nervos.appchain.abi.TypeReference<?>>asList(new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.generated.Uint256>() {}, new org.nervos.appchain.abi.TypeReference<org.nervos.appchain.abi.datatypes.Utf8String>() {}));\n"
                        + "    org.nervos.appchain.protocol.core.methods.request.AppFilter filter = new org.nervos.appchain.protocol.core.methods.request.AppFilter(startBlock, endBlock, getContractAddress());\n"
                        + "    filter.addSingleTopic(org.nervos.appchain.abi.EventEncoder.encode(event));\n"
                        + "    return appChainj.appLogFlowable(filter).map(new io.reactivex.functions.Function<org.nervos.appchain.protocol.core.methods.response.Log, TransferEventResponse>() {\n"
                        + "      @java.lang.Override\n"
                        + "      public TransferEventResponse apply(org.nervos.appchain.protocol.core.methods.response.Log log) {\n"
                        + "        org.nervos.appchain.abi.EventValues eventValues = extractEventParameters(event, log);\n"
                        + "        TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "        typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n"
                        + "        typedResponse.from = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n"
                        + "        typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n"
                        + "        typedResponse.value = (java.math.BigInteger) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "        typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "        return typedResponse;\n"
                        + "      }\n"
                        + "    });\n"
                        + "  }\n"
                        + "\n"
                        + "  public static class TransferEventResponse {\n"
                        + "    public byte[] id;\n"
                        + "\n"
                        + "    public java.lang.String from;\n"
                        + "\n"
                        + "    public java.lang.String to;\n"
                        + "\n"
                        + "    public java.math.BigInteger value;\n"
                        + "\n"
                        + "    public java.lang.String message;\n"
                        + "  }\n"
                        + "}\n";
        //CHECKSTYLE:ON

        System.out.println("testBuildEventConstantMultipleValueReturn" + builder.build().toString());

        assertThat(builder.build().toString(), is(expected));
    }

}
