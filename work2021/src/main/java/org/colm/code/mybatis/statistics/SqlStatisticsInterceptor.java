package org.colm.code.mybatis.statistics;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlStatisticsInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SqlStatisticsInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        String statementId = mappedStatement.getId();
        int size = -1;
        boolean flag = false;
        Object result;
        try {
            flag = true;
            Object temp = invocation.proceed();

            try {
                if (temp instanceof Collection) {
                    size = ((Collection<?>) temp).size();
                } else if (temp instanceof Integer) {
                    size = (Integer) temp;
                }
            } catch (Exception e) {
                size = -2;
            }
            result = temp;
            flag = false;
        } finally {
            if (flag) {
                long end = System.currentTimeMillis();
                logger.debug("sql {} process time is {} ms. result size is {}", statementId, end - start, size);
            }
        }

        return result;
    }

}
