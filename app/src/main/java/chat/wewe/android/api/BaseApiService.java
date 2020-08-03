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

    // Fungsi ini untuk memanggil API http://10.0.2.2/mahasiswa/register.php


    @FormUrlEncoded
    @POST("rest_api/register/")
    public Call<ResponseBody> registerRequest(@Field("DEVID") String nama,
                                              @Field("USER_LOGIN") String email,
                                              @Field("USER_PASSWORD") String password);

    @POST("rest/user/change_password/")
    Call<JsonObject>change_password(@Header("Authorization-Token") String Token, @Header("Content-Type") String Type, @Body Map<String, Object> params);

    @Headers({"Content-type: application/json"})
    @POST("rest/user/voip_token/")
    public Call<ResponseBody> voip_token(@Header("Authorization-Token") String Token, @Body Map<String, Object> params);

    @GET("rest/user/settings/")
    Call<ResponseBody> getSettings(@Header("Authorization-Token") String authKeys);

    @GET("rest/user/devices/")
    Call<ResponseBody> getDevices(@Header("Authorization-Token") String authKeys);

    @FormUrlEncoded
    @GET("rest/user/access_device/")
    Call<ResponseBody>getDevice(@Header("Authorization-Token") String authKeys);


    @Headers({"Content-type: application/json"})
    @POST("rest/user/push_token/")
    Call<ResponseBody>postPush(@Header("Authorization-Token") String authKeys, @Body Map<String, Object> params);

    @FormUrlEncoded
    @POST("rest/user/access_device/")
    Call<ResponseBody>postDevice(@Header("Authorization-Token") String authKeys, @Field("UF_ACCESS_OTH_DEVICE") String DEVICE);



    @POST("rest/user/subscription/")
    Call<JsonObject>subscription(@Header("Authorization-Token") String Token, @Header("Content-Type") String Type, @Body Map<String, Object> params);

    @GET("rest/user/remove_transactionid/")
    Call<ResponseBody>removeTransactionid(@Header("Authorization-Token") String Token);




    @GET("api/v1/users.info?")
    public Call<ResponseBody>getStatus(@Query("username") String user, @Header("X-Auth-Token") String Token, @Header("X-User-Id") String id);

@GET("api/v1/spotlight?")
    public Call<ResponseBody>getList(@Query("query") String user, @Header("X-Auth-Token") String Token, @Header("X-User-Id") String id);


    @GET("rest/blacklist/get/")
    Call<ResponseBody>getBlacklist(@Header("Authorization-Token") String Token);

    @Headers({"Content-Type: application/json"})
    @POST("rest/blacklist/add/")
    Call<ResponseBody>getBlacklistAdd(@Header("Authorization-Token") String Token, @Body Map<String, Object> params);

    @Headers({"Content-Type: application/json"})
    @POST("rest/call/voip/")
    Call<ResponseBody>postCallvoip(@Header("Authorization-Token") String Token, @Body Map<String, Object> params);

    @Headers({"Content-Type: application/json"})
    @POST("rest/blacklist/delete/")
    Call<JsonObject>getBlacklistDell(@Header("Authorization-Token") String Token, @Body Map<String, Object> params);


    @GET("api/v1/users.list?count=0")
    Call<ResponseBody>getListStatus(@Header("X-Auth-Token") String Token, @Header("X-User-Id") String Id);


    @Headers({"Content-type: application/json"})
    @POST("api/v1/push.token")
    Call<ResponseBody>setPush(@Header("Authorization-Token") String Token, @Header("X-User-Id") String Id, @Body Map<String, Object> params);

    @Multipart
    @POST("api/v1/users.setAvatar")
    Call<ResponseBody> setAvatarFile(@Header("X-Auth-Token") String Token, @Header("X-User-Id") String Id,
                                     @Part MultipartBody.Part image);

    @GET("api/v1/chat.search?")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> chat_search(@Header("X-Auth-Token") String Token, @Header("X-User-Id") String Id,
                                   @Query("roomId") String text, @Query("searchText") String search);

    @GET("api/v1/chat.ignoreUser?")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> chat_ignoreUserh(@Header("X-Auth-Token") String Token, @Header("X-User-Id") String Id,
                                        @Query("rid") String text, @Query("userId") String userId, @Query("ignore") Boolean ignore);

    @GET("api/v1/im.files?")
    Call<JsonObject> im_files(@Header("X-Auth-Token") String Token, @Header("X-User-Id") String Id,
                              @Query("roomId") String text);


    @GET
     Call<ResponseBody> image_set(@Header("X-Auth-Token") String Token, @Header("X-User-Id") String Id,
                                  @Url String url);
}
