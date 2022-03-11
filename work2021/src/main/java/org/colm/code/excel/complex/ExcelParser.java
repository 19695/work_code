package org.colm.code.excel.complex;

import org.apache.poi.ss.usermodel.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelParser {

    private static Map<Class, List<ExcelFieldInfo>> cachedType = new ConcurrentHashMap<>();

    // todo 还可以扩展指定长度解析

    /**
     * 标注 ExcelCell 注解的对象进行 excel 解析
     * @param file
     * @param type
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> excel2List(File file, Class<T> type, Integer sheetIndex) throws Exception {
        if(null == type) {
            throw new RuntimeException("解析类型必须指定");
        }
        Workbook workbook = WorkbookFactory.create(file);
        List<ExcelFieldInfo> fieldInfoList = parseType(type);
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if(sheet == null) {
            return null;
        }
        Iterator<Row> iterator = sheet.rowIterator();
        // 首行是 title 直接跳过
        if(iterator.hasNext()) {
            iterator.next();
        }
        List<T> resultList = new ArrayList<>();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            // type.newInstance() 已经被淘汰了
            T bean = type.getDeclaredConstructor().newInstance();
            for (ExcelFieldInfo fieldInfo : fieldInfoList) {
                Cell cell = row.getCell(fieldInfo.getIndex());
                // 这里的判断可以使用 predicate
                if(cell != null) {
                    String value = getStringValue4Cell(cell, fieldInfo);
                    Object basicValue = string2BasicType(value, fieldInfo);
                    if(null != basicValue) {
                        fieldInfo.getWriteMethod().invoke(bean, value);
                    }
                }
            }
            resultList.add(bean);
        }
        return resultList;
    }

    /**
     * 标注 ExcelCell 注解的对象导出为 excel
     * @param fileName
     * @param sheetName
     * @param workbook
     * @param dataList
     * @param type
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> File list2Excel(String fileName, String sheetName, Workbook workbook, List<T> dataList, Class<T> type) throws Exception {
        if(dataList == null) {
            return null;
        }
        Sheet sheet = workbook.createSheet(sheetName);
        Row titleRow = sheet.createRow(0);
        // 生成 title
        List<ExcelFieldInfo> fieldInfoList = parseType(type);
        for(ExcelFieldInfo info : fieldInfoList) {
            Cell cell = titleRow.createCell(info.getIndex());
            cell.setCellValue(info.getTitle());
        }
        int rowIndex = 1;
        for (T date : dataList) {
            Row row = sheet.createRow(rowIndex);
            for (ExcelFieldInfo fieldInfo : fieldInfoList) {
                Cell cell = row.createCell(fieldInfo.getIndex());
                Object field = fieldInfo.getReadMethod().invoke(date);
                if(field != null) {
                    setCellValue(cell, fieldInfo, field);
                }
            }
        }
        rowIndex++;
        File file = new File(fileName);
        file.createNewFile();
        try(FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
            outputStream.flush();
        } finally {
            workbook.close();
        }
        return file;
    }

    /**
     * 导出 excel 时设置单元格值
     * @param cell
     * @param excelFieldInfo
     * @param value
     */
    private static void setCellValue(Cell cell, ExcelFieldInfo excelFieldInfo, Object value) {
        Class<?> propertyType = excelFieldInfo.getPropertyType();
        if(String.class.equals(propertyType)) {
            cell.setCellValue(String.valueOf(value));
        }
        if(Integer.class.equals(propertyType) || int.class.equals(propertyType)) {
            cell.setCellValue(Integer.parseInt(String.valueOf(value)));
        }
        if(Long.class.equals(propertyType) || long.class.equals(propertyType)) {
            cell.setCellValue(Long.parseLong(String.valueOf(value)));
        }
        if(BigDecimal.class.equals(propertyType)) {
            cell.setCellValue(value.toString());
        }
        if(Boolean.class.equals(propertyType) || boolean.class.equals(propertyType)) {
            cell.setCellValue(Boolean.parseBoolean(String.valueOf(value)));
        }
        if(Date.class.equals(propertyType)) {
            String dateFormat = excelFieldInfo.getDateFormat();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            if(!(value instanceof Date)) {
                throw new IllegalArgumentException("非日期类型");
            }
            String format = simpleDateFormat.format(value);
            cell.setCellValue(format);
        }
    }

    /**
     * 将解析的 String 转为基本数据类型
     * @param value
     * @param excelFieldInfo
     * @return
     */
    private static Object string2BasicType(String value, ExcelFieldInfo excelFieldInfo) {
        Class<?> propertyType = excelFieldInfo.getPropertyType();
        if(value == null) {
            return null;
        }
        if(String.class.equals(propertyType)) {
            return value;
        }
        if(!"".equals(value)) {
            if(Integer.class.equals(propertyType) || int.class.equals(propertyType)) {
                return Integer.valueOf(value);
            }
            if(Long.class.equals(propertyType) || long.class.equals(propertyType)) {
                return Long.valueOf(value);
            }
            if(BigDecimal.class.equals(propertyType)) {
                return new BigDecimal(value);
            }
            if(Boolean.class.equals(propertyType) || boolean.class.equals(propertyType)) {
                return Boolean.valueOf(value);
            }
            if(Double.class.equals(propertyType) || double.class.equals(propertyType)) {
                return BigDecimal.valueOf(Double.valueOf(value));
            }
            if(Date.class.equals(propertyType)) {
                String dateFormat = excelFieldInfo.getDateFormat();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
                try {
                    return simpleDateFormat.parse(value);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 将单元格数据解析为 String
     * @param cell
     * @param excelFieldInfo
     * @return
     */
    private static String getStringValue4Cell(Cell cell, ExcelFieldInfo excelFieldInfo) {
        String value = "";
        switch (cell.getCellTypeEnum()) {
            case STRING:
                value = String.valueOf(cell.getStringCellValue());
                break;
            case NUMERIC:
                String dateFormat = excelFieldInfo.getDateFormat();
                if (dateFormat != "") {
                    Date dateCellValue = cell.getDateCellValue();
                    if(dateCellValue != null) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
                        value = simpleDateFormat.format(dateCellValue);
                    }
                } else {
                    BigDecimal bigDecimal = new BigDecimal(cell.getNumericCellValue());
                    bigDecimal.setScale(excelFieldInfo.getScaleLen(), excelFieldInfo.getRoundingMode());
                    value = bigDecimal.toString();
                }
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case BLANK:
                break;
            case FORMULA:
                value = String.valueOf(cell.getCellFormula());
                break;
            case _NONE:
                value = "warning:Unknown type";
                break;
            case ERROR:
                value = "warning:Error type";
                break;
        }
        return value;
    }

    /**
     * 解析 excel 对应的实体类
     * @param type
     * @param <T>
     * @throws IntrospectionException
     * @throws NoSuchFieldException
     */
    private static <T> List<ExcelFieldInfo> parseType(Class<T> type) throws IntrospectionException, NoSuchFieldException {
        if(cachedType.containsKey(type)) {
            return cachedType.get(type);
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        List<ExcelFieldInfo> fieldInfoList = new ArrayList<>();
        if(propertyDescriptors.length <= 0) {
            return fieldInfoList;
        }
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if("class".equals(propertyDescriptor.getName())) {
                continue;
            }
            Field field = type.getField(propertyDescriptor.getName());
            ExcelCell excelCell = field.getAnnotation(ExcelCell.class);
            if(excelCell == null) {
                continue;
            }
            ExcelFieldInfo excelFieldInfo = new ExcelFieldInfo();
            excelFieldInfo.setPropertyType(propertyDescriptor.getPropertyType());
            excelFieldInfo.setReadMethod(propertyDescriptor.getReadMethod());
            excelFieldInfo.setWriteMethod(propertyDescriptor.getWriteMethod());
            excelFieldInfo.setIndex(excelCell.index());
            excelFieldInfo.setTitle(excelCell.title());
            excelFieldInfo.setRoundingMode(excelCell.roundingMode());
            excelFieldInfo.setScaleLen(excelCell.scaleLen());
            excelFieldInfo.setDateFormat(excelCell.dateFormat());
            fieldInfoList.add(excelFieldInfo);
        }
        cachedType.put(type, fieldInfoList);
        return fieldInfoList;
    }

    private ExcelParser () {}

}