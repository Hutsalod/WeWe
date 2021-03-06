package chat.wewe.android.fragment.sidebar.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.activity.AddTaskActivity;
import chat.wewe.android.activity.StatusConnect;
import chat.wewe.android.adapter.AdapterTask;
import chat.wewe.android.adapter.AdapterTaskList;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.AbstractFragment;
import chat.wewe.android.fragment.FragmentGetTask;
import chat.wewe.android.fragment.sidebar.SidebarMainContract;
import chat.wewe.android.widget.WaitingView;
import icepick.State;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;



public class FragmentTask extends AbstractFragment implements StatusConnect {

  @State
  protected String hostname;
  @State
  protected String userId;

  public String ActiveNumber = "",  name = "";
  private RecyclerView recyclerView;
  private Spinner room_filtr,user_filtr;
  private JSONArray info,users;

  private LinearLayout filtr_layaut;

  private ArrayList<String>  user = new ArrayList<>(), add = new ArrayList<>(), room = new ArrayList<>();

  private WaitingView waiting;

  public ArrayList<AdapterTaskList> adapter;

  public ArrayList<AdapterTaskList> adapterTaskList = new ArrayList<>();


  private RecyclerView.LayoutManager mLayoutManager;
  private Boolean status;

  public  MethodCallHelper methodCall;
  public ImageView addTaskActivity,filtr_button,stanUsers;
  private static final String HOSTNAME = "hostname";
  TextView testUsers;


  public FragmentTask() {
  }

  public static FragmentTask create(String hostname, Boolean status) {

    Bundle args = new Bundle();
    args.putString(HOSTNAME, hostname);
    args.putBoolean("status", status);

    FragmentTask fragment = new FragmentTask();
    fragment.setArguments(args);

    return fragment;
  }


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    userId =  RocketChatCache.INSTANCE.getUserId();

    hostname = getArguments().getString(HOSTNAME);
    status = getArguments().getBoolean("status");

