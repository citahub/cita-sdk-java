package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

/**
 * app_getTransactionReceipt.
 */
public class AppGetTransactionReceipt extends Response<TransactionReceipt> {

    public TransactionReceipt getTransactionReceipt() {
        return getResult();
    }

}
