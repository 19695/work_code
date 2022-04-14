package org.colm.code;

import java.util.Collection;
import java.util.Map;

public class AssertUtil {

    private static final String EMPTY_ERROR_MSE = "parameter cannot be empty";
    private static final String NULL_ERROR_MSE = "parameter cannot be null";

    public static void assertNotEmpty(Object object) {
        assertNotEmpty(object, EMPTY_ERROR_MSE);
    }

    public static void assetNotNull(Object object) {
        assertNotEmpty(object, NULL_ERROR_MSE);
    }

    public static void assertNotEmpty(Object object, String msg) {

        if (object instanceof Map && !((Map) object).isEmpty()) {
            return;
        }
        if (object instanceof Collection && !((Collection) object).isEmpty()) {
            return;
        }
        if (object instanceof String && !"".equals((String) object)) {
            return;
        }
        throw new IllegalArgumentException(msg);
    }

    public static void assertNotNull(Object object, String msg) {
        if (object == null) {
            throw new IllegalArgumentException(msg);
        }
    }

}
