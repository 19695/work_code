package org.colm.code.mybatis.resultsetLimit.util;

import org.colm.code.mybatis.resultsetLimit.dialect.LimitDialect;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public abstract class HandleConfigreUtil {

    public HandleConfigreUtil(){}

    public static boolean handleSkipLimitedSqlConfigure(Properties properties) {
        String skipLimitedSqlStr = properties.getProperty("skipLimitedSql");
        if (skipLimitedSqlStr != null) {
            if ("true".equalsIgnoreCase(skipLimitedSqlStr)) {
                return true;
            } else if ("false".equalsIgnoreCase(skipLimitedSqlStr)) {
                return false;
            } else {
                throw new IllegalArgumentException("property 'skipLimitedSql' is not a boolean value: " + skipLimitedSqlStr);
            }
        }
        return true;
    }

    public static void handleAllowListConfigure(Properties properties, Map allowList) {
        String allowListStr = properties.getProperty("allowList");
        if (allowListStr != null) {
            /**
             @see String#split(String, int)
             */
            String[] splits = allowListStr.split(",", -1);
            for (String item : splits) {
                String[] allowItemStrs = item.split(",", 0);
                if (allowItemStrs.length >= 2) {
                    allowList.put(allowItemStrs[0].trim(), Integer.parseInt(allowItemStrs[1].trim()));
                } else {
                    allowList.put(allowItemStrs[0], -1);
                }
            }
        }
    }

    public static int handleMaxResultSetSizeConfigure(Properties properties) {
        String maxResultSetSizeStr = properties.getProperty("maxResultSetSize");
        if (maxResultSetSizeStr != null) {
            return Integer.parseInt(maxResultSetSizeStr);
        }
        return -1;
    }

    public static Class<? extends LimitDialect> handleDialectConfigure(Properties properties, Map<String, Class<? extends LimitDialect>> builtInDialects) {
        String dialect = properties.getProperty("dialect");
        Objects.requireNonNull(dialect, "No dialect configured");
        // todo 这里可以加一个别名处理 alias
        Class<? extends LimitDialect> dialectClass = builtInDialects.get(dialect);
        // 提供扩展能力，可以传入全类名
        if (dialectClass == null) {
            try {
                dialectClass = (Class<? extends LimitDialect>) Class.forName(dialect);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to get dialect class: " + dialect, e);
            }
        }
        return dialectClass;
    }
}
