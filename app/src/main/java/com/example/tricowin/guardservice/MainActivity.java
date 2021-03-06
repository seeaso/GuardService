package com.example.tricowin.guardservice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.utils.Include;
import com.example.utils.RootCmd;
import com.example.utils.SPUtils;
import com.example.view.MyArrowDownloadButton;
import com.fenjuly.library.ArrowDownloadButton;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Call;

public class MainActivity extends Activity {

    private Timer timer;
    private Timer timer1;
    private EditText editText;
    private Button buttonChg;
    private Button buttonSave;
    private MyArrowDownloadButton buttonStart;
    private int downloadCount=0;//?????????????????????
    //private Button textView;
    private ProgressDialog progressDialog;
    private int flg=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.apkurl_text);
        editText.setText("?????????.apk");
        editText.clearFocus();
        buttonChg= findViewById(R.id.id_chg);
        buttonSave= findViewById(R.id.id_save);
        buttonStart= findViewById(R.id.id_startOne);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setValueToProp("persist.sys.sys_bar","hide");
                setValueToProp("persist.sys.statebarstate","0");
                setValueToProp("persist.sys.usb","1");

                Intent bar_intent= new Intent("MyReceiver_Action");
                bar_intent.putExtra("cmd","hide");
                MainActivity.this.sendBroadcast(bar_intent);

            }
        }, 500);
    }
    public static void setValueToProp(String key, String val) {
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method method = classType.getDeclaredMethod("set", new Class[] { String.class, String.class });
            method.invoke(classType, new Object[] { key, val });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * ???????????????app
     * */
    private void startMainProgram(){
        PackageManager packageManager = getPackageManager();
        if (checkPackInfo("com.tricowin.vending")) {
            Intent intent = packageManager.getLaunchIntentForPackage("com.tricowin.vending");
            startActivity(intent);
            cancelTimer();
            SPUtils.put(MainActivity.this, "reboot", "1");
        } else {
        }
    }


    public void onStartShop(View view) {
        //???????????????????????????????????????????????????
        String reboot = (String) SPUtils.get(MainActivity.this, "reboot", "");
        if ("1".equals(reboot)){
            Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
            return;
        }
        //????????????????????????????????????????????????
        cancelTimer();
        startMainProgram();
    }
    private void cancelTimer(){
        if (timer != null) {
            timer.cancel();
            // ???????????????null?????????????????????????????????
            timer = null;
        }
    }
    public void onClickSave(View view) {
        String url = "";
        url = editText.getText().toString();
        if ("".equals(url)){
            Toast.makeText(MainActivity.this, "?????????apk???????????????", Toast.LENGTH_LONG).show();
            return;
        }
        if (url.equals("?????????.apk")){
            url ="https://tricobucket.oss-cn-hangzhou.aliyuncs.com/apk/shopGwc_old.apk";
        }else if (url.equals("?????????.apk")){
            url ="https://tricobucket.oss-cn-hangzhou.aliyuncs.com/apk/shopDg.apk";
        }else if (url.equals("?????????.apk")){
            url ="https://tricobucket.oss-cn-hangzhou.aliyuncs.com/apk/jinjb.apk";
        }

        SPUtils.put(MainActivity.this, "apk_url", url);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Message msg2 = new Message();
                msg2.what = 3;
                if (mhandler != null) {
                    mhandler.sendMessage(msg2);
                }
            }
        }, 2000);
    }

    public void onClickClean(View view) {
        switch(flg){
            case 0:
                editText.setText("???????????????.apk");
                flg = 1;
                Toast.makeText(MainActivity.this, "???????????????apk", Toast.LENGTH_LONG).show();
                break;
            case 1:
                editText.setText("?????????.apk");
                Toast.makeText(MainActivity.this, "????????????apk", Toast.LENGTH_LONG).show();
                flg = 2;
                break;
            case 2:
                editText.setText("?????????.apk");
                Toast.makeText(MainActivity.this, "?????????apk", Toast.LENGTH_LONG).show();
                flg = 0;
                break;
        }

        SPUtils.put(MainActivity.this, "apk_url", "null");
    }

    private boolean checkPackInfo(String pname){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(pname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    @Override
    protected void onStop() {
        Log.e("ssss","GuardService===="+"onStop");
        cancelTimer();
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        Log.e("ssss","GuardService===="+"onPostResume");
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        Log.e("ssss","GuardService===="+"onDestroy");
    }

    private Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String url = (String) SPUtils.get(MainActivity.this, "apk_url", "null");
            switch (msg.what) {
                case 1:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Message msg2 = new Message();
                            msg2.what = 3;
                            if (mhandler != null) {
                                mhandler.sendMessage(msg2);
                            }
                        }
                    }, 2000);
                    break;
                case 2:
                    if (downloadCount>5){
                        Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }else{
                        Toast.makeText(MainActivity.this, "?????????????????????????????????", Toast.LENGTH_LONG).show();
                        downloadCount++;
                        startToDownload(url, "tricowin.apk",  "");
                    }
                    break;

                case 3:
                    PackageManager packageManager = getPackageManager();
                    if (checkPackInfo("com.tricowin.vending")) {
                        Intent intent = packageManager.getLaunchIntentForPackage("com.tricowin.vending");
                        startActivity(intent);
                        cancelTimer();
                        SPUtils.put(MainActivity.this, "reboot", "1");
                    }else{
                        if ("null".equals(url)||"".equals(url)){
                            Toast.makeText(MainActivity.this, "?????????apk??????????????????????????????", Toast.LENGTH_LONG).show();
                        }else{
                            startToDownload(url, "tricowin.apk",  "");
                            cancelTimer();
                            SPUtils.put(MainActivity.this, "reboot", "1");
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        downloadCount =0;
        SPUtils.put(MainActivity.this, "apk_url", "null");
        SPUtils.put(MainActivity.this, "reboot", "0");
        super.onResume();
        //????????????????????????????????????
        if (timer==null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startMainProgram();
                }
            }, 6000, 10000);
        }
        Log.e("ssss","GuardService===="+"onResume");
    }

    /**
     * ????????????
     *
     * @param url      //https://imtt.dd.qq.com/16891/2AA79FBBCEFDC45F0B85A2362E0DBA86.apk?fsname=com.dianping.v1_10.6.13_100611.apk&csr=1bbd
     * @param fileName //bus_new.apk
     */
    public void startToDownload(String url, String fileName, final String msg) {
        //????????????????????????
        Log.e("ssss","??????url = "+url);
        Include.delete("/storage/emulated/0/tricowin.apk");
        buttonStart.startAnimating();
        OkHttpUtils.get().url(url).build().execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //???????????????????????????
                                Message msg2 = new Message();
                                msg2.what = 2;
                                if (mhandler != null) {
                                    mhandler.sendMessage(msg2);
                                }
                            }
                        }, 2000);
                        buttonStart.reset();
                    }
                    @Override
                    public void onResponse(File response, int id) {
                        buttonStart.reset();
                        try {
                            openAPK(response.getAbsolutePath());
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_LONG).show();
                            Log.e("Download","????????????");
                        }

                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        super.inProgress(progress, total, id);
                        buttonStart.setProgress(progress *100);
                    }
                });
    }


    private void openAPK(String fileSavePath) {
        Log.e("ssss","fileSavePath = "+fileSavePath);
        File apk = new File(fileSavePath);
        try {
            File apkfile = new File(fileSavePath);
            if (!apkfile.exists()) {
                Log.e("sssss","??????????????????");
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // ????????????????????????7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //????????????
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.tricowin.guardservice.fileProvider", apkfile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.parse("file://" + fileSavePath), "application/vnd.android.package-archive");
                intent.putExtra("IMPLUS_INSTALL", "SILENT_INSTALL");
                startActivity(intent);
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //???????????????????????????
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        return super.onTouchEvent(event);
    }


    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }






}
