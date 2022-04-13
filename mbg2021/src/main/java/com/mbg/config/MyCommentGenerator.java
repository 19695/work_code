package com.mbg.config;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

public class MyCommentGenerator extends DefaultCommentGenerator {

    private boolean addRemarkComments = false;

    public MyCommentGenerator() {
        super();
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        String remarks = introspectedColumn.getRemarks();
        if (this.addRemarkComments && StringUtility.stringHasValue(remarks)) {
            field.addJavaDocLine(formatRemark(remarks));
        }
    }

    private String formatRemark(String remarks) {
        if (remarks.contains("\n") || remarks.contains("\r\n")) {
            return "/*\n\t" + remarks + "\n\t";
        }
        return "// " + remarks;
    }
}
