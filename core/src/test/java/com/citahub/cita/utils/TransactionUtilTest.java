package com.citahub.cita.utils;

import com.citahub.cita.protobuf.Blockchain;
import com.google.gson.Gson;
import org.junit.Test;

/**
 * Created by duanyytop on 2019-01-11.
 */
public class TransactionUtilTest {
    private String content = "0x0a9e011213393031343037303133313139383739353538361880ade20420682a2460fe47b100000000000000000000000000000000000000000000000000000000000000013220000000000000000000000000000000000000000000000000000000000000000040014a14ed3762cfa63030826ce8178d0deca2f62a9fec015220000000000000000000000000000000000000000000000000000000000000000112413f05795f92c35051435d49cba6df0912ffe9c0b71f4b5fecfac0a7c9a37e376553c51d2a86eb74334fa211759c8908f76a194b75525f76c15943fe255951f69300";

    @Test
    public void getTransactionTest() {
        Blockchain.Transaction transaction = TransactionUtil.getTransaction(content);
        System.out.println(new Gson().toJson(transaction));
    }

}
