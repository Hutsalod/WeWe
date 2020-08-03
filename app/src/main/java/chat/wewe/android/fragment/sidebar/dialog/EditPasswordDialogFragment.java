package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.api.UtilsApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * add Channel, add Private-group.
 */


public class EditPasswordDialogFragment extends DialogFragment {


    private TextInputEditText editor_username,editor_passwd;

    private Button editor;


    public EditPasswordDialogFragment() {
    }

    public static EditPasswordDialogFragment create() {
        Bundle args = new Bundle();

        EditPasswordDialogFragment fragment = new EditPasswordDialogFragment();
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
                .inflate(R.layout.dialog_edit_password, null, false);
        editor_username =  dialog.findViewById(R.id.editor_username);
        editor_passwd =  dialog.findViewById(R.id.editor_passwd);
        editor =  dialog.findViewById(R.id.editor);
Log.d("TT","Token "+getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""));

                dialog.findViewById(R.id.editor).setOnClickListener(registerButton -> {
                    Map<String, Object> jsonParams = new ArrayMap<>();
                    jsonParams.put("OLD_PASSWORD", editor_username.getText().toString());
                    jsonParams.put("NEW_PASSWORD", editor_passwd.getText().toString());
                    UtilsApi.getAPIService().change_password("KEY:"+getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""),"application/json",jsonParams)
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
                                            Toast.makeText(getActivity(), "Ваш пароль успешно изменён",
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


