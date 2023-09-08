package com.itforone.bapcar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itforone.bapcar.databinding.ActivityMyPageBinding;
import com.itforone.bapcar.retrofit.FileData;
import com.itforone.bapcar.retrofit.Files;
import com.itforone.bapcar.retrofit.RetrofitService;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnMultiSelectedListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.BackPressCloseHandler;
import util.Common;
import util.LocationPosition;
import util.RealPath;

public class MyPageActivity extends AppCompatActivity {
    ActivityMyPageBinding binding;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300,MATISSE_REQ_CODE=1400;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;
    String firstUrl = "https://www.yoonfoodsystem.co.kr/bbs/mypage.php";

    MyService myService;
    String ss_mb_id="";

    static public WebView mainWebView;
    static public ImageView notNetworkImg;

    int mCount = 0;
    private ActivityResultLauncher<Intent> resultLauncher;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.actList.add(this);
        ss_mb_id = Common.getPref(MyPageActivity.this,"ss_mb_id","");


        binding = DataBindingUtil.setContentView(this,R.layout.activity_my_page);
        binding.setMypage(this);

        mainWebView=binding.webView;
        notNetworkImg=binding.notNetworkImg;

        //화면을 계속 켜짐
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 웹뷰를 실행할 때 메모리 누수가 심하지 않게 설정하는 것
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        Log.d("ss_mb_id",Common.getPref(MyPageActivity.this,"ss_mb_id",""));





