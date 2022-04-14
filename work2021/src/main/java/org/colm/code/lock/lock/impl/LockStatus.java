package org.colm.code.lock.lock.impl;

public enum LockStatus {
    LOCK("1"),
    UNLOCK("0")
    ;

    private String status;

    LockStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
