package util;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.DownloadListener;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.net.URLDecoder;

public class DownloadWebViewClient extends WebViewClient implements DownloadListener {
    static String TAG = "TAG";
    Activity act;
    public DownloadWebViewClient(Activity act){
        this.act=act;
    }
    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            DownloadManager dm = (DownloadManager) act.getSystemService(DOWNLOAD_SERVICE);

            contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8"); //디코딩
            String FileName = contentDisposition.replace("attachment;filename=", ""); //attachment; filename*=UTF-8''뒤에 파일명이있는데 파일명만 추출하기위해 앞에 attachment; filename*=UTF-8''제거

            String fileName = FileName; //위에서 디코딩하고 앞에 내용을 자른 최종 파일명
            request.setMimeType(mimetype);
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading File");
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);
            request.setTitle(fileName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                request.setRequiresCharging(false);
            }

            request.allowScanningByMediaScanner();
            request.setAllowedOverMetered(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            dm.enqueue(request);
            Toast.makeText(act.getApplicationContext(),"파일이 다운로드됩니다.", Toast.LENGTH_LONG).show();
            act.finish();
        }
        catch (Exception e) {
            if (ContextCompat.checkSelfPermission(act.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            }
        }

    }
}