        //Toast.makeText(MyPageActivity.this, binding.w, Toast.LENGTH_SHORT).show();
        Log.i("isView",binding.webView.getVisibility()+"");
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            //    CookieSyncManager.createInstance(this);
        }

        webViewSetting();
    }
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
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
        binding.webView.loadUrl(firstUrl);

        //하단 메뉴 클릭 이벤트리스너
        binding.btnLayout1.setOnClickListener(mClickListener);
        binding.btnLayout2.setOnClickListener(mClickListener);
        binding.btnLayout3.setOnClickListener(mClickListener);
        binding.btnLayout4.setOnClickListener(mClickListener);
        binding.btnLayout5.setOnClickListener(mClickListener);
        //키보드가 올라왔는지 안 올라왔는지
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
                new AlertDialog.Builder(MyPageActivity.this)
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
                new AlertDialog.Builder(MyPageActivity.this)
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
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
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }


            // For Android 5.0+\
            // SDK 21 이상부터 웹뷰에서 파일 첨부를 해주는 기능입니다.
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                //카메라 프로바이더로 이용해서 파일을 가져오는 방식입니다.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// API 24 이상 일경우..
                    File imageStorageDir = new File(MyPageActivity.this.getFilesDir() + "/Pictures", "bapcar_phto");
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                    // Create camera captured image file path and name

                    //Toast.makeText(mainActivity.getApplicationContext(),imageStorageDir.toString(),Toast.LENGTH_LONG).show();
                    File file = new File(imageStorageDir, "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    Uri providerURI = FileProvider.getUriForFile(MyPageActivity.this, MyPageActivity.this.getPackageName() + ".provider", file);
                    mCapturedImageURI = providerURI;

                } else {// API 24 미만 일경우..

                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "bapcar_phto");
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                    // Create camera captured image file path and name
                    File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    mCapturedImageURI = Uri.fromFile(file);
                }
                if (filePathCallbackLollipop != null) {
//                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "File Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
                return true;

            }

        };
    }

    WebViewClient client;
    {
        client = new WebViewClient() {



            //페이지 로딩중일 때 (마시멜로) 6.0 이후에는 쓰지 않음
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LocationFind();
                //binding.progressLayout.setVisibility(View.VISIBLE);
                /*Glide.with(MyPageActivity.this)
                        .load(R.raw.progress)
                        .into(binding.progressImageView);*/
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
                    /*Intent intent = new Intent(MyPageActivity.this,DownLoadWebViewActivity.class);
                    intent.putExtra("url",url);
                    startActivity(intent);*/
                    return true;

                }



                /*if (0 < url.indexOf("view.php") ||
                        0 < url.indexOf("write.php") ||
                        0 < url.indexOf("login.php") ||
                        0 < url.indexOf("register")) {
                    Intent intent = new Intent(MyPageActivity.this,WebActivity.class);
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


                if (url.equals(getString(R.string.url))
                        || url.equals(getString(R.string.domain)+"index.php")
                        ||url.startsWith(getString(R.string.domain)+"index.php?")
                        ||url.startsWith(getString(R.string.domain)+"?")
                ) {
                    if(0 < url.indexOf("#")){
                        isIndex = false;
                    }else {
                        isIndex = true;
                    }
                } else {
                    isIndex=false;
                }
                Log.d("isIndex",isIndex+"");
                //binding.webLayout.setEnabled(false);
                //메인 화면이 아닌 페이지는 새로고침을 할 수 있고
                if(isIndex==false) {

                }else{


                    //버전체크 하기
                    binding.webView.loadUrl("javascript:versionCheck('"+Common.getVersion(MyPageActivity.this)+"','"+Common.getMyDeviceId(MyPageActivity.this)+"')");
                }

                String address= LocationPosition.getAddress(LocationPosition.lat,LocationPosition.lng);
                //binding.webView.loadUrl("javascript:setAddress('"+address+"')");
                try{
                    if(!Common.TOKEN.equals("")){
                        view.loadUrl("javascript:fcmKey('"+Common.TOKEN+"','"+Common.getMyDeviceId(MyPageActivity.this)+"')");
                    }
                }catch (Exception e){
                    //토큰 생성
                    refreshToken();
                }

                if(0 < url.indexOf("mypage")){
                    binding.webView.clearHistory();
                }
                CookieManager.getInstance().flush();

                LocationFind();


            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("web-error",errorCode+""+description);

                view.loadUrl(firstUrl);
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
        LocationPosition.removePosition(this);
    }
    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함
        Log.d("goBack",binding.webView.canGoBack()+"");

        if(binding.webView.canGoBack()) {
            binding.webView.goBack();
        }else{
            super.onBackPressed();
            setResult(Activity.RESULT_OK);
            finish();
            overridePendingTransition(R.anim.slide_inleft, R.anim.slide_outright);

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //여기서부터 카메라와 갤러리에서 가져오는 값이 조금씩 달라집니다.
        Uri[] results = null;
        if(resultCode==RESULT_OK) {

            if(requestCode==1000){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.webView.clearCache(true);
                        binding.webView.reload();
                    }
                });

            }else if(requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE){
                Uri[] result = new Uri[0];
                Log.d("filePath1", mCapturedImageURI.toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


                    if (resultCode == RESULT_OK) {
                        result = (intent == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                    }

                    try{
                        Log.d("images",intent.getData().toString());
                        if(0 < intent.getData().toString().lastIndexOf("image")){
                            CropImage.activity(result[0])
                                    .setGuidelines(CropImageView.Guidelines.ON)//가이드라인을 보여줄 것인지 여부
                                    //.setAspectRatio(1,1)//가로 세로 1:1로 자르기 기능 * 1:1 4:3 16:9로 정해져 있어요
                                    //.setCropShape(CropImageView.CropShape.OVAL)//사각형과 동그라미를 선택할 수 있어요 OVAL은 동그라미 RECTAGLE은 사각형이예요 안 넣으면 사각형이예요
                                    .start(this);
                        }else{
                            filePathCallbackLollipop.onReceiveValue(result);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        if(0 < String.valueOf(result[0]).lastIndexOf("jpg")){
                            CropImage.activity(result[0])
                                    .setGuidelines(CropImageView.Guidelines.ON)//가이드라인을 보여줄 것인지 여부
                                    //.setAspectRatio(1,1)//가로 세로 1:1로 자르기 기능 * 1:1 4:3 16:9로 정해져 있어요
                                    //.setCropShape(CropImageView.CropShape.OVAL)//사각형과 동그라미를 선택할 수 있어요 OVAL은 동그라미 RECTAGLE은 사각형이예요 안 넣으면 사각형이예요
                                    .start(this);
                        }
                        //Log.d("results", String.valueOf(result[0]));
                        Log.d("errors",e.toString());
                    }
                }


            }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(intent);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    Uri[] result2 = new Uri[0];
                    result2 =  new Uri[]{resultUri} ;

                    Log.d("image-uri", resultUri.toString());
                    filePathCallbackLollipop.onReceiveValue(result2);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }


        }else{
            try {
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
            }catch (Exception e){

            }
        }
    }
    /*
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("event",event.sensor.getType()+"");
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            if (binding.webView.getUrl().equals(getString(R.string.url)) || binding.webView.getUrl().equals(getString(R.string.domain))) {
                mCount++;
                if(mCount == 3){
                    mCount = 0;
                    binding.webView.loadUrl(getString(R.string.domain)+"bbs/qrcode.php");
                }

            }
        }
    }*/

    //@Override
    /*public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }*/
    //위치 잡기
    private void LocationFind(){
        LocationPosition.act=MyPageActivity.this;
        LocationPosition.setPosition(MyPageActivity.this);

        if(LocationPosition.lng==0.0){

            LocationPosition.setPosition(MyPageActivity.this);
        }else{

        }
    }

    //현재위치 주소 잡기
    public void getAddress(){


        String address=LocationPosition.getAddress(LocationPosition.lat,LocationPosition.lng);

        binding.webView.loadUrl("javascript:getAddress('"+address+"','"+LocationPosition.lat+"','"+LocationPosition.lng+"')");
    }
    //로그인 로그아웃
    class WebJavascriptEvent{
        @JavascriptInterface
        public void setLogin(String mb_id,String mb_password){
            Log.d("login","로그인");
            Common.savePref(getApplicationContext(),"ss_mb_id",mb_id);
            Common.savePref(getApplicationContext(),"ss_mb_password",mb_password);
        }
        @JavascriptInterface
        public void setLogout(){
            Log.d("logout","로그아웃");
            Common.savePref(getApplicationContext(),"ss_mb_id","");
            Common.savePref(getApplicationContext(),"ss_mb_password","");
        }
        @JavascriptInterface
        public void doShare(String url){
            Intent sharedIntent = new Intent();
            sharedIntent.setAction(Intent.ACTION_SEND);
            sharedIntent.setType("text/plain");
            sharedIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sharedIntent.putExtra(Intent.EXTRA_TEXT, url);
            Intent chooser = Intent.createChooser(sharedIntent, "공유");
            startActivity(chooser);
        }
        @JavascriptInterface
        public void getMessage(String message){
            Toast.makeText(MyPageActivity.this, message, Toast.LENGTH_SHORT).show();
        }
        @JavascriptInterface
        public void getPosition(){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(LocationPosition.lat == 0.0){

                        mHandler.sendEmptyMessage(0);
                        //LocationFind();
                        //Toast.makeText(MyPageActivity.this, "다시 한번 현재위치 설정을 누르시면 됩니다.", Toast.LENGTH_SHORT).show();
                    }else {
                        String address=LocationPosition.getAddress(LocationPosition.lat,LocationPosition.lng);
                        binding.webView.loadUrl("javascript:getAddress('"+address+"','"+LocationPosition.lat+"','"+LocationPosition.lng+"')");
                    }
                }
            });

        }
        @JavascriptInterface
        public void loadingProgress(){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.progressLayout.setVisibility(View.VISIBLE);
                    Glide.with(MyPageActivity.this)
                            .load(R.raw.progress)
                            .into(binding.progressImageView);
                }
            });

        }
        @JavascriptInterface
        public void progressBarHidden(){
            binding.progressLayout.setVisibility(View.GONE);
        }
        //다중 이미지 첨부하기 기능
        @JavascriptInterface
        public void matisse(String number){
           /* int num=Integer.parseInt(number);
            Matisse.from(MyPageActivity.this)
                  .choose(Collections.singleton(MimeType.JPEG))
                  .countable(true)
                  .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                  .maxSelectable(num)
                  .spanCount(3)
                  .imageEngine(new GlideEngine())
                  .showPreview(true) // Default is `true`
                  .forResult(100);*/

            TedImagePicker.with(MyPageActivity.this)
                    .max(Integer.parseInt(number),number+"까지만 선택이 가능합니다.")//최대갯수
                    .mediaType(gun0912.tedimagepicker.builder.type.MediaType.IMAGE)//파일타입설정
                    .startMultiImage(new OnMultiSelectedListener() {
                        @Override
                        public void onSelected(@NotNull List<? extends Uri> uriList) {
                            postFile(uriList);
                        }
                    });
        }
        @JavascriptInterface
        public  void goMain(String url){
            Intent intent= new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("goUrl",url);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
        }

        @JavascriptInterface
        public void qrcodeCheck(String mb_id){
            Intent intent = new Intent(getApplicationContext(),ZxingActivity.class);
            intent.putExtra("mb_id",mb_id);
            intent.putExtra("camera","front");
            startActivity(intent);
        }



    }
    private void refreshToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("tdaeridriver");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        Common.TOKEN=task.getResult();
                        String msg =getString(R.string.msg_token_fmt,Common.TOKEN);
                        // Toast.makeText(MyPageActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                });
        //Common.TOKEN= FirebaseInstanceId.getInstance().getToken()
    }
    //네트워크 연결이 끊어질 때
    public void disconnectNetwork(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
    //네트워크 연결이 될 때
    public void connectNetwork(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    //위치 핸들러로 잡기
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LocationFind();
                        mHandler.sendEmptyMessage(1);
                    }
                },500);
            }else{
                //Toast.makeText(MyPageActivity.this, "위치잡기", Toast.LENGTH_SHORT).show();
                LocationFind();
                getAddress();
            }
        }
    };
    //파일을 서버에 업로드 하기
    private void postFile(List<? extends Uri>  uri){
        binding.webView.loadUrl("javascript:setUploading()");
        List<Uri> mSelected = (List<Uri>) uri;
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);//로그 기록
        //client http 설정
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();

        //레트로핏 설정
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(getString(R.string.domain))//url 주소
                .client(client)//http  연결
                .addConverterFactory(GsonConverterFactory.create())//gson(json) 가져오기
                .build();//실행하기
        //파라미터 설정
        //여러 파일들을 담아줄 ArrayList
        ArrayList<MultipartBody.Part> files = new ArrayList<>();
        Log.d("matisse",mSelected.toString());

        for(int i =0 ; i< mSelected.size();i++ ){
            RequestBody fileBody;
            File file;
            String fileName ="";
            InputStream inputStream = null;
            if(0 <= mSelected.get(i).toString().indexOf("file://")) {
                try {
                    inputStream = MyPageActivity.this.getContentResolver().openInputStream(mSelected.get(i));
                    //이미지파일을 비트맵으로 만들기
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                    //비트맵을 만드는 것을 서버에 MediaType으로 변환
                    fileBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArrayOutputStream.toByteArray());
                    file = new File(String.valueOf(mSelected.get(i)));
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo[]", file.getName(), fileBody);
                    files.add(filePart);
                } catch (Exception e) {

                }
            }else{
                RealPath rp = new RealPath(MyPageActivity.this,mSelected.get(i));
                file = new File(rp.getRealPath());//파일 객체로 변경하기
                fileBody =  RequestBody.create(MediaType.parse("image/jpeg"),file);;

                MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo[]", rp.getRealPath(),fileBody);
                files.add(filePart);
            }

            // RequestBody로 Multipart.Part 객체 생성





            /*RealPath rp = new RealPath(MyPageActivity.this,mSelected.get(i));//절대경로 찾아내기
            File file = new File(rp.getRealPath());//파일 객체로 변경하기
            Log.d("file-indexof",String.valueOf(mSelected.get(i)).indexOf("ile://")+"");
            if(0 <= mSelected.get(i).toString().indexOf("file://")){

                Log.d("file-path", String.valueOf(file).replace("file:",""));


                RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"),file);//서버에 보낼 데이터 생성하기
                String fileName="photo"+i+".jpg";
                //RequestBody로 Multipart.Part로 객체 생성 -> php $_FILES 변환하기 위함
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo[]", fileName,fileBody);
                files.add(filePart);
            }else{
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"),file);//서버에 보낼 데이터 생성하기
                String fileName="photo"+i+".jpg";
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo[]", fileName,fileBody);
                files.add(filePart);
            }*/

        }

        //Log.d("files-array",files.toString());

        //레트로핏 서비스 실행하기
        RetrofitService retrofitService=retrofit.create(RetrofitService.class);
        //데이터 보내기
        Call<FileData> call = retrofitService.postFile(files);
        //서버에서 응답받기
        call.enqueue(new Callback<FileData>() {
            @Override
            public void onResponse(Call<FileData> call, Response<FileData> response) {
                Log.d("repo",response.body().toString());
                //응답 받기 성공을 했을 때
                if(response.isSuccessful()){
                    FileData repo = response.body();
                    ArrayList<Files> data= repo.getFiles();
                    //ArrayList<String> jData=new ArrayList<>();
                    String jData="";
                    for(int i=0; i < data.size();i++){
                        //jData.add(data.get(i).getFileName());
                        jData+=data.get(i).getFileName()+",";
                    }
                    jData=jData.substring(0,jData.length()-1);
                    binding.webView.loadUrl("javascript:setMultiImages('"+jData+"');");
                }
            }

            @Override
            public void onFailure(Call<FileData> call, Throwable t) {
                Log.e("call-error",call.toString());
                t.printStackTrace();
            }
        });
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

                    binding.webView.loadUrl(getString(R.string.url)+"/bbs/mypage.php");
                    break;
                //설정
                case R.id.btnLayout5:

                    intent = new Intent(getApplicationContext(),SettingActivity.class);
                    resultLauncher.launch(intent);
                    //startActivity(intent);

                    overridePendingTransition(R.anim.slide_inleft,R.anim.slide_outright);
                    break;
            }
        }
    };
}