package org.colm.code.mybatis.resultsetLimit.dialect;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

public interface LimitDialect {

    String getLimitSql(MappedStatement mappedStatement, BoundSql boundSql, Object param, RowBounds rowBounds,
                       CacheKey cacheKey, int limit);

    boolean isLimitedsql(BoundSql boundSql);

}
