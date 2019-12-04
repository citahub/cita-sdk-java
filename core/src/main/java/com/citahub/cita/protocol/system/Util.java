package com.citahub.cita.protocol.system;

import static com.citahub.cita.abi.FunctionEncoder.buildMethodId;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;

import com.citahub.cita.abi.FunctionEncoder;
import com.citahub.cita.abi.datatypes.Function;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.utils.Numeric;

/**
 * remove this class later.
 * **/

public class Util {

    static String getNonce() {
        Random random = new Random(System.currentTimeMillis());
        return String.valueOf(Math.abs(random.nextLong()));
    }

    static BigInteger getCurrentHeight(CITAj service) {
        return getCurrentHeight(service, 3);
    }

    private static BigInteger getCurrentHeight(CITAj service, int retry) {
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

    static BigInteger getValidUtilBlock(CITAj service, int validUntilBlock) {
        return getCurrentHeight(service).add(
                BigInteger.valueOf(validUntilBlock));
    }

    static BigInteger getValidUtilBlock(CITAj service) {
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


    static String generateFunSig(String funcSignature) {
        if(funcSignature.contains("(") && funcSignature.contains(")")){
            return buildMethodId(funcSignature);
        } else {
            // compatible with old ways of using, only the func name
            return buildMethodId(funcSignature+"()");
        }
    }

    static String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
}
