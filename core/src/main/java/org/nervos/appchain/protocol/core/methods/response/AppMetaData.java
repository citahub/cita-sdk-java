package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.protocol.core.Response;


public class AppMetaData extends Response<AppMetaData.EthMetaDataResult> {

    public EthMetaDataResult getEthMetaDataResult() {
        return getResult();
    }

    public boolean isEmpty() {
        return getResult() == null;
    }

    public static class EthMetaDataResult {
        public int chainId;
        public String chainName;
        public String operator;
        public String website;
        public String genesisTimestamp;
        public String basicToken;
        public Address[] validators;
        public int blockInterval;
    }

}
