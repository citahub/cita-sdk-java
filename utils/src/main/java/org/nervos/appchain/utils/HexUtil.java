package org.nervos.appchain.utils;

public class HexUtil {

    public static byte[] hexToBytes(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }


    public static byte hexToByte(String inHex) {
        return (byte)Integer.parseInt(inHex,16);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            int c = b;
            c = c < 0 ? c + 256 : c;
            String n = Integer.toHexString(c);
            if (n.length() == 1) {
                n = "0" + n;
            }
            builder.append(n);
        }
        return builder.toString();
    }

    public static void printBytes(byte[] bytes) {
        for (byte b : bytes) {
            int c = b;
            c = c < 0 ? c + 256 : c;
            String n = Integer.toHexString(c);
            if (n.length() == 1) {
                n = "0" + n;
            }
            System.out.print(n + "");
        }
        System.out.println(" ");
    }


}
