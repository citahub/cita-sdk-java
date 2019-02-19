package com.cryptape.cita.abi.datatypes.generated;

import java.math.BigInteger;
import com.cryptape.cita.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use com.cryptape.cita.codegen.AbiTypesGenerator in the
 */
public class Uint48 extends Uint {
    public static final Uint48 DEFAULT = new Uint48(BigInteger.ZERO);

    public Uint48(BigInteger value) {
        super(48, value);
    }

    public Uint48(long value) {
        this(BigInteger.valueOf(value));
    }
}
