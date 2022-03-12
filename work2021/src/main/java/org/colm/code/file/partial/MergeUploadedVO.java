package org.colm.code.file.partial;

import lombok.Data;

import java.util.List;

@Data
public class MergeUploadedVO {

    private List<PartialInfo> partialInfoList;
    private String suffix;

}
