package com.citahub.cita.crypto.sm2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import com.citahub.cita.utils.HexUtil;
import com.citahub.cita.utils.Numeric;

import static com.citahub.cita.crypto.sm2.SM2.Signature.fillBytes32;
import static com.citahub.cita.crypto.sm2.SM2.Signature.fillStr64;

public class SM2 {
    private static BigInteger n = new BigInteger(
            "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "7203DF6B" + "21C6052B" + "53BBF409" + "39D54123", 16);
    private static BigInteger p = new BigInteger(
            "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFF", 16);
    private static BigInteger a = new BigInteger(
            "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFC", 16);
    private static BigInteger b = new BigInteger(
            "28E9FA9E" + "9D9F5E34" + "4D5A9E4B" + "CF6509A7" + "F39789F5" + "15AB8F92" + "DDBCBD41" + "4D940E93", 16);
    private static BigInteger gx = new BigInteger(
            "32C4AE2C" + "1F198119" + "5F990446" + "6A39C994" + "8FE30BBF" + "F2660BE1" + "715A4589" + "334C74C7", 16);
    private static BigInteger gy = new BigInteger(
            "BC3736A2" + "F4F6779C" + "59BDCEE3" + "6B692153" + "D0A9877C" + "C62A4740" + "02DF32E5" + "2139F0A0", 16);
    private static ECDomainParameters ecc_bc_spec;
    private static int w = (int) Math.ceil(n.bitLength() * 1.0 / 2) - 1;
    private static BigInteger _2w = new BigInteger("2").pow(w);
    private static final int DIGEST_LENGTH = 32;

    private static SecureRandom random = new SecureRandom();
    private static ECCurve.Fp curve;
    private static ECPoint G;
    private boolean debug = false;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public static void printHexString(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }
        System.out.println();
    }

    private static BigInteger random(BigInteger max) {

        BigInteger r = new BigInteger(256, random);

        while (r.compareTo(max) >= 0) {
            r = new BigInteger(128, random);
        }

        return r;
    }

