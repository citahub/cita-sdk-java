package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

/**
 * app_getTransactionReceipt.
 */
public class AppGetTransactionReceipt extends Response<TransactionReceipt> {

    public TransactionReceipt getTransactionReceipt() {
        return getResult();
    }

}
