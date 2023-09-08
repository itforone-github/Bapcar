package com.itforone.bapcar.retrofit;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by 투덜이2 on 2017-02-14.
 */
// GET 방식 POST 방식 변수 세팅등등 설정
public interface RetrofitService {
   /* @FormUrlEncoded
    @POST("adm/json/query.php")
    Call<BoardData> getBoardList(
            @FieldMap Map<String, String> option
    );
    @FormUrlEncoded
    @POST("adm/json/query.php")
    Call<co.kr.itforone.smg2520.bbs.BoardData> getBbsBoardList(
            @FieldMap Map<String, String> option
    );
    @FormUrlEncoded
    @POST("adm/json/query.php")
    Call<LoginData> postLogin(
            @FieldMap Map<String, String> option
    );*/
    //도메인/bbs/ajax.multiupload.php로 보내기
    @Multipart
    @POST("bbs/ajax.multiupload.php")
   Call<FileData> postFile(
            @Part List<MultipartBody.Part> files
    );

    @FormUrlEncoded
    @POST("bbs/ajax.qrcode_check.php")
    Call<QrcodeData> qrcodeCheck(
            @FieldMap Map<String, String> option
    );
}