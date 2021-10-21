package root;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringEncoder {
    public static String encrypt(String str) {
        try {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = MessageDigest.getInstance("SHA-1").digest(str.getBytes(StandardCharsets.UTF_8));
            for (byte b : bytes) {
                sb.append(Integer.toHexString((b >> 4) & 15));
                sb.append(Integer.toHexString(b & 15));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}