package org.colm.code.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class FileUtil {

    public static String file2Base64(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            is.read(bytes);
            return bytes2Base64(bytes);
        } catch (Exception e) {
            throw e;
        }
    }

    public static String bytes2Base64(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes).replaceAll("\\=", "");
    }

    public static byte[] bytesFromBase64(String base64) {
        int mod4 = base64.length() % 4;
        if(mod4 > 0) {
            base64 = base64 + "====".substring(mod4);
        }
        return Base64.getUrlDecoder().decode(base64);
    }

    public static String standardBase64ToUrlBase64(String normalBase64) {
        return normalBase64.replaceAll("\\+", "")
                .replaceAll("\\/", "")
                .replaceAll("\\=", "")
                .replaceAll("\r", "")
                .replaceAll("\n", "");
    }

}
