package org.colm.code.sequence;

import lombok.Data;

@Data
public class SequencePO {

    private int id;
    private String key;
    private String sequence;
    private String version;
    private String usedStatus;

}
