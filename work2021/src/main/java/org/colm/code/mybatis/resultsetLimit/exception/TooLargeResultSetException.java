package org.colm.code.mybatis.resultsetLimit.exception;

public class TooLargeResultSetException extends RuntimeException {

    public TooLargeResultSetException(String queryId, int maxResultSize, int actualResultSize) {
        super(String.format("[ %s ] reached max rows limitation: %d >= %d", queryId, actualResultSize, maxResultSize));
    }

}
