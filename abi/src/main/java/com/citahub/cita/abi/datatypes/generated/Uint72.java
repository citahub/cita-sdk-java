package com.citahub.cita.abi.datatypes.generated;

import java.math.BigInteger;
import com.citahub.cita.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use com.citahub.cita.codegen.AbiTypesGenerator in the
 */
public class Uint72 extends Uint {
    public static final Uint72 DEFAULT = new Uint72(BigInteger.ZERO);

    public Uint72(BigInteger value) {
        super(72, value);
    }

    public Uint72(long value) {
        this(BigInteger.valueOf(value));
    }
}
