package org.nervos.appchain.tests;

import com.google.protobuf.ByteString;

import org.nervos.appchain.crypto.ECKeyPair;
import org.nervos.appchain.crypto.Sign;
import org.nervos.appchain.protobuf.Blockchain.UnverifiedTransaction;
import org.nervos.appchain.protobuf.ConvertStrByte;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.utils.Numeric;

public class VerifySignatureExample {
    private static String privateKey;
    private static int chainId;
    private static int version;
    private static AppChainj service;
    private static String to;

    static {
        Config conf = new Config();
        conf.buildService(false);

        privateKey = conf.primaryPrivKey;
        service = conf.service;
        chainId = TestUtil.getChainId(service);
        version = TestUtil.getVersion(service);
        to = conf.auxAddr1;
    }

    public static boolean verfiySig(
            UnverifiedTransaction unverifiedTransaction, String pubKey)
            throws Exception {

        //signature = r (32 byte) + s (32 byte) + v (1 byte)
        ByteString sigByteString = unverifiedTransaction.getSignature();
        byte[] sigBytes = sigByteString.toByteArray();
        String sig = ConvertStrByte.bytesToHexString(sigBytes);

        if (sig.length() != 130) {
            throw new IllegalArgumentException(
                    "Transaction signature is not in correct format");
        }

        String r = sig.substring(0, 64);
        String s = sig.substring(64, 128);
        String v = sig.substring(128);

        byte[] bytesR = ConvertStrByte.hexStringToBytes(r);
        byte[] bytesS = ConvertStrByte.hexStringToBytes(s);
        byte[] bytesV = ConvertStrByte.hexStringToBytes(v);
        byte byteV = bytesV[0];

        String recoveredPubKey = Sign.signedMessageToKey(
                unverifiedTransaction.getTransaction().toByteArray(),
                new Sign.SignatureData(byteV, bytesR, bytesS)).toString(16);

        return recoveredPubKey.equals(Numeric.cleanHexPrefix(pubKey));
    }

    public static Transaction createTransaction() {
        String message = Numeric.cleanHexPrefix(ConvertStrByte.stringToHexString("somthing"));
        Transaction tx = new Transaction(
                to, TestUtil.getNonce(), 21000,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId, "1", message);
        return tx;
    }

    public static void main(String[] args) throws Exception {
        //create a sample transaction
        Transaction tx = createTransaction();

        //sign the sample transaction
        String signedTx = tx.sign(privateKey);

        //parse signed transaction to unverifiedTransaction
        UnverifiedTransaction unverifiedTransaction
                  = UnverifiedTransaction.parseFrom(
                          TestUtil.convertHexToBytes(signedTx));

        //get public key from private key used to sign
        ECKeyPair tempPair = ECKeyPair.create(TestUtil.convertHexToBytes(privateKey));

        //verify the signed transaction
        boolean valid = verfiySig(unverifiedTransaction,
                tempPair.getPublicKey().toString(16));

        System.out.println("Verification: " + valid);
    }
}
