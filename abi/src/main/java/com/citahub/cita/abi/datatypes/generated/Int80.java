package com.citahub.cita.abi.datatypes.generated;

import java.math.BigInteger;
import com.citahub.cita.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use com.citahub.cita.codegen.AbiTypesGenerator in the
 */
public class Int80 extends Int {
    public static final Int80 DEFAULT = new Int80(BigInteger.ZERO);

    public Int80(BigInteger value) {
        super(80, value);
    }

    public Int80(long value) {
        this(BigInteger.valueOf(value));
    }
}
