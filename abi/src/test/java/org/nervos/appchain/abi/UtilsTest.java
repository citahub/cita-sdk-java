package org.nervos.appchain.abi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.nervos.appchain.abi.datatypes.Bool;
import org.nervos.appchain.abi.datatypes.DynamicArray;
import org.nervos.appchain.abi.datatypes.DynamicBytes;
import org.nervos.appchain.abi.datatypes.Fixed;
import org.nervos.appchain.abi.datatypes.Int;
import org.nervos.appchain.abi.datatypes.StaticArray;
import org.nervos.appchain.abi.datatypes.Ufixed;
import org.nervos.appchain.abi.datatypes.Uint;
import org.nervos.appchain.abi.datatypes.Utf8String;
import org.nervos.appchain.abi.datatypes.generated.Int64;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.abi.datatypes.generated.Uint64;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.nervos.appchain.abi.Utils.typeMap;

public class UtilsTest {

    @Test
    public void testGetTypeName() throws ClassNotFoundException {
        assertThat(Utils.getTypeName(new TypeReference<Uint>(){}), is("uint256"));
        assertThat(Utils.getTypeName(new TypeReference<Int>(){}), is("int256"));
        assertThat(Utils.getTypeName(new TypeReference<Ufixed>(){}), is("ufixed256"));
        assertThat(Utils.getTypeName(new TypeReference<Fixed>(){}), is("fixed256"));

        assertThat(Utils.getTypeName(new TypeReference<Uint64>(){}), is("uint64"));
        assertThat(Utils.getTypeName(new TypeReference<Int64>(){}), is("int64"));
        assertThat(Utils.getTypeName(new TypeReference<Bool>(){}), is("bool"));
        assertThat(Utils.getTypeName(new TypeReference<Utf8String>(){}), is("string"));
        assertThat(Utils.getTypeName(new TypeReference<DynamicBytes>(){}), is("bytes"));

        assertThat(Utils.getTypeName(
                new TypeReference.StaticArrayTypeReference<StaticArray<Uint>>(5){}),
                is("uint256[5]"));
        assertThat(Utils.getTypeName(
                new TypeReference<DynamicArray<Uint>>(){}),
                is("uint256[]"));
    }

    @Test
    public void testTypeMap() throws Exception {
        final List<BigInteger> input = Arrays.asList(
                BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN);

        assertThat(typeMap(input, Uint256.class),
                equalTo(Arrays.asList(
                        new Uint256(BigInteger.ZERO),
                        new Uint256(BigInteger.ONE),
                        new Uint256(BigInteger.TEN))));
    }

    @Test
    public void testTypeMapEmpty() {
        assertThat(typeMap(new ArrayList<BigInteger>(), Uint256.class),
                equalTo(new ArrayList<Uint256>()));
    }
}
