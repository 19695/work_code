package org.colm.code.mybatis.resultsetLimit.dialect;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DB2LimitDialect implements LimitDialect {

    private static final Pattern LIMITED_SQL_PATTERN = Pattern.compile(
            "(.+ROWNUMBER\\s*\\(\\s*\\)\\s+OVER\\s*\\(\\s*\\).+|.+FETCH\\s+FIRST\\s+[0-9]+\\s+ROWS+\\s+ONLY.*|.+\\s+LIMIT\\s+[0-9]+.*)",
            Pattern.CASE_INSENSITIVE);

    @Override
    public String getLimitSql(MappedStatement ms, BoundSql bs, Object param, RowBounds rb, CacheKey ck, int limit) {
        String sql = bs.getSql();
        return String.format("select * from ( %s ) fetch first %d rows only", sql, limit);
    }

    @Override
    public boolean isLimitedsql(BoundSql bs) {
        String sql = bs.getSql();
        if (sql == null) {
            return false;
        }
        // 因为正则不考虑换行
        sql = sql.replaceAll("\\n",  " ");
        Matcher matcher = LIMITED_SQL_PATTERN.matcher(sql);
        return matcher.matches();
    }

}
