package org.colm.code.sftp.constant;

public enum ModeEnum {

    OVERWRITE(0, "OVERWRITE"),
    RESUME(1, "RESUME"),
    APPEND(2, "APPEND")
    ;

    private int value;
    private String desc;

    ModeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
