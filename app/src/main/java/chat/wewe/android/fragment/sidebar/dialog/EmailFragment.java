package chat.wewe.android.fragment.sidebar.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.UtilsApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * add Channel, add Private-group.
 */


public class EmailFragment extends DialogFragment {


    private TextInputEditText editor_username;

    private Button editor;


    public EmailFragment() {
    }

    public static EmailFragment create() {
        Bundle args = new Bundle();

        EmailFragment fragment = new EmailFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog)
                .setView(createDialogView())
                .create();
    }


    private View createDialogView() {
        View dialog = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_email, null, false);
        editor_username =  dialog.findViewById(R.id.editor_username);
        String token = getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE","");
        Log.d("QQWEWE","tok "+token);
        UtilsApi.getAPIService().getEmail("KEY:"+ token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("jsonRESULTS",""+response.toString());
                        if (response.isSuccessful()){
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getJSONObject("result").getString("SUCCESS").equals("true")){
                                    editor_username.setText(jsonRESULTS.getJSONObject("result").getString("EMAIL"));
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
                    }
                });

                dialog.findViewById(R.id.editor).setOnClickListener(registerButton -> {
                    Map<String, Object> jsonParams = new ArrayMap<>();
                    jsonParams.put("EMAIL", editor_username.getText().toString());
                    UtilsApi.getAPIService().postEmail("KEY:"+ getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE",""),jsonParams)
                            .enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject > response) {
                                    try{
                                        if (response.body().get("SUCCESS").equals("false")){
                                            Log.d("ttt","err");
                                            String msg = response.body().get("ERROR").toString();
                                            Toast.makeText(getActivity(), ""+msg,
                                                    Toast.LENGTH_SHORT).show();
                                            Log.d("ttt","err");
                                        }else{
                                            Log.d("ttt","yes");
                                            dismiss();
                                            Toast.makeText(getActivity(), getString(R.string.send_email),
                                                    Toast.LENGTH_SHORT).show();
                                            EmailPasswordDialogFragment.create()
                                                    .show(getFragmentManager(), "EmailPasswordDialogFragment");
                                        }

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                }
                            });

        });
  
        return  dialog;
    }




    public interface ActionListener{
        void onClick(String uid);
    }

    private ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }


}


