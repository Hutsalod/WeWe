package chat.wewe.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.chip.Chip;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.FragmentGetTask;
import chat.wewe.android.fragment.sidebar.dialog.FragmentTask;

public class AddTaskActivity extends AppCompatActivity {

    private  TextInputEditText name_edit_text,message,day,time;
    private Spinner prior,chips_input,soispoln,chips_room;
    private JSONArray info,users;
    private JSONObject object;

    private Button createTask;

    private ArrayList<String> add = new ArrayList<>(), room = new ArrayList<>();

    private  MethodCallHelper methodCall;
    private String hostname, userId = "";
    private static final String HOSTNAME = "hostname",USERID = "userId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        String hostname = intent.getStringExtra("hostname");
        String userId = intent.getStringExtra("userId");
        String name = intent.getStringExtra("name");
        methodCall = new MethodCallHelper(getApplicationContext(), hostname);

        setContentView(R.layout.activity_task);
        Toolbar toolbar =  findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        name_edit_text = findViewById(R.id.name_edit_text);
        prior = findViewById(R.id.prior);
        soispoln = findViewById(R.id.soispoln);
        chips_room = findViewById(R.id.chips_room);
        chips_input = findViewById(R.id.chips_input);
        message = findViewById(R.id.message);
        day = findViewById(R.id.day);
        time = findViewById(R.id.time);

        createTask = findViewById(R.id.createTask);

        String[] items = new String[]{getString(R.string.hard), getString(R.string.normal), getString(R.string.low)};

        prior.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items));

        methodCall.rooms_get().onSuccessTask(task -> {
            JSONArray info = task.getResult();
            String[] data = new String[info.length()+1];
            data[0] = "Нет";

            for (int i = 1; i < info.length()+1; i++) {
                data[i] = info.getJSONObject(i-1).isNull("fname") ? "" : info.getJSONObject(i-1).getString("fname");
            }

            soispoln.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, data));
            prior.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, data);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            chips_input.setAdapter(adapter);

            prior.setSelection(1);
            soispoln.setSelection(1);
            return null;}, Task.UI_THREAD_EXECUTOR);


        methodCall.rooms_get().continueWith(task -> {
            final JSONArray roomRoles = task.getResult();


            for (int i = 0; i < task.getResult().length(); i++) {
                if(!roomRoles.getJSONObject(i).isNull("t")) {
                 //   if (roomRoles.getJSONObject(i).getString("t").contains("p") || roomRoles.getJSONObject(i).getString("t").contains("c"))

                        if (!roomRoles.getJSONObject(i).isNull("u")) {
                            add.add(roomRoles.getJSONObject(i).getString("fname"));
                            room.add(roomRoles.getJSONObject(i).getString("rid"));
                            Log.d("TEST_WEWE", "" + roomRoles.getJSONObject(i).getString("name"));
                        }
                }
            }


          ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, add);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            chips_room.setAdapter(adapter);

            return null;
        }, Task.UI_THREAD_EXECUTOR);

        createTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                methodCall.AddTask(room.get(chips_room.getSelectedItemPosition())
                        , RocketChatCache.INSTANCE.getUserId()
                        , name_edit_text.getText().toString()
                        , message.getText().toString()
                        , RocketChatCache.INSTANCE.getUserName()
                        , chips_input.getSelectedItem().toString()
                        , prior.getSelectedItemPosition()
                        , Integer.parseInt(day.getText().toString().isEmpty() ?  "0" : day.getText().toString() )
                        , Integer.parseInt(time.getText().toString().isEmpty() ?  "0" : day.getText().toString()))

                        .continueWith(task -> {
                    finish();
                    return null;
                });
            }
        });




    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return false;
    }
}
