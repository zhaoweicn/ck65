package com.km.eda51_demo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.*;
import com.km.eda51_demo.data.Part;
import com.km.eda51_demo.util.ReadFile;
import com.km.eda51_demo.util.SQLiteHelper;

import java.io.File;
import java.nio.file.Files;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.km.eda51_demo.util.MyDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeListener,BarcodeReader.TriggerListener {

    // SQLITE文件路径
    private static String dbScan = "/data/data/com.km.eda51_demo/databases/scan.db";
    private static String dbPart = "/data/data/com.km.eda51_demo/databases/part.db";

    private static Cursor cursor;
    private static MyDatabase myDatabase;
    private SQLiteDatabase sdb_scan;
    private SQLiteDatabase sdb_part;

    private SQLiteHelper sqLiteHelper;

    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Button btnAutomaticBarcode;
    boolean useTrigger=true;
    boolean btnPressed = false;
    TextView textView;
    private TextView tv_message = null;
    private LinearLayout lyMain=null;
    private LinearLayout lyTitle = null;
    private TextView tv_partcode = null;
    private TextView tv_red_state = null;
    private FrameLayout state = null;

    private TextView tv_ptname = null;
    private TextView tv_inventory = null;
    private TextView tv_export = null;

    private TextView tv_enginetype = null;
    // 零件图号
    private TextView tv_title = null;
    // 零件名称
    private TextView tv_partname = null;
    // 发动机型号
    private TextView tv_engine = null;

    private boolean IsScan = false;
    private boolean engineIsScan = false;
    private boolean pcodeIsScan = false;
    private boolean IsAlert = false;
    private Dialog mDialog;

    private static final int RED = 0xffff0000;
    private static final int BLUE = 0xff8080FF;
    private static final int CYAN = 0xff80ffff;
    private static final int GREEN = 0xff80ff80;
    private static final int WHITE = 0xffffffff;
    private static final int BLACK = 0xff000000;
    // 动画变量
    ValueAnimator colorAnim=null;
    private WaveView mWaveView;
    // 振动变量
    Vibrator vibrator;

    public class MyDialog extends Dialog {
        //    style引用style样式
        public MyDialog(Context context, int width, int height, View layout, int style) {
            super(context, style);
            setContentView(layout);
            Window window = getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }

    //点击按钮，弹出圆角对话框
    public void showDialog(int layout, String msg) {
        View view = getLayoutInflater().inflate(layout,null);
        TextView tv_Message = view.findViewById(R.id.tv_Message);
        TextView tv_Cancel = view.findViewById(R.id.tv_Cancel);
        TextView tv_OK = view.findViewById(R.id.tv_OK);
        tv_Message.setText(msg);
        tv_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        tv_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.exit(0);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mDialog = new MyDialog(this, 0, 0, view, R.style.Theme_AppCompat_Dialog);
        mDialog.setCancelable(true);
        mDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDatabase = new MyDatabase(MainActivity.this);

        InitScan();

        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try {
                    barcodeReader = manager.createBarcodeReader();
                    if(barcodeReader!=null) {
                        Log.d("honeywellscanner: ", "barcodereader not claimed in OnCreate()");
                        barcodeReader.claim();
                    }
                    // apply settings
                    /*
                    barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, false);
                    barcodeReader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);

                    // set the trigger mode to automatic control
                    barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                            BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
                } catch (UnsupportedPropertyException e) {
                    Toast.makeText(MainActivity.this, "Failed to apply properties",
                            Toast.LENGTH_SHORT).show();
                    */
                }
                catch (ScannerUnavailableException e) {
                    Toast.makeText(MainActivity.this, "Failed to claim scanner",
                            Toast.LENGTH_SHORT).show();
                    //e.printStackTrace();
                } catch (InvalidScannerNameException e) {
                    e.printStackTrace();
                }
                // register bar code event listener
                barcodeReader.addBarcodeListener(MainActivity.this);
            }
        });
        ActivitySetting();
    }

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event){
//        switch (KeyCode){
//            case KeyEvent.KEYCODE_F1:
//            case KeyEvent.KEYCODE_BACK:
//                if (colorAnim!=null && colorAnim.isRunning() && IsAlert){
//                    //colorAnim = ObjectAnimator.ofInt(lymain, "backgroundColor", WHITE, WHITE);
//                    //lyMain.setBackgroundColor(Color.WHITE);
//                    Stop_Vibrator();
//                    lyTitle.setVisibility(View.VISIBLE);
//                    tv_message.setTextColor(Color.GREEN);
//                    tv_message.setBackgroundColor(Color.WHITE);
//                    colorAnim.reverse();
//                    colorAnim.cancel();
//                    colorAnim = null;
//                    tv_message.setVisibility(View.VISIBLE);
//                    tv_message.setText("扫描条码");
//                    tv_message.setTextColor(Color.GREEN);
//                    IsAlert = false;
//                }else if (!IsAlert){
//                    Intent intent = new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
//                break;
//        }
        if ((KeyCode==KeyEvent.KEYCODE_F1) || (KeyCode==KeyEvent.KEYCODE_BACK) && IsAlert){
            if (colorAnim!=null && colorAnim.isRunning() && IsAlert){
//            colorAnim = ObjectAnimator.ofInt(lymain, "backgroundColor", WHITE, WHITE);
                //lyMain.setBackgroundColor(Color.WHITE);
                Stop_Vibrator();
                lyTitle.setVisibility(View.VISIBLE);
                tv_message.setTextColor(Color.GREEN);
                tv_message.setBackgroundColor(Color.WHITE);
                colorAnim.reverse();
                colorAnim.cancel();
                colorAnim = null;
                IsAlert = false;
            }
            return true;
        } else if (KeyCode==KeyEvent.KEYCODE_BACK && !IsAlert){
                showDialog(R.layout.layout_dialog,"是否退出扫描？");
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }
        return false;
    }

    @Override
    public void onResume(){  //will always? be called before app becomes visible?
        super.onResume();
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
                Log.d("honeywellscanner: ", "scanner claimed");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(barcodeReader!=null)
            barcodeReader.release();

    }

    @SuppressLint("ResourceAsColor")
    private void InitScan(){
        lyMain = findViewById(R.id.lyMain);
        lyTitle = findViewById(R.id.lyTitle);

        tv_engine = findViewById(R.id.tv_engine);
        tv_engine.setText("发动机型号:");
        tv_engine.setTextColor(R.color.md_orange_900);

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText("零件图号:");
        tv_title.setTextColor(R.color.md_orange_900);

        tv_partname = findViewById(R.id.tv_partname);
        tv_partname.setText("零件名称:");
        tv_partname.setTextColor(R.color.md_orange_900);

        tv_message = findViewById(R.id.tv_message);
        tv_message.setText("扫描发动机型号");
        tv_message.setTextColor(Color.GREEN);

        tv_partcode = findViewById(R.id.tv_partcode);
        tv_red_state = (TextView) findViewById(R.id.tv_red_state);
        tv_red_state.setVisibility(View.GONE);

        tv_ptname = findViewById(R.id.tv_ptname);
        tv_ptname.setText("");

        tv_enginetype = findViewById(R.id.tv_enginetype);
        tv_enginetype.setText("");
    }

    // 按钮事件
    public void ActivitySetting() {
//        btnAutomaticBarcode = (Button) findViewById(R.id.btnScan);
//        btnAutomaticBarcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 判断是否已经扫描
//                String barcodeData = et_barcode.getText().toString();
//                if (IsScan(barcodeData)){
//                    //tv_message.setVisibility(View.GONE);
//                    //waveState();
////                    Start();
////                    Start_Vibrator();
//                    tv_message.setText("条码重复");
//                    tv_message.setTextColor(Color.RED);
//                }else{
//                    InsertBarcode(MainActivity.this, dbScan, barcodeData);
//                    tv_message.setText("扫描成功");
//                    tv_message.setTextColor(Color.GREEN);
//                }
////                DeleteScan();
////                Stop_Vibrator();
////                et_barcode.setText("");
////                tv_message.setText("数据清空");
////                if(barcodeReader!=null){
////                    try {
////                        barcodeReader.softwareTrigger(true);
////                    } catch (ScannerNotClaimedException e) {
////                        // TODO Auto-generated catch block
////                        e.printStackTrace();
////                    } catch (ScannerUnavailableException e) {
////                        // TODO Auto-generated catch block
////                        e.printStackTrace();
////                    }
////                }
////                else{
////                    showToastMsg("Barcodereader not available");
////                }
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (barcodeReader != null) {
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
    }

    // 扫描条码事件
    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        try {
            barcodeReader.softwareTrigger(false);
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() { // function begin
            @Override
            public void run() {
                List<Part> parts = null;
                String strEngine = "";
                String strCode = "";
                String barcodeData = event.getBarcodeData();
                //et_barcode.setText(barcodeData);
                // 1, 先判断是否为有效发动机型号
                if (!engineIsScan){ // begin ifelse 3
//                    if (barcodeData.trim().length()!=7){
//                        Start();
//                        IsAlert = true;
//                        engineIsScan = false;
//                        tv_enginetype.setText(barcodeData);
//                        tv_partcode.setText("");
//                        tv_ptname.setText("");
//                        tv_message.setText("无效发动机二维码\n" + barcodeData);
//                        return;
//                    }
                    // 2, 判断发动机型号是否存在
                    parts = getInfo(barcodeData.substring(0, 7));
                    if (parts.size()==0){
                        Start();
                        IsAlert = true;
                        engineIsScan = false;
                        tv_enginetype.setText(barcodeData);
                        tv_partcode.setText("");
                        tv_ptname.setText("");
                        tv_message.setText("发动机型号不存在\n" + barcodeData);
                        return;
                    }
                    strEngine = parts.get(0).getEnginetype();
                    tv_enginetype.setText(strEngine);
                    tv_partcode.setText("");
                    tv_ptname.setText("");
                    tv_message.setText("扫描零件图号");
                    engineIsScan = true;
                }else{ // begin ifelse 2
                    strEngine = tv_enginetype.getText().toString();
                    // 3, 判断是否为有效零件图号
                    if (barcodeData.length()!=13){
                        Start();
                        IsAlert = true;
                        pcodeIsScan = false;
                        tv_partcode.setText(barcodeData);
                        tv_ptname.setText("");
                        tv_message.setText("无效零件图号二维码\n"+barcodeData);
                        return;
                    }
                    strCode = barcodeData.trim();
                    tv_partcode.setText(strCode);
                    parts = getInfo(strEngine);
                    // 4, 判断零件编号是否存在
                    String strPartname = parts.get(0).getPartname();
                    if (strPartname.isEmpty()){
                        Start();
                        IsAlert = true;
                        pcodeIsScan = false;
                        tv_partcode.setText(barcodeData);
                        tv_ptname.setText("");
                        tv_message.setText("零件图号不存在\n"+barcodeData);
                        return;
                    }
                    tv_ptname.setText(strPartname);
                    pcodeIsScan = true;
                    if (engineIsScan && pcodeIsScan){  // begin ifelse 1
                        Stop();
                        // 4, 判断零件编号是否已经扫描过
                        if (isVerify(strCode, strEngine)){
                            Start();
                            IsAlert = true;
                            tv_message.setText("当前发动机的零件编号已经扫描\n按F1终止提示");
                        }else {
                            IsAlert = false;
                            // 5, 写入数据库
                            List<Part> partList = new ArrayList<Part>();
                            Part part = new Part();
                            part.setPartcode(strCode);
                            part.setPartname(strPartname);
                            part.setEnginetype(strEngine);
                            partList.add(part);
                            InsertBarcode(MainActivity.this, dbScan, partList);
                            tv_message.setText("开始新的扫描\n扫描发动机型号");
//                            tv_enginetype.setText("");
//                            tv_partcode.setText("");
//                            tv_ptname.setText("");
                        }
                        engineIsScan = false;
                        pcodeIsScan = false;
                    } //end ifelse1
                } // end ifelse 2
            } // function end
        });
    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        // TODO Auto-generated method stub
        if (!engineIsScan){
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText("扫描发动机型号");
            tv_message.setTextColor(Color.GREEN);
        }
        if (engineIsScan){
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText("扫描零件图号");
            tv_message.setTextColor(Color.GREEN);
        }
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        // TODO Auto-generated method stub
        try {
            barcodeReader.softwareTrigger(false);
            if (!engineIsScan){
                tv_message.setVisibility(View.VISIBLE);
                tv_message.setText("扫描发动机型号");
                tv_message.setTextColor(Color.GREEN);
            }
            if (engineIsScan){
                tv_message.setVisibility(View.VISIBLE);
                tv_message.setText("扫描零件图号");
                tv_message.setTextColor(Color.GREEN);
            }
            Stop();
            Stop_Vibrator();
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void waveState(){
        mWaveView = findViewById(R.id.wave_view);
        tv_red_state.setVisibility(View.VISIBLE);
        tv_red_state.setText("点击取消");
        mWaveView.setDuration(5000);
        mWaveView.setStyle(Paint.Style.FILL);
        mWaveView.setColor(Color.RED);
        mWaveView.setInterpolator(new LinearOutSlowInInterpolator());
        mWaveView.start();
        Start_Vibrator();
        mWaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stop_Vibrator();
            }
        });
        mWaveView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mWaveView.stop();
