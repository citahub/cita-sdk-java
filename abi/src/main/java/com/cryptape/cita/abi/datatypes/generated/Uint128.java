package com.cryptape.cita.abi.datatypes.generated;

import java.math.BigInteger;
import com.cryptape.cita.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use com.cryptape.cita.codegen.AbiTypesGenerator in the
 */
public class Uint128 extends Uint {
    public static final Uint128 DEFAULT = new Uint128(BigInteger.ZERO);

    public Uint128(BigInteger value) {
        super(128, value);
    }

    public Uint128(long value) {
        this(BigInteger.valueOf(value));
    }
}
