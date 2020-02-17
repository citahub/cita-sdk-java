package com.citahub.cita.abi.datatypes.generated;

import java.math.BigInteger;
import com.citahub.cita.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use com.citahub.cita.codegen.AbiTypesGenerator in the
 */
public class Int232 extends Int {
    public static final Int232 DEFAULT = new Int232(BigInteger.ZERO);

    public Int232(BigInteger value) {
        super(232, value);
    }

    public Int232(long value) {
        this(BigInteger.valueOf(value));
    }
}
