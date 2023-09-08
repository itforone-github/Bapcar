package util;

import android.app.Activity;
import android.widget.Toast;

import com.itforone.bapcar.MainActivity;


/**
 * Created by 투덜이2 on 2016-11-21.
 */

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity act) {
        this.activity = act;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            MainActivity.is_splash=false;
            activity.finish();
            toast.cancel();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(activity,
                "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

}
