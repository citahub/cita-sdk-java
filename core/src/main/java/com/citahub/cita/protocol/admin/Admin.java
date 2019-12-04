package com.citahub.cita.protocol.admin;

import java.math.BigInteger;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.CITAjService;
import com.citahub.cita.protocol.admin.methods.response.NewAccountIdentifier;
import com.citahub.cita.protocol.admin.methods.response.PersonalListAccounts;
import com.citahub.cita.protocol.admin.methods.response.PersonalUnlockAccount;
import com.citahub.cita.protocol.core.Request;
import com.citahub.cita.protocol.core.methods.request.Transaction;
import com.citahub.cita.protocol.core.methods.response.AppSendTransaction;

/**
 * JSON-RPC Request object building factory for common Parity and Geth.
 */
public interface Admin extends CITAj {

    static Admin build(CITAjService citajService) {
        return new JsonRpc2_0Admin(citajService);
    }

    public Request<?, PersonalListAccounts> personalListAccounts();

    public Request<?, NewAccountIdentifier> personalNewAccount(String password);

    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase, BigInteger duration);

    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase);

    public Request<?, AppSendTransaction> personalSendTransaction(
            Transaction transaction, String password);

}
