package org.colm.code.mybatis.resultsetLimit.util;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.colm.code.mybatis.resultsetLimit.dialect.LimitDialect;

import java.sql.SQLException;
import java.util.List;

public abstract class ExecutorUtil {

    private ExecutorUtil() {}

    public static <E> List<E> limitQuery(LimitDialect dialect, Executor executor, MappedStatement mappedStatement,
                                         Object param, RowBounds rowBounds, ResultHandler resultHandler,
                                         BoundSql boundSql, CacheKey cacheKey, int limit) throws SQLException {
        String limitSql = dialect.getLimitSql(mappedStatement, boundSql, param, rowBounds, cacheKey, limit);
        BoundSql limitBoundSql = new BoundSql(mappedStatement.getConfiguration(), limitSql,
                boundSql.getParameterMappings(), param);
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String property = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(property)) {
                limitBoundSql.setAdditionalParameter(property, boundSql.getAdditionalParameter(property));
            }
        }
        return executor.query(mappedStatement, param, rowBounds, resultHandler, cacheKey, limitBoundSql);
    }

}
