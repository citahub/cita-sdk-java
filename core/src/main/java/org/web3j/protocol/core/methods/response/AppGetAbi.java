package org.web3j.protocol.core.methods.response;

import org.web3j.protocol.core.Response;

public class AppGetAbi extends Response<String> {

    public boolean isEmpty() {
        return getResult() == null;
    }

    public String getAbi() {
        return getResult();
    }

}
