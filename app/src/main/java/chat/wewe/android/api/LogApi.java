package chat.wewe.android.api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import chat.wewe.android.BuildConfig;
import chat.wewe.android.service.PortSipService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/*https://comeliest-adaptions.000webhostapp.com/*/
public class LogApi {


    public void logPost(String token){
        Map<String, Object> jsonParams = new ArrayMap<>();
        jsonParams.put("device_id", Build.MODEL);
        jsonParams.put("version", BuildConfig.VERSION_NAME);
        UtilsApi.getAPIService().postLog(" KEY:"+token,jsonParams)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }




}
