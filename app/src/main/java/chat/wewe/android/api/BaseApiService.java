package chat.wewe.android.api;

import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface BaseApiService {


    @POST("rest_api/auth/")
    public Call<ResponseBody> getStatusUsers();

    @FormUrlEncoded
    @POST("rest_api/auth/")
    public Call<ResponseBody> loginRequest(@Field("USER_LOGIN") String email,
                                           @Field("USER_PASSWORD") String password,
                                           @Field("DEVID") String devid,
                                           @Field("DEVICE_TYPE") String type,
                                           @Field("DEVNAME") String devidname,
                                           @Field("VOIP") String voip,
                                           @Field("PUSH") String push);


    @FormUrlEncoded
    @POST("rest_api/register/")
    public Call<ResponseBody> registerRequest(@Field("DEVID") String nama,
                                              @Field("USER_LOGIN") String email,
                                              @Field("USER_PASSWORD") String password);

    @POST("rest/user/change_password/")
    Call<JsonObject>change_password(@Header("Authorization-Token") String Token, @Header("Content-Type") String Type, @Body Map<String, Object> params);

    @POST("rest_api/auth/send_checkword.php")
    Call<JsonObject>send_checkword( @Body Map<String, Object> params);

    @GET("rest/user/email/")
    Call<ResponseBody> getEmail(@Header("Authorization-Token") String authKeys);

    @Headers({"Content-type: application/json"})
    @POST("rest/user/email/")
    Call<JsonObject> postEmail(@Header("Authorization-Token") String authKeys, @Body Map<String, Object> params);

    @GET("rest/user/settings/")
    Call<ResponseBody> getSettings(@Header("Authorization-Token") String authKeys);


    @GET("rest/user/access_device/")
    Call<ResponseBody>getDevice(@Header("Authorization-Token") String authKeys);


    @Headers({"Content-type: application/json"})
    @POST("rest/user/push_token")
    Call<ResponseBody>postPush(@Header("Authorization-Token") String authKeys, @Body Map<String, Object> param);

    @Headers({"Content-type: application/json"})
    @POST("rest/user/save_version")
    Call<ResponseBody>postLog(@Header("Authorization-Token") String authKeys, @Body Map<String, Object> param);

    @Headers({"Content-type: application/json"})
    @POST("rest/user/access_device/")
    Call<JsonObject>postDevice(@Header("Authorization-Token") String Token, @Body Map<String, Object> DEVICE);

    @Headers({"Content-type: application/json"})
    @POST("rest/user/public_key")
    Call<ResponseBody>setPublicKey(@Header("Authorization-Token") String authKeys, @Body Map<String, Object> param);


    @GET("rest/user/public_key")
    Call<ResponseBody>getPublicKey(@Header("Authorization-Token") String authKeys, @Query("login") String name );

    @POST("rest/user/subscription/")
    Call<JsonObject>subscription(@Header("Authorization-Token") String Token, @Header("Content-Type") String Type, @Body Map<String, Object> params);

    @GET("rest/user/remove_transactionid/")
    Call<ResponseBody>removeTransactionid(@Header("Authorization-Token") String Token);




}
