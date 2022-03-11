package org.colm.code.excel.csv;

import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

import static org.colm.code.excel.reflect.ParseUtil.list2StringArray;

public class CSVParser {

    // Carriage-Return Line-Feed
    private static char[] crlf;

    /**
     * 通过对象的集合导出 csv
     * @param fileName
     * @param data
     * @param type
     * @param <T>
     * @throws Exception
     */
    public static <T> void export(String fileName, List<T> data, Class<T> type) throws Exception {
        export(fileName, list2StringArray(data, type));
    }

    /**
     * 通过 String 数组的集合导出 csv
     * @param fileName
     * @param strList
     * @param <T>
     * @throws Exception
     */
    public static <T> void export(String fileName, List<String[]> strList) throws Exception {
        export(fileName, convertContent(strList));
    }

    /**
     * 通过 String 导出 csv
     * @param fileName
     * @param content
     * @param <T>
     * @throws Exception
     */
    public static <T> void export(String fileName, String content) throws Exception {
        try (Writer writer = new FileWriter(fileName);
             BufferedWriter buffWriter = new BufferedWriter(writer)
        ) {
            buffWriter.write(content);
            buffWriter.flush();
        }
    }

    private static String convertContent(List<String[]> strArrList) {
        if (CollectionUtils.isEmpty(strArrList)) {
            return "";
        }
        StringBuilder content = new StringBuilder();
        for (String[] strArray : strArrList) {
            int length = strArray.length;
            for (int i = 0; i < length; i++) {
                content.append(strArray[i]);
                if (i != length - 1) {
                    content.append(",");
                } else {
                    content.append(crlf());
                }
            }
        }
        return content.toString();
    }

    private static char[] crlf() {
        if (crlf == null) {
            try {
                String separator = System.getProperties().getProperty("line.separator");
                crlf = separator.toCharArray();
            } catch (Exception e) {
                /*
                    unix        \n
                    windows     \r\n
                 */
                try {
                    String platform = String.valueOf(System.getProperties().get("sun.desktop"));
                    if ("windows".equalsIgnoreCase(platform)) {
                        return crlf = new char[]{'\r', '\n'};
                    }
                } catch (Exception ex) {}
                crlf = new char['\n'];
            }
        }
        return crlf;
    }

    private CSVParser () {}

}
