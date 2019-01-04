package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.appchain.utils.Numeric;

public class TestUtil {

    static byte[] convertHexToBytes(String hex) {
        String clearedStr = Numeric.cleanHexPrefix(hex);
        return ConvertStrByte.hexStringToBytes(clearedStr);
    }

    static int getVersion(AppChainj service) {
        AppMetaData appMetaData = null;
        try {
            appMetaData = service.appMetaData(DefaultBlockParameterName.PENDING).send();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return appMetaData.getAppMetaDataResult().getVersion();
    }

    static BigInteger getChainId(AppChainj service) {
        AppMetaData appMetaData = null;
        try {
            appMetaData = service.appMetaData(DefaultBlockParameterName.PENDING).send();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return appMetaData.getAppMetaDataResult().getChainId();
    }

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
}
