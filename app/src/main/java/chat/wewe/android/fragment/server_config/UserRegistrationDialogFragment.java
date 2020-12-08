package chat.wewe.android.fragment.server_config;

import android.app.Dialog;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import chat.wewe.android.R;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.helper.TextUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dialog for user registration.
 */
public class UserRegistrationDialogFragment extends DialogFragment {
    private String hostname;
    private String username;
    private String email;
    private String password;
    private TextInputEditText txtUsername;
    private TextInputEditText txtEmail;
    private TextInputEditText txtPasswd;
    private TextInputLayout textInputUsername;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;
    private View waitingView;
    private BaseApiService mApiService;
    private String model = "Android";

    public UserRegistrationDialogFragment() {
        super();
    }

    /**
     * build UserRegistrationDialogFragment with auto-detect email/username.
     */
    public static UserRegistrationDialogFragment create(String hostname,
                                                        String usernameOrEmail, String password) {
        if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
            return create(hostname, null, usernameOrEmail, password);
        } else {
            return create(hostname, usernameOrEmail, null, password);
        }
    }

    /**
     * build UserRegistrationDialogFragment.
     */
    public static UserRegistrationDialogFragment create(String hostname,
                                                        String username, String email,
                                                        String password) {
        Bundle args = new Bundle();
        args.putString("hostname", hostname);
        if (!TextUtils.isEmpty(username)) {
            args.putString("username", username);
        }
        if (!TextUtils.isEmpty(email)) {
            args.putString("email", email);
        }
        if (!TextUtils.isEmpty(password)) {
            args.putString("password", password);
        }
        UserRegistrationDialogFragment dialog = new UserRegistrationDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            hostname = args.getString("hostname");
            username = args.getString("username");
            email = args.getString("email");
            password = args.getString("password");
        }

        mApiService = UtilsApi.getAPIService();
        model = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
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
                .inflate(R.layout.dialog_user_registration, null, false);

        initViews(dialog);
        setUpRxBinders();

        if (!TextUtils.isEmpty(username)) {
            txtUsername.setText(username);
        }
        if (!TextUtils.isEmpty(email)) {
            txtEmail.setText(email);
        }
        if (!TextUtils.isEmpty(password)) {
            txtPasswd.setText(password);
        }

        waitingView.setVisibility(View.GONE);

        dialog.findViewById(R.id.btn_register_user).setOnClickListener(registerButton -> {
            username = txtUsername.getText().toString();
            email = txtEmail.getText().toString();
            password = txtPasswd.getText().toString();
          /*  if (checkIfEditTextsEmpty())
                return;

            registerButton.setEnabled(false);
            registerButton.setAlpha(0.5f);
            waitingView.setVisibility(View.VISIBLE);

            username = txtUsername.getText().toString();
            email = txtEmail.getText().toString();
            password = txtPasswd.getText().toString();
            MethodCallHelper methodCallHelper = new MethodCallHelper(getContext(), hostname);
            methodCallHelper.registerUser(username, email, password, password)
                    .onSuccessTask(task -> methodCallHelper.loginWithEmail(email, password))
                    .onSuccessTask(task -> methodCallHelper.setUsername(username)) //TODO: should prompt!
                    .onSuccessTask(task -> methodCallHelper.joinDefaultChannels())
                    .onSuccessTask(task -> {
                        dismiss();
                        return task;
                    })
                    .continueWith(task -> {
                        if (task.isFaulted()) {
                            Exception exception = task.getError();
                            showError(exception.getMessage());
                            registerButton.setEnabled(true);
                            waitingView.setVisibility(View.GONE);
                        }
                        return null;
                    });
                    */
            requestRegister();
        });



        return dialog;
    }

    private void initViews(View dialog) {
        txtUsername = dialog.findViewById(R.id.editor_username);
        txtEmail = dialog.findViewById(R.id.editor_email);
        txtPasswd = dialog.findViewById(R.id.editor_passwd);
        textInputEmail = dialog.findViewById(R.id.text_input_email);
        textInputUsername = dialog.findViewById(R.id.text_input_username);
        textInputPassword = dialog.findViewById(R.id.text_input_passwd);
        waitingView = dialog.findViewById(R.id.waiting);
    }

    private boolean checkIfEditTextsEmpty() {
        boolean check = false;
        if (TextUtils.isEmpty(txtEmail.getText().toString())) {
            textInputEmail.setError("Enter an email address");
            textInputEmail.setErrorEnabled(true);
            check = true;
        }
        if (TextUtils.isEmpty(txtUsername.getText().toString())) {
            textInputUsername.setError("Enter a username");
            textInputUsername.setErrorEnabled(true);
            check = true;
        }
        if (TextUtils.isEmpty(txtPasswd.getText().toString())) {
            textInputPassword.setError("Enter a password");
            textInputPassword.setErrorEnabled(true);
            check = true;
        }
        return check;
    }

    private void setUpRxBinders() {
        RxTextView.textChanges(txtUsername).subscribe(text -> {
            if (!TextUtils.isEmpty(text) && textInputUsername.isErrorEnabled())
                textInputUsername.setErrorEnabled(false);
        });
        RxTextView.textChanges(txtEmail).subscribe(text -> {
            if (!TextUtils.isEmpty(text) && textInputEmail.isErrorEnabled())
                textInputEmail.setErrorEnabled(false);
        });
        RxTextView.textChanges(txtPasswd).subscribe(text -> {
            if (!TextUtils.isEmpty(text) && textInputPassword.isErrorEnabled())
                textInputPassword.setErrorEnabled(false);
        });
    }

    private void showError(String errMessage) {
        Toast.makeText(getContext(), errMessage, Toast.LENGTH_SHORT).show();
    }

    private void requestRegister(){
        waitingView.setVisibility(View.VISIBLE);
        if(password.length()<6)
            Toast.makeText(getActivity(), getString(R.string.pass_six), Toast.LENGTH_SHORT).show();
        mApiService.registerRequest(model,username,password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        JSONObject jsonRESULTS = new JSONObject(response.body().string());
                        if (jsonRESULTS.getString("SUCCESS").equals("false")){

                         String   msg = jsonRESULTS.getString("ERROR_CODE");
                            Log.d("debug", "onResponse: GA BERHASIL"+msg);
                            switch (msg) {
                                case "1": msg = getString(R.string.reg_error_1);
                                    break;
                                case "2": msg = getString(R.string.reg_error_2);
                                    break;
                                case "3": msg = getString(R.string.reg_error_3);
                                    break;
                                case "4": msg = getString(R.string.reg_error_4);
                                    break;
                                case "5": msg = getString(R.string.reg_error_5);
                                    break;
                            }
                            Toast.makeText(getActivity(), ""+msg, Toast.LENGTH_SHORT).show();
                            waitingView.setVisibility(View.GONE);
                        }else{
                            dismiss();
                            Toast.makeText(getActivity(), getString(R.string.reg_ok), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        waitingView.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                        waitingView.setVisibility(View.GONE);
                    }
                } else {
                    Log.i("debug", "onResponse: GA BERHASIL");
                    waitingView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("debug", "onFailure: ERROR > " + t.getMessage());
                Toast.makeText(getContext(), "Koneksi Internet Bermasalah", Toast.LENGTH_SHORT).show();
                waitingView.setVisibility(View.GONE);
            }
        });
    }

}
