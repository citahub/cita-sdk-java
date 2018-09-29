package org.nervos.appchain.tests;

import java.io.FileInputStream;
import java.util.Properties;

public class Config {
    private static final String configPath = "tests/src/main/resources/config.properties";
    public static Properties props;

    public static final String CHAIN_ID = "ChainId";
    public static final String VERSION = "Version";
    public static final String TEST_NET_ADDR = "TestNetIpAddr";
    public static final String SENDER_PRIVATE_KEY = "SenderPrivateKey";
    public static final String SENDER_ADDR = "SenderAddress";
    public static final String TEST_PRIVATE_KEY_1 = "TestPrivateKey1";
    public static final String TEST_ADDR_1 = "TestAddress1";
    public static final String TEST_PRIVATE_KEY_2 = "TestPrivateKey2";
    public static final String TEST_ADDR_2 = "TestAddress2";
    public static final String TOKEN_SOLIDITY = "TokenSolidity";
    public static final String TOKEN_BIN = "TokenBin";
    public static final String TOKEN_ABI = "TokenAbi";
    public static final String DEFAULT_QUOTA = "DefaultQuota";
    public static final String DEFAULT_QUOTA_Transfer = "QuotaForTransfer";
    public static final String DEFAULT_QUOTA_Deployment = "QuotaForDeployment";

    public static Properties load(String path) {
        props = new Properties();
        try {
            props.load(new FileInputStream(path));
        } catch (Exception e) {
            System.out.println("Failed to read config at path " + path);
            System.exit(1);
        }
        return props;
    }

    public static Properties load() {
        try {
            props = new Properties();
            props.load(new FileInputStream(configPath));
        } catch (Exception e) {
            System.out.println("Failed to read config file. Error: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

}
