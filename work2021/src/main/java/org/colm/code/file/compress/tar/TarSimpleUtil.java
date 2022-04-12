package org.colm.code.file.compress.tar;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class TarSimpleUtil {

    public static void archive(List<File> fileList, File outFile) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(outFile.toPath());
             TarArchiveOutputStream tarOUtStream = new TarArchiveOutputStream(outputStream)
        ) {
            archive0(tarOUtStream, fileList, "");
            tarOUtStream.flush();
        }
    }

    // todo
    public static void dearchive(File tarFile) {

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
        File[] files = file.listFiles();
        archive(Arrays.asList(files), new File("E:\\test\\test.tar"));
    }

}
