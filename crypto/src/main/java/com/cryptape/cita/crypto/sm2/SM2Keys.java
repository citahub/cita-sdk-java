package com.cryptape.cita.crypto.sm2;

import java.io.IOException;
import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;
import com.cryptape.cita.utils.HexUtil;
import com.cryptape.cita.utils.Numeric;
import com.cryptape.cita.utils.Strings;

public class SM2Keys {

    private static final int PUBLIC_KEY_SIZE = 64;

    private static final int ADDRESS_SIZE = 160;
    private static final int ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE >> 2;
    private static final int PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE << 1;

    public static String getAddress(ECPoint key) {
        String publicKey = key.getRawXCoord().toString() + key.getRawYCoord().toString();
        return getAddress(publicKey);
    }

    public static String getAddress(String publicKey) {
        String publicKeyNoPrefix = Numeric.cleanHexPrefix(publicKey);

        if (publicKeyNoPrefix.length() < PUBLIC_KEY_LENGTH_IN_HEX) {
            publicKeyNoPrefix = Strings.zeros(
                    PUBLIC_KEY_LENGTH_IN_HEX - publicKeyNoPrefix.length())
                    + publicKeyNoPrefix;
        }
        String hash = null;
        try {
            hash = new BigInteger(1, (SM3.hash(HexUtil.hexToBytes(publicKeyNoPrefix)))).toString(16);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hash.substring(hash.length() - ADDRESS_LENGTH_IN_HEX);  // right most 160 bits
    }
}
