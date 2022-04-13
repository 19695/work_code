package org.colm.code.file.compress.zip;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private static final String ZIP_SUFFIX = ".zip";
    private static final List<Charset> PRIMARY_CHASET_LIST = Arrays.asList(
            StandardCharsets.UTF_8,
            Charset.forName("GBK"),
            Charset.forName("GB18030"),
            Charset.forName("GB2312"),
            StandardCharsets.ISO_8859_1
    );

    private ZipUtil() {
    }

    /**
     * 递归压缩 srcFiles 到 zipFle
     * @param zipFile
     * @param srcFiles
     * @throws IOException
     */
    public static void compress(File zipFile, File... srcFiles) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            for (File srcFile : srcFiles) {
                if (srcFile.isDirectory()) {
                    File file = new File(srcFile.getPath() + ZIP_SUFFIX);
                    compress(file, srcFile.listFiles());
                    zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                    Files.copy(file.toPath(), zipOutputStream);
                    file.delete();
                } else {
                    zipOutputStream.putNextEntry(new ZipEntry(srcFile.getName()));
                    Files.copy(srcFile.toPath(), zipOutputStream);
                }
            }
        }
    }

    /**
     * 解压 srcFile 到 path
     * 之前遇到字符集问题，导致解析不了多层路径；改用 zip4j 解决字符集问题
     * @param srcFile
     * @param path
     * @throws ZipException
     */
    public static void decompress(File srcFile, File path) throws ZipException, ZipCharsetException {
        Charset charset = charsetDetect(srcFile);
        if (!path.exists()) {
            path.mkdir();
        }
        decompress0(srcFile, path, charset);
    }

    private static void decompress0(File srcFile, File path, Charset charset) throws ZipException {
        ZipFile zipFile = new ZipFile(srcFile);
        zipFile.setCharset(charset);
        List<FileHeader> fileHeaderList = zipFile.getFileHeaders();
        for (FileHeader fileHeader : fileHeaderList) {
            String fileName = fileHeader.getFileName();
            // 如果存在中文问题，可以尝试通过此方法探测编码获取 utf8 编码下的文件名
//           fileName = StringUtil.getUtf8String(fileName);
            if (fileName.endsWith(ZIP_SUFFIX)) {
                zipFile.extractFile(fileHeader, path.getPath(), fileName, null);
                File tmpFile = new File(path.getPath(), fileName);
                String directoryPath = path.getPath() + File.separator + fileName.substring(0, fileName.lastIndexOf(ZIP_SUFFIX));
                File filePath = new File(directoryPath);
                filePath.mkdir();
                decompress0(tmpFile, filePath, charset);
                tmpFile.delete();
            } else if (fileHeader.isDirectory()){
                File file = new File(path.getPath(), fileName);
                file.mkdir();
                continue;
            } else {
                zipFile.extractFile(fileHeader, path.getPath(), fileName, null);
            }
        }
    }

    private static Charset charsetDetect(File srcFile) throws ZipCharsetException {
        // 首选 UTF8，不然可能和 Big5 混淆
        try {
            return getCharset(srcFile, PRIMARY_CHASET_LIST);
        } catch (ZipCharsetException e) {
            return getCharset(srcFile, Charset.availableCharsets().values());
        }
    }

    private static Charset getCharset(File srcFile, Collection<Charset> charsets) throws ZipCharsetException {
        for (Charset charset : charsets) {
            if (judgeEncoding(srcFile, charset)) {
                return charset;
            }
        }
        throw new ZipCharsetException("没有匹配到编码集");
    }

    private static boolean judgeEncoding(File file, Charset charset) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream zis = new ZipInputStream(bis, charset)
        ) {
            while (zis.getNextEntry() != null) {
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IOException, ZipCharsetException {
        File file = new File("E:\\test\\tar");
        File zip = new File("E:\\test\\test.zip");
        File folderZip = new File("E:\\test\\t.zip");
        File decompress = new File("E:\\test\\decompress");
//        compress(zip, file);
//        decompress(zip, decompress);
        decompress(folderZip, decompress);
//        Charset.availableCharsets();
    }

}
