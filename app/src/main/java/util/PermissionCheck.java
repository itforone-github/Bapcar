package util;

import android.Manifest;
import android.app.Activity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class PermissionCheck{
    Activity mActivity;
    static public boolean isPermissionCheck=false;
    String permissions[] = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 기기, 사진, 미디어, 파일 엑세스 권한
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
            //Manifest.permission.ACTIVITY_RECOGNITION
    };
    public  PermissionCheck(Activity act){
        this.mActivity=act;
    }

    public void setPermission(String message){

        TedPermission.with(mActivity)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(message)
                .setDeniedMessage("권한설정에 거부하시면 앱설정에서 직접하셔야 합니다.")
                .setPermissions(permissions)
                .check();

    }
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            try{
                isPermissionCheck=true;
                /*LocationPosition.act= mActivity;
                LocationPosition.setPosition(mActivity);
                if(LocationPosition.lng==0.0){
                    LocationPosition.setPosition(mActivity);
                }*/
            }catch(Exception e){

            }

        }
        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            isPermissionCheck=false;
        }
    };
}
