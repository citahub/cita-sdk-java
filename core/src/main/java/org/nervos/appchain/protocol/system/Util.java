package org.nervos.appchain.protocol.system;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.utils.Collection;
import org.nervos.appchain.utils.Numeric;

/**
 * remove this class later.
 * **/

public class Util {

    static String getNonce() {
        Random random = new Random(System.currentTimeMillis());
        return String.valueOf(Math.abs(random.nextLong()));
    }

    static BigInteger getCurrentHeight(AppChainj service) {
        return getCurrentHeight(service, 3);
    }

    private static BigInteger getCurrentHeight(AppChainj service, int retry) {
        int count = 0;
        long height = -1;
        while (count < retry) {
            try {
                height = service.appBlockNumber().send().getBlockNumber().longValue();
            } catch (Exception e) {
                height = -1;
                System.out.println("getBlockNumber failed retry ..");
                try {
                    Thread.sleep(2000);
                } catch (Exception e1) {
                    System.out.println("failed to get block number, Exception: " + e1);
                    System.exit(1);
                }
            }
            count++;
        }
        if (height == -1) {
            System.out.println("Failed to get block number after " + count + " times.");
            System.exit(1);
        }
        return BigInteger.valueOf(height);
    }

    static BigInteger getValidUtilBlock(AppChainj service, int validUntilBlock) {
        return getCurrentHeight(service).add(
                BigInteger.valueOf(validUntilBlock));
    }

    static BigInteger getValidUtilBlock(AppChainj service) {
        return getCurrentHeight(service).add(BigInteger.valueOf(88));
    }

    static String addUpTo64Hex(String hexStr) {
        String result = Numeric.cleanHexPrefix(hexStr);
        int len = 64 - result.length();
        for (int i = 0; i < len; i++) {
            result = "0" + result;
        }
        return Numeric.prependHexPrefix(result);
    }


    static String generateFunSig(String funcName) {
        Function func = new Function(funcName, Collections.emptyList(), Collections.emptyList());
        return FunctionEncoder.encode(func);
    }
}
