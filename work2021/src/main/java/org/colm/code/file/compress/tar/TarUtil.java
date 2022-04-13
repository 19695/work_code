package org.colm.code.file.compress.tar;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;

import java.io.*;

/**
 * 从别的地方改过来的，没有实际测试过
 * 改动点：对流的关闭
 */
public class TarUtil {

    private static final String EMPTY = "";
    private static final String SEPARATOR = File.separator;
    private static final int BUFFER_SIZE = 1024;
    private static final String TAR_SUFFIX = ".tar";

    private TarUtil() {}

    /**
     * 指定源文件进行归档
     * @param srcFile
     */
    public static void archive(File srcFile) throws IOException {
        String fileName = srcFile.getName();
        String tarArchiveName = fileName + TAR_SUFFIX;
        archive(srcFile, tarArchiveName);
    }

    /**
     * 指定源文件和归档文件进行归档
     * @param srcFile
     * @param tarArchiveFile
     * @throws IOException
     */
    public static void archive(File srcFile, File tarArchiveFile) throws IOException {
        TarArchiveOutputStream tarOutStream = null;
        try {
            tarOutStream = new TarArchiveOutputStream(new FileOutputStream(tarArchiveFile));
            archive(srcFile, tarOutStream, EMPTY);
            tarOutStream.flush();
        } finally {
            if (tarOutStream != null)
                tarOutStream.close();
        }
    }

    /**
     * 指定源文件和归档文件名进行归档
     * @param srcFile
     * @param tarArchiveName
     * @throws IOException
     */
    public static void archive(File srcFile, String tarArchiveName) throws IOException {
        archive(srcFile, new File(tarArchiveName));
    }

    /**
     * 指定源文件、归档输出流、父路径进行归档
     * @param srcFile
     * @param tarOutputStream
     * @param parentPath
     */
    private static void archive(File srcFile, TarArchiveOutputStream tarOutputStream, String parentPath) throws IOException {
        if (srcFile.isDirectory()) {
            archiveDirectory(srcFile, tarOutputStream, parentPath);
        } else {
            archiveFile(srcFile, tarOutputStream, parentPath);
        }
    }

    /**
     * 指定源文件名进行归档
     * @param srcFileName
     */
    public static void archive(String srcFileName) throws IOException {
        File srcFile = new File(srcFileName);
        archive(srcFile);
    }

    /**
     * 指定源文件名和归档文件名进行归档
     * @param srcFileName
     * @param tarArchiveName
     */
    public static void archive(String srcFileName, String tarArchiveName) throws IOException {
        File srcFile = new File(srcFileName);
        archive(srcFile, tarArchiveName);
    }

    /**
     * 对目录进行归档
     * @param srcFile
     * @param tarOutputStream
     * @param parentPath
     * @throws IOException
     */
    private static void archiveDirectory(File srcFile, TarArchiveOutputStream tarOutputStream, String parentPath) throws IOException {
        File[] fileArray = srcFile.listFiles();
        int length = fileArray.length;
        if (length < 1) {
            TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(parentPath + srcFile.getName() + SEPARATOR);
            tarOutputStream.putArchiveEntry(tarArchiveEntry);
            tarOutputStream.closeArchiveEntry();
        }

        for (int i = 0; i < length; i++) {
            archive(fileArray[i], tarOutputStream, parentPath + srcFile.getName() + SEPARATOR);
        }
    }

    /**
     * 对文件进行归档
     * @param srcFile
     * @param tarOutputStream
     * @param parentPath
     * @throws IOException
     */
    private static void archiveFile(File srcFile, TarArchiveOutputStream tarOutputStream, String parentPath) throws IOException {
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(parentPath + srcFile.getName());
        tarArchiveEntry.setSize(srcFile.length());
        tarOutputStream.putArchiveEntry(tarArchiveEntry);

        FileInputStream fileInputStream = null;
        BufferedInputStream buffInStream = null;
        try {
            fileInputStream = new FileInputStream(srcFile);
            buffInStream = new BufferedInputStream(fileInputStream);
            byte[] buff = new byte[BUFFER_SIZE];
            int len;
            while ((len = buffInStream.read(buff, 0, BUFFER_SIZE)) != -1) {
                tarOutputStream.write(buff, 0, len);
            }
        } finally {
            if (buffInStream != null)
                buffInStream.close();
            if (fileInputStream != null)
                fileInputStream.close();
        }
        tarOutputStream.closeArchiveEntry();
    }

    /**
     * 指定归档文件进行提档，并指定是否递归
     * @param tarArchiveFile
     * @param recursion
     */
    public static void dearchive(File tarArchiveFile, boolean recursion) throws IOException {
        String parent = tarArchiveFile.getParent();
        dearchive(tarArchiveFile, parent, recursion);
    }