    private boolean allZero(byte[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public byte[] encrypt(String input, ECPoint publicKey) {

        byte[] inputBuffer = input.getBytes();
        if (debug) {
            printHexString(inputBuffer);
        }

        byte[] c1Buffer;
        ECPoint kpb;
        byte[] t;
        do {
            /* 1 generate random k, and k is in [1, n-1] */
            BigInteger k = random(n);
            if (debug) {
                System.out.print("k: ");
                printHexString(k.toByteArray());
            }

            /* 2 calculate ellipse point C1 = [k]G = (x1, y1) */
            ECPoint c1 = G.multiply(k);
            c1Buffer = c1.getEncoded(false);
            if (debug) {
                System.out.print("C1: ");
                printHexString(c1Buffer);
            }

            /*
             * 3 calculate ellipse point S = [h]Pb
             */
            BigInteger h = ecc_bc_spec.getH();
            if (h != null) {
                ECPoint s = publicKey.multiply(h);
                if (s.isInfinity()) {
                    throw new IllegalStateException();
                }
            }

            /* 4 [k]PB = (x2, y2) */
            kpb = publicKey.multiply(k).normalize();

            /* 5 t = KDF(x2||y2, klen) */
            byte[] kpbBytes = kpb.getEncoded(false);
            t = kdf(kpbBytes, inputBuffer.length);
        } while (allZero(t));

        /* 6 C2=M^t */
        byte[] c2 = new byte[inputBuffer.length];
        for (int i = 0; i < inputBuffer.length; i++) {
            c2[i] = (byte) (inputBuffer[i] ^ t[i]);
        }

        /* 7 C3 = Hash(x2 || M || y2) */
        byte[] c3 = sm3hash(kpb.getXCoord().toBigInteger().toByteArray(), inputBuffer,
                kpb.getYCoord().toBigInteger().toByteArray());

        /* 8 C=C1 || C2 || C3 */

        byte[] encryptResult = new byte[c1Buffer.length + c2.length + c3.length];

        System.arraycopy(c1Buffer, 0, encryptResult, 0, c1Buffer.length);
        System.arraycopy(c2, 0, encryptResult, c1Buffer.length, c2.length);
        System.arraycopy(c3, 0, encryptResult, c1Buffer.length + c2.length, c3.length);

        if (debug) {
            System.out.print("密文: ");
            printHexString(encryptResult);
        }

        return encryptResult;
    }

    public String decrypt(byte[] encryptData, BigInteger privateKey) {

        if (debug) {
            System.out.println("encryptData length: " + encryptData.length);
        }

        byte[] c1Byte = new byte[65];
        System.arraycopy(encryptData, 0, c1Byte, 0, c1Byte.length);

        ECPoint c1 = curve.decodePoint(c1Byte).normalize();

        /*
         * S = [h]C1
         */
        BigInteger h = ecc_bc_spec.getH();
        if (h != null) {
            ECPoint s = c1.multiply(h);
            if (s.isInfinity()) {
                throw new IllegalStateException();
            }
        }
        /* [dB]C1 = (x2, y2) */
        ECPoint dBC1 = c1.multiply(privateKey).normalize();

        /* t = KDF(x2 || y2, klen) */
        byte[] dBC1Bytes = dBC1.getEncoded(false);
        int klen = encryptData.length - 65 - DIGEST_LENGTH;
        byte[] t = kdf(dBC1Bytes, klen);

        if (allZero(t)) {
            System.err.println("all zero");
            throw new IllegalStateException();
        }

        /* 5 M'=C2^t */
        byte[] m = new byte[klen];
        for (int i = 0; i < m.length; i++) {
            m[i] = (byte) (encryptData[c1Byte.length + i] ^ t[i]);
        }
        if (debug) {
            printHexString(m);
        }

        /* 6 u = Hash(x2 || M' || y2) check if u == C3 */
        byte[] c3 = new byte[DIGEST_LENGTH];

        if (debug) {
            try {
                System.out.println("M = " + new String(m, "UTF8"));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }

        System.arraycopy(encryptData, encryptData.length - DIGEST_LENGTH, c3, 0, DIGEST_LENGTH);
        byte[] u = sm3hash(dBC1.getXCoord().toBigInteger().toByteArray(), m,
                dBC1.getYCoord().toBigInteger().toByteArray());
        if (Arrays.equals(u, c3)) {
            if (debug) {
                System.out.println("解密成功");
            }
            try {
                return new String(m, "UTF8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            if (debug) {
                System.out.print("u = ");
                printHexString(u);
                System.out.print("C3 = ");
                printHexString(c3);
                System.err.println("解密验证失败");
            }
            return null;
        }

    }

    // /**
    // * SHA摘要
    // * @param x2
    // * @param M
    // * @param y2
    // * @return
    // */
    // private byte[] calculateHash(BigInteger x2, byte[] M, BigInteger y2) {
    // ShortenedDigest digest = new ShortenedDigest(new SHA256Digest(),
    // DIGEST_LENGTH);
    // byte[] buf = x2.toByteArray();
    // digest.update(buf, 0, buf.length);
    // digest.update(M, 0, M.length);
    // buf = y2.toByteArray();
    // digest.update(buf, 0, buf.length);
    //
    // buf = new byte[DIGEST_LENGTH];
    // digest.doFinal(buf, 0);
    //
    // return buf;
    // }

    private boolean between(BigInteger param, BigInteger min, BigInteger max) {
        if (param.compareTo(min) >= 0 && param.compareTo(max) < 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPublicKey(ECPoint publicKey) {

        if (!publicKey.isInfinity()) {

            BigInteger x = publicKey.getXCoord().toBigInteger();
            BigInteger y = publicKey.getYCoord().toBigInteger();

            if (between(x, new BigInteger("0"), p) && between(y, new BigInteger("0"), p)) {

                BigInteger xResult = x.pow(3).add(a.multiply(x)).add(b).mod(p);

                if (debug) {
                    System.out.println("xResult: " + xResult.toString());
                }

                BigInteger yResult = y.pow(2).mod(p);

                if (debug) {
                    System.out.println("yResult: " + yResult.toString());
                }

                if (yResult.equals(xResult) && publicKey.multiply(n).isInfinity()) {
                    return true;
                }
            }
        }
        return false;
    }

    public SM2KeyPair fromPrivateKey(String privateKey) {
        BigInteger pk = Numeric.toBigInt(privateKey);
        SM2KeyPair keyPair = new SM2KeyPair(G.multiply(pk).normalize(), pk);
        if (checkPublicKey(keyPair.getPublicKey())) {
            if (debug) {
                System.out.println("generate key successfully");
            }
            return keyPair;
        } else {
            if (debug) {
                System.err.println("generate key failed");
            }
            return null;
        }
    }

    public SM2KeyPair generateKeyPair() {

        BigInteger d = random(n.subtract(new BigInteger("1")));

        SM2KeyPair keyPair = new SM2KeyPair(G.multiply(d).normalize(), d);

        if (checkPublicKey(keyPair.getPublicKey())) {
            if (debug) {
                System.out.println("generate key successfully");
            }
            return keyPair;
        } else {
            if (debug) {
                System.err.println("generate key failed");
            }
            return null;
        }
    }

    public SM2() {
        curve = new ECCurve.Fp(p, // q
                a, // a
                b); // b
        G = curve.createPoint(gx, gy);
        ecc_bc_spec = new ECDomainParameters(curve, G, n);
    }

    public SM2(boolean debug) {
        this();
        this.debug = debug;
    }

    public void exportPublicKey(ECPoint publicKey, String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] buffer = publicKey.getEncoded(false);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ECPoint importPublicKey(String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[16];
            int size;
            while ((size = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, size);
            }
            fis.close();
            return curve.decodePoint(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ECPoint toPublicKey(byte [] publickey) {
        return curve.decodePoint(publickey);
    }

    public void exportPrivateKey(BigInteger privateKey, String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(privateKey);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BigInteger importPrivateKey(String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            BigInteger res = (BigInteger) (ois.readObject());
            ois.close();
            fis.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] join(byte[]... params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] res = null;
        try {
            for (int i = 0; i < params.length; i++) {
                baos.write(params[i]);
            }
            res = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static byte[] sm3hash(byte[]... params) {
        byte[] res = null;
        try {
            res = SM3.hash(join(params));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }

    private static byte[] za(String ida, ECPoint aPublicKey) {
        byte[] idaBytes = ida.getBytes();
        int entlenA = idaBytes.length * 8;
        byte[] entla = new byte[] { (byte) (entlenA & 0xFF00), (byte) (entlenA & 0x00FF) };

        byte[] za = sm3hash(
                entla,
                idaBytes,
                getBytesFromBigInteger(a),
                getBytesFromBigInteger(b),
                getBytesFromBigInteger(gx),
                getBytesFromBigInteger(gy),
                fillBytes32(getBytesFromBigInteger(aPublicKey.getXCoord().toBigInteger())),
                fillBytes32(getBytesFromBigInteger(aPublicKey.getYCoord().toBigInteger())));

        return za;
    }

    private static byte[] getBytesFromBigInteger(BigInteger value) {
        byte[] array = value.toByteArray();
        if (array[0] == 0) {
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }
        return array;
    }


    public Signature sign(byte [] data, String ida, SM2KeyPair keyPair) {
        byte[] za = za(ida, keyPair.getPublicKey());
        byte[] m = join(za, data);
        BigInteger e = new BigInteger(1, sm3hash(m));
        // BigInteger k = new BigInteger(
        // "6CB28D99 385C175C 94F94E93 4817663F C176D925 DD72B727 260DBAAE
        // 1FB2F96F".replace(" ", ""), 16);
        BigInteger k;
        BigInteger r;
        do {
            k = random(n);
            ECPoint p1 = G.multiply(k).normalize();
            BigInteger x1 = p1.getXCoord().toBigInteger();
            r = e.add(x1);
            r = r.mod(n);
        } while (r.equals(BigInteger.ZERO) || r.add(k).equals(n));

        BigInteger s = ((keyPair.getPrivateKey().add(BigInteger.ONE).modInverse(n))
                .multiply((k.subtract(r.multiply(keyPair.getPrivateKey()))).mod(n))).mod(n);
        return new Signature(r, s);
    }


    public boolean verify(byte [] data, Signature signature, String ida, ECPoint aPublicKey) {
        if (!between(signature.r, BigInteger.ONE, n)) {
            return false;
        }
        if (!between(signature.s, BigInteger.ONE, n)) {
            return false;
        }

        byte[] m = join(za(ida, aPublicKey), data);
        BigInteger e = new BigInteger(1, sm3hash(m));
        BigInteger t = signature.r.add(signature.s).mod(n);

        if (t.equals(BigInteger.ZERO)) {
            return false;
        }

        ECPoint p1 = G.multiply(signature.s).normalize();
        ECPoint p2 = aPublicKey.multiply(t).normalize();
        BigInteger x1 = p1.add(p2).normalize().getXCoord().toBigInteger();
        BigInteger r = e.add(x1).mod(n);
        if (r.equals(signature.r)) {
            return true;
        }
        return false;
    }

    private static byte[] kdf(byte[] z, int klen) {
        int ct = 1;
        int end = (int) Math.ceil(klen * 1.0 / 32);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (int i = 1; i < end; i++) {
                baos.write(sm3hash(z, SM3.toByteArray(ct)));
                ct++;
            }
            byte[] last = sm3hash(z, SM3.toByteArray(ct));
            if (klen % 32 == 0) {
                baos.write(last);
            } else {
                baos.write(last, 0, klen % 32);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class TransportEntity implements Serializable {
        final byte[] r;
        final byte[] s;
        final byte[] z;
        final byte[] k;

        public TransportEntity(byte[] r, byte[] s,byte[] z,ECPoint pKey) {
            this.r = r;
            this.s = s;
            this.z = z;
            this.k = pKey.getEncoded(false);
        }
    }

    public static class KeyExchange {
        BigInteger rA;
        ECPoint ra;
        ECPoint v;
        byte[] z;
        byte[] key;

        String id;
        SM2KeyPair keyPair;

        public KeyExchange(String id, SM2KeyPair keyPair) {
            this.id = id;
            this.keyPair = keyPair;
            this.z = za(id, keyPair.getPublicKey());
        }

        public TransportEntity keyExchange_1() {
            rA = random(n);
            // rA=new BigInteger("83A2C9C8 B96E5AF7 0BD480B4 72409A9A 327257F1
            // EBB73F5B 073354B2 48668563".replace(" ", ""),16);
            ra = G.multiply(rA).normalize();
            return new TransportEntity(ra.getEncoded(false), null, z, keyPair.getPublicKey());
        }

        public TransportEntity keyExchange_2(TransportEntity entity) {
            BigInteger rB = random(n);
            // BigInteger rB=new BigInteger("33FE2194 0342161C 55619C4A 0C060293
            // D543C80A F19748CE 176D8347 7DE71C80".replace(" ", ""),16);
            ECPoint rb = G.multiply(rB).normalize();

            this.rA = rB;
            this.ra = rb;

            BigInteger x2 = rb.getXCoord().toBigInteger();
            x2 = _2w.add(x2.and(_2w.subtract(BigInteger.ONE)));

            BigInteger tB = keyPair.getPrivateKey().add(x2.multiply(rB)).mod(n);
            ECPoint RA = curve.decodePoint(entity.r).normalize();

            BigInteger x1 = RA.getXCoord().toBigInteger();
            x1 = _2w.add(x1.and(_2w.subtract(BigInteger.ONE)));

            ECPoint aPublicKey = curve.decodePoint(entity.k).normalize();
            ECPoint temp = aPublicKey.add(RA.multiply(x1).normalize()).normalize();
            ECPoint v = temp.multiply(ecc_bc_spec.getH().multiply(tB)).normalize();
            if (v.isInfinity()) {
                throw new IllegalStateException();
            }
            this.v = v;

            byte[] xV = v.getXCoord().toBigInteger().toByteArray();
            byte[] yV = v.getYCoord().toBigInteger().toByteArray();
            byte[] kb = kdf(join(xV, yV, entity.z, this.z), 16);
            key = kb;
            System.out.print("协商得B密钥:");
            printHexString(kb);
            byte[] sB = sm3hash(new byte[] { 0x02 }, yV,
                    sm3hash(xV, entity.z, this.z, RA.getXCoord().toBigInteger().toByteArray(),
                            RA.getYCoord().toBigInteger().toByteArray(), rb.getXCoord().toBigInteger().toByteArray(),
                            rb.getYCoord().toBigInteger().toByteArray()));
            return new TransportEntity(rb.getEncoded(false), sB, this.z, keyPair.getPublicKey());
        }

        public TransportEntity keyExchange_3(TransportEntity entity) {
            BigInteger x1 = ra.getXCoord().toBigInteger();
            x1 = _2w.add(x1.and(_2w.subtract(BigInteger.ONE)));

            BigInteger tA = keyPair.getPrivateKey().add(x1.multiply(rA)).mod(n);
            ECPoint RB = curve.decodePoint(entity.r).normalize();

            BigInteger x2 = RB.getXCoord().toBigInteger();
            x2 = _2w.add(x2.and(_2w.subtract(BigInteger.ONE)));

            ECPoint bPublicKey = curve.decodePoint(entity.k).normalize();
            ECPoint temp = bPublicKey.add(RB.multiply(x2).normalize()).normalize();
            ECPoint u = temp.multiply(ecc_bc_spec.getH().multiply(tA)).normalize();
            if (u.isInfinity()) {
                throw new IllegalStateException();
            }
            this.v = u;

            byte[] xU = u.getXCoord().toBigInteger().toByteArray();
            byte[] yU = u.getYCoord().toBigInteger().toByteArray();
            byte[] ka = kdf(join(xU, yU,
                    this.z, entity.z), 16);
            key = ka;
            System.out.print("协商得A密钥:");
            printHexString(ka);
            byte[] s1 = sm3hash(new byte[] { 0x02 }, yU,
                    sm3hash(xU, this.z, entity.z, ra.getXCoord().toBigInteger().toByteArray(),
                            ra.getYCoord().toBigInteger().toByteArray(), RB.getXCoord().toBigInteger().toByteArray(),
                            RB.getYCoord().toBigInteger().toByteArray()));
            if (Arrays.equals(entity.s, s1)) {
                System.out.println("B->A 密钥确认成功");
            } else {
                System.out.println("B->A 密钥确认失败");
            }
            byte[] sA = sm3hash(new byte[] { 0x03 }, yU,
                    sm3hash(xU, this.z, entity.z, ra.getXCoord().toBigInteger().toByteArray(),
                            ra.getYCoord().toBigInteger().toByteArray(), RB.getXCoord().toBigInteger().toByteArray(),
                            RB.getYCoord().toBigInteger().toByteArray()));

            return new TransportEntity(ra.getEncoded(false), sA, this.z, keyPair.getPublicKey());
        }

        public void keyExchange_4(TransportEntity entity) {
            byte[] xV = v.getXCoord().toBigInteger().toByteArray();
            byte[] yV = v.getYCoord().toBigInteger().toByteArray();
            ECPoint RA = curve.decodePoint(entity.r).normalize();
            byte[] s2 = sm3hash(new byte[] { 0x03 }, yV,
                    sm3hash(xV, entity.z, this.z, RA.getXCoord().toBigInteger().toByteArray(),
                            RA.getYCoord().toBigInteger().toByteArray(), this.ra.getXCoord().toBigInteger().toByteArray(),
                            this.ra.getYCoord().toBigInteger().toByteArray()));
            if (Arrays.equals(entity.s, s2)) {
                System.out.println("A->B 密钥确认成功");
            } else {
                System.out.println("A->B 密钥确认失败");
            }
        }
    }

    public static byte[] getSignature(Signature signature, ECPoint publicKey) {
        String publicKeyBytes = fillStr64(publicKey.getRawXCoord().toString()) + fillStr64(publicKey.getRawYCoord().toString());
        return join(HexUtil.hexToBytes(signature.getSign()), HexUtil.hexToBytes(publicKeyBytes));
    }


    public static class Signature {
        BigInteger r;
        BigInteger s;

        public Signature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        public String toString() {
            return "r: " + r.toString(16) + "," + "s: " + s.toString(16);
        }

        public String getSign() {
            return fillStr64(r.toString(16)) + fillStr64(s.toString(16));
        }

        static String fillStr64(String str) {
            if (str.length() >= 64) {
                return str;
            }
            int len = 64 - str.length();
            StringBuffer sb = new StringBuffer(str);
            for (int i = 0; i < len; i++) {
                sb.insert(0,"0");
            }
            return sb.toString();
        }

        static byte[] fillBytes32(byte[] bytes) {
            if (bytes.length >= 32) {
                return bytes;
            }
            byte[] res = new byte[32];
            int index = 32 - bytes.length;
            System.arraycopy(bytes, 0, res, index, bytes.length);
            return res;
        }
    }
}
