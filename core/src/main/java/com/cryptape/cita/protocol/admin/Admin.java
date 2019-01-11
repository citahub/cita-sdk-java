package com.cryptape.cita.protocol.admin;

import java.math.BigInteger;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.CITAjService;
import com.cryptape.cita.protocol.admin.methods.response.NewAccountIdentifier;
import com.cryptape.cita.protocol.admin.methods.response.PersonalListAccounts;
import com.cryptape.cita.protocol.admin.methods.response.PersonalUnlockAccount;
import com.cryptape.cita.protocol.core.Request;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;

/**
 * JSON-RPC Request object building factory for common Parity and Geth.
 */
public interface Admin extends CITAj {

    static Admin build(CITAjService CITAjService) {
        return new JsonRpc2_0Admin(CITAjService);
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
