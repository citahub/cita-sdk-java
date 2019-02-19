package com.cryptape.cita.abi.datatypes.generated;

import java.math.BigInteger;
import com.cryptape.cita.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use com.cryptape.cita.codegen.AbiTypesGenerator in the
 */
public class Int16 extends Int {
    public static final Int16 DEFAULT = new Int16(BigInteger.ZERO);

    public Int16(BigInteger value) {
        super(16, value);
    }

    public Int16(long value) {
        this(BigInteger.valueOf(value));
    }
}
