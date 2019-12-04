package com.citahub.cita.codegen;

import org.junit.Test;

import com.citahub.cita.TempFileProvider;


public class AbiTypesMapperGeneratorTest extends TempFileProvider {

    @Test
    public void testGeneration() throws Exception {
        AbiTypesMapperGenerator.main(new String[] { tempDirPath });
    }
}
