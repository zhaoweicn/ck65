package com.km.eda51_demo.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import com.km.eda51_demo.util.ReadExcel;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadFile {

    // 导入EXCEL文件到SQLITE
    public static boolean read2DB(String excelname, Context con, String filename) {
        try {
            SQLiteDatabase db = new SQLiteHelper(con, filename)
                    .getWritableDatabase();
            String[] columns = {"序号","ID","工位号","产品型号代码","标号","零件图号","零件名称","零件数量","零部件编号","零件批号","生产厂家","发动机型号","备注","是否验证","是否主物料","是否显示","是否保存","物料号","当前装配数量"};
            List<Map<String, String>> mapList = new ReadExcel().readExcel(excelname, columns);

//            Workbook course = Workbook.getWorkbook(f);
//            Sheet sheet = course.getSheet(0);

            String id = null;
            String staffid = null;
            String pmcode = null;
            String flagcode = null;
            String partcode = null;
            String partname = null;
            String partnum = null;
            String partnumber = null;
            String partbatch = null;
            String manufacturer = null;
            String enginetype = null;
            String remark = null;
            String isverify = null;
            String ismaster = null;
            String isdisplay = null;
            String issave = null;
            String material = null;
            String number = null;

            db.beginTransaction();
            ContentValues values = new ContentValues();

            //先更新
//            for (int i = 1; i < sheet.getRows(); i++) {
            for (Map<String, String> map: mapList){
                //更新数据
                //Log.d("hdyupdate", sheet.getCell(0, i).getContents());
                Log.d("序号", map.get("序号"));
//                for (Map.Entry<String, String> entry : map.entrySet()){
//                    Log.d("", entry.getKey() + ":" + entry.getValue());
//                }
                id = map.get("ID");
                staffid =map.get("工位号");
                pmcode = map.get("产品型号代码");
                flagcode =map.get("标号");
                partcode =map.get("零件图号");
                partname =map.get("零件名称");
                partnum = map.get("零件数量");
                partnumber =map.get("零部件编号");
                partbatch =map.get("零件批号");
                manufacturer = map.get("生产厂家");
                enginetype =map.get("发动机型号");
                remark = map.get("备注");
                isverify = map.get("是否验证");
                ismaster =map.get("是否主物料");
                isdisplay = map.get("是否显示");
                issave =map.get("是否保存");
                material = map.get("物料号");
                number = map.get("当前装配数量");

                values.put("id", id);
                values.put("staffid", staffid);
                values.put("pmcode", pmcode);
                values.put("flagcode", flagcode);
                values.put("partcode", partcode);
                values.put("partname", partname);
                values.put("partnum", partnum);
                values.put("partnumber", partnumber);
                values.put("partbatch", partbatch);
                values.put("manufacturer", manufacturer);
                values.put("enginetype", enginetype);
                values.put("remark", remark);
                values.put("isverify", isverify);
                values.put("ismaster", ismaster);
                values.put("isdisplay", isdisplay);
                values.put("issave", issave);
                values.put("material", material);
                values.put("number", number);
                db.insertWithOnConflict("info", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("hdySqlError",e.getMessage());
            return false;
        }
    }

    // 从SQLITE导入到EXCEL
    public static boolean ExportExcel(String excelname, Context context, String filename){
        try{
            SQLiteDatabase db = new SQLiteHelper(context, filename)
                    .getReadableDatabase();
            Cursor cur = db.rawQuery("select *from scan",null);
            if (cur.getCount()==0){
                return false;
            }
            XSSFWorkbook workbook = null;
            FileOutputStream fos = null;
            XSSFSheet sheet = null;
            try{

                fos = new FileOutputStream(excelname);
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("BOM核对清单");
                XSSFRow row;
                XSSFCell cell;
                int rowIndex = 0;
                row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue("零件编号");
                row.createCell(1).setCellValue("零件名称");
                row.createCell(2).setCellValue("发动机型号");
                row.createCell(3).setCellValue("扫描时间");

                cur.moveToFirst();
                while(cur.moveToNext()){
                    row = sheet.createRow(++rowIndex);
                    row.createCell(0).setCellValue(cur.getString(cur.getColumnIndex("partcode")));
                    row.createCell(1).setCellValue(cur.getString(cur.getColumnIndex("partname")));
                    row.createCell(2).setCellValue(cur.getString(cur.getColumnIndex("enginetype")));
                    row.createCell(3).setCellValue(cur.getString(cur.getColumnIndex("scdate")));
                }

                workbook.write(fos);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try{
                    cur.close();
                    workbook.close();
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            return true;

        }catch (Exception e){
            e.printStackTrace();
            Log.d("ExcelError", e.getMessage());
            return false;
        }
    }
}
