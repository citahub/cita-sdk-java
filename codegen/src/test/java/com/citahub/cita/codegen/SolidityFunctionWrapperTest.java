package com.citahub.cita.codegen;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.citahub.cita.abi.datatypes.generated.Int256;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;

import com.citahub.cita.TempFileProvider;
import com.citahub.cita.abi.datatypes.Address;
import com.citahub.cita.abi.datatypes.Bool;
import com.citahub.cita.abi.datatypes.DynamicArray;
import com.citahub.cita.abi.datatypes.DynamicBytes;
import com.citahub.cita.abi.datatypes.StaticArray;
import com.citahub.cita.abi.datatypes.Utf8String;
import com.citahub.cita.abi.datatypes.generated.Bytes32;
import com.citahub.cita.abi.datatypes.generated.StaticArray10;
import com.citahub.cita.abi.datatypes.generated.Uint256;
import com.citahub.cita.abi.datatypes.generated.Uint64;
import com.citahub.cita.protocol.core.methods.response.AbiDefinition;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static com.citahub.cita.codegen.SolidityFunctionWrapper.buildTypeName;
import static com.citahub.cita.codegen.SolidityFunctionWrapper.createValidParamName;
import static com.citahub.cita.codegen.SolidityFunctionWrapper.getEventNativeType;
import static com.citahub.cita.codegen.SolidityFunctionWrapper.getNativeType;


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
                "public com.citahub.cita.protocol.core.RemoteCall<com.citahub.cita.protocol.core.methods.response.TransactionReceipt> functionName(java.math.BigInteger param, java.lang.Long quota, java.lang.String nonce, java.lang.Long validUntilBlock, java.lang.Integer version, java.math.BigInteger chainId, java.lang.String value) {\n" +
                        "  com.citahub.cita.abi.datatypes.Function function = new com.citahub.cita.abi.datatypes.Function(\n" +
                        "      \"functionName\", \n" +
                        "      java.util.Arrays.<com.citahub.cita.abi.datatypes.Type>asList(new com.citahub.cita.abi.datatypes.generated.Uint8(param)), \n" +
                        "      java.util.Collections.<com.citahub.cita.abi.TypeReference<?>>emptyList());\n" +
                        "  return executeRemoteCallTransaction(function, quota, nonce, validUntilBlock, version, chainId, value);\n" +
                        "}\n";
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
                "public com.citahub.cita.protocol.core.RemoteCall<com.citahub.cita.protocol.core.methods.response.TransactionReceipt> functionName(java.math.BigInteger param, java.math.BigInteger weiValue, java.lang.Long quota, java.lang.String nonce, java.lang.Long validUntilBlock, java.lang.Integer version, java.math.BigInteger chainId, java.lang.String value) {\n" +
                        "  com.citahub.cita.abi.datatypes.Function function = new com.citahub.cita.abi.datatypes.Function(\n" +
                        "      \"functionName\", \n" +
                        "      java.util.Arrays.<com.citahub.cita.abi.datatypes.Type>asList(new com.citahub.cita.abi.datatypes.generated.Uint8(param)), \n" +
                        "      java.util.Collections.<com.citahub.cita.abi.TypeReference<?>>emptyList());\n" +
                        "  return executeRemoteCallTransaction(function, weiValue, quota, nonce, validUntilBlock, version, chainId, value);\n" +
                        "}\n";
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
                "public com.citahub.cita.protocol.core.RemoteCall<java.math.BigInteger> functionName(java.math.BigInteger param) {\n" +
                        "  com.citahub.cita.abi.datatypes.Function function = new com.citahub.cita.abi.datatypes.Function(\"functionName\", \n" +
                        "      java.util.Arrays.<com.citahub.cita.abi.datatypes.Type>asList(new com.citahub.cita.abi.datatypes.generated.Uint8(param)), \n" +
                        "      java.util.Arrays.<com.citahub.cita.abi.TypeReference<?>>asList(new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.generated.Int8>() {}));\n" +
                        "  return executeRemoteCallSingleValueReturn(function, java.math.BigInteger.class);\n" +
                        "}\n";
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
        String expected = "public com.citahub.cita.protocol.core.RemoteCall<com.citahub.cita.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>> functionName(java.math.BigInteger param1, java.math.BigInteger param2) {\n" +
                "  final com.citahub.cita.abi.datatypes.Function function = new com.citahub.cita.abi.datatypes.Function(\"functionName\", \n" +
                "      java.util.Arrays.<com.citahub.cita.abi.datatypes.Type>asList(new com.citahub.cita.abi.datatypes.generated.Uint8(param1), \n" +
                "      new com.citahub.cita.abi.datatypes.generated.Uint32(param2)), \n" +
                "      java.util.Arrays.<com.citahub.cita.abi.TypeReference<?>>asList(new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.generated.Int8>() {}, new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.generated.Int32>() {}));\n" +
                "  return new com.citahub.cita.protocol.core.RemoteCall<com.citahub.cita.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>>(\n" +
                "      new java.util.concurrent.Callable<com.citahub.cita.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>>() {\n" +
                "        @java.lang.Override\n" +
                "        public com.citahub.cita.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger> call() throws java.lang.Exception {\n" +
                "          java.util.List<com.citahub.cita.abi.datatypes.Type> results = executeCallMultipleValueReturn(function);;\n" +
                "          return new com.citahub.cita.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>(\n" +
                "              (java.math.BigInteger) results.get(0).getValue(), \n" +
                "              (java.math.BigInteger) results.get(1).getValue());\n" +
                "        }\n" +
                "      });\n" +
                "}\n";
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
                "class testClass {\n" +
                        "  public java.util.List<TransferEventResponse> getTransferEvents(com.citahub.cita.protocol.core.methods.response.TransactionReceipt transactionReceipt) {\n" +
                        "    final com.citahub.cita.abi.datatypes.Event event = new com.citahub.cita.abi.datatypes.Event(\"Transfer\", \n" +
                        "        java.util.Arrays.<com.citahub.cita.abi.TypeReference<?>>asList(new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Utf8String>() {}, new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Address>() {}, new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Address>() {}),\n" +
                        "        java.util.Arrays.<com.citahub.cita.abi.TypeReference<?>>asList(new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.generated.Uint256>() {}, new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Utf8String>() {}));\n" +
                        "    java.util.List<com.citahub.cita.abi.EventValues> valueList = extractEventParameters(event, transactionReceipt);\n" +
                        "    java.util.ArrayList<TransferEventResponse> responses = new java.util.ArrayList<TransferEventResponse>(valueList.size());\n" +
                        "    for (com.citahub.cita.abi.EventValues eventValues : valueList) {\n" +
                        "      TransferEventResponse typedResponse = new TransferEventResponse();\n" +
                        "      typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n" +
                        "      typedResponse.from = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n" +
                        "      typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n" +
                        "      typedResponse.value = (java.math.BigInteger) eventValues.getNonIndexedValues().get(0).getValue();\n" +
                        "      typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n" +
                        "      responses.add(typedResponse);\n" +
                        "    }\n" +
                        "    return responses;\n" +
                        "  }\n" +
                        "\n" +
                        "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(com.citahub.cita.protocol.core.DefaultBlockParameter startBlock, com.citahub.cita.protocol.core.DefaultBlockParameter endBlock) {\n" +
                        "    final com.citahub.cita.abi.datatypes.Event event = new com.citahub.cita.abi.datatypes.Event(\"Transfer\", \n" +
                        "        java.util.Arrays.<com.citahub.cita.abi.TypeReference<?>>asList(new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Utf8String>() {}, new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Address>() {}, new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Address>() {}),\n" +
                        "        java.util.Arrays.<com.citahub.cita.abi.TypeReference<?>>asList(new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.generated.Uint256>() {}, new com.citahub.cita.abi.TypeReference<com.citahub.cita.abi.datatypes.Utf8String>() {}));\n" +
                        "    com.citahub.cita.protocol.core.methods.request.AppFilter filter = new com.citahub.cita.protocol.core.methods.request.AppFilter(startBlock, endBlock, getContractAddress());\n" +
                        "    filter.addSingleTopic(com.citahub.cita.abi.EventEncoder.encode(event));\n" +
                        "    return citaj.appLogFlowable(filter).map(new io.reactivex.functions.Function<com.citahub.cita.protocol.core.methods.response.Log, TransferEventResponse>() {\n" +
                        "      @java.lang.Override\n" +
                        "      public TransferEventResponse apply(com.citahub.cita.protocol.core.methods.response.Log log) {\n" +
                        "        com.citahub.cita.abi.EventValues eventValues = extractEventParameters(event, log);\n" +
                        "        TransferEventResponse typedResponse = new TransferEventResponse();\n" +
                        "        typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n" +
                        "        typedResponse.from = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n" +
                        "        typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n" +
                        "        typedResponse.value = (java.math.BigInteger) eventValues.getNonIndexedValues().get(0).getValue();\n" +
                        "        typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n" +
                        "        return typedResponse;\n" +
                        "      }\n" +
                        "    });\n" +
                        "  }\n" +
                        "\n" +
                        "  public static class TransferEventResponse {\n" +
                        "    public byte[] id;\n" +
                        "\n" +
                        "    public java.lang.String from;\n" +
                        "\n" +
                        "    public java.lang.String to;\n" +
                        "\n" +
                        "    public java.math.BigInteger value;\n" +
                        "\n" +
                        "    public java.lang.String message;\n" +
                        "  }\n" +
                        "}\n";
        //CHECKSTYLE:ON

        System.out.println("testBuildEventConstantMultipleValueReturn" + builder.build().toString());

        assertThat(builder.build().toString(), is(expected));
    }

}
