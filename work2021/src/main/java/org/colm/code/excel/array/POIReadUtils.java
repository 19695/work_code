package org.colm.code.excel.array;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 String[] 解析 excel 文件
 */
public class POIReadUtils {

    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";
    public static final String DATE_FORMAT = "yyyy/MM/dd";

    /**
     * 读入 excel 文件，解析后返回内容
     * @param file
     * @return
     * @throws IOException
     */
    public static List<String[]> readExcel(MultipartFile file) throws IOException {
        // 检查文件类型
        checkFile(file);
        // 获取 Workbook 对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        List<String[]> list = new ArrayList<String[]>();
        if(workbook != null){
            // 遍历 sheet
            for(int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                // 获取当前 sheet 工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if(sheet == null) {
                    continue;
                }
                // 获得当前sheet的开始行
                int firstRowNum = sheet.getFirstRowNum();
                // 获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                // 循环除了第一行的所有行
                for(int rowNum = firstRowNum + 1; rowNum < lastRowNum; rowNum++) {
                    // 获得当前行
                    Row row = sheet.getRow(rowNum);
                    if(row == null) {
                        continue;
                    }
                    // 获得当前行的开始列
                    short firstCellNum = row.getFirstCellNum();
                    // 获得当前行的最后列
                    short lastCellNum = row.getLastCellNum();
                    // 获得当前行的列数
                    // 我不知道为什么一开始这么做，可以测试一下在不同情况的 excel 填写下的取值是什么
//                    short lastCellNum = row.getPhysicalNumberOfCells();
                    // 获取不为空的列个数作为数组初始化大小
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    // 循环当前行
                    for(int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        cells[cellNum] = getCellValue(cell);
                    }
                    list.add(cells);
                }
            }
            workbook.close();
        }
        return list;
    }

    /**
     * 将所有单元格的数据都按照字符串读出
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell) {
        String cellValue = "";
        if(cell == null){
            return cellValue;
        }
        //如果当前单元格内容为日期类型，需要特殊处理
        String dataFormatString = cell.getCellStyle().getDataFormatString();
        if(dataFormatString.equals("m/d/yy")) {
            cellValue = new SimpleDateFormat(DATE_FORMAT).format(dataFormatString);
            return cellValue;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC: //数字
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA: //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case Cell.CELL_TYPE_BLANK: //空值
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }

        return cellValue;
    }

    /**
     * 判断是否是 excel
     * @param file
     * @throws IOException
     */
    private static void checkFile(MultipartFile file) throws IOException {
        //判断文件是否存在
        if(null == file){
            throw new FileNotFoundException("文件不存在！");
        }
        //获得文件名
        String fileName = file.getOriginalFilename();
        //判断文件是否是excel文件
        if(!fileName.endsWith(XLS) && !fileName.endsWith(XLSX)){
            throw new IOException(fileName + "不是excel文件");
        }
    }

    /**
     * 依据文件后缀来获取不同版本的 Workbook
     * @param file
     * @return
     */
    private static Workbook getWorkBook(MultipartFile file) {
        // 获取文件的原始名称
        String filename = file.getOriginalFilename();
        Workbook workbook = null;
        try (InputStream in = file.getInputStream();) {
            // 也可以使用这样的方式创建，这种方式 Excel 2003/2007/2010 都是可以处理的， 它会依据流的 first 8 byte 判断是什么类型
//            WorkbookFactory.create(in);
            if(filename.endsWith(XLS)) {
                //2003
                workbook = new HSSFWorkbook(in);
            }
            if(filename.endsWith(XLSX)) {
                //2007
                workbook = new XSSFWorkbook(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

}
