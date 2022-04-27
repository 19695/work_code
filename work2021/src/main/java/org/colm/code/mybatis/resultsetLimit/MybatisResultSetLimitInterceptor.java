package org.colm.code.mybatis.resultsetLimit;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.colm.code.mybatis.resultsetLimit.dialect.DB2LimitDialect;
import org.colm.code.mybatis.resultsetLimit.dialect.LimitDialect;
import org.colm.code.mybatis.resultsetLimit.dialect.MySQLLimitDialect;
import org.colm.code.mybatis.resultsetLimit.exception.TooLargeResultSetException;
import org.colm.code.mybatis.resultsetLimit.util.ExecutorUtil;
import org.colm.code.mybatis.resultsetLimit.util.HandleConfigreUtil;

import java.util.*;

@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class MybatisResultSetLimitInterceptor implements Interceptor {

    // 最大结果集，-1 不做限制
    private int maxResultSetSize = -1;

    // 特别指定结果集限制
    private final Map<String, Integer> allowList = new HashMap<>();

    // 如果原始 sql 已经做了分页限制则不处理
    private boolean skipLimitedSql = true;

    private LimitDialect dialect;

    private static final Map<String, Class<? extends LimitDialect>> builtInDialects = new HashMap<>();

    static {
        builtInDialects.put("DB2", DB2LimitDialect.class);
        builtInDialects.put("MySQL", MySQLLimitDialect.class);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        String queryId = mappedStatement.getId();
        Integer allowSize = allowList.get(queryId);
        int localMaxResultSize = allowSize != null ? allowSize : maxResultSetSize;

        // 不做限制
        if (localMaxResultSize < 0) {
            return invocation.proceed();
        }

        Object param = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;

        // 因为方法是重载的，这两个参数需要判断有没有
        if (args.length == 4) {
            boundSql = mappedStatement.getBoundSql(param);
            cacheKey = executor.createCacheKey(mappedStatement, param, rowBounds, boundSql);
        } else {
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        if (skipLimitedSql && dialect.isLimitedsql(boundSql)) {
            return invocation.proceed();
        }

        // 实际执行查询
        List resultList = ExecutorUtil.limitQuery(dialect, executor, mappedStatement, param, rowBounds, resultHandler,
                boundSql, cacheKey, localMaxResultSize);

        // 策略
        return handleLargeResultSet(resultList, localMaxResultSize, queryId);
    }

    protected List handleLargeResultSet(List resultList, int maxResultSetSize, String queryId) {
        if (resultList != null && resultList.size() >= maxResultSetSize) {
            throw new TooLargeResultSetException(queryId, maxResultSetSize, resultList.size());
        }
        return resultList;
    }

    @Override
    public void setProperties(Properties properties) {

        Class<? extends LimitDialect> dialectClass = HandleConfigreUtil.handleDialectConfigure(properties, builtInDialects);
        try {
            this.dialect = dialectClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to create dialect instance for: " + dialectClass, e);
        }

        this.maxResultSetSize = HandleConfigreUtil.handleMaxResultSetSizeConfigure(properties);

        this.skipLimitedSql = HandleConfigreUtil.handleSkipLimitedSqlConfigure(properties);

        HandleConfigreUtil.handleAllowListConfigure(properties, allowList);
    }

}
