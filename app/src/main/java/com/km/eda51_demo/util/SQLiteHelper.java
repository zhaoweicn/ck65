package com.km.eda51_demo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
    }
    public SQLiteHelper(Context con, String name){
        this(con, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        //db.execSQL("create table wis(partNo varchar(20) primary key, partName varchar(20), position varchar(20), partSale varchar(20),abcPro varchar(20),partPro varchar(20),partType varchar(20))");
        //db.execSQL("create table table2(partNo varchar(20), position varchar(20), partNum varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {
        // TODO Auto-generated method stub
        //db.execSQL("drop table if exists wis");
        //db.execSQL("drop table if exists table2");
        onCreate(db);
    }

}