    methodCall = new MethodCallHelper(getContext(), hostname);

  }


  @Override
  protected int getLayout() {
    return 0;
  }

  @Override
  protected void onSetupView() {

  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_task,  null);


    recyclerView = v.findViewById(R.id.recyclerv_view);
    filtr_layaut = v.findViewById(R.id.filtr);
    addTaskActivity = v.findViewById(R.id.addTaskActivity);
    filtr_button = v.findViewById(R.id.filtr_button);
    waiting = v.findViewById(R.id.waiting);
    room_filtr = v.findViewById(R.id.room_filtr);
    user_filtr = v.findViewById(R.id.user_filtr);
    testUsers = v.findViewById(R.id.testUsers);
    stanUsers = v.findViewById(R.id.stanUsers);

    if(status==true)
      this.stanUsers.setImageResource(R.drawable.s222);
    else
      this.stanUsers.setImageResource(R.drawable.s000);

    recyclerView.setNestedScrollingEnabled(false);
    mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
    showSpiner();
    showTasks();

    addTaskActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(getActivity(), AddTaskActivity.class).putExtra("hostname",hostname).putExtra("userId",userId));
      }
    });


    filtr_button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        filtr();
      }
    });

    return  v;
  }

  @Override
  public void onResume() {
    super.onResume();
    showSpiner();
    showTasks();
  }



  public void filtr(){
    if(filtr_layaut.getVisibility()==VISIBLE) {
      filtr_layaut.setVisibility(GONE);
    }else {
      filtr_layaut.setVisibility(VISIBLE);
    }
    showTasks();
  }


  private void showSpiner(){
    methodCall.rooms_get().continueWith(task -> {
      final JSONArray roomRoles = task.getResult();
      user.add( "Нет");
      for (int i = 0; i < task.getResult().length(); i++) {
        if(!roomRoles.getJSONObject(i).isNull("t")) {
     //     if (roomRoles.getJSONObject(i).getString("t").contains("p")|| roomRoles.getJSONObject(i).getString("t").contains("c"))
            if (!roomRoles.getJSONObject(i).isNull("u")) {
              add.add(roomRoles.getJSONObject(i).getString("fname"));
              room.add(roomRoles.getJSONObject(i).getString("rid"));
            }

          if (roomRoles.getJSONObject(i).getString("t").contains("d"))
            if (!roomRoles.getJSONObject(i).isNull("u")) {
              user.add(roomRoles.getJSONObject(i).getString("name"));
            }
        }
      }

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, add);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      room_filtr.setAdapter(adapter);

      ArrayAdapter<String> users = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, user);
      users.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      user_filtr.setAdapter(users);

      user_filtr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
          adapterTaskList.clear();
            showTasks();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
        }
      });

      room_filtr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
          adapterTaskList.clear();
          showTasks();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
        }
      });


      return null;
    }, Task.UI_THREAD_EXECUTOR);
  }

  public void showTasks() {
    Log.d("WEWE_Q","TASKS !");

    waiting.setVisibility(VISIBLE);
    methodCall.getAllTaskAndUsersByUserId(userId).continueWithTask(task -> {
      Log.d("TEST_WEWE","TASKS !"+task.getResult().getJSONArray("tasks").toString());

      info = task.getResult().getJSONArray("tasks");
      Log.d("TEST_WEWE","TASKS !"+info);

      for (int i = 0; i < info.length(); i++) {
        Log.d("TEST_WEWE","TASKS !");
        if (info.getJSONObject(i).getString("_closed").equals("false")) {
          Log.d("TEST_WEWE","TASKS !");
          if(filtr_layaut.getVisibility()==VISIBLE){
            Log.d("TEST_WEWE","TASKS !");
            if(info.getJSONObject(i).getString("_rid").contains(room.get(room_filtr.getSelectedItemPosition())) )
              if(user_filtr.getSelectedItemId()!=0) {
                Log.d("TEST_WEWE","TASK !"+room_filtr.getSelectedItem().toString().contains(info.getJSONObject(i).getString("_createdBy")));
                Log.d("TEST_WEWE","TASKS !"+info);
                if (room_filtr.getSelectedItem().toString().contains(info.getJSONObject(i).getString("_createdBy")))
                add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : "Ответственный: " + info.getJSONObject(i).getString("_createdBy"), "от " + new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"), info.getJSONObject(i).isNull("_rid")  ? "" : info.getJSONObject(i).getString("_rid"), info.getJSONObject(i).isNull("_priority")  ? "" : info.getJSONObject(i).getString("_priority"));
              }else{

                add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : "Ответственный: " + info.getJSONObject(i).getString("_createdBy"), "от " + new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"), info.getJSONObject(i).isNull("_rid")  ? "" : info.getJSONObject(i).getString("_rid"), info.getJSONObject(i).isNull("_priority")  ? "" : info.getJSONObject(i).getString("_priority"));
              }

          }else{
            add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : "Ответственный: " + info.getJSONObject(i).getString("_createdBy"), "от " + new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"), info.getJSONObject(i).isNull("_rid")  ? "" : info.getJSONObject(i).getString("_rid"), info.getJSONObject(i).isNull("_priority")  ? "" : info.getJSONObject(i).getString("_priority"));
          }
          initRecyclerView();
        }

      }
      Log.d("TEST_WEWE","TASKS  2!");
      for (int i = 0; i < info.length(); i++) {
        if (info.getJSONObject(i).getString("_closed").equals("true")){
          if(filtr_layaut.getVisibility()==VISIBLE){
            if(info.getJSONObject(i).getString("_rid").contains(room.get(room_filtr.getSelectedItemPosition())))
              Log.d("TEST_WEWE","TASKS !");
              if(user_filtr.getSelectedItemId()!=0)
                Log.d("TEST_WEWE","TASK !"+room_filtr.getSelectedItem().toString().contains(info.getJSONObject(i).getString("_createdBy")));

            if (room_filtr.getSelectedItem().toString().contains(info.getJSONObject(i).getString("_createdBy")))

              add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : "Ответственный: " + info.getJSONObject(i).getString("_createdBy"), "от " + new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"), info.getJSONObject(i).isNull("_rid")  ? "" : info.getJSONObject(i).getString("_rid"), info.getJSONObject(i).isNull("_priority")  ? "" : info.getJSONObject(i).getString("_priority"));


          }else {
            add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : "Ответственный: " + info.getJSONObject(i).getString("_createdBy"), "от " + new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"), info.getJSONObject(i).isNull("_rid")  ? "" : info.getJSONObject(i).getString("_rid"), info.getJSONObject(i).isNull("_priority")  ? "" : info.getJSONObject(i).getString("_priority"));
          }
          initRecyclerView();
          }

      }
      waiting.setVisibility(GONE);




      return null;
    }, Task.UI_THREAD_EXECUTOR);
  }

  private void add(String text1, String text2, String text3, String text4,int set,Boolean close, String rid,String priority){
    adapterTaskList.add(new AdapterTaskList(text1,text2,text3,  text4, set, close,  rid, priority));
  }

  public void initRecyclerView(){
    AdapterTask adaptert = new AdapterTask(adapterTaskList);
    recyclerView.setAdapter(adaptert);
    recyclerView.setLayoutManager(mLayoutManager);
    adaptert.setActionListener(new AdapterTask.ActionListener() {
      @Override
      public void onGet(String position, String rid) {

   getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.cont_item4, FragmentGetTask.create(RocketChatCache.INSTANCE.getSelectedServerHostname(),rid,position,status))
                .commit();
      }
    });
  }

  @Override
  public void noConnect() {
    this.stanUsers.setImageResource(R.drawable.s000);
  }

  @Override
  public void Connecting() {
    this.stanUsers.setImageResource(R.drawable.s112);
  }

  @Override
  public void okConnect() {
    this.stanUsers.setImageResource(R.drawable.s222);
  }

  public interface ActionListener{
    void onClick(String uid);
  }

  private FragmentTask.ActionListener mListener;

  public void setActionListener(FragmentTask.ActionListener listener){
    mListener = listener;
  }


}
