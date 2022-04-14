package org.colm.code.blankReplace;

import org.colm.code.StringUtil;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlankReplaceUtil {

    private static Map<Class, List<PropertyDescriptor>> fieldContainer = new HashMap<>();
    private static Map<Class, List<PropertyDescriptor>> typeContainer = new HashMap<>();

    public static <T> void resolveBlankObject(T bean) throws IntrospectionException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        resolveBlank(bean, CheckType.Type);
    }

    public static <T> void resolveBlankField(T bean) throws IntrospectionException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        resolveBlank(bean, CheckType.Field);
    }

    /*
        背景介绍：有些时候对象中的属性值为空串，实际业务逻辑中要求为 null
     */
    private static <T> void resolveBlank(T bean, CheckType checkType) throws InvocationTargetException, IllegalAccessException, IntrospectionException, NoSuchFieldException {
        Class<T> type = (Class<T>) bean.getClass();
        List<PropertyDescriptor> descriptors;
        switch (checkType) {
            case Type:
                descriptors = getTypeDescriptor(type);
                break;
            case Field:
                descriptors = getFieldDescriptor(type);
                break;
            default:
                throw new IllegalArgumentException("检测类型错误");
        }
        if (descriptors != null && !descriptors.isEmpty()) {
            for (PropertyDescriptor descriptor : descriptors) {
                String value = StringUtil.valueOf(descriptor.getReadMethod().invoke(bean));
                if (!"".equals(value.trim())) {
                    descriptor.getWriteMethod().invoke(bean, (Object) null);
                }
            }
        }
    }

    private static <T> List<PropertyDescriptor> getTypeDescriptor(Class<T> type) throws IntrospectionException {
        List<PropertyDescriptor> result = typeContainer.get(type);
        if (result == null) {
            result = new ArrayList<>();
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String fieldName = propertyDescriptor.getName();
                if ("class".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                result.add(propertyDescriptor);
            }
            typeContainer.put(type, result);
        }
        return result;
    }

    private static <T> List<PropertyDescriptor> getFieldDescriptor(Class<T> type) throws IntrospectionException, NoSuchFieldException {
        List<PropertyDescriptor> result = fieldContainer.get(type);
        if (result == null) {
            result = new ArrayList<>();
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String fieldName = propertyDescriptor.getName();
                if ("class".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                Field declaredField = type.getDeclaredField(fieldName);
                BlankCheck annotation = declaredField.getAnnotation(BlankCheck.class);
                if (annotation != null) {
                    result.add(propertyDescriptor);
                }
            }
            fieldContainer.put(type, result);
        }
        return result;
    }


    private static enum CheckType {
        Field,
        Type
    }

}
