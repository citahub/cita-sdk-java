package org.web3j.tests;

import org.web3j.protocol.Web3j;

import java.math.BigInteger;
import java.util.Random;

public class testUtil {
    static BigInteger getNonce(){
        Random random = new Random(System.currentTimeMillis());
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }

    static BigInteger getCurrentHeight(Web3j service){
        return getCurrentHeight(service, 3);
    }

    static BigInteger getCurrentHeight(Web3j service, int retry){
        int count = 0;
        long height = -1;
        while (count < retry) {
            try {
                height = service.ethBlockNumber().send().getBlockNumber().longValue();
            } catch (Exception e){
                height = -1;
                System.out.println("getBlockNumber failed retry ..");
                try {
                    Thread.sleep(2000);
                } catch(Exception e1){
                    System.out.println("failed to get block number, Exception: " + e1);
                    System.exit(1);
                }
            }
            count++;
        }
        if(height == -1){
            System.out.println("Failed to get block number after " + count + " times.");
            System.exit(1);
        }
        return BigInteger.valueOf(height);
    }

    static BigInteger getValidUtilBlock(Web3j service, int validUntilBlock){
        return getCurrentHeight(service).add(BigInteger.valueOf(validUntilBlock));
    }

    static BigInteger getValidUtilBlock(Web3j service){
        return getCurrentHeight(service).add(BigInteger.valueOf(88));
    }
}
