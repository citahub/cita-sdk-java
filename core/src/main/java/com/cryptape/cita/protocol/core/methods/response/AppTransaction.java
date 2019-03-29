package com.cryptape.cita.protocol.core.methods.response;

import com.cryptape.cita.protocol.core.Response;

public class AppTransaction extends Response<Transaction> {

    public Transaction getTransaction() {
        return getResult();
    }

}
