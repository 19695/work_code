package com.mbg;

import com.mbg.tools.StringBuilderDecorator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static com.mbg.config.MapperConfig.*;

public class GenerateMapperFile {

    private static final String INDENTATION = "\t";
    private static List<String> fieldList = new ArrayList<>();
    private static List<String> columnList = new ArrayList<>();
    private static Set<Map.Entry<String, String>> fieldColumnEntrySet;
    private static Class typeClass;
    private static StringBuilderDecorator out = new StringBuilderDecorator(100, INDENTATION);

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        resolveFields();
        appendNode();
        if (output2Console) {
            output2Console();
        } else {
            output2File();
        }
    }

    private static void output2File() throws IOException {
        String fileName = mapperPath + File.separator + simpleName.toLowerCase() + suffix;
        try (BufferedOutputStream buffOutStream = new BufferedOutputStream(new FileOutputStream(fileName))) {
            buffOutStream.write(out.toString().getBytes());
            buffOutStream.flush();
        }
    }

    private static void output2Console() {
        System.out.println(out.toString());
    }

    private static void appendNode() {
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
            .append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"")
            .append("\t\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">")
            .append("<mapper namespace=\"" + nameSpace + "\">")
            .setIndentation(1)
            .emptyLine();

        appendInsert();
        out.emptyLine();

        appendDelete();
        appendUpdate();
        appendQuery();

        out.resetIndentation()
            .emptyLine()
            .append("</mapper>");
    }

    // todo
    private static void appendQuery() {

    }

    // todo
    private static void appendUpdate() {

    }

    private static void appendDelete() {
        out.appendWithIndent("<delete id=\"delete\" parameterType=\"java.util.List\">")
            .increaseIndent(1)
            .appendWithIndent("DELETE FROM " + tableName)
            .appendWithIndent("<where>")
            .increaseIndent(1)
            .appendWithIndent("<foreach collection=\"list\" item=\"item\">")
            .increaseIndent(1)
            .appendWithIndent("<trim prefix=\"(\" suffix=\")\" prefixOverrides=\"AND\">")
            .increaseIndent(1);
        for (Map.Entry<String, String> entry : fieldColumnEntrySet) {
            out.appendWithIndent("<if test=\"item." + entry.getKey() + " != null\">")
                .increaseIndent(1)
                .appendWithIndent("AND " + entry.getValue() + " = #{item." + entry.getKey() + "}")
                .decreaseIndent(1)
                .appendWithIndent("</if>");
        }
        out.decreaseIndent(1)
            .appendWithIndent("</trim>")
            .decreaseIndent(1)
            .appendWithIndent("</foreach>")
            .decreaseIndent(1)
            .appendWithIndent("</where>")
            .decreaseIndent(1)
            .appendWithIndent("</delete>");
    }

    private static void appendInsert() {
        out.appendWithIndent("<insert id=\"insert\" parameterType=\"java.util.List\">")
            .increaseIndent(1)
            .appendWithIndent("INSERT INTO " + tableName)
            .appendWithIndent("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");

        for (Map.Entry<String, String> entry : fieldColumnEntrySet) {
            out.indent()
                .appendWithIndent("<if test=\"item." + entry.getKey() + " != null\">")
                .indent(2)
                .appendWithIndent(entry.getValue() + ",")
                .indent()
                .appendWithIndent("</if>");
        }
        out.appendWithIndent("</trim>")
            .appendWithIndent("VALUES")
            .appendWithIndent("<foreach collection=\"list\" item=\"item\" separator=\",\">")
            .indent()
            .appendWithIndent("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (String field : fieldList) {
            out.indent(2)
                .appendWithIndent("<if test=\"item." + field + " != null \">")
                .indent(3)
                .appendWithIndent("#{item." + field + "},")
                .indent(2)
                .appendWithIndent("</if>");
        }
        out.indent()
            .appendWithIndent("</trim>")
            .appendWithIndent("</foreach>")
            .decreaseIndent(1)
            .appendWithIndent("</insert>");
    }

    private static void resolveFields() throws ClassNotFoundException {
        typeClass = Class.forName(typeName);
        Field[] fieldArray = typeClass.getDeclaredFields();
        LinkedHashMap<String, String> fieldColumnMap = new LinkedHashMap<>();
        for (Field field : fieldArray) {
            fieldList.add(field.getName());
            addColumnList(field.getName(), fieldColumnMap);
        }
        fieldColumnEntrySet = fieldColumnMap.entrySet();
    }

    private static void addColumnList(String filedName, LinkedHashMap fieldColumnMap) {
        StringBuilder builder = new StringBuilder();
        for (char ch : filedName.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                builder.append("_").append(Character.toLowerCase(ch));
                continue;
            }
            builder.append(ch);
        }
        columnList.add(builder.toString());
        fieldColumnMap.put(filedName, builder.toString());
    }

}
