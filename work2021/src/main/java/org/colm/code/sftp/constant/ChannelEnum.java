package org.colm.code.sftp.constant;

public enum ChannelEnum {
    /**
        这里我只摘出部分协议
        @see
     */
    SFTP("sftp"),
    SESSION("session"),
    SHELL("shell"),
    EXEC("exec"),
    X11("x11")
    ;

    private String channel;

    ChannelEnum(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}
