package chat.wewe.android.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.adapter.RecyclerViewCheck;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.sidebar.dialog.FragmentTask;

public class FragmentTaskTab extends Fragment {

    private String hostname, userId = "",_rid = "";
    private static final String HOSTNAME = "hostname",USERID = "userId";

    public  MethodCallHelper methodCall;

    private int size;

    private String index = "";

    private Button button8,button9,button6;

    private EditText editText;

    private RecyclerView recyclerv_check;

    private ArrayList<Integer> mitemId = new ArrayList<>();
    private ArrayList<String> mitemText = new ArrayList<>();
    private ArrayList<Integer> mNumberIdCheck = new ArrayList<>();
    private ArrayList<Boolean> mcheck = new ArrayList<>();

    private TextView header,description, priority,postavka,create,deadline,admin,soispoln;

    public static Fragment create(String hostname, String rid, String index) {
        Bundle args = new Bundle();
        args.putString("hostname", hostname);
        args.putString("_rid", rid);
        args.putString("index", index);
        FragmentTaskTab fragment = new FragmentTaskTab();
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_task_get, container,false);

        header = v.findViewById(R.id.textView24);
        description = v.findViewById(R.id.description);
        create = v.findViewById(R.id.create);
        postavka = v.findViewById(R.id.postavka);
        admin = v.findViewById(R.id.admin);
        priority = v.findViewById(R.id.priority);
        deadline = v.findViewById(R.id.deadline);
        soispoln = v.findViewById(R.id.soispoln);
        button8 = v.findViewById(R.id.button8);
        button9 = v.findViewById(R.id.button9);
        button6 = v.findViewById(R.id.button6);
        editText = v.findViewById(R.id.editText);


        recyclerv_check = v.findViewById(R.id.recyclerv_check);

        getTask();

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methodCall.closeTask(_rid, Integer.valueOf(index), RocketChatCache.INSTANCE.getUserName()).continueWith(task -> {
                    getTask();
                    return null;
                });
            }
        });

        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methodCall.removeTask(_rid, Integer.valueOf(index), RocketChatCache.INSTANCE.getUserName()).continueWith(task -> {
                    getTask();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.cont_item4, FragmentTask.create( RocketChatCache.INSTANCE.getSelectedServerHostname(), true)).setReorderingAllowed(false)
                            .commit();
                    return null;
                });
            }
        });


        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("MessageR",""+_rid+" "+RocketChatCache.INSTANCE.getUserId()+" "+index+" "+editText.getText().toString());
                methodCall.addCheckListItem(_rid, RocketChatCache.INSTANCE.getUserId() ,Integer.valueOf(index),size+1,editText.getText().toString()).continueWith(task -> {
                    editText.setText("");
                    getTask();
                    return null;
                });
            }
        });

        return v;
    }

    public void getTask(){

        header.setText("test");
        methodCall.getOneTask(_rid, Integer.parseInt(index)).onSuccessTask(task -> {
            final JSONObject info = task.getResult();

            if(info.getString("_priority").equals("0")) {
                priority.setVisibility(View.GONE);
            }
            if(info.getString("_priority").equals("1")) {
                priority.setTextColor(Color.parseColor("#F2994A"));
                priority.setText("Низкий приоритет");
                priority.setBackgroundResource(R.drawable.item_task_grean);
            }
            if(info.getString("_priority").equals("2")){
                priority.setBackgroundResource(R.drawable.item_button_task);
                priority.setText("Высокий приоритет");
                priority.setTextColor(Color.parseColor("#EB5757"));
            }

            header.setText("#"+ info.getInt("_numberId")+ " "+info.getString("_name"));
            description.setText(""+info.getString("_taskText"));
            postavka.setText("Постановщик: "+info.getString("_createdBy"));
            admin.setText("Ответственный: "+info.getString("_responsible"));
            if(info.getJSONArray("_assistants").length()<=0)
                soispoln.setVisibility(View.GONE);
            soispoln.setText("Соисполнители: "+info.getJSONArray("_assistants").toString());
            deadline.setText(getString(R.string.dad_line)+ " " + getString(R.string.day)+(info.isNull("_deadline") ? " " : info.getJSONArray("_deadline").getString(0))+ " " + getString(R.string.time)+(info.isNull("_deadline") ? " " : info.getJSONArray("_deadline").getString(1)));
            create.setText(getString(R.string.sozdana) + " " +  (new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject("_date").getLong("$date")))));

            if(info.getJSONArray("_checklist").length()>0) {
                size = info.getJSONArray("_checklist").length();
                for (int m = 0; m < info.getJSONArray("_checklist").length(); m++) {
                    addCheck(info.getJSONArray("_checklist").getJSONObject(m).getInt("itemId"), info.getJSONArray("_checklist").getJSONObject(m).getString("itemText"), info.getJSONArray("_checklist").getJSONObject(m).getBoolean("check"),info.isNull("_numberId") ? 0 : info.getInt("_numberId"));
                }
            }


            initRecyclerViewCheck();

            if(info.getBoolean("_closed")){
                button8.setVisibility(View.GONE);
                button9.setVisibility(View.VISIBLE);
            }else {
                button8.setVisibility(View.VISIBLE);
                button9.setVisibility(View.GONE);
            }

            return null;
        }, Task.UI_THREAD_EXECUTOR);

    }

    public void initRecyclerViewCheck(){
        RecyclerViewCheck adapter = new RecyclerViewCheck(getContext(), mitemId,mitemText,mcheck,mNumberIdCheck);
        recyclerv_check.setAdapter(adapter);
        recyclerv_check.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.setActionListener(new RecyclerViewCheck.ActionListener() {
            @Override
            public void onClick(int position,int mNumberIdCheck,Boolean cheak) {
                methodCall.checkUncheckItem(_rid, RocketChatCache.INSTANCE.getUserId(), mNumberIdCheck,position,cheak).continueWith(task -> {
                    Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();
                    getTask();
                    return null;
                });
            }

            @Override
            public void onClosed(int position, int cheak) {
                methodCall.removeCheckListItem(_rid, RocketChatCache.INSTANCE.getUserId() ,cheak,position).continueWith(task -> {
                    Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();
                    getTask();
                    return null;
                });
            }
        });


    }

    private void addCheck(int itemId,String itemText,Boolean check,int numberId){
        mitemId.add(itemId);
        mNumberIdCheck.add(numberId);
        mitemText.add(itemText);
        mcheck.add(check);
    }


    @Override
    public void onResume(){
        super.onResume();

    }
}
