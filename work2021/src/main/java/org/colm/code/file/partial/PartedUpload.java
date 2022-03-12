package org.colm.code.file.partial;

import org.colm.code.file.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

public class PartedUpload {

    @Value("${file.parent.path}")
    private String path;

    @PostMapping("/partial/upload")
    public void upload(PartialUploadVO uploadVO) throws IOException {
        String base64 = uploadVO.getBase64();
        String seqNo = uploadVO.getSeqNo();
        String docId = DocIdUtil.getPartialDocId();
        FileUtil.base64ToFile(base64, path, docId);
    }

}
