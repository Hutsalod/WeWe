package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.adapter.RecyclerViewCheck;
import chat.wewe.android.adapter.RecyclerViewTask;
import chat.wewe.android.widget.RocketChatAvatar;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class AddTaskFragment extends AbstractAddRoomDialogFragment implements NumberPicker.OnValueChangeListener{
  public String ActiveNumber = "", userId = "";
  private RecyclerView recyclerView,recyclerv_check;
  private Button buttonBlackList,addMessage;
  private LinearLayout addTask,getTask,getTasks;
  private EditText editCheak,day,time,textMessage;
  private Button closed,red,addCheck;
  private Spinner spinner,prior,spinner2,spinner3;
  private JSONArray info,users;
  private JSONObject object;
  private int position,size;
  private boolean editText = false;
  private CheckBox checkBox2;
  private ListView list_view;
  private TextView task_name,_taskText,_createdBy,date,text_day_line,text_prior,nameTaskin,msgTaskin,_responsible;
  private String[] myArray, myName,items,add;
  private ArrayList<String> mNames = new ArrayList<>();
  private ArrayList<String> mCreatedBy= new ArrayList<>();
  private ArrayList<String> mPosition = new ArrayList<>();
  private ArrayList<String> mData = new ArrayList<>();
  private ArrayList<Integer> mNumberId = new ArrayList<>();
  private ArrayList<Boolean> mClosed = new ArrayList<>();
  private ArrayList<String> mRid= new ArrayList<>();

  private ArrayList<Integer> mitemId = new ArrayList<>();
  private ArrayList<String> mitemText = new ArrayList<>();
  private ArrayList<Integer> mNumberIdCheck = new ArrayList<>();
  private ArrayList<Boolean> mcheck = new ArrayList<>();

  public AddTaskFragment() {
  }

  public static AddTaskFragment create(String hostname,String romid,String userId) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    args.putString("romid", romid);
    args.putString("userId", userId);

    AddTaskFragment fragment = new AddTaskFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_add_task;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupDialog() {
    Button buttonAddChannel = getDialog().findViewById(R.id.btn_add_channel);
    buttonBlackList = getDialog().findViewById(R.id.buttonBlackList);
    recyclerView = getDialog().findViewById(R.id.recyclerv_view);
    recyclerv_check = getDialog().findViewById(R.id.recyclerv_check);
    addTask = getDialog().findViewById(R.id.addTask);
    getTasks = getDialog().findViewById(R.id.getTasks);
    spinner = getDialog().findViewById(R.id.spinner);
    prior = getDialog().findViewById(R.id.prior);
    getTask = getDialog().findViewById(R.id.getTask);
    text_day_line = getDialog().findViewById(R.id.text_day_line);
    text_prior = getDialog().findViewById(R.id.text_prior);
    editCheak = getDialog().findViewById(R.id.editCheak);
    spinner2 = getDialog().findViewById(R.id.spinner2);
    spinner3 = getDialog().findViewById(R.id.spinner3);
    textMessage = getDialog().findViewById(R.id.textMessage);

    addMessage = getDialog().findViewById(R.id.addMessage);
    task_name = getDialog().findViewById(R.id.task_name);
    _taskText = getDialog().findViewById(R.id._taskText);
    _createdBy = getDialog().findViewById(R.id._createdBy);
    _responsible = getDialog().findViewById(R.id._responsible);
    date = getDialog().findViewById(R.id.date);
    closed = getDialog().findViewById(R.id.closed);
    red = getDialog().findViewById(R.id.red);
    addCheck = getDialog().findViewById(R.id.addCheck);
    checkBox2 = getDialog().findViewById(R.id.checkBox2);

    day = getDialog().findViewById(R.id.day);
    time = getDialog().findViewById(R.id.time);

    nameTaskin = getDialog().findViewById(R.id.nameTaskin);
    msgTaskin = getDialog().findViewById(R.id.msgTaskin);

    userId = getArguments().getString("userId");
    items = new String[]{getString(R.string.hard), getString(R.string.normal), getString(R.string.low)};

    list_view = getDialog().findViewById(R.id.list_view);

    ActiveNumber = getArguments().getString("romid");

    methodCall.getTask(ActiveNumber).onSuccessTask(task -> {
              info = task.getResult();
      for (int i = 0; i < info.length(); i++) {
        if (info.getJSONObject(i).getString("_closed").equals("false")) {
          add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy"), "от "+ new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"),info.getJSONObject(i).getString("_rid"));
        }
        }

      for (int i = 0; i < info.length(); i++) {
        if (info.getJSONObject(i).getString("_closed").equals("true")){
          add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy"), "от "+new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"),info.getJSONObject(i).getString("_rid"));
      }
      }
      initRecyclerView();

      return null;});

      methodCall.getUsersByRoomId(ActiveNumber).onSuccessTask(task -> {
      JSONArray info = task.getResult();
      String[] data = new String[info.length()+1];
      data[0] = "Нет";

      for (int i = 1; i < info.length()+1; i++) {
        data[i] = info.getJSONObject(i-1).isNull("username") ? "" : info.getJSONObject(i-1).getString("username");
      }

      prior.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items));
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, data);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      spinner.setAdapter(adapter);

      prior.setSelection(1);
      return null;});

    buttonBlackList.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          if(recyclerView.getVisibility()== GONE) {
            recyclerView.setVisibility(VISIBLE);
            addTask.setVisibility(GONE);
            getTask.setVisibility(GONE);
            checkBox2.setVisibility(VISIBLE);
            getTasks.setVisibility(GONE);
            buttonBlackList.setText(getString(R.string.add_task));
          }else{
              addTask.setVisibility(VISIBLE);
              recyclerView.setVisibility(GONE);
              checkBox2.setVisibility(GONE);
              buttonBlackList.setText(getString(R.string.back_task) );
              editText=false;
        }
        mitemId.clear();
        mitemText.clear();
        mcheck.clear();
      }
    });

    day.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showDay();

      }
    });

    time.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showHours();

      }
    });

    checkBox2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(checkBox2.isChecked()==true) {
          getTasks.setVisibility(VISIBLE);
          methodCall.getAllTaskAndUsersByUserId(userId).onSuccessTask(task -> {
              add = new String[task.getResult().getJSONArray("users").length()+1];
              add[0] = "все";
              for (int i = 1; i < task.getResult().getJSONArray("users").length()+1; i++) {
                add[i] = task.getResult().getJSONArray("users").getString(i-1);

              }
            spinner2.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, add));
            spinner3.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, add));
            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(checkBox2.isChecked()==true) {
                  restart(true);
                }else {
                  restart(false);
                }
              }

              @Override
              public void onNothingSelected(AdapterView<?> parentView) {
              }
            });
            spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(checkBox2.isChecked()==true) {
                  restart(true);
                }else {
                  restart(false);
                }
              }

              @Override
              public void onNothingSelected(AdapterView<?> parentView) {
              }
            });
            restart(true);
            return null;});
        }else{
          getTasks.setVisibility(GONE);
          restart(false);
        }

      }
    });

      addCheck.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              methodCall.addCheckListItem(ActiveNumber, userId ,position,size+1,editCheak.getText().toString()).continueWith(task -> {
                  Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

                  return null;
              });restart(false);
          }
      });

    addMessage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        methodCall.AddMsgToTask(ActiveNumber, position,textMessage.getText().toString(),userId).continueWith(task -> {
        //  Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();
          list_view.setAdapter(null);
          getTask(position);
          textMessage.setText("");
          return null;
        });


      }
    });

    closed.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          if(object.getBoolean("_closed")==false) {
            methodCall.closeTask(ActiveNumber, position, getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("UF_SIP_NUMBER", "")).continueWith(task -> {
              Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

              return null;
            });
          }else{
            methodCall.removeTask(ActiveNumber, position, userId).continueWith(task -> {
              Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

              return null;
            });
            }
        } catch (JSONException e) {
          e.printStackTrace();
        }
        restart(false);
      dismiss();
      }
    });

      red.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            editText=true;

            try {
              nameTaskin.setText(object.getString("_name"));
              msgTaskin.setText(object.getString("_taskText"));
                prior.setSelection(object.getInt("_priority"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
            addTask.setVisibility(VISIBLE);
            getTask.setVisibility(GONE);
            recyclerView.setVisibility(GONE);
            buttonBlackList.setText(getString(R.string.str_8));
            getTasks.setVisibility(GONE);
            checkBox2.setVisibility(GONE);
          }
      });



    buttonAddChannel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(editText==false)
        createRoom();
        else {
          try {
            object.put("_name",nameTaskin.getText().toString());
            object.put("_taskText",msgTaskin.getText().toString());
              object.put("_priority",prior.getSelectedItemPosition());
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }

          methodCall.updateTask(object).continueWith(task -> {
            Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();
              editText=false;
            return null;
          });dismiss();
      }
    });
    initRecyclerView();
  }

  DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {

    }
  };

  public void showDay()
  {

    final Dialog d = new Dialog(getActivity());
    d.setTitle("NumberPicker");
    d.setContentView(R.layout.dialog);
    Button b1 = (Button) d.findViewById(R.id.button1);
    final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
    np.setMaxValue(100); // max value 100
    np.setMinValue(0);   // min value 0
    np.setWrapSelectorWheel(false);
    np.setOnValueChangedListener(this);

    b1.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v) {
        day.setText(String.valueOf(np.getValue())); //set the value to textview
        d.dismiss();
      }
    });
    d.show();
  }

  public void showHours()
  {

    final Dialog d = new Dialog(getActivity());
    d.setTitle("NumberPicker");
    d.setContentView(R.layout.dialog);
    Button b1 = (Button) d.findViewById(R.id.button1);
    final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
    np.setMaxValue(24);
    np.setMinValue(0);   // min value 0
    np.setWrapSelectorWheel(false);
    np.setOnValueChangedListener(this);

    b1.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v) {
        time.setText(String.valueOf(np.getValue())); //set the value to textview
        d.dismiss();
      }
    });
    d.show();
  }

  private void add(String name,String uid,String createdBy,String data,Integer numberId,Boolean closed,String rid){
    mNames.add(name);
    mPosition.add(uid);
    mCreatedBy.add(createdBy);
    mData.add(data);
    mNumberId.add(numberId);
    mClosed.add(closed);
    mRid.add(rid);
  }

  private void addCheck(int itemId,String itemText,Boolean check,int numberId){
    mitemId.add(itemId);
    mNumberIdCheck.add(numberId);
    mitemText.add(itemText);
    mcheck.add(check);
  }

  public void initRecyclerView(){
    RecyclerViewTask adapter = new RecyclerViewTask(getContext(), mNames,mPosition,mCreatedBy,mData,mNumberId,mClosed,mRid);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter.setActionListener(new RecyclerViewTask.ActionListener() {
      @Override
      public void onClick(String uid,int position) {
        Log.d("MOSTTEST", "NO" + uid);
        methodCall.closeTask(ActiveNumber, position ,getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("UF_SIP_NUMBER", "")).continueWith(task -> {
          Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

          return null;
        });
      }

      @Override
      public void onRed(String uid, int position) {
        addTask.setVisibility(addTask.getVisibility()== VISIBLE ? GONE : VISIBLE);
        recyclerView.setVisibility(addTask.getVisibility()== VISIBLE ? GONE : VISIBLE);
        buttonBlackList.setText(addTask.getVisibility()== VISIBLE ? getString(R.string.str_8) : getString(R.string.add_task));
      }

      @Override
      public void onGet(int position,String roomId) {
        getTask(position);
        if(checkBox2.isChecked())
        mListener.onClick(roomId);
      }
    });
  }

  public void initRecyclerViewCheck(){
    RecyclerViewCheck adapter = new RecyclerViewCheck(getContext(), mitemId,mitemText,mcheck,mNumberIdCheck);
    recyclerv_check.setAdapter(adapter);
    recyclerv_check.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter.setActionListener(new RecyclerViewCheck.ActionListener() {
      @Override
      public void onClick(int position,int mNumberIdCheck,Boolean cheak) {
          methodCall.checkUncheckItem(ActiveNumber, userId, mNumberIdCheck,position,cheak).continueWith(task -> {
              Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

              return null;
          });restart(false);
      }

      @Override
      public void onClosed(int position, int cheak) {
        methodCall.removeCheckListItem(ActiveNumber, userId ,cheak,position).continueWith(task -> {
          Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

          return null;
        });restart(false);
      }
    });


  }


  @Override
  protected Task<Void> getMethodCallForSubmitAction() {
    TextView nameTaskin = getDialog().findViewById(R.id.nameTaskin);
    TextView msgTaskin = getDialog().findViewById(R.id.msgTaskin);
    TextView day = getDialog().findViewById(R.id.day);
    TextView time = getDialog().findViewById(R.id.time);
    ActiveNumber = getArguments().getString("romid");
    userId = getArguments().getString("userId");

    if (nameTaskin.getText().length()>0) {
      return   methodCall.AddTask(ActiveNumber, userId , nameTaskin.getText().toString() , msgTaskin.getText().toString() ,getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("UF_SIP_NUMBER", ""),spinner.getSelectedItem().toString(),prior.getSelectedItemPosition(),Integer.parseInt(day.getText().toString()),Integer.parseInt(time.getText().toString()));
    } else {
      return   methodCall.AddTask(ActiveNumber, userId , nameTaskin.getText().toString() , msgTaskin.getText().toString() ,getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("UF_SIP_NUMBER", ""),spinner.getSelectedItem().toString(),prior.getSelectedItemPosition(),Integer.parseInt(day.getText().toString()),Integer.parseInt(time.getText().toString()));
    }
  }

  public void getTask(int tab){
    try {

      for (int i = 0; i < info.length(); i++) {
        if(tab==info.getJSONObject(i).getInt("_numberId")) {
          object = info.getJSONObject(i);

          task_name.setText(getString(R.string.task_and) + (info.getJSONObject(i).isNull("_numberId") ? " " : info.getJSONObject(i).getString("_numberId")) + (info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name")));
          _taskText.setText(info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"));
          _createdBy.setText(getString(R.string.postav) + (info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy")));
          _responsible.setText(getString(R.string.otvetstv) + (info.getJSONObject(i).isNull("_responsible") ? " " : info.getJSONObject(i).getString("_responsible")));
          text_prior.setText(getString(R.string.prior) + (info.getJSONObject(i).isNull("_priority") ? " " : items[info.getJSONObject(i).getInt("_priority")]));
          text_day_line.setText(getString(R.string.dad_line) + getString(R.string.day)+(info.getJSONObject(i).isNull("_deadline") ? " " : info.getJSONObject(i).getJSONArray("_deadline").getString(0))+ getString(R.string.time)+(info.getJSONObject(i).isNull("_deadline") ? " " : info.getJSONObject(i).getJSONArray("_deadline").getString(1)));
          date.setText(getString(R.string.sozdana) + (new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date")))));
          if(object.getBoolean("_closed")==true) {
            closed.setText(getString(R.string.delete));
          }else {
            closed.setText(getString(R.string.closed));
          }
          if(info.getJSONObject(i).getJSONArray("_messages").length()>0) {
            myArray = new String[info.getJSONObject(i).getJSONArray("_messages").length()];
            myName = new String[info.getJSONObject(i).getJSONArray("_messages").length()];
            myArray[0] = "";
            myName[0] = "";
            for (int m = 0; m < info.getJSONObject(i).getJSONArray("_messages").length(); m++) {
            myArray[m] = info.getJSONObject(i).getJSONArray("_messages").getJSONObject(0).getJSONObject("u").getString("username")+": "+info.getJSONObject(i).getJSONArray("_messages").getJSONObject(m).getString("msg");
            myName[m] = info.getJSONObject(i).getJSONArray("_messages").getJSONObject(0).getJSONObject("u").getString("username");
            }

          list_view.setAdapter(new ZodiacAdapter(getContext(), R.layout.list_item, myArray));
          }else {
            String[] myArray = new String[0];
            list_view.setAdapter(new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, myArray));
          }
          if(info.getJSONObject(i).getJSONArray("_checklist").length()>0) {
            size = info.getJSONObject(i).getJSONArray("_checklist").length();
            for (int m = 0; m < info.getJSONObject(i).getJSONArray("_checklist").length(); m++) {
              addCheck(info.getJSONObject(i).getJSONArray("_checklist").getJSONObject(m).getInt("itemId"), info.getJSONObject(i).getJSONArray("_checklist").getJSONObject(m).getString("itemText"), info.getJSONObject(i).getJSONArray("_checklist").getJSONObject(m).getBoolean("check"),info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"));
            }
          }


          initRecyclerViewCheck();


          position = tab;
          break;
        }
      }
      checkBox2.setVisibility(GONE);
      getTasks.setVisibility(GONE);
      addTask.setVisibility(GONE);
      recyclerView.setVisibility(GONE);
      getTask.setVisibility(VISIBLE);
      buttonBlackList.setText(getString(R.string.back_task));
    } catch (JSONException e) {
      e.printStackTrace();
    }

  }



  @Override
  public void onValueChange(NumberPicker numberPicker, int i, int i1) {

  }


  private class ZodiacAdapter extends ArrayAdapter<String> {
    ZodiacAdapter(Context context, int textViewResourceId,
                  String[] objects) {
      super(context, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      LayoutInflater inflater = getLayoutInflater();
      View row = inflater.inflate(R.layout.list_item, parent, false);
      TextView label = row.findViewById(R.id.text_view_cat_name);
      label.setText(myArray[position]);
      ImageView iconImageView = row.findViewById(R.id.image_view_icon);
        new DownloadImageFromInternet(iconImageView).execute("https://chat.weltwelle.com/avatar/"+myName[position]);
      return row;
    }
  }

  private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;
    public DownloadImageFromInternet(ImageView imageView) {
      this.imageView = imageView;
    }
    protected Bitmap doInBackground(String... urls) {
      String imageURL = urls[0];
      Bitmap bimage = null;
      try {
        InputStream in = new java.net.URL(imageURL).openStream();
        bimage = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        Log.e("Error Message", e.getMessage());
        e.printStackTrace();
      }
      return bimage;
    }
    protected void onPostExecute(Bitmap result) {
      if(result==null){
        imageView.setVisibility(GONE);}
      else{
        imageView.setImageBitmap(result);
        imageView.setVisibility(VISIBLE);
      }
    }
  }

  public void restart(boolean vse){
    mNames.clear();
    mCreatedBy.clear();
    mPosition.clear();
    mData.clear();
    mNumberId.clear();
    mClosed.clear();

    mitemId.clear();
    mitemText.clear();
    mNumberIdCheck.clear();
    mcheck.clear();

      if(vse==false) {
          methodCall.getTask(ActiveNumber).onSuccessTask(task -> {
              info = task.getResult();
              for (int i = 0; i < info.length(); i++) {
                  if (info.getJSONObject(i).getString("_closed").equals("false")) {
                      add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy"), new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"),info.getJSONObject(i).getString("_rid"));
                  }
              }

              for (int i = 0; i < info.length(); i++) {
                  if (info.getJSONObject(i).getString("_closed").equals("true")) {
                      add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy"), new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"),info.getJSONObject(i).getString("_rid"));
                  }
              }
              initRecyclerView();
              return null;
          });
      }else {
          methodCall.getAllTaskAndUsersByUserId(userId).onSuccessTask(task -> {
              info = task.getResult().getJSONArray("tasks");
              for (int i = 0; i < info.length(); i++) {
                  if (info.getJSONObject(i).getString("_closed").equals("false")) {
                    if (info.getJSONObject(i).getString("_createdBy").equals(spinner3.getSelectedItem().toString()) || "все".equals(spinner3.getSelectedItem().toString())) {
                      if (info.getJSONObject(i).getString("_responsible").equals(spinner2.getSelectedItem().toString()) || "все".equals(spinner2.getSelectedItem().toString())) {
                      add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy"), new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"),info.getJSONObject(i).getString("_rid"));
                  }}}
              }

              for (int i = 0; i < info.length(); i++) {
                  if (info.getJSONObject(i).getString("_closed").equals("true")) {
                    if (info.getJSONObject(i).getString("_createdBy").equals(spinner3.getSelectedItem().toString()) || "все".equals(spinner3.getSelectedItem().toString())) {
                      if (info.getJSONObject(i).getString("_responsible").equals(spinner2.getSelectedItem().toString()) || "все".equals(spinner2.getSelectedItem().toString())) {
                      add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"), info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"), info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy"), new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))), info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"), info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"),info.getJSONObject(i).getString("_rid"));
                  }}}
              }
              initRecyclerView();
              return null;
          });
      }
  }



  public interface ActionListener{
    void onClick(String uid);
  }

  private AddTaskFragment.ActionListener mListener;

  public void setActionListener(AddTaskFragment.ActionListener listener){
    mListener = listener;
  }


}
