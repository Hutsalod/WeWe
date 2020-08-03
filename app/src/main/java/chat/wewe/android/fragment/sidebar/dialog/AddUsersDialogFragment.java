package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.persistence.realm.RealmHelper;

public class AddUsersDialogFragment extends AbstractAddRoomDialogFragment {

    public String ActiveNumber = "", userId = "";
    private int type ;

    protected RealmHelper realmHelper;
    public AddUsersDialogFragment() {
    }

  public static AddUsersDialogFragment create(String hostname,String romid,String userId,int type) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    args.putString("romid", romid);
    args.putString("userId", userId);
    args.putInt("type", type);

    AddUsersDialogFragment fragment = new AddUsersDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_add_users;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupDialog() {

      userId = getArguments().getString("userId");
      type = getArguments().getInt("type");
      getMethodCall();

  }

    @Override
    protected Task<Void> getMethodCallForSubmitAction() {
        return null;
    }


    protected Task<Void> getMethodCall() {
      ListView lvMain =  getDialog().findViewById(R.id.list_view);

      if(type==0) {
          return   methodCall.getAllTaskAndUsersByUserId(userId).onSuccessTask(task -> {

              String[] add = new String[task.getResult().getJSONArray("users").length()];
              for (int i = 0; i < task.getResult().getJSONArray("users").length(); i++) {
                  add[i] = task.getResult().getJSONArray("users").getString(i);
                  Log.d("TEST_WEWE", "USER" + task.getResult().getJSONArray("users").getString(i));
              }

              ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                      R.layout.list_item_users, add);

              lvMain.setAdapter(adapter);

              lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  public void onItemClick(AdapterView<?> parent, View view,
                                          int position, long id) {
                      mListener.onClick(lvMain.getItemAtPosition(position).toString());
                  }
              });

              return null;
          }, Task.UI_THREAD_EXECUTOR);
      }else{
      return methodCall.rooms_get().continueWith(task -> {
          final JSONArray roomRoles = task.getResult();
          String[] add = new String[task.getResult().length()];
          String[] room = new String[task.getResult().length()];
          for (int i = 0; i < task.getResult().length(); i++) {
              if(!roomRoles.getJSONObject(i).isNull("u")) {
                  add[i] = roomRoles.getJSONObject(i).getString("name");
                  room[i] = roomRoles.getJSONObject(i).getString("rid");
                  Log.d("TEST_WEWE",""+roomRoles.getJSONObject(i).getString("name"));
              }
          }

          ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                  R.layout.list_item_users, add);

          lvMain.setAdapter(adapter);
          lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              public void onItemClick(AdapterView<?> parent, View view,
                                      int position, long id) {
                  mListener.onClick(room[position]);
                  dismiss();
              }
          });

          return null;
      }, Task.UI_THREAD_EXECUTOR);}

  }

    public interface ActionListener{
        void onClick(String uid);
    }

    private ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }

}


