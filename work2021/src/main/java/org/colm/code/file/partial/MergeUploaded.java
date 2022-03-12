package org.colm.code.file.partial;

import org.colm.code.RecordUtil;
import org.colm.code.file.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;

public class MergeUploaded {

    @Value("${file.parent.path}")
    private String path;

    @PostMapping("/partial/merge")
    public void merge(MergeUploadedVO mergeUploadedVO) throws IOException {
        String suffix = mergeUploadedVO.getSuffix();
        List<PartialInfo> partialInfoList = mergeUploadedVO.getPartialInfoList();
        /*
            单线程处理忽略，这里直接使用多线程，但是这种方式非常的占用内存
            尤其是 Java8 之后的版本，直接使用物理内存，实际使用中存在将容器内存打满的情况
            后续可以考虑使用文件的随机写入方式，不在内存中拼接，而是直接写到文件中
         */
//        byte[] fileAllBytes = FileUtil.asynchronized(partialInfoList);
        byte[] fileAllBytes = RecordUtil.recordSpendTime(() -> {
            try {
                return FileUtil.asynchronized(partialInfoList, path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        String docId = DocIdUtil.getPartialDocId();
        String fileName = docId + suffix;
        FileUtil.bytesToFile(fileAllBytes, path, fileName);

        // 主动释放 byte[]
        fileAllBytes = null;
    }

}
