package com.citahub.cita.abi.datatypes.generated;

import java.util.List;
import com.citahub.cita.abi.datatypes.StaticArray;
import com.citahub.cita.abi.datatypes.Type;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use com.citahub.cita.codegen.AbiTypesGenerator in the
 */
public class StaticArray3<T extends Type> extends StaticArray<T> {
    public StaticArray3(List<T> values) {
        super(3, values);
    }

    @SafeVarargs
    public StaticArray3(T... values) {
        super(3, values);
    }
}
