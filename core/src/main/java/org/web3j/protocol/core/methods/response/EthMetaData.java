package org.web3j.protocol.core.methods.response;

import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.Response;


public class EthMetaData extends Response<EthMetaData.EthMetaDataResult> {

    public EthMetaDataResult getEthMetaDataResult() {
        return getResult();
    }

    public boolean isEmpty() {
        return getResult() == null;
    }

    public static class EthMetaDataResult {
        public int chainId;
        public String chainName;
        public String operator;    //运营方
        public String website;     //网站
        public String genesisTimestamp;
        public String basicToken;
        public Address[] validators;
        public int blockInterval;
    }

}
