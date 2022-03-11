package org.colm.code.file.compress.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

public class ZipUtil {


    private static boolean judgeEncoding(File file, Charset charset) {
        try (FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis, charset)
        ) {
            while (zis.getNextEntry() != null){}
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
