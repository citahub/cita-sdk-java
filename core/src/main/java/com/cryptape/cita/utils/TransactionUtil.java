package com.cryptape.cita.utils;

import java.math.BigInteger;
import java.security.SignatureException;

import com.cryptape.cita.crypto.Keys;
import com.cryptape.cita.crypto.Sign;
import com.cryptape.cita.protobuf.Blockchain;
import com.cryptape.cita.protobuf.ConvertStrByte;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class TransactionUtil {

    public static Blockchain.Transaction getTransaction(String content) {
        Blockchain.Transaction transaction = null;
        try {
            Blockchain.UnverifiedTransaction unverifiedTransaction =
                    Blockchain.UnverifiedTransaction.parseFrom(
                            ConvertStrByte.hexStringToBytes(content.substring(2)));
            transaction = unverifiedTransaction.getTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transaction;
    }

    public static String getTo(String content) {
        Blockchain.Transaction transaction = getTransaction(content);
        return transaction.getTo();
    }

    public static String getNonce(String content) {
        Blockchain.Transaction transaction = getTransaction(content);
        return transaction.getNonce();
    }

    public static long getQuota(String content) {
        Blockchain.Transaction transaction = getTransaction(content);
        return transaction.getQuota();
    }

    public static String getData(String content) {
        Blockchain.Transaction transaction = getTransaction(content);
        byte[] bDate = transaction.getData().toByteArray();
        return ConvertStrByte.bytesToHexString(bDate);
    }

    public static long getValidUntilBlock(String content) {
        Blockchain.Transaction transaction = getTransaction(content);
        return transaction.getValidUntilBlock();
    }

    public static boolean verifySignature(String addr, String content)
            throws InvalidProtocolBufferException, SignatureException {

        if (!checkAddress(addr)) {
            throw new IllegalArgumentException("Address is not in correct format");
        }

        byte[] contentBytes = ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(content));
        Blockchain.UnverifiedTransaction unverifiedTx
                = Blockchain.UnverifiedTransaction.parseFrom(contentBytes);

        //signature = r (32 byte) + s (32 byte) + v (1 byte)
        ByteString sigByteString = unverifiedTx.getSignature();
        byte[] sigBytes = sigByteString.toByteArray();
        String sig = ConvertStrByte.bytesToHexString(sigBytes);

        if (sig.length() != 130) {
            throw new IllegalArgumentException("Transaction signature is not in correct format");
        }

        String r = sig.substring(0, 64);
        String s = sig.substring(64, 128);
        String v = sig.substring(128);

        byte[] bytesR = ConvertStrByte.hexStringToBytes(r);
        byte[] bytesS = ConvertStrByte.hexStringToBytes(s);
        byte[] bytesV = ConvertStrByte.hexStringToBytes(v);
        byte byteV = bytesV[0];

        String recoveredPubKey = Sign.signedMessageToKey(
                unverifiedTx.getTransaction().toByteArray(),
                new Sign.SignatureData(byteV, bytesR, bytesS)).toString(16);

        String recoveredAddr = Keys.getAddress(recoveredPubKey);

        return recoveredAddr.equals(Numeric.cleanHexPrefix(addr));
    }

    public static Transaction
            decodeContent(String content) throws InvalidProtocolBufferException {

        Blockchain.UnverifiedTransaction unverifiedTx
                = Blockchain.UnverifiedTransaction.parseFrom(
                ConvertStrByte.hexStringToBytes(Numeric.cleanHexPrefix(content)));

        Blockchain.Transaction blockChainTx = unverifiedTx.getTransaction();

        int version = blockChainTx.getVersion();
        String nonce = blockChainTx.getNonce();
        long quota = blockChainTx.getQuota();
        long validUntilBlock = blockChainTx.getValidUntilBlock();
        String data = bytesToHexString(blockChainTx.getData());
        String value = bytesToHexString(blockChainTx.getValue());

        String to = null;
        BigInteger chainId = null;

        //version 0: cita 0.19
        //version 1: cita 0.20
        //version 2: cita 0.24
        if (version == 0) {
            to = blockChainTx.getTo();
            chainId = BigInteger.valueOf(blockChainTx.getChainId());
        } else if (version == 1 || version == 2) {
            to = bytesToHexString(blockChainTx.getToV1());
            chainId = Numeric.toBigInt(bytesToHexString(blockChainTx.getChainIdV1()));
        }

        if (chainId == null || to == null) {
            throw new NullPointerException("Cannot get chainId or to from chain.");
        }

        return new Transaction(
                to, nonce, quota, validUntilBlock, version, chainId, value, data);
    }

    private static String bytesToHexString(ByteString byteStr) {
        return ConvertStrByte.bytesToHexString(byteStr.toByteArray());
    }

    public static boolean checkAddress(String addr) {
        return addr.matches("0[xX][0-9a-fA-F]+") && addr.length() == 42;
    }
}
