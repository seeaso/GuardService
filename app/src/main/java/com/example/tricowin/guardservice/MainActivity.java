package com.example.tricowin.guardservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.utils.Include;
import com.example.utils.SPUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.w3c.dom.Text;

import java.io.File;
import java.security.MessageDigest;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

public class MainActivity extends Activity {

    private Timer timer;
    private Timer timer1;
    private EditText editText;
    private Button buttonChg;
    private Button buttonSave;
    private Button buttonStart;
    //private Button textView;
    private ProgressDialog progressDialog;
    private boolean flg=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.apkurl_text);
        editText.setText("http://www.tricowin.net/shouhou/shopGwc.apk");
        editText.clearFocus();

        buttonChg= findViewById(R.id.id_chg);
        buttonSave= findViewById(R.id.id_save);
        buttonStart= findViewById(R.id.id_start);

        Log.e("ssss","getSystemModel() = "+getSystemModel());
       // textView = findViewById(R.id.status_text);


        Typeface type= Typeface.createFromAsset(getAssets(), "a1.otf");
        editText.setTypeface(type);
        buttonChg.setTypeface(type);
        buttonSave.setTypeface(type);
        Typeface type1= Typeface.createFromAsset(getAssets(), "word1.TTF");
        buttonStart.setTypeface(type1);

    }


    /**
     * 启动主程序app
     * */
    private void startMainProgram(){
        PackageManager packageManager = getPackageManager();
        if (checkPackInfo("com.tricowin.vending")) {
            Intent intent = packageManager.getLaunchIntentForPackage("com.tricowin.vending");
            startActivity(intent);
            cancelTimer();
            SPUtils.put(MainActivity.this, "reboot", "1");
        } else {
            Message msg2 = new Message();
            msg2.what = 1;
            if (mhandler != null) {
                mhandler.sendMessage(msg2);
            }
        }
    }


    public void onStartShop(View view) {

        //如果之前点击过启动按钮则不能再点击
        String reboot = (String) SPUtils.get(MainActivity.this, "reboot", "");
        if ("1".equals(reboot)){
            Toast.makeText(MainActivity.this, "不能重复点击", Toast.LENGTH_LONG).show();
            return;
        }
        //点击启动售货机程序则取消定时任务
        cancelTimer();
        startMainProgram();
    }


    private void cancelTimer(){
        if (timer != null) {
            timer.cancel();
            // 一定设置为null，否则定时器不会被回收
            timer = null;
        }
    }

    public void onClickSave(View view) {
        String url = "";
        url = editText.getText().toString();
        if ("".equals(url)){
            Toast.makeText(MainActivity.this, "请输入apk的下载链接", Toast.LENGTH_LONG).show();
            return;
        }
        SPUtils.put(MainActivity.this, "apk_url", url);
    }

    public void onClickClean(View view) {

        if (flg){
            editText.setText("http://www.tricowin.net/shouhou/shopGwc.apk");
            flg = false;
            Toast.makeText(MainActivity.this, "购物车功能apk", Toast.LENGTH_LONG).show();
        }else{
            editText.setText("http://www.tricowin.net/shouhou/shopDg.apk");
            Toast.makeText(MainActivity.this, "单品购买apk", Toast.LENGTH_LONG).show();
            flg = true;
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

            switch (msg.what) {
                case 1:
                    String url = (String) SPUtils.get(MainActivity.this, "apk_url", "null");
                    Log.e("ssss","startToDownload url = "+url);
                    if ("null".equals(url)||"".equals(url)){
                        Toast.makeText(MainActivity.this, "请输入apk的下载链接并点击保存", Toast.LENGTH_LONG).show();
                       // textView.setText("输入链接点击保存");
                    }else{
                        startToDownload(url, "tricowin.apk",  "");
                        cancelTimer();
                        SPUtils.put(MainActivity.this, "reboot", "1");
                    }
                    break;

            }
        }
    };

    @Override
    protected void onResume() {
        SPUtils.put(MainActivity.this, "reboot", "0");
        super.onResume();
        //这里必须判断一下是否为空
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
     * 开始下载
     *
     * @param url      //https://imtt.dd.qq.com/16891/2AA79FBBCEFDC45F0B85A2362E0DBA86.apk?fsname=com.dianping.v1_10.6.13_100611.apk&csr=1bbd
     * @param fileName //bus_new.apk
     */
    public void startToDownload(String url, String fileName, final String msg) {
        //下载前先删除文件
        //textView.setText("正在下载");
        Include.delete("/storage/emulated/0/tricowin.apk");
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("正在下载自动售货机新版本");
        //不能手动取消下载进度对话框
        progressDialog.setCancelable(false);
        progressDialog.show();

        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName)
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
//                       textView.setText("网络未连接，正跳转设置");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(intent);
                            }
                        }, 2000);


                        Log.e("Download", "error");
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        progressDialog.dismiss();
                        //textView.setText("下载完成");
                        try {
                            openAPK(response.getAbsolutePath());
                        } catch (Exception e) {
                           // textView.setText("安装出错");
                            Toast.makeText(MainActivity.this, "安装出错", Toast.LENGTH_LONG).show();
                            Log.e("Download","安装出错");
                        }

                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        super.inProgress(progress, total, id);
                        progressDialog.setMax((int) total);
                        progressDialog.setProgress((int) (progress * total));
                        Log.e("Download", progress + "/" + total);
                    }
                });
    }


    private void openAPK(String fileSavePath) {
        Log.e("ssss","fileSavePath = "+fileSavePath);
        File apk = new File(fileSavePath);
        try {
            File apkfile = new File(fileSavePath);
            if (!apkfile.exists()) {
                Log.e("sssss","文件打开错误");
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //临时授权
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


    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

}
