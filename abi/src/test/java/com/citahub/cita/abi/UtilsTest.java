package com.citahub.cita.abi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.citahub.cita.abi.datatypes.generated.Int64;
import com.citahub.cita.abi.datatypes.generated.Uint256;
import com.citahub.cita.abi.datatypes.generated.Uint64;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import com.citahub.cita.abi.datatypes.Bool;
import com.citahub.cita.abi.datatypes.DynamicArray;
import com.citahub.cita.abi.datatypes.DynamicBytes;
import com.citahub.cita.abi.datatypes.Fixed;
import com.citahub.cita.abi.datatypes.Int;
import com.citahub.cita.abi.datatypes.StaticArray;
import com.citahub.cita.abi.datatypes.Ufixed;
import com.citahub.cita.abi.datatypes.Uint;
import com.citahub.cita.abi.datatypes.Utf8String;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

        MatcherAssert.assertThat(Utils.typeMap(input, Uint256.class),
                equalTo(Arrays.asList(
                        new Uint256(BigInteger.ZERO),
                        new Uint256(BigInteger.ONE),
                        new Uint256(BigInteger.TEN))));
    }

    @Test
    public void testTypeMapEmpty() {
        MatcherAssert.assertThat(Utils.typeMap(new ArrayList<BigInteger>(), Uint256.class),
                equalTo(new ArrayList<Uint256>()));
    }
}
