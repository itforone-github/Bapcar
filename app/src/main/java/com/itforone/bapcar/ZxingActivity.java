package com.itforone.bapcar;

import static android.speech.tts.TextToSpeech.ERROR;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.ResultPoint;
import com.itforone.bapcar.retrofit.QrcodeData;
import com.itforone.bapcar.retrofit.RetrofitService;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ZxingActivity extends AppCompatActivity {
    CaptureManager capture; // [CaptureManager 객체 선언]
    DecoratedBarcodeView barcodeScannerView; //[XML 레이아웃 DecoratedBarcodeView]
    CameraSettings cameraSettings = new CameraSettings(); //[카메라 설정 관련]
    String cameraSettingData = ""; //[전방, 후방 카메라 설정 관련]
    boolean captureFlag = false; //[QR 스캔 후 지속적으로 스캔 방지 플래그]
    String companyId="";//푸드업체 아이디
    TextToSpeech tts;//텍스트를 음성을 변경하는 객체
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxing);

        //액션바 없애기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Log.d("---","---");
        Log.d("//===========//","================================================");
        Log.d("","\n"+"[A_QR_Scan > onCreate() 메소드 : 액티비티 시작 실시]");
        Log.d("//===========//","================================================");
        Log.d("---","---");

        //TODO [전방 및 후방 카메라 초기 설정 여부 확인]
        try {
            Intent intent = getIntent(); //TODO [getIntent() 를 사용해서 데이터를 받아온다]
            cameraSettingData = String.valueOf(intent.getStringExtra("camera")); // [키값 호출]
            companyId = String.valueOf(intent.getStringExtra("mb_id"));

            if(cameraSettingData.equals("front")){ //TODO [전방 카메라]
                Log.d("---","---");
                Log.w("//===========//","================================================");
                Log.d("","\n"+"[A_QR_Scan > 초기 카메라 설정 여부 확인]");
                Log.d("","\n"+"[설정 : "+"전방 카메라"+"]");
                Log.w("//===========//","================================================");
                Log.d("---","---");
                cameraSettings.setRequestedCameraId(1);
            }
            else if(cameraSettingData.equals("back")){ //TODO [후방 카메라]
                Log.d("---","---");
                Log.w("//===========//","================================================");
                Log.d("","\n"+"[A_QR_Scan > 초기 카메라 설정 여부 확인]");
                Log.d("","\n"+"[설정 : "+"후방 카메라"+"]");
                Log.w("//===========//","================================================");
                Log.d("---","---");
                cameraSettings.setRequestedCameraId(0);
            }
            else { //TODO [디폴트 : 후방 카메라]
                Log.d("---","---");
                Log.w("//===========//","================================================");
                Log.d("","\n"+"[A_QR_Scan > 초기 카메라 설정 여부 확인]");
                Log.d("","\n"+"[설정 : "+"디폴트 설정 - 후방 카메라"+"]");
                Log.w("//===========//","================================================");
                Log.d("---","---");
                cameraSettings.setRequestedCameraId(0);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //TODO [컴포넌트 매칭 실시]
        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.barcodeScannerView);
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(this.getIntent(),savedInstanceState);
        capture.decode();

        //TODO [QR 스캔 데이터 확인]
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                try {
                    String result_data = "";
                    if(captureFlag == false){ //TODO [최초 1번 스캔된 경우]
                        result_data = String.valueOf(result.toString());
                        byte result_byte [];
                        //TODO Arrays.toString 형식 바이트 문자열 [104,101,108,108,111]
                        if(result_data.contains("[") && result_data.contains("]")
                                && result_data.contains(",")){ //TODO [바이트 값 형식 문자열인 경우 > 한글로 출력]
                            result_byte = getByteArray(result_data);
                            result_data = new String(result_byte, "utf-8");
                        }
                        Log.d("---","---");
                        Log.w("//===========//","================================================");
                        Log.d("","\n"+"[A_QR_Normal_Scan > QR 스캔 정보 확인 실시]");
                        Log.d("","\n"+"[결과 : "+String.valueOf(result_data)+"]");
                        Log.w("//===========//","================================================");
                        Log.d("---","---");

                        //TODO [팝업창 호출 실시]
                        String alertTittle = "[QR 스캔 정보 확인]";
                        String alertMessage = "[정보]"+"\n"+"\n"+String.valueOf(result_data);
                        String buttonYes = "다시 스캔";
                        String buttonNo = "종료";
                        if(!captureFlag) {
                            captureFlag = true;
                            qrcodeCheck(companyId,String.valueOf(result_data));
                            //Toast.makeText(ZxingActivity.this, String.valueOf(result_data), Toast.LENGTH_SHORT).show();
                        }
                        /*new AlertDialog.Builder(ZxingActivity.this)
                                .setTitle(alertTittle)
                                .setMessage(alertMessage)
                                .setCancelable(false)
                                .setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //TODO Auto-generated method stub
                                        //TODO [다시 스캔을 하기 위해 플래그값 변경]
                                        captureFlag = false;
                                    }
                                })
                                .setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        try {
                                            //TODO [액티비티 종료 실시]
                                            finish();
                                            overridePendingTransition(0,0);
                                        }
                                        catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .show();*/

                        //TODO [플래그 값을 변경 실시 : 중복 스캔 방지]

                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });


    }
    //TODO [백버튼 터치시 뒤로 가기]
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("---","---");
            Log.d("//===========//","================================================");
            Log.d("","\n"+"[A_QR_Scan > onKeyDown() 메소드 : 백버튼 터치시 뒤로 가기 이벤트 실시]");
            Log.d("//===========//","================================================");
            Log.d("---","---");
            try {
                //TODO [액티비티 종료 실시]
                finish();
                overridePendingTransition(0,0);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }

    //TODO [액티비티 실행 준비]
    @Override
    protected void onResume(){
        super.onResume();
        Log.d("---","---");
        Log.d("//===========//","================================================");
        Log.d("","\n"+"[A_QR_Scan > onResume() 메소드 : 액티비티 실행 준비]");
        Log.d("//===========//","================================================");
        Log.d("---","---");
        try {
            capture.onResume();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //TODO [액티비티 정지 실시]
    @Override
    protected void onPause(){
        super.onPause();
        Log.d("---","---");
        Log.d("//===========//","================================================");
        Log.d("","\n"+"[A_QR_Scan > onPause() 메소드 : 액티비티 일시 정지]");
        Log.d("//===========//","================================================");
        Log.d("---","---");
        try {
            capture.onPause();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //TODO [capture State 설정]
    @Override
    protected  void  onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    //TODO [액티비티 종료 실시]
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("---","---");
        Log.d("//===========//","================================================");
        Log.d("","\n"+"[A_QR_Scan > onDestroy() 메소드 : 액티비티 종료 실시]");
        Log.d("//===========//","================================================");
        Log.d("---","---");
        if(tts!=null){ // 사용한 TTS객체 제거
            tts.stop();
            tts.shutdown();
        }
        try {
            capture.onDestroy();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //TODO [Arrays 형태 문자열 파싱 실시 : 한글 데이터 출력 시]
    public static byte [] getByteArray(String data) {
        byte ok_result [] = null;
        try {
            //초기 Arrays.toString 형태로 저장된 문자열에서 불필요한 문자 제거 실시
            data = data.replaceAll("\\[", ""); //괄호를 지운다(역슬래시 특수문자 지정)
            data = data.replaceAll("\\]", ""); //괄호를 지운다(역슬래시 특수문자 지정)
            data = data.replaceAll(" ", ""); //공백을 지운다

            //Arrays.toString 형태는 콤마 기준으로 저장된다 (콤마 개수 체크)
            int check = 0;

            for(int i=0; i<data.length(); i++) {
                if(data.charAt(i) == ',') {
                    check ++;
                }
            }

            //check 개수 확인 후 배열 크기 지정 실시
            ok_result = new byte [check+1];

            //몇개의 데이터가 포함되었는지 확인 실시
            if(data.length() > 0) {
                if(check > 0) { //데이터가 한개 초과 저장된 경우
                    for(int j=0; j<=check; j++) { //콤마가 포함된 [기준]으로 문자열을 분리시킨다
                        ok_result [j] = Byte.valueOf(data.split("[,]")[j]);
                    }
                }
                else { //데이터가 한개만 저장된 경우
                    ok_result [0] = Byte.valueOf(data);
                }
            }
            else {
                ok_result [0] = 0x00;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return ok_result;
    }

    void qrcodeCheck(String companyId,String data){



        String mb_id = data;
        //http 로그를 보여주기(가로채기)
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //서버와 클라이언트 연결
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
        //레트로핏으로 json 파싱 준비
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Map map = new HashMap();
        map.put("mb_id", mb_id);
        map.put("company_id", companyId);
        //json 문서 가져오기
        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<QrcodeData> call = retrofitService.qrcodeCheck(map);
        call.enqueue(new Callback<QrcodeData>() {

            @Override
            public void onResponse(Call<QrcodeData> call, Response<QrcodeData> response) {
                //실제 문서 가져오기 성공했을 경우
                if (response.isSuccessful()) {
                    QrcodeData repo = response.body();
                    if(Boolean.parseBoolean(repo.getIs_success())) {
                        Toast.makeText(ZxingActivity.this, "서버 전송중입니다.", Toast.LENGTH_SHORT).show();
                        isFinish();
                    }else{
                        isNotFinish(repo.getMessage());
                    }
                    //실패했을 경우
                } else {
                    captureFlag=false;
                    Toast.makeText(ZxingActivity.this, "인증에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QrcodeData> call, Throwable t) {
                Log.e("call-error",call.toString());
                t.printStackTrace();
            }
        });
    }
    //QR코드 인증 되었다는 표시
    public void isFinish(){
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);*/
                captureFlag=false;
                //tts 초기값 설정
                tts = new TextToSpeech(ZxingActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != ERROR){
                            int result = tts.setLanguage(Locale.KOREAN);//tts 음성을 한국어로
                            if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                                Log.e("TTS", "This Language is not supported");
                            }else{
                                tts.speak("인증되었습니다.",TextToSpeech.QUEUE_FLUSH,null,"uid");
                            }
                        }
                    }
                });

                //tts.speak("인증되었습니다.",TextToSpeech.QUEUE_FLUSH,null);
                Toast.makeText(ZxingActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show();

            }
        }, 3000);
    }
    //QR코드 인증 되었다는 표시
    public void isNotFinish(String message){
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);*/
                captureFlag=false;
                //tts 초기값 설정
                tts = new TextToSpeech(ZxingActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != ERROR){
                            int result = tts.setLanguage(Locale.KOREAN);//tts 음성을 한국어로
                            if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                                Log.e("TTS", "This Language is not supported");
                            }else{
                                tts.setPitch((float) 1.0);
                                tts.setSpeechRate((float) 1.0);
                                tts.speak(message,TextToSpeech.QUEUE_FLUSH,null,"uid");
                            }
                        }
                    }
                });


                //tts.speak(message,TextToSpeech.QUEUE_FLUSH,null);
                Toast.makeText(ZxingActivity.this, message, Toast.LENGTH_SHORT).show();

            }
        }, 3000);
    }
}