//                Stop_Vibrator();
            }
        }, 10000);
    }

    // 振动开始
    private void Start_Vibrator(){
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {100, 300, 100, 300};
            vibrator.vibrate(pattern, 2);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 振动结束
    private void Stop_Vibrator(){
        if (vibrator!=null){
            vibrator.cancel();
        }
        if (mWaveView!=null){
            mWaveView.stop();
        }
        if (!engineIsScan){
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText("扫描发动机型号");
            tv_message.setTextColor(Color.GREEN);
        }
        if (engineIsScan){
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText("扫描零件图号");
            tv_message.setTextColor(Color.GREEN);
        }
    }

    private void showToastMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // 动画开始
    private void Start(){
        lyTitle.setVisibility(View.GONE);
        colorAnim = ObjectAnimator.ofInt(tv_message, "textColor", RED, WHITE);
        colorAnim = ObjectAnimator.ofInt(tv_message, "backgroundColor", WHITE, RED);
        colorAnim.setDuration(500);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(ValueAnimator.INFINITE);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.start();
        Start_Vibrator();
    }

    // 动画结束
    private void Stop(){
        if (colorAnim!=null && colorAnim.isRunning()){
//            colorAnim = ObjectAnimator.ofInt(lymain, "backgroundColor", WHITE, WHITE);
            //lyMain.setBackgroundColor(Color.WHITE);
            Stop_Vibrator();
            lyTitle.setVisibility(View.VISIBLE);
            tv_message.setTextColor(Color.GREEN);
            tv_message.setBackgroundColor(Color.WHITE);
            colorAnim.reverse();
            colorAnim.cancel();
            colorAnim = null;
        }
    }

    // 初始化盘点明细数据库
//    public void InitDBScan(){
//        sqLiteHelper = new SQLiteHelper(MainActivity.this, dbScan);
//        sdb_scan = sqLiteHelper.getReadableDatabase();
//        String DB_CREATE_TABLE_SCAN_SQL = "create table if not exists scan("
//                + "partcode varchar(100),"
//                + "partname varchar(100),"
//                + "enginetype varchar(100),"
//                + "scdate varchar(100)"
//                + ");";
//
//        try{
//            sdb_scan = SQLiteDatabase.openOrCreateDatabase(dbScan, null);
//            sdb_scan.execSQL(DB_CREATE_TABLE_SCAN_SQL);
//        }catch (SQLException e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            return;
//        }
//    }

    // 判断零件号是否存在
    public static String IsScan(String barcode){
          return myDatabase.getPartName(barcode);
    }

    // 判断零件编号是否已经扫描过
    public boolean isVerify(String pcode, String engine){
        boolean isverify = false;

        sqLiteHelper = new SQLiteHelper(MainActivity.this, dbScan);
        sdb_scan = sqLiteHelper.getReadableDatabase();
        try{
            String SELECT_SCAN_SQL = "select *from scan where partcode=? and enginetype=?";
            Cursor cursor = sdb_scan.rawQuery(SELECT_SCAN_SQL, new String[]{pcode, engine});
            if (cursor.getCount()==0){
                cursor.close();
                isverify = false;
            }else{
                cursor.close();
                isverify = true;
            }
        }catch (SQLiteException ex){
            ex.printStackTrace();
        }finally {
            sdb_part.close();
            sqLiteHelper.close();
        }

        return isverify;
    }

    // 记录扫描数据
    public static int InsertBarcode(Context context, String filename, List<Part> parts){
        try {
            SQLiteDatabase sqLiteDatabase = new SQLiteHelper(context, filename).getWritableDatabase();
            sqLiteDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("partcode", parts.get(0).getPartcode());
            values.put("partname", parts.get(0).getPartname());
            values.put("enginetype", parts.get(0).getEnginetype());
            values.put("scdate", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(System.currentTimeMillis())));
            sqLiteDatabase.insertWithOnConflict("scan", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
            return 1;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("", e.getMessage());
            return 0;
        }

    }

    // 获取零件名称、发动机型号
    public List<Part> getInfo(String barcode){
        List<Part> partList = new ArrayList<>();
        String GET_INFO_SQL = "select partcode, partname, enginetype from info where enginetype=?";
        try{
            sqLiteHelper = new SQLiteHelper(MainActivity.this, dbPart);
            sdb_part = sqLiteHelper.getReadableDatabase();
            Cursor cursor = sdb_part.rawQuery(GET_INFO_SQL, new String[]{barcode});
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
        }catch (SQLiteAssetHelper.SQLiteAssetException ex){
            sdb_part.close();
            sqLiteHelper.close();
            ex.printStackTrace();
        }
        sdb_part.close();
        sqLiteHelper.close();
        return partList;
    }
}
