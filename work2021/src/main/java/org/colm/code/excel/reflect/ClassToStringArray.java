package org.colm.code.excel.reflect;

import java.lang.reflect.Method;

public class ClassToStringArray {

    private Method readMethod;
    private Method writeMethod;
    private int index;
    private Class<?> fieldType;

    public Method getReadMethod() {
        return readMethod;
    }

    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public String toString() {
        return "ClassToStringArray{" +
                "readMethod=" + readMethod +
                ", writeMethod=" + writeMethod +
                ", index=" + index +
                ", fieldType=" + fieldType +
                '}';
    }
}
