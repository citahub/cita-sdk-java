package org.nervos.appchain.protobuf;

public class ConvertStrByte {

    public static String bytesToHexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex);
        }
        return result.toString();
    }

    /**
     * Convert hex string to byte array,
     * length of byte array is src.length / 2
     * length of bit of data is src.length * 4
     *
     * @param src hex string without "0x"
     * @return byte array converted from src.
     */
    public static byte[] hexStringToBytes(String src) {
        src = (src.length() % 2 == 1 ? "0" : "") + src;
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * Convert hex string to byte array with fixed bit length
     *
     * @param src hex string without "0x"
     * @param length length of bits of data to be sent to node.
     * @return byte array converted from src.
     */
    public static byte[] hexStringToBytes(String src, int length) {
        int byteLength = length / 8;
        if (src.length() > length / 4) {
            return new byte[byteLength];
        } else {
            src = (src.length() % 2 == 1 ? "0" : "") + src;
            int l = src.length() / 2;
            byte[] value = new byte[l];
            for (int i = 0; i < l; i++) {
                value[i] = (byte) Integer
                        .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
            }
            int m = byteLength - l;
            byte[] toAdd = new byte[m];
            byte[] ret = new byte[byteLength];
            System.arraycopy(toAdd, 0, ret, 0, m);
            System.arraycopy(value, 0, ret, m, l);
            return ret;
        }
    }


    public static String stringToHexString(String strPart) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        return hexString.toString();
    }


    public static String hexStringToString(String src) {
        String temp = "";
        for (int i = 0; i < src.length() / 2; i++) {
            temp = temp
                    + (char) Integer.valueOf(src.substring(i * 2, i * 2 + 2),
                    16).byteValue();
        }
        return temp;
    }


    public static Byte charToByte(Character src) {
        return Integer.valueOf((int)src).byteValue();
    }


    private static String intToHexString(int a,int len) {
        len <<= 1;
        String hexString = Integer.toHexString(a);
        int b = len - hexString.length();
        if (b > 0) {
            for (int i = 0; i < b; i++) {
                hexString = "0" + hexString;
            }
        }
        return hexString;
    }
}
