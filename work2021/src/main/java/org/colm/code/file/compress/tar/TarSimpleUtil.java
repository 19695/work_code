package org.colm.code.file.compress.tar;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class TarSimpleUtil {

    private TarSimpleUtil() {}

    /**
     * 文件归档
     * @param fileList
     * @param outFile
     * @throws IOException
     */
    public static void archive(List<File> fileList, File outFile) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(outFile.toPath());
             TarArchiveOutputStream tarOUtStream = new TarArchiveOutputStream(outputStream)
        ) {
            archive0(tarOUtStream, fileList, "");
            tarOUtStream.flush();
        }
    }

    /**
     * 文件提档
     * @param tarFile
     * @throws IOException
     */
    public static void dearchive(File tarFile) throws IOException {
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(Files.newInputStream(tarFile.toPath()))) {
            dearchive0(tarInputStream, tarFile);
        }
     }

    private static void dearchive0(TarArchiveInputStream tarInputStream, File tarFile) throws IOException {
        String parent = tarFile.getParent();
        TarArchiveEntry tarArchiveEntry;
        while ((tarArchiveEntry = tarInputStream.getNextTarEntry()) != null) {
            String filePath = parent + File.separator + tarArchiveEntry.getName();
            File file = new File(filePath);
            if (tarArchiveEntry.isDirectory()) {
                file.mkdir();
                continue;
            }
            Files.copy(tarInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

    }

    private static void archive0(TarArchiveOutputStream tarOUtStream, List<File> fileList, String base) throws IOException {
        for (File file : fileList) {
            TarArchiveEntry archiveEntry = new TarArchiveEntry(file, base + file.getName());
            tarOUtStream.putArchiveEntry(archiveEntry);
            if (file.isFile()) {
                Files.copy(file.toPath(), tarOUtStream);
                tarOUtStream.closeArchiveEntry();
            } else {
                tarOUtStream.closeArchiveEntry();
                File[] fileArray = file.listFiles();
                if (fileArray != null) {
                    archive0(tarOUtStream, Arrays.asList(fileArray), base + file.getName() + File.separator);
                }
            }

        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("E:\\test\\tar");
        File tar = new File("E:\\test\\test.tar");
        dearchive(tar);
//        archive(Arrays.asList(file.listFiles()), tar);
    }

}