    /**
     * 指定归档文件、提档文件，并指定是否递归
     * @param tarArchiveFile
     * @param file
     * @param recursion
     * @throws IOException
     */
    public static void dearchive(File tarArchiveFile, File file, boolean recursion) throws IOException {
        FileInputStream fileInputStream = null;
        TarArchiveInputStream tarInputStream = null;
        try {
            fileInputStream = new FileInputStream(tarArchiveFile);
            tarInputStream = new TarArchiveInputStream(fileInputStream);
            dearchive(file, tarInputStream, recursion);
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
        }
    }

    /**
     * 指定归档文件、提档文件名，并指定是否递归
     * @param tarArchiveFile
     * @param fileName
     * @param recursion
     * @throws IOException
     */
    public static void dearchive(File tarArchiveFile, String fileName, boolean recursion) throws IOException {
        dearchive(tarArchiveFile, new File(fileName), recursion);
    }

    /**
     * 指定提档路径、归档输入流进行提档，并指定是否递归
     * @param file
     * @param tarInputStream
     * @param recursion
     * @throws IOException
     */
    private static void dearchive(File file, TarArchiveInputStream tarInputStream, boolean recursion) throws IOException {
        TarArchiveEntry tarArchiveEntry;
        try {
            while ((tarArchiveEntry = tarInputStream.getNextTarEntry()) != null) {
                String filePath = file.getPath() + SEPARATOR + tarArchiveEntry.getName();
                File directory = new File(filePath);
                dearchiveDirectory(directory);
                if (tarArchiveEntry.isDirectory()) {
                    directory.mkdir();
                } else {
                    dearchiveFile(directory, tarInputStream);
                    if (recursion && filePath.endsWith(TAR_SUFFIX)) {
                        FileInputStream fileInputStream = null;
                        try {
                            fileInputStream = new FileInputStream(directory);
                            dearchive(new File(directory.getParent()), new TarArchiveInputStream(fileInputStream), recursion);
                        } finally {
                            if (fileInputStream != null)
                                fileInputStream.close();
                        }
                    }
                }
            }
        } finally {
            if (tarInputStream != null)
                tarInputStream.close();
        }
    }

    /**
     * 指定归档文件名进行提档
     * @param fileName
     * @param recursion
     * @throws IOException
     */
    public static void dearchive(String fileName, boolean recursion) throws IOException {
        File file = new File(fileName);
        dearchive(file, recursion);
    }

    /**
     * 指定提档文件名、父路径进行提档，并指定是否递归
     * @param filePath
     * @param parentPath
     * @param recursion
     * @throws IOException
     */
    public static void dearchive(String filePath, String parentPath, boolean recursion) throws IOException {
        File file = new File(filePath);
        String fileName = file.getName();
        String fullFileName = parentPath + SEPARATOR + fileName.substring(0, fileName.lastIndexOf(TAR_SUFFIX));
        File releaseFile = new File(fullFileName);
        if (releaseFile.exists()) {
            FileUtils.deleteDirectory(releaseFile);
        }

        dearchive(file, parentPath, recursion);
        if (recursion) {
            delTemporal(releaseFile);
        }
    }

    /**
     * 指定提档文件和归档输入流来提档
     * @param releaseFile
     * @param tarInputStream
     * @throws IOException
     */
    private static void dearchiveFile(File releaseFile, TarArchiveInputStream tarInputStream) throws IOException {
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream buffOutStream = null;
        try {
            fileOutputStream = new FileOutputStream(releaseFile);
            buffOutStream = new BufferedOutputStream(fileOutputStream);
            byte[] buff = new byte[BUFFER_SIZE];
            int len;
            while ((len = tarInputStream.read(buff, 0, BUFFER_SIZE)) != -1) {
                buffOutStream.write(buff, 0, len);
            }
        } finally {
            if (buffOutStream != null)
                buffOutStream.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }

    /**
     * 指定路径进行提档
     * @param directory
     */
    private static void dearchiveDirectory(File directory) {
        File parentDirectory = directory.getParentFile();
        if (!parentDirectory.exists()) {
            dearchiveDirectory(parentDirectory);
            parentDirectory.mkdir();
        }
    }

    /**
     * 清理临时文件
     * @param releaseFile
     */
    private static void delTemporal(File releaseFile) {
        File[] fileArray = releaseFile.listFiles();
        int length = fileArray.length;

        for (int i = 0; i < length; i++) {
            File tmpFile = fileArray[i];
            if (tmpFile.isDirectory()) {
                delTemporal(tmpFile);
            } else if (tmpFile.getName().endsWith(TAR_SUFFIX)) {
                tmpFile.delete();
            }
        }
    }

}
