package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import bolts.Task;
import chat.wewe.android.R;

/**
 * add Channel, add Private-group.
 */


public class LogDialogFragment extends AbstractAddRoomDialogFragment {

    public String ActiveNumber = "", userId = "";
    private EditText editText4;
    ListView listView;

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


    @Override
    protected void onSetupDialog() {

       listView = getDialog().findViewById(R.id.listView);
        final ArrayList<String> logArray = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, logArray);
        listView.setAdapter(adapter);


        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("logcat -d");
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        logArray.add(0, line);
                        adapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {

                }
            }
        }, 0);


    }



    @Override
    protected Task<Void> getMethodCallForSubmitAction() {

        return null;

    }


    public interface ActionListener{
        void onClick(String uid);
    }

    private ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }


}


