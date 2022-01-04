package org.colm.code.excel.reflect;

import lombok.Data;

import java.lang.reflect.Method;
import java.math.RoundingMode;

@Data
public class ExcelFieldInfo {
    private Class<?> propertyType;
    private Method readMethod;
    private Method writeMethod;
    private int index;
    private String title;
    private RoundingMode roundingMode;
    private int scaleLen;
    private String dateFormat;
}
