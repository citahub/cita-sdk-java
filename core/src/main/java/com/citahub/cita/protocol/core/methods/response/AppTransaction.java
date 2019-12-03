package com.citahub.cita.protocol.core.methods.response;

import com.citahub.cita.protocol.core.Response;

public class AppTransaction extends Response<Transaction> {

    public Transaction getTransaction() {
        return getResult();
    }

}
