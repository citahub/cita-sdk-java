package org.nervos.appchain.crypto;

import java.math.BigInteger;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

import org.nervos.appchain.rlp.RlpString;
import org.nervos.appchain.rlp.RlpType;
import org.nervos.appchain.utils.Numeric;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TransactionEncoderTest {

    @Test
    public void testSignMessage() {
        byte[] signedMessage = TransactionEncoder.signMessage(
                createEtherTransaction(), SampleKeys.CREDENTIALS);
        String hexMessage = Numeric.toHexString(signedMessage);
        assertThat(hexMessage,
                is("0xf86080010a840add5355933932323333373230333"
                        + "63835343737353830378001a0dd9316183231a788f6"
                        + "1b8797d8f91abac283159ad343eb19a09e048616c9a"
                        + "fb7a01c9bf41469e6bdffd8e39c350c053c5b311cde"
                        + "df481caea071fa43682afc1472"));
    }

    @Test
    public void testEtherTransactionAsRlpValues() {
        List<RlpType> rlpStrings = TransactionEncoder.asRlpValues(createEtherTransaction(),
                new Sign.SignatureData((byte) 0, new byte[32], new byte[32]));
        assertThat(rlpStrings.size(), is(9));
        assertThat(rlpStrings.get(3),
                IsEqual.<RlpType>equalTo(RlpString.create(new BigInteger("add5355", 16))));
    }

    @Test
    public void testContractAsRlpValues() {
        List<RlpType> rlpStrings = TransactionEncoder.asRlpValues(
                createContractTransaction(), null);
        assertThat(rlpStrings.size(), is(6));
        assertThat(rlpStrings.get(3), CoreMatchers.<RlpType>is(RlpString.create("")));
    }

    @Test
    public void testEip155Encode() {
        assertThat(TransactionEncoder.encode(createEip155RawTransaction(), (byte) 1),
                is(Numeric.hexStringToByteArray(
                        "f837098504a817c800825208943535353535353535"
                                + "35353535353535353535353593313030303030303"
                                + "0303030303030303030303080018080")));
    }

    @Test
    public void testEip155Transaction() {
        // https://github.com/ethereum/EIPs/issues/155
        Credentials credentials = Credentials.create(
                "0x4646464646464646464646464646464646464646464646464646464646464646");
        assertThat(TransactionEncoder.signMessage(
                createEip155RawTransaction(), (byte) 1, credentials),
                is(Numeric.hexStringToByteArray("f877098504a817c80082520894353535"
                        + "353535353535353535353535353535353593313"
                        + "0303030303030303030303030303030303080"
                        + "0aa080f800b1b9cbd6a09d063f7f4857aa271"
                        + "ffd34bedd7cbc65ec672f7c21f16798a017ac"
                        + "2005fccd66cd6743d7cdde2cc47b75cee987e"
                        + "161f7f0815d6e0accf24009")));
    }

    private static RawTransaction createEtherTransaction() {
        return RawTransaction.createEtherTransaction(
                BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN, "0xadd5355",
                String.valueOf(Long.MAX_VALUE));
    }

    static RawTransaction createContractTransaction() {
        return RawTransaction.createContractTransaction(
                BigInteger.ZERO, BigInteger.ONE,
                BigInteger.TEN, String.valueOf(Long.MAX_VALUE),
                "01234566789");
    }

    private static RawTransaction createEip155RawTransaction() {
        return RawTransaction.createEtherTransaction(
                BigInteger.valueOf(9), BigInteger.valueOf(20000000000L),
                BigInteger.valueOf(21000),
                "0x3535353535353535353535353535353535353535",
                String.valueOf(1000000000000000000L));
    }
}
