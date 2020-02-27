package com.km.eda51_demo.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import  org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

import static java.sql.Types.NUMERIC;
import static org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType.FORMULA;
import static org.apache.xmlbeans.impl.piccolo.xml.Piccolo.STRING;


public class ReadExcel {

    /**
     * 读取excel 第1张sheet （xls和xlsx）
     *
     * @param filePath	excel路径
     * @param columns	列名（表头）
     * @author lizixiang ,2018-05-08
     * @return
     */
    public List<Map<String, String>> readExcel(String filePath,String columns[]) {
        Sheet sheet = null;
        Row row = null;
        Row rowHeader = null;
        List<Map<String, String>> list = null;
        String cellData = null;
        Workbook wb = null;
        if (filePath == null) {
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = null;
            }
            if (wb != null) {
                // 用来存放表中数据
                list = new ArrayList<Map<String, String>>();
                // 获取第一个sheet
                sheet = wb.getSheetAt(0);
                // 获取最大行数
                int rownum = sheet.getPhysicalNumberOfRows();
                // 获取第一行
                rowHeader = sheet.getRow(0);
                row = sheet.getRow(0);
                // 获取最大列数
                int colnum = row.getPhysicalNumberOfCells();
                for (int i = 1; i < rownum; i++) {
                    Map<String, String> map = new LinkedHashMap<String, String>();
                    row = sheet.getRow(i);
                    if (row != null) {
                        for (int j = 0; j < colnum; j++) {
                            //String excelCellname = getCellFormatValue(rowHeader.getCell(j)).toString().trim();
                            String excelCellname = rowHeader.getCell(j).getStringCellValue().trim();
                            String columnsName = columns[j].trim();
                            if(columnsName.equals(excelCellname)){
                                //cellData = row.getCell(i).getStringCellValue().trim();
                                cellData = (String) getCellFormatValue(row.getCell(j));
                                map.put(columns[j], cellData);
                            }
                        }
                    } else {
                        break;
                    }
                    list.add(map);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**	获取单个单元格数据
     * @param cell
     * @return
     * @author lizixiang ,2018-05-08
     */
    public Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            // 判断cell类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    cellValue = String.valueOf((int)cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    // 判断cell是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    } else {
                        // 数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }

    public void WriteXLSX(String filePath,String columns[]){
        XSSFWorkbook workbook = null;
        FileOutputStream fos = null;
        XSSFSheet sheet = null;
        try{

            fos = new FileOutputStream(filePath);
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("BOM核对清单");
            XSSFRow row;
            XSSFCell cell;
            int rowIndex = 0;
            int cellIndex = 0;
            row = sheet.createRow(rowIndex);
            cell = row.createCell(0);
            cell.setCellValue("零件编号");
            cell.setCellValue("零件名称");
            cell.setCellValue("发动机型号");
            cell.setCellValue("扫描时间");

            workbook.write(fos);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fos.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
