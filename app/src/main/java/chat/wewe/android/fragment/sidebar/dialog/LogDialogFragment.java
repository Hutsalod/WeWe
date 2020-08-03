package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import bolts.Task;
import chat.wewe.android.R;

/**
 * add Channel, add Private-group.
 */


public class LogDialogFragment extends AbstractAddRoomDialogFragment {

    public String ActiveNumber = "", userId = "";
    private EditText editText4;


    public LogDialogFragment() {
    }

    public static LogDialogFragment create() {
        Bundle args = new Bundle();

        LogDialogFragment fragment = new LogDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.dialog_log;
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    protected void onSetupDialog() {

        editText4 =  getDialog().findViewById(R.id.editText4);

        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }

            editText4.setText(log.toString());
        } catch (IOException e) {
            // Handle Exception
        }


    }



    @Override
    protected Task<Void> getMethodCallForSubmitAction() {

        return methodCall.createChannel("", "", true);

    }


    public interface ActionListener{
        void onClick(String uid);
    }

    private ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }


}


