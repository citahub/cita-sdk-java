package org.nervos.appchain.protocol.admin;

import java.math.BigInteger;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjService;
import org.nervos.appchain.protocol.admin.methods.response.NewAccountIdentifier;
import org.nervos.appchain.protocol.admin.methods.response.PersonalListAccounts;
import org.nervos.appchain.protocol.admin.methods.response.PersonalUnlockAccount;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;

/**
 * JSON-RPC Request object building factory for common Parity and Geth. 
 */
public interface Admin extends Nervosj {

    static Admin build(NervosjService nervosjService) {
        return new JsonRpc2_0Admin(nervosjService);
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
