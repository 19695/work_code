package org.colm.code.excel.simple;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import static org.colm.code.excel.reflect.ParseUtil.*;

public class ExcelParser {

    /**
     * 生成 excel
     * @param list
     * @param type
     * @param fileName
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> File list2Excel (List<T> list, Class<T> type, String fileName) throws Exception {
        File file = new File(fileName);
        try (OutputStream outStream = new FileOutputStream(file);
            Workbook workbook = WorkbookFactory.create(file)
        ) {
            workbook.createSheet();
            List<String[]> strArrList = list2StringArray(list, type);
            writeLines(workbook, 0, 0, strArrList);
            workbook.write(outStream);
        }
        return file;
    }

    // todo
    public static <T> List<T> excel2List () {
        return null;
    }

    public static int writeLines(Workbook workbook, int sheetIndex, int startLine, List<String[]> targetList) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        int totalLines = targetList.size();
        int endLine = 0;
        for (int i = 0; i < totalLines; i++) {
            endLine = startLine + i;
            Row row = sheet.createRow(endLine);
            String[] target = targetList.get(i);
            for (int j = 0; j < target.length; j++) {
                Cell cell = row.createCell(j, CellType.STRING);
                cell.setCellValue(target[j]);
            }
        }
        return endLine;
    }

    private static Workbook generateWorkbook(String fileName) throws Exception {
        Workbook workbook = null;
        if (fileName.endsWith("xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (fileName.endsWith("xls")) {
            workbook = new HSSFWorkbook();
        } else {
            throw new RuntimeException("excel 文件类型只能是 xlsx 或 xls");
        }
        return workbook;
    }

    private ExcelParser () {}

}
