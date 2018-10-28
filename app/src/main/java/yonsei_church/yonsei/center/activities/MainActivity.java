package yonsei_church.yonsei.center.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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


import com.google.firebase.iid.FirebaseInstanceId;

import java.lang.ref.WeakReference;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.app.MarketVersionChecker;
import yonsei_church.yonsei.center.media.MediaPlayerService;


public class MainActivity extends AppCompatActivity {

    String storeVersion;
    String deviceVersion;
    WebView mWebView;
    TextView errorVeiw;
    private Activity mContext;

    SQLiteDatabase contentDB = null;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // 인터넷 연결확인
        checkInternetConnection();

        if (getIntent().getBooleanExtra("EXIT", false)) {
            //Stop media player here
            /*NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(AppConst.NOTIFICATION_ID);
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            stopService(intent);*/
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            stopService(intent);
            finish();

            return;
        }
        //errorVeiw = (TextView) findViewById(R.id.net_error_view);

        /*mWebView = (WebView) findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);


        mWebView.setWebViewClient(new WebViewClient() {


            @Override

            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);

                return true;

            }

            //네트워크연결에러

            @Override

            public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {

                switch(errorCode) {

                    case ERROR_AUTHENTICATION: break;               // 서버에서 사용자 인증 실패

                    case ERROR_BAD_URL: break;                           // 잘못된 URL

                    case ERROR_CONNECT: break;                          // 서버로 연결 실패

                    case ERROR_FAILED_SSL_HANDSHAKE: break;    // SSL handshake 수행 실패

                    case ERROR_FILE: break;                                  // 일반 파일 오류

                    case ERROR_FILE_NOT_FOUND: break;               // 파일을 찾을 수 없습니다

                    case ERROR_HOST_LOOKUP: break;           // 서버 또는 프록시 호스트 이름 조회 실패

                    case ERROR_IO: break;                              // 서버에서 읽거나 서버로 쓰기 실패

                    case ERROR_PROXY_AUTHENTICATION: break;   // 프록시에서 사용자 인증 실패

                    case ERROR_REDIRECT_LOOP: break;               // 너무 많은 리디렉션

                    case ERROR_TIMEOUT: break;                          // 연결 시간 초과

                    case ERROR_TOO_MANY_REQUESTS: break;     // 페이지 로드중 너무 많은 요청 발생

                    case ERROR_UNKNOWN: break;                        // 일반 오류

                    case ERROR_UNSUPPORTED_AUTH_SCHEME: break; // 지원되지 않는 인증 체계

                    case ERROR_UNSUPPORTED_SCHEME: break;          // URI가 지원되지 않는 방식

                }

                super.onReceivedError(view, errorCode, description, failingUrl);

                mWebView.setVisibility(View.GONE);

                errorVeiw.setVisibility(View.VISIBLE);

            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            //alert 처리

            @Override

            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

                new AlertDialog.Builder(view.getContext())

                        .setTitle("알림")

                        .setMessage(message)

                        .setPositiveButton(android.R.string.ok,

                                new AlertDialog.OnClickListener(){

                                    public void onClick(DialogInterface dialog, int which) {

                                        result.confirm();

                                    }

                                })

                        .setCancelable(false)

                        .create()

                        .show();

                return true;

            }

            //confirm 처리

            @Override

            public boolean onJsConfirm(WebView view, String url, String message,

                                       final JsResult result) {

                new AlertDialog.Builder(view.getContext())

                        .setTitle("알림")

                        .setMessage(message)

                        .setPositiveButton("Yes",

                                new AlertDialog.OnClickListener(){

                                    public void onClick(DialogInterface dialog, int which) {

                                        result.confirm();

                                    }

                                })

                        .setNegativeButton("No",

                                new AlertDialog.OnClickListener(){

                                    public void onClick(DialogInterface dialog, int which) {

                                        result.cancel();

                                    }

                                })

                        .setCancelable(false)

                        .create()

                        .show();

                return true;

            }


        });
*/
        BackgroundThread thread = new BackgroundThread();
        thread.setDaemon(true);
        thread.start();

        //mWebView.setWebChromeClient(new FullscreenableChromeClient(MainActivity.this));
        //mWebView.loadUrl("http://app.dnsnet.co.kr/main/main.html");  //http://www.ybstv.com

        // FCM 토큰 업데이트
        try {
            Log.d("FCM_TOKEN", "BEFORE");
            String fcmToken = FirebaseInstanceId.getInstance().getToken();
            Log.d("FCM_TOKEN", fcmToken);
        } catch (Exception ex) {
            Log.d("FCM_TOKEN", "ERROR");
           ex.printStackTrace();
        }

        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra("URL", "http://app.dnsnet.co.kr/main/main.html");
        startActivity(intent);

        makeContentTableIfNull();
        /*Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        startActivity(intent);
*/

    }



    /*@Override

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);

    }*/

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

            contentDB.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
