package org.web3j.tests;

import java.util.Properties;
import java.io.FileInputStream;

public class Config {
    private static final String configPath = "tests/src/main/resources/config.properties";
    public static Properties props;

    public final static String CHAIN_ID = "ChainId";
    public final static String VERSION = "Version";
    public final static String TEST_NET_ADDR = "TestNetIpAddr";
    public final static String SENDER_PRIVATE_KEY = "SenderPrivateKey";
    public final static String SENDER_ADDR = "SenderAddress";
    public final static String TEST_PRIVATE_KEY_1 = "TestPrivateKey1";
    public final static String TEST_ADDR_1 = "TestAddress1";
    public final static String TEST_PRIVATE_KEY_2 = "TestPrivateKey2";
    public final static String TEST_ADDR_2 = "TestAddress2";
    public final static String TOKEN_SOLIDITY = "TokenSolidity";
    public final static String TOKEN_BIN = "TokenBin";
    public final static String TOKEN_ABI = "TokenAbi";
    public final static String DEFAULT_QUOTA = "DefaultQuota";

    public static Properties load(String path) {
        props = new Properties();
        try{
            props.load(new FileInputStream(path));
        }catch (Exception e){
            System.out.println("Failed to read config at path " + path);
            System.exit(1);
        }
        return props;
    }

    public static Properties load() {
        try{
            props = new Properties();
            props.load(new FileInputStream(configPath));
        }catch (Exception e){
            System.out.println("Failed to read config file. Error: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

}
