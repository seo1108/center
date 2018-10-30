package yonsei_church.yonsei.center.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.net.URLDecoder;
import java.util.ArrayList;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.app.GlobalApplication;
import yonsei_church.yonsei.center.media.DownloadContentService;
import yonsei_church.yonsei.center.media.MediaPlayerService;
import yonsei_church.yonsei.center.util.Util;
import yonsei_church.yonsei.center.widget.WaitingDialog;

public class WebViewActivity extends AppCompatActivity {

    private WebView mWebView;
    TextView errorVeiw;
    private String mUrl;
    boolean doubleBackToExitPressedOnce = false;
    MediaPlayer mediaPlayer;
    Activity mActivity;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_webview);

        // Google Analytics
        GlobalApplication application = (GlobalApplication) getApplication();

        mUrl = getIntent().getStringExtra("URL");
        errorVeiw = (TextView) findViewById(R.id.net_error_view);
        initializeView();
    }

    @Override
    public void onResume(){
        super.onResume();
        mWebView.resumeTimers();
        mWebView.onResume();
        this.doubleBackToExitPressedOnce = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
        mWebView.stopLoading();
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            if (doubleBackToExitPressedOnce) {
                Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            } else {

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            }

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
               }
            }, 2000);

        }
    }


    @SuppressLint("JavascriptInterface")
    public void initializeView() {
        mWebView = findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebClient()); // 응용프로그램에서 직접 url 처리

        //mWebView.setWebChromeClient(new FullscreenableChromeClient(WebViewActivity.this));





        /*WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);*/
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSavePassword(false);

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

        mWebView.addJavascriptInterface(new AndroidBridge(), "audio");
        mWebView.loadUrl(mUrl);



        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermissionForDonwload();

                /*Intent intent = new Intent( getApplicationContext(), DownloadListActivity.class );
                startActivity(intent);*/
            }
        });
        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                intent.setAction( MediaPlayerService.ACTION_PLAY );
                intent.putExtra("streamLink","https://s3-ap-northeast-2.amazonaws.com/webaudio.ybstv.com/mp3/yn20181021-322.mp3");
                intent.putExtra("position",0);
                AppConst.MEDIA_CURRENT_POSITION = 0;

                startService(intent);
            }
        });

    }

    class WebClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            Log.d("onPageLoading", url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("onPageStarted", url);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("onPageFinished", url);


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
    }

    void checkStoragePermissionForDonwload(){
        new TedPermission(mActivity)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("저장소 접근을 허용해주세요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //download("https://player.vimeo.com/external/296317003.sd.mp4?s=906fbda76388c0e6974dc95d98ae7c1863be49bd&profile_id=164&download=1", "296317003.sd.mp4");
            Intent intent = new Intent( getApplicationContext(), DownloadContentService.class );
            intent.putExtra("url","https://www.radiantmediaplayer.com/media/bbb-360p.mp4");
            intent.putExtra("title","동영상다운로드테스트.mp4");
            startService(intent);
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(mActivity, "사용권한이 없습니다." + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    public class AndroidBridge {
        @JavascriptInterface
        public void send(final String arg) {
            new Handler().post(new Runnable() {
                public void run() {
                    try {
                        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                        stopService(intent);
                    } catch (Exception ex) {

                    }

                    Log.d("JAVASCRIPT", Util.urlDecode(arg));
                    String[] args = Util.urlDecode(arg).split("\\^");
                    String mediaUrl = args[0];
                    String mediaTitle = args[1];
                    String mediaImage = "";
                    try {
                        mediaImage = Util.checkNull(args[2], "");
                    } catch (Exception ex) {
                        
                    }



                    Log.d("JAVASCRIPT", mediaUrl + "__" + mediaTitle + "__" + mediaImage);
                    AppConst.MEDIA_CURRENT_POSITION = 0;
                    AppConst.MEDIA_MP3_URL = mediaUrl;
                    AppConst.MEDIA_MP3_TITLE = mediaTitle;
                    AppConst.MEDIA_MP3_IMAGE = mediaImage;
                    AppConst.MEDIA_MP3_ISPLAY = true;
                    Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                    intent.setAction( MediaPlayerService.ACTION_PLAY );
/*
                    intent.putExtra("streamLink",AppConst.MEDIA_MP3_URL);
                    intent.putExtra("position",AppConst.MEDIA_CURRENT_POSITION);
                    intent.putExtra("title",mediaTitle);
                    intent.putExtra("image",mediaImage);
*/

                    startService(intent);
                }
            });
        }
    }


}
