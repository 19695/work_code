package org.colm.code.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.colm.code.file.partial.PartialInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import static org.colm.code.ThreadPoolUtil.FIXED_POOL;

public class FileUtil {

    public static String fileToBase64(File file) throws IOException {
        return bytesToBase64(fileToBytes(file));
    }

    public static byte[] fileToBytes(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            is.read(bytes);
            return bytes;
        } catch (IOException e) {
            throw e;
        }
    }

    public static byte[] base64ToBytes(String base64) {
        int mod4 = base64.length() % 4;
        if(mod4 > 0) {
            base64 = base64 + "====".substring(mod4);
        }
        return Base64.getUrlDecoder().decode(base64);
    }

    public static void base64ToFile(String base64, String path, String fileName) throws IOException {
        bytesToFile(base64ToBytes(base64), path, fileName);
    }

    public static String bytesToBase64(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes).replaceAll("\\=", "");
    }

    public static void bytesToFile(byte[] bytes, String path, String fileName) throws IOException {
        File file = new File(path, fileName);
        try (OutputStream outStream = Files.newOutputStream(file.toPath())){
            outStream.write(bytes);
            outStream.flush();
        } catch (IOException e) {
            throw e;
        }
    }

    public static String standardBase64ToUrlBase64(String normalBase64) {
        return normalBase64.replaceAll("\\+", "")
                .replaceAll("\\/", "")
                .replaceAll("\\=", "")
                .replaceAll("\r", "")
                .replaceAll("\n", "");
    }

    public static File multipartFile2File(MultipartFile multipartFile, String parentPath) {
        File file = new File(parentPath, multipartFile.getOriginalFilename());
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static byte[] asynchronized(List<PartialInfo> partialInfoList, String parentPath) throws ExecutionException, InterruptedException {
        AtomicInteger totalLength = new AtomicInteger(0);
        List<Future<PartialByteInfo>> futureList = new ArrayList<>();
        // 读取每一个分片，将每个分片转为 byte[]
        for (PartialInfo partialInfo : partialInfoList) {
            Future<PartialByteInfo> submit = FIXED_POOL.submit(() -> {
                String fileName = partialInfo.getDocId();
                File file = new File(parentPath, fileName);
                if (!file.exists() || file.length() == 0) {
                    throw new RuntimeException("文件 " + file.getAbsolutePath() + " 不存在或者文件内容为空");
                }
                try {
                    byte[] bytes = fileToBytes(file);
                    totalLength.getAndAdd(bytes.length);
                    return new PartialByteInfo(partialInfo.getSeqNo(), bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            futureList.add(submit);
        }

        // 先收集结果，得到所有的 byte[]
        byte[][] allBytes = new byte[futureList.size()][];
        for (int i = 0; i < futureList.size(); i++) {
            PartialByteInfo byteInfo = futureList.get(i).get();
            allBytes[byteInfo.seqNo] = byteInfo.getBytes();
        }

        // 将所有 byte[] 合并
        byte[] fileBytes = new byte[totalLength.get()];
        int copyStartIndex = 0;
        for (int i = 0; i < allBytes.length; i++) {
            byte[] bytes = allBytes[i];
            System.arraycopy(bytes, 0, fileBytes, copyStartIndex, bytes.length);
            copyStartIndex += bytes.length;
        }
        return fileBytes;
    }

    @Data
    @AllArgsConstructor
    static class PartialByteInfo {
        private int seqNo;
        private byte[] bytes;
    }

}
