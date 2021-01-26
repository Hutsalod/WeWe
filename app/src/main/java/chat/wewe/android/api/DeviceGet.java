package chat.wewe.android.api;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import chat.wewe.android.fragment.setting.FragmentSetting;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceGet {

    public static void getSettings(String token, SwitchCompat compat){

                UtilsApi.getAPIService().getSettings("KEY:" + token)
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {

                                    try {
                                        JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                        Log.d("WEQQ", "Test " + response.body().string());
                                        Log.d("WEQQ", "Test " + jsonRESULTS.getJSONObject("result").getString("UF_ACCESS_OTH_DEVICE"));

                                        if (jsonRESULTS.getString("status").equals("200")) {

                                            if (jsonRESULTS.getJSONObject("result").getInt("UF_ACCESS_OTH_DEVICE") == 1) {
                                                compat.setChecked(true);
                                            } else {
                                                compat.setChecked(false);
                                            }


                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("debug", "onFailure: ERROR > " + t.toString());
                            }
                        });


    }

    public static void postDevice(String token, String device){
        Map<String, Object> jsonParams = new ArrayMap<>();
        jsonParams.put("UF_ACCESS_OTH_DEVICE", device);
        UtilsApi.getAPIService().postDevice(" KEY:"+token,jsonParams)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()){
                            Log.d("WEQQ", "Test " + response.body().toString());
                            try {

                                if (response.body().get("SUCCESS").equals("false")){

                                }
                                Log.d("WEQQ", "Test " + response.body().get("SUCCESS"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }

    public static void getDevice(String token) {

        UtilsApi.getAPIService().getDevice(" KEY:" + token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d("WEQQ", "Test " + response.body().toString());
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getJSONObject("result").getString("UF_ACCESS_OTH_DEVICE").equals("0") && jsonRESULTS.getJSONObject("result").getString("ACCESS_THIS_DEVICE").equals("0")) {
                                    FragmentSetting.setLogout();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }
}
