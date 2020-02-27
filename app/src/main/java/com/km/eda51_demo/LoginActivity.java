package com.km.eda51_demo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.km.eda51_demo.util.ReadFile;
import com.km.eda51_demo.util.SQLiteHelper;

import java.io.File;

public class LoginActivity extends AppCompatActivity {

    // EXCEL文件路径
    private static String importExcel = "/";
    private static String exportExcel = "/";
    // SQLITE文件路径
    private static String dbScan = "/data/data/com.km.eda51_demo/databases/scan.db";
    private static String dbPart = "/data/data/com.km.eda51_demo/databases/part.db";
    private SQLiteDatabase sdb_part;
    private SQLiteDatabase sdb_scan;
    private SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // EXCEL文件路径
        importExcel = Environment.getExternalStorageDirectory() + File.separator;
        exportExcel = Environment.getExternalStorageDirectory() + File.separator;

        InitDBPart();
        InitDBScan();

        // 获取权限
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    // 扫描操作
    public void onClickInvActivity(View view){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event){
        if (KeyCode==KeyEvent.KEYCODE_BACK){
            System.exit(0);
        }
        return false;
    }

    // 导入EXCEL文件
    public void onClickImportExcel(View view){
        File importFile = new File(importExcel, "BOM.xlsx");
        if (importFile.exists()){
            new Important().execute();
        }else{
            Toast.makeText(LoginActivity.this, "文件未找到", Toast.LENGTH_SHORT).show();
        }
    }

    // 导出EXCEL文件
    public void onClickExportExcel(View view){
        new OutImportant().execute();
    }

    // 导入数据对话框
    class Important extends AsyncTask<Integer, String, Boolean> {
        private ProgressDialog pDialog = null;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("正在导入excel，请稍候");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean imp) {
            // TODO Auto-generated method stub
            super.onPostExecute(imp);
            pDialog.dismiss();
            String result = "";
            if (imp) {
                result = "读取成功!";
                //Application.sharedHelper.put("save", true);
            } else {
                result = "读取失败！";
            }
            new AlertDialog.Builder(LoginActivity.this).setTitle("提示")
                    .setMessage(result).setPositiveButton("确定", null).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            File coursefile = new File(importExcel + "BOM.xlsx");
            boolean res = ReadFile.read2DB(importExcel+"BOM.xlsx", LoginActivity.this, dbPart);
            return res;
        }
    }

    // 导出EXCEL文件
    class OutImportant extends AsyncTask<Integer, String, Boolean> {
        private ProgressDialog pDialog = null;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("正在导出excel，请稍候");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean imp) {
            // TODO Auto-generated method stub
            super.onPostExecute(imp);
            pDialog.dismiss();
            String result = "";
            if (imp) {
                result = "导出成功!";
            } else {
                result = "导出失败！";
            }
            new AlertDialog.Builder(LoginActivity.this).setTitle("提示")
                    .setMessage(result).setPositiveButton("确定", null).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            File coursefile = new File(exportExcel + "BOM比对结果.xls");
            if (coursefile.exists()){
                coursefile.delete();
            }
            boolean res = ReadFile.ExportExcel(exportExcel + "BOM比对结果.xlsx", LoginActivity.this, dbScan);
            return res;
        }
    }

    // 初始化固定资产数据库
    public void InitDBPart(){
        sqLiteHelper = new SQLiteHelper(LoginActivity.this, dbPart);
        sdb_part = sqLiteHelper.getReadableDatabase();
        String DB_CREATE_TABLE_SQL = "create table if not exists info("
                +"autoid       INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,"
                +"id varchar(20),"
                +"staffid varchar(20),"
                +"pmcode varchar(50),"
                +"flagcode varchar(20),"
                +"partcode varchar(30),"
                +"partname varchar(100),"
                +"partnum integer default (0),"
                +"partnumber varchar(100),"
                +"partbatch integer default (0),"
                +"manufacturer varchar(100),"
                +"enginetype varchar(20),"
                +"remark varchar(100),"
                +"isverify integer default (1),"
                +"ismaster integer default(0),"
                +"isdisplay integer default(1),"
                +"issave integer default(1),"
                +"material varchar(100),"
                +"number integer default(0)"
                +")";

        try{
            sdb_part = SQLiteDatabase.openOrCreateDatabase(dbPart, null);
            sdb_part.execSQL(DB_CREATE_TABLE_SQL);
        }catch (SQLException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // 初始化盘点明细数据库
    public void InitDBScan(){
        sqLiteHelper = new SQLiteHelper(LoginActivity.this, dbScan);
        sdb_scan = sqLiteHelper.getReadableDatabase();
        String DB_CREATE_TABLE_SCAN_SQL = "create table if not exists scan("
                + "partcode varchar(100),"
                + "partname varchar(100),"
                + "enginetype varchar(100),"
                + "scdate varchar(100)"
                + ");";

        try{
            sdb_scan = SQLiteDatabase.openOrCreateDatabase(dbScan, null);
            sdb_scan.execSQL(DB_CREATE_TABLE_SCAN_SQL);
        }catch (SQLException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
