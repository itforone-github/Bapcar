package com.itforone.bapcar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.itforone.bapcar.databinding.ActivitySettingBinding;

import java.lang.reflect.Method;

import util.BackPressCloseHandler;
import util.Common;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;
    final private String SEARCH_URL="https://www.yoonfoodsystem.co.kr/bbs/app_setting.php";
    private BackPressCloseHandler backPressCloseHandler;
    private ActivityResultLauncher<Intent> resultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.actList.add(this);
        setContentView(R.layout.activity_search);
        //화면을 계속 켜짐
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_setting);
        binding.setSetting(this);
        // 웹뷰를 실행할 때 메모리 누수가 심하지 않게 설정하는 것
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        webViewSetting();
    }

    @SuppressLint("JavascriptInterface")
    public void webViewSetting() {
        //Common.setTOKEN(this);
        WebSettings setting = binding.webView.getSettings();//웹뷰 세팅용
        if(Build.VERSION.SDK_INT >= 21) {
            binding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            //쿠키 생성
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(binding.webView,true);
        }
        //웹뷰 하드웨어 가속 시키기
        if(Build.VERSION.SDK_INT >= 19){
            binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        }else{
            binding.webView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        }
        setting.setAllowFileAccess(true);//웹에서 파일 접근 여부
//        setting.setAppCacheEnabled(true);//캐쉬 사용여부
        setting.setGeolocationEnabled(true);//위치 정보 사용여부
        setting.setDatabaseEnabled(true);//HTML5에서 db 사용여부
        setting.setDomStorageEnabled(true);//HTML5에서 DOM 사용여부
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);//캐시 사용모드 LOAD_NO_CACHE는 캐시를 사용않는다는 뜻
        setting.setJavaScriptEnabled(true);//자바스크립트 사용여부
        setting.setSupportMultipleWindows(false);//윈도우 창 여러개를 사용할 것인지의 여부 무조건 false로 하는 게 좋음
        setting.setUseWideViewPort(true);//웹에서 view port 사용여부
        setting.setTextZoom(100);
        setting.setSupportZoom(true);



        setting.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Mobile Safari/537.36"+"/FoodBap/AppBarCar");
        binding.webView.setWebChromeClient(chrome);//웹에서 경고창이나 또는 컴펌창을 띄우기 위한 메서드
        binding.webView.setWebViewClient(client);//웹페이지 관련된 메서드 페이지 이동할 때 또는 페이지가 로딩이 끝날 때 주로 쓰임

        binding.webView.addJavascriptInterface(new WebJavascriptEvent(), "Android");
        //뒤로가기 버튼을 눌렀을 때 클래스로 제어함
        backPressCloseHandler = new BackPressCloseHandler(this);
        binding.webView.loadUrl(SEARCH_URL);
        //하단 메뉴 클릭 이벤트리스너
        binding.btnLayout1.setOnClickListener(mClickListener);
        binding.btnLayout2.setOnClickListener(mClickListener);
        binding.btnLayout3.setOnClickListener(mClickListener);
        binding.btnLayout4.setOnClickListener(mClickListener);
        binding.btnLayout5.setOnClickListener(mClickListener);
        //인텐트 값 받기
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        binding.webView.reload();
                    }
                });
    }

    WebChromeClient chrome;
    {
        chrome = new WebChromeClient() {
            //새창 띄우기 여부
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return false;
            }

            //경고창 띄우기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(SettingActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                }).create().show();
                return true;
            }

            //컴펌 띄우기
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(SettingActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.cancel();
                                    }
                                }).create().show();
                return true;
            }

            //현재 위치 정보 사용여부 묻기
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Should implement this function.
                final String myOrigin = origin;
                final GeolocationPermissions.Callback myCallback = callback;
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Request message");
                builder.setMessage("Allow current location?");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, true, false);
                    }

                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, false, false);
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }


        };
    }

    WebViewClient client;
    {
        client = new WebViewClient() {



            //페이지 로딩중일 때 (마시멜로) 6.0 이후에는 쓰지 않음
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //binding.progressLayout.setVisibility(View.VISIBLE);
                /*Glide.with(SettingActivity.this)
                        .load(R.raw.progress)
                        .into(binding.progressImageView);*/

                Log.d("url",url);
                if(0 < url.indexOf("play.google")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                if(0 < url.indexOf("playstore")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                if(url.startsWith("kakao")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                if(url.startsWith("tel:")){
                    Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(tel);
                    return true;
                }
                if(url.startsWith("sms:")){
                    Uri smsUri = Uri.parse(url);
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO,smsUri);
                    startActivity((smsIntent));
                    return true;
                }

                //파일 다운로드
                if((0 < url.indexOf("download.php") )){
                    Log.d("indexof",url.indexOf(".php")+"");
                    Log.d("indexof",url);
                    DownloadManager.Request  request= new DownloadManager.Request(Uri.parse(url));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    );

                    String filename[] = url.split("/");
                    request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            filename[filename.length-1]
                    );
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    /*Intent intent = new Intent(SettingActivity.this,DownLoadWebViewActivity.class);
                    intent.putExtra("url",url);
                    startActivity(intent);*/
                    return true;

                }



                /*if (0 < url.indexOf("view.php") ||
                        0 < url.indexOf("write.php") ||
                        0 < url.indexOf("login.php") ||
                        0 < url.indexOf("register")) {
                    Intent intent = new Intent(SettingActivity.this,WebActivity.class);
                    intent.putExtra("goUrl",url);
                    startActivityForResult(intent,1000);
                    //startActivity(intent);
                    return true;
                }*/ /*else {
                    //위치기 안 잡힐 때
                    if(LocationPosition.lat == 0.0){
                        LocationFind();
                        return false;
                    }else {
                        if (0 < url.indexOf("?")) {
                            binding.webView.loadUrl(url+"&currentLng="+LocationPosition.lat+"&currentLat="+LocationPosition.lng);
                        } else {
                            binding.webView.loadUrl(url+"?currentLng="+LocationPosition.lat+"&currentLat="+LocationPosition.lng);
                        }
                        return true;
                    }
                }*/
                return false;
            }
            //페이지 로딩이 다 끝났을 때
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.progressLayout.setVisibility(View.GONE);
                CookieManager.getInstance().flush();




            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("web-error",errorCode+""+description);


            }
        };
    }
    //다시 들어왔을 때
    @Override
    protected void onResume() {
        super.onResume();
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
        //binding.webView.reload();


        //sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);



        Log.d("newtork","onResume");


        //netCheck.networkCheck();
    }
    //홈버튼 눌러서 바탕화면 나갔을 때
    @Override
    protected void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(this);
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }

    }

    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함

        super.onBackPressed();
        setResult(Activity.RESULT_OK);
        finish();
        overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);

    }

    //자바스크립트 브릿지
    class WebJavascriptEvent{
        @JavascriptInterface
        public void goBack(){
            finish();
            overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
        }
        @JavascriptInterface
        public  void goMain(String url){
            Intent intent= new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("goUrl",url);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
        }
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent;
            switch(v.getId()){
                //메인
                case R.id.btnLayout1:
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
                    finish();

                    break;
                //검색
                case R.id.btnLayout2:
                    //binding.webView.loadUrl(binding.webView.getUrl()+"#hash-search");
                    intent = new Intent(getApplicationContext(),SearchActivity.class);
                    resultLauncher.launch(intent);
                    //startActivity(intent);
                    overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
                    break;
                //내주변업체
                case R.id.btnLayout3:
                    intent = new Intent(getApplicationContext(),AroundStoreActivity.class);
                    resultLauncher.launch(intent);
                    //startActivity(intent);
                    overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
                    break;
                //마이페이지
                case R.id.btnLayout4:

                    intent = new Intent(getApplicationContext(),MyPageActivity.class);
                    resultLauncher.launch(intent);
                    //startActivity(intent);
                    overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
                    break;
                //설정
                case R.id.btnLayout5:

                    binding.webView.reload();
                    break;
            }
        }
    };
}