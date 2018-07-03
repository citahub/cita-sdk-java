package org.web3j.utils;

import org.web3j.protobuf.Blockchain;
import org.web3j.protobuf.ConvertStrByte;

public class TransactionUtil {

    public static Blockchain.Transaction getTransaction(String content) {
        Blockchain.Transaction transaction =null;
        try {
            Blockchain.UnverifiedTransaction unverifiedTransaction =
                    Blockchain.UnverifiedTransaction.parseFrom(ConvertStrByte.hexStringToBytes(content.substring(2)));
            transaction = unverifiedTransaction.getTransaction();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return transaction;
    }

    public static String getTo(String content) {
        Blockchain.Transaction transaction= getTransaction(content);
        return transaction.getTo();
    }

    public static String getNonce(String content) {
        Blockchain.Transaction transaction= getTransaction(content);
        return transaction.getNonce();
    }

    public static long getQuota(String content) {
        Blockchain.Transaction transaction= getTransaction(content);
        return transaction.getQuota();
    }

    public static String getData(String content) {
        Blockchain.Transaction transaction= getTransaction(content);
        byte[] bDate = transaction.getData().toByteArray();
        return ConvertStrByte.bytesToHexString(bDate);
    }

    public static long getValidUntilBlock(String content) {
        Blockchain.Transaction transaction= getTransaction(content);
        return transaction.getValidUntilBlock();
    }

}
