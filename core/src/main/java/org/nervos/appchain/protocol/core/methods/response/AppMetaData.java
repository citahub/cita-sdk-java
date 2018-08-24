package org.nervos.appchain.protocol.core.methods.response;

import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.protocol.core.Response;


public class AppMetaData extends Response<AppMetaData.AppMetaDataResult> {

    public AppMetaDataResult getAppMetaDataResult() {
        return getResult();
    }

    public boolean isEmpty() {
        return getResult() == null;
    }

    public static class AppMetaDataResult {
        public int chainId;
        public String chainName;
        public String operator;
        public String website;
        public String genesisTimestamp;
        public Address[] validators;
        public int blockInterval;
        public String tokenName;
        public String tokenSymbol;
        public String tokenAvatar;
    }

}
