package chat.wewe.android.fragment.sidebar.dialog;

import android.app.Dialog;
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

import java.util.Map;

import chat.wewe.android.R;
import chat.wewe.android.api.UtilsApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * add Channel, add Private-group.
 */


public class EmailPasswordDialogFragment extends DialogFragment {


    private TextInputEditText editor_login,editor_code,editor_pass,editor_pass_two;

    private Button editor;


    public EmailPasswordDialogFragment() {
    }

    public static EmailPasswordDialogFragment create() {
        Bundle args = new Bundle();

        EmailPasswordDialogFragment fragment = new EmailPasswordDialogFragment();
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
                .inflate(R.layout.dialog_email_password, null, false);
        editor_login =  dialog.findViewById(R.id.editor_login);
        editor_code =  dialog.findViewById(R.id.editor_code);
        editor_pass =  dialog.findViewById(R.id.editor_pass);
        editor_pass_two =  dialog.findViewById(R.id.editor_pass_two);


                dialog.findViewById(R.id.editor).setOnClickListener(registerButton -> {
                    Map<String, Object> jsonParams = new ArrayMap<>();
                    jsonParams.put("LOGIN", editor_login.getText().toString());
                    jsonParams.put("CHECKWORD", editor_code.getText().toString());
                    jsonParams.put("PASSWORD", editor_pass.getText().toString());
                    jsonParams.put("CONFIRM_PASSWORD", editor_pass_two.getText().toString());
                    UtilsApi.getAPIService().send_checkword(jsonParams)
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
                                            Toast.makeText(getActivity(), getString(R.string.send_email),
                                                    Toast.LENGTH_SHORT).show();
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


