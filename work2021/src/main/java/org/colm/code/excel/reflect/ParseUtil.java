package org.colm.code.excel.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParserUtil {

    // String[] cache
    private static final Map<Class<?>, List<ClassToStringArray>> arrayCache = new ConcurrentHashMap<>();
    
    protected static <T> List<String[]> list2StringArray(List<T> list, Class<T> type) throws Exception {
        List<ClassToStringArray> toStrArrList = class2StringDesc(type); 
        int length = toStrArrList.size();
        List<String[]> arrayList = new ArrayList<>();
        for (T t : list) {
            String[] array = new String[length];
            for (ClassToStringArray toStringArray : toStrArrList) {
                Object value = toStringArray.getReadMethod().invoke(t);
                array[toStringArray.getIndex()] = object2String(value, false);
            }
            arrayList.add(array);
        }
        return arrayList;
    }
    
    protected static <T> List<ClassToStringArray> class2StringDesc(Class<T> type) {
        return arrayCache.computeIfAbsent(type, t -> {
            List<ClassToStringArray> toStrArrList = new ArrayList<>();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(type);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor descriptor : propertyDescriptors) {
                    String fieldName = descriptor.getName();
                    if ("class".equalsIgnoreCase(fieldName)) {
                        continue;
                    }
                    type.getDeclaredField(fieldName);
                }
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 对象转为字符串，若为 null 返回 ""
     * @param obj
     * @param trim 当为 true 时去除前后空格
     * @return
     */
    protected static String object2String (Object obj, boolean trim) {
        if (obj == null) {
            return "";
        }
        String value = String.valueOf(obj);
        if (trim) {
            return value.trim();
        }
        return value;
    }
    
}
