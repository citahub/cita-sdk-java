package com.citahub.cita.protobuf;

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
            ret[i] = Integer
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * Convert hex string to byte array with fixed bit length
     *
     * @param src    hex string without "0x"
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
                value[i] = Integer
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
        char[] chars = "0123456789abcdef".toCharArray();
        StringBuilder sb = new StringBuilder();
        byte[] bs = strPart.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }


    public static String hexStringToString(String src) {
        String str = "0123456789abcdef";
        char[] hexs = src.toCharArray();
        byte[] bytes = new byte[src.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


    public static Byte charToByte(Character src) {
        return Integer.valueOf((int) src).byteValue();
    }


    private static String intToHexString(int a, int len) {
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
