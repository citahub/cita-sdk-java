package org.nervos.appchain.tests;

import java.io.FileInputStream;
import java.util.Properties;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.http.HttpService;

public class Config {

    private static final String configPath = "tests/src/main/resources/config.properties";
    private static final String CHAIN_ID = "ChainId";
    private static final String VERSION = "Version";
    private static final String TEST_NET_ADDR = "TestNetIpAddr";
    private static final String SENDER_PRIVATE_KEY = "SenderPrivateKey";
    private static final String SENDER_ADDR = "SenderAddress";
    private static final String TEST_PRIVATE_KEY_1 = "TestPrivateKey1";
    private static final String TEST_ADDR_1 = "TestAddress1";
    private static final String TEST_PRIVATE_KEY_2 = "TestPrivateKey2";
    private static final String TEST_ADDR_2 = "TestAddress2";
    private static final String TOKEN_SOLIDITY = "TokenSolidity";
    private static final String TOKEN_BIN = "TokenBin";
    private static final String TOKEN_ABI = "TokenAbi";
    private static final String DEFAULT_QUOTA_Transfer = "QuotaForTransfer";
    private static final String DEFAULT_QUOTA_Deployment = "QuotaForDeployment";
    private static final String ADMIN_PRIVATE_KEY = "AdminPrivateKey";

    private Properties props;
    public String chainId;
    public String version;
    public String ipAddr;
    public String primaryPrivKey;
    public String primaryAddr;
    public String auxPrivKey1;
    public String auxAddr1;
    public String auxPrivKey2;
    public String auxAddr2;
    public String tokenSolidity;
    public String tokenBin;
    public String tokenAbi;
    public String defaultQuotaTransfer;
    public String defaultQuotaDeployment;
    public Nervosj service;
    public String adminPrivateKey;


    public Config() {
        props = load();
        loadPropsToAttr(props);
    }

    public Config(String configFilePath) {
        props = load(configFilePath);
        loadPropsToAttr(props);
    }

    public void buildService(boolean debugMode) {
        HttpService.setDebug(debugMode);
        this.service = Nervosj.build(new HttpService(this.ipAddr));
    }


    public static Properties load(String path) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(path));
        } catch (Exception e) {
            System.out.println("Failed to read config at path " + path);
            System.exit(1);
        }
        return props;
    }

    public static Properties load() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configPath));
        } catch (Exception e) {
            System.out.println("Failed to read config file. Error: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

    private void loadPropsToAttr(Properties props) {
        chainId = props.getProperty(CHAIN_ID);
        version = props.getProperty(VERSION);
        ipAddr = props.getProperty(TEST_NET_ADDR);
        primaryPrivKey = props.getProperty(SENDER_PRIVATE_KEY);
        primaryAddr = props.getProperty(SENDER_ADDR);
        auxPrivKey1 = props.getProperty(TEST_PRIVATE_KEY_1);
        auxAddr1 = props.getProperty(TEST_ADDR_1);
        auxPrivKey2 = props.getProperty(TEST_PRIVATE_KEY_2);
        auxAddr2 = props.getProperty(TEST_ADDR_2);
        tokenSolidity = props.getProperty(TOKEN_SOLIDITY);
        tokenBin = props.getProperty(TOKEN_BIN);
        tokenAbi = props.getProperty(TOKEN_ABI);
        defaultQuotaTransfer = props.getProperty(DEFAULT_QUOTA_Transfer);
        defaultQuotaDeployment = props.getProperty(DEFAULT_QUOTA_Deployment);
        adminPrivateKey = props.getProperty(ADMIN_PRIVATE_KEY);
    }

}
