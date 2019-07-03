package cn.joytur.common.extensions.wechat.weblistener;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Util {
    static final Random RANDOM = new Random();

    static String get15RandomText() {
        StringBuilder sb = new StringBuilder("e");
        sb.append(RANDOM.nextInt(9) + 1);
        for (int i = 0; i < 14; i++)
            sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }


    static String getStringMiddle(String str, String left, String right) {
        int i = str.indexOf(left);
        if (i == -1)
            return "";
        int j = str.indexOf(right, i + left.length() + 1);
        if (j == -1)
            return "";
        return str.substring(i + left.length(), j);
    }

    static String getStringRight(String str, String left) {
        int i = str.indexOf(left);
        if (i == -1)
            return "";
        return str.substring(i + left.length());
    }

    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final Charset CHARSET = Charset.forName("UTF-8");

    static String getMD5(String string) {
        MessageDigest mdInst;
        try {
            mdInst = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
        byte[] md = string.getBytes(CHARSET);
        for (int i = 0; i < md.length; i++)
            md[i] = (byte) ~md[i];
        mdInst.update(md);
        md = mdInst.digest();
        char str[] = new char[md.length << 1];
        int k = 0;
        for (byte byte0 : md) {
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }
}
