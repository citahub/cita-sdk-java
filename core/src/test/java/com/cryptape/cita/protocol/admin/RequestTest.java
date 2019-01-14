package com.cryptape.cita.protocol.admin;

import java.math.BigInteger;

import org.junit.Ignore;
import org.junit.Test;

import com.cryptape.cita.protocol.RequestTester;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.http.HttpService;
/*
* Method tested in the file are used to do wallet management in console for ethereum.
* personal_xx rpc requests are not supported in CITA, so test case in the class is not required.
* I just keep admin tests(RequestTest, ResponseTest) if they added in the future.
* */

public class RequestTest extends RequestTester {

    private Admin web3j;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        web3j = Admin.build(httpService);
    }


    @Test
    public void testPersonalListAccounts() throws Exception {
        web3j.personalListAccounts().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"personal_listAccounts\","
                + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testPersonalNewAccount() throws Exception {
        web3j.personalNewAccount("password").send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"personal_newAccount\","
                + "\"params\":[\"password\"],\"id\":1}");
    } 

    @Ignore
    //personal_sendTransaction is not supported in CITA
    //to make this work, I need to refactor Transaction, I will do it in next sprint if necessary.
    @Test
    public void testPersonalSendTransaction() throws Exception {
        web3j.personalSendTransaction(
                new Transaction(
                        "FROM",
                        "1",
                        BigInteger.TEN.longValue(),
                        BigInteger.ONE.longValue(),
                        0,
                        BigInteger.ONE,
                        "0",
                        "DATA"
                ),
                "password"
        ).send();

        //CHECKSTYLE:OFF
        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"personal_sendTransaction\",\"params\":[{\"from\":\"FROM\",\"to\":\"TO\",\"gas\":\"0x1\",\"gasPrice\":\"0xa\",\"value\":\"0x0\",\"data\":\"0xDATA\",\"nonce\":\"0x1\"},\"password\"],\"id\":1}");
        //CHECKSTYLE:ON
    }   

    @Test
    public void testPersonalUnlockAccount() throws Exception {
        web3j.personalUnlockAccount(
                "0xfc390d8a8ddb591b010fda52f4db4945742c3809", "hunter2", BigInteger.ONE).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"personal_unlockAccount\","
                + "\"params\":[\"0xfc390d8a8ddb591b010fda52f4db4945742c3809\",\"hunter2\",1],"
                + "\"id\":1}");
    }

    @Test
    public void testPersonalUnlockAccountNoDuration() throws Exception {
        web3j.personalUnlockAccount("0xfc390d8a8ddb591b010fda52f4db4945742c3809", "hunter2").send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"personal_unlockAccount\","
                + "\"params\":[\"0xfc390d8a8ddb591b010fda52f4db4945742c3809\",\"hunter2\",null],"
                + "\"id\":1}");
    }
}
