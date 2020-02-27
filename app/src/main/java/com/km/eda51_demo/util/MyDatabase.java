package com.km.eda51_demo.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import com.km.eda51_demo.data.Part;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class MyDatabase extends SQLiteAssetHelper {

    private static final String DB_NAME="info.db";

    public MyDatabase(Context context) {
        super(context, DB_NAME, null,13);
    }

    // 获取零件名称
    public String getPartName(String partcode){
        String result = "";
        SQLiteDatabase db = getReadableDatabase();
        String SELECT_SCAN_SQL = "select *from info where partcode=?";
        Cursor cursor = db.rawQuery(SELECT_SCAN_SQL, new String[]{partcode});
        if (cursor.getCount()==0){
            cursor.close();
            return result;
        }else{
            cursor.moveToFirst();
            result = cursor.getString(1);
            cursor.close();
            return result;
        }
    }

    // 判断零件编号是否已经扫描过
    public boolean isVerify(String partcode){
        boolean isverify = false;

        SQLiteDatabase db = getReadableDatabase();
        String SELECT_SCAN_SQL = "select *from scan where partcode=?";
        Cursor cursor = db.rawQuery(SELECT_SCAN_SQL, new String[]{partcode});
        if (cursor.getCount()==0){
            cursor.close();
            isverify = false;
        }else{
            cursor.moveToFirst();
            cursor.close();
            isverify = true;
        }

        return isverify;
    }

    // 获取零件名称、发动机型号
    public List<Part> getInfo(String barcode){
        List<Part> partList = new ArrayList<>();
        String GET_INFO_SQL = "select partcode, partname, enginetype from info where partcode=?";
        try{
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(GET_INFO_SQL, new String[]{barcode});
            if (cursor.getCount()==0){
                cursor.close();
                return partList;
            }else{
                Part part;
                cursor.moveToFirst();
                part = new Part();
                part.setPartcode(cursor.getString(cursor.getColumnIndex("partcode")));
                part.setPartname(cursor.getString(cursor.getColumnIndex("partname")));
                part.setEnginetype(cursor.getString(cursor.getColumnIndex("enginetype")));
                partList.add(part);
//                while (cursor.moveToNext()){
//                    part = new Part();
//                    part.setPartcode(cursor.getString(cursor.getColumnIndex("partcode")));
//                    part.setPartname(cursor.getString(cursor.getColumnIndex("partname")));
//                    part.setEnginetype(cursor.getString(cursor.getColumnIndex("enginetype")));
//                    partList.add(part);
//                }
                cursor.close();
            }
        }catch (SQLiteAssetException ex){
            ex.printStackTrace();
        }

        return partList;
    }

}
