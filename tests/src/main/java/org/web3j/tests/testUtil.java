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
        long height = -1;
        while (height == -1) {
            try {
                height = service.ethBlockNumber().send().getBlockNumber().longValue();
            } catch (Exception e){
                height = -1;
                System.out.println("getBlockNumber failed retry ..");
                try {Thread.sleep(2000);} catch(Exception e1){}
            }
        }
        return BigInteger.valueOf(height);
    }

    static BigInteger getValidUtilBlock(Web3j service, int util){
        return getCurrentHeight(service).add(BigInteger.valueOf(util));
    }

    static BigInteger getValidUtilBlock(Web3j service){
        return getCurrentHeight(service).add(BigInteger.valueOf(88));
    }
}
