package yonsei_church.yonsei.center.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.exoplayer2.C;
import com.google.firebase.iid.FirebaseInstanceId;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;

import retrofit.RetrofitError;
import retrofit.client.Response;
import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.api.APIService;
import yonsei_church.yonsei.center.api.CommonAPI;
import yonsei_church.yonsei.center.api.CommonCallback;
import yonsei_church.yonsei.center.api.StringCallback;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.app.DialogHelper;
import yonsei_church.yonsei.center.app.MarketVersionChecker;
import yonsei_church.yonsei.center.data.AppVersionModel;
import yonsei_church.yonsei.center.data.ResponseModel;
import yonsei_church.yonsei.center.media.AudioFocusService;
import yonsei_church.yonsei.center.media.MediaPlayerService;
import yonsei_church.yonsei.center.util.Util;


public class MainActivity extends AppCompatActivity {

    String storeVersion;
    String deviceVersion;
    WebView mWebView;
    TextView errorVeiw;
    private Activity mContext;
    private String mUserSeq;

    SQLiteDatabase contentDB = null;

    private static final String TAG = MainActivity.class.getSimpleName();
    Activity mActivity;
    String mUrl = "";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
        mActivity = this;

        setContentView(R.layout.activity_main);



        // 인터넷 연결확인
        checkInternetConnection();

        if (getIntent().getBooleanExtra("EXIT", false)) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            stopService(intent);
            finish();

            return;
        }

        BackgroundThread thread = new BackgroundThread();
        thread.setDaemon(true);
        thread.start();

        // FCM 토큰 업데이트
        try {
            String fcmToken = FirebaseInstanceId.getInstance().getToken();
            Log.d("TOKEN_GET", fcmToken);
            requesUpdateFCMToken(fcmToken);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra("URL", "http://app.yonsei.or.kr/main/main.html");
        startActivity(intent);*/


        makeContentTableIfNull();

        mUrl= "";
        if (null != getIntent().getStringExtra("URL")) {
            mUrl =  getIntent().getStringExtra("URL");

        } else {
            mUrl = "";
        }




        appVersionCheck();

       /* Intent intent = new Intent(getApplicationContext(), AudioFocusService.class);
        startService(intent);*/
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {

            return capitalize(model);
        }

        return model + "^" + capitalize(manufacturer);
    }

    private String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }


    public void requesUpdateFCMToken(String token) {
        // 사용자 정보획득
        SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
        mUserSeq = pref.getString("userSeq", "0");

        String agent = getDeviceName();

        // 사용자 전화번호 획득
        String mPhoneNumber = "";
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = mgr.getLine1Number();
            mPhoneNumber = mPhoneNumber.replace("+82", "0");
        } catch (SecurityException se) {
            mPhoneNumber = "";
        }

        CommonAPI api = APIService.createService(CommonAPI.class, this);
        api.token(mUserSeq, mPhoneNumber, token, "A", agent,
                new StringCallback<String>() {
                    @Override
                    public void apiSuccess(String responseString) {
                    }
                    @Override
                    public void apiError(RetrofitError error) {
                    }
                }
        );
    }

    public boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }


    private final DeviceVersionCheckHandler deviceVersionCheckHandler = new DeviceVersionCheckHandler(this);

    // 핸들러 객체 만들기
    private static class DeviceVersionCheckHandler extends Handler{
        private final WeakReference<MainActivity> mainActivityWeakReference;
        public DeviceVersionCheckHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mainActivityWeakReference.get();
            if (activity != null) {
                activity.handleMessage(msg);
                // 핸들메세지로 결과값 전달
            }
        }
    }

    public class BackgroundThread extends Thread {
        @Override
        public void run() {
            // 패키지 네임 전달
            storeVersion = MarketVersionChecker.getMarketVersion(getPackageName());
            // 디바이스 버전 가져옴
            try {
                deviceVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            deviceVersionCheckHandler.sendMessage(deviceVersionCheckHandler.obtainMessage());
            // 핸들러로 메세지 전달
        }
    }

    private void appVersionCheck(){
        CommonAPI api = APIService.createService(CommonAPI.class, this);
        api.version(
                new CommonCallback<AppVersionModel>() {
                    @Override
                    public void apiSuccess(AppVersionModel model) {
                        String flag = model.getFlag();
                        int newVersion = Integer.valueOf(model.getVersion().replace(".", "")).intValue();
                        int oldVersion = Integer.valueOf((Util.getAppVersion(getApplicationContext())).replace(".", "")).intValue();

                        if (newVersion > oldVersion) {
                            if ("Y".equals(model.getFlag())) {
                                DialogHelper.alert(MainActivity.this, "최신버전으로 업데이트하셔야 이용이 가능합니다", "업데이트", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                DialogHelper.alert(MainActivity.this, "최신버전이 있습니다. 업데이트하시겠습니까?", "업데이트","나중에",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                                                startActivity(intent);
                                            }
                                        },
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                goWebview();
                                            }
                                        }
                                );
                            }
                        } else {
                            goWebview();
                        }
                    }
                    @Override
                    public void apiError(RetrofitError error) {
                        goWebview();
                    }
                }
        );
    }

    private void goWebview() {
        String appendUrl = "";

        boolean hasURL = false;
        if (null != mUrl && mUrl.contains("http")) {
            appendUrl = "&url=" + mUrl;

            hasURL = true;
        } else {
            appendUrl = "";
            hasURL = false;

        }

        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        if (hasURL) {
            intent.putExtra("URL", "http://app.yonsei.or.kr/main/main.html?mseq=" + mUserSeq + appendUrl);
        } else {
            intent.putExtra("URL", "http://app.yonsei.or.kr/main/main.html?mseq=" + mUserSeq);
        }
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



    private void handleMessage(Message msg) {
        //핸들러에서 넘어온 값 체크
        if(TextUtils.isEmpty(storeVersion)) {
            Toast.makeText(getApplicationContext(), " deviceVersion : " + deviceVersion, Toast.LENGTH_LONG).show();
        }else {
            if (storeVersion.compareTo(deviceVersion) > 0) {
                // 업데이트 필요

                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light));
                alertDialogBuilder.setTitle("업데이트");
                alertDialogBuilder
                        .setMessage("새로운 버전이 있습니다.\n보다 나은 사용을 위해 업데이트 해 주세요. 신버전 - " + storeVersion +", 폰버전 - "+deviceVersion)
                        .setPositiveButton("업데이트 바로가기", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);

                                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                                startActivity(intent);
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();

            }
        }
    }

    public void makeContentTableIfNull() {
        try {
            contentDB = this.openOrCreateDatabase(AppConst.DB_NAME, MODE_PRIVATE, null);
            //contentDB.execSQL("DROP TABLE TB_DOWNLOAD");
            contentDB.execSQL("CREATE TABLE IF NOT EXISTS TB_DOWNLOAD"
                            + " (url VARCHAR(256), path VARCHAR(126), title VARCHAR(126), image VARCHAR(256), downDate DATETIME );");

            contentDB.execSQL("CREATE TABLE IF NOT EXISTS TB_USER"
                    + " (mseq VARCHAR(128) );");

            contentDB.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
