package org.colm.code.file.compress.gz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzUtil {

    private GzUtil() {}

    /**
     * 压缩文件：默认压缩后文件名为 源文件名.gz
     * @param sourceFile 需要压缩的文件
     * @return  压缩后的文件
     * @throws IOException
     */
    public static File compress(File sourceFile) throws IOException {
        File outFile = new File(sourceFile.getAbsolutePath() + ".gz");
        compress(sourceFile, outFile);
        return outFile;
    }

    /**
     * 压缩文件：指定压缩后的文件名
     * @param sourceFile 源文件
     * @param outFile 压缩后的文件
     * @throws IOException
     */
    public static void compress(File sourceFile, File outFile) throws IOException {
        try (GZIPOutputStream gzOutStream = new GZIPOutputStream(Files.newOutputStream(outFile.toPath()))) {
            Files.copy(sourceFile.toPath(), gzOutStream);
            // 将缓冲区数据刷新到磁盘并刷空输出流
            gzOutStream.flush();
        }
    }

    /**
     * 解压文件：默认加压后文件名为 源文件名去掉 .gz 后缀
     * @param gzFile 压缩文件
     * @return 解压后的文件
     * @throws IOException
     */
    public static File decompress(File gzFile) throws IOException {
        String absolutePath = gzFile.getAbsolutePath();
        File outFile = new File(absolutePath.substring(0, absolutePath.lastIndexOf(".")));
        decompress(gzFile, outFile);
        return outFile;
    }

    /**
     * 解压文件：指定解压后的文件名
     * @param gzFile 压缩文件
     * @param outFile 解压后的文件
     * @throws IOException
     */
    public static void decompress(File gzFile, File outFile) throws IOException {
        try (GZIPInputStream gzInStream = new GZIPInputStream(Files.newInputStream(gzFile.toPath()))) {
            Files.copy(gzInStream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
