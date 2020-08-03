package chat.wewe.android.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.adapter.RecyclerViewTask;
import chat.wewe.android.adapter.TaskMessage;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.sidebar.dialog.AddTaskFragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FragmentTaskMessage extends AbstractFragment {


    private String hostname, userId = "",_rid = "";
    private static final String HOSTNAME = "hostname",USERID = "userId";

    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<String> mMessage = new ArrayList<>();

    private ImageButton send;

    private MethodCallHelper methodCall;

    private TextView textMessage;

    private String index = "";

    private RecyclerView list_view;


    public static FragmentTaskMessage create(String hostname, String rid, String index) {
        Bundle args = new Bundle();
        args.putString("hostname", hostname);
        args.putString("_rid", rid);
        args.putString("index", index);

        FragmentTaskMessage fragment = new FragmentTaskMessage();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        hostname = getArguments().getString(HOSTNAME);
        _rid = getArguments().getString("_rid");
        index = getArguments().getString("index");
        methodCall = new MethodCallHelper(getContext(), hostname);

    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_tab_task_message;
    }

    @Override
    protected void onSetupView() {

        list_view = rootView.findViewById(R.id.recyclerv_view);
        list_view.setLayoutManager(new LinearLayoutManager(getContext()));
        send = rootView.findViewById(R.id.button_send);
        textMessage = rootView.findViewById(R.id.textMessage);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);


        getTask();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("TASK_TEST"," "+_rid+" "+index+" "+textMessage.getText().toString()+" "+ RocketChatCache.INSTANCE.getUserId());
                        methodCall.AddMsgToTask(_rid, Integer.parseInt(index),textMessage.getText().toString(),RocketChatCache.INSTANCE.getUserId()).continueWith(task -> {
                            textMessage.setText("");
                            getTask();
                            return null;
                        });

            }
        });

        return view;
    }



    public Task<Void> getTask(){
        mName.clear();
        mData.clear();
        mMessage.clear();
        return methodCall.getOneTask(_rid, Integer.parseInt(index)).onSuccessTask(task -> {

            final JSONObject info = task.getResult();

            for (int m = 0; m < info.getJSONArray("_messages").length(); m++) {

                mName.add(info.getJSONArray("_messages").getJSONObject(m).getJSONObject("u").getString("username"));
                mData.add(info.getJSONArray("_messages").getJSONObject(m).getString("clearText"));
                mMessage.add(info.getJSONArray("_messages").getJSONObject(m).getString("clearText"));

                initRecyclerView();
            }

            return null;
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void  initRecyclerView(){
        Log.d("MessageR","EEE");
        TaskMessage adapter = new TaskMessage(getContext(), mName, mData, mMessage);
        list_view.setAdapter(adapter);

        Log.d("MessageR","TTT");
    }

    @Override
    public void onResume(){
        super.onResume();

    }
}
