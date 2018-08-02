package org.nervos.appchain.crypto;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.nervos.appchain.crypto.TransactionUtils.generateTransactionHashHexEncoded;

public class TransactionUtilsTest {

    @Test
    public void testGenerateTransactionHash() {
        assertThat(generateTransactionHashHexEncoded(
                TransactionEncoderTest.createContractTransaction(), SampleKeys.CREDENTIALS),
                is("0x7e29b4e8a8b155638965cadb09ff0f7abbbc50eca9d85857c60f2d1e7bb749fd"));
    }

    @Test
    public void testGenerateEip155TransactionHash() {
        assertThat(generateTransactionHashHexEncoded(
                TransactionEncoderTest.createContractTransaction(), (byte) 1,
                SampleKeys.CREDENTIALS),
                is("0x62140b583dc4421fbdda95157fd8e3737588e7f7ede321f85ce9565199d3be89"));
    }
}
