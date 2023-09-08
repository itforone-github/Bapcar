package com.itforone.bapcar;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.itforone.bapcar.databinding.ActivityDownLoadWebViewBinding;

import java.lang.reflect.Method;

import util.BackPressCloseHandler;
import util.DownloadWebViewClient;

public class DownLoadWebViewActivity extends AppCompatActivity {
    ActivityDownLoadWebViewBinding binding;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;
    String firstUrl = "";
    //비디오 전체 보기 변수 선언들
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    private FrameLayout mFullscreenContainer;
    private static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_down_load_web_view);
        binding.setDownload(this);
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
        firstUrl=getString(R.string.url);//웹뷰에 처음 실행할 것을 지정하는 url 주소입니다.
        Intent intent = getIntent();
        //푸시가 있을 때는 아래에 있는 소스가 실행이 됩니다.
        try{
            if(!intent.getExtras().getString("url").equals("")){
                firstUrl=intent.getExtras().getString("url");
            }
        }catch (Exception e){

        }

        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }

        binding.webView.setDownloadListener(new DownloadWebViewClient(DownLoadWebViewActivity.this));//다운로드 기능
        binding.webView.loadUrl(firstUrl);
    }

}