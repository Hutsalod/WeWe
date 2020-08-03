package chat.wewe.android.fragment;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.activity.ContactAdapter;
import chat.wewe.android.activity.ContactModel;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.adapter.TabAdapter;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.sidebar.dialog.FragmentTask;

public class FragmentGetTask extends Fragment {

    private List<ContactModel> contactModelList = new ArrayList<>();
    private  String[] PERMISSION_CONTACT = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
    public TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView taskId;
    private String hostname, userId = "",_rid = "",index = "";
    private Toolbar toolbar;
    public  MethodCallHelper methodCall;
    private String[] name, messages;
    private static final String HOSTNAME = "hostname",USERID = "userId";


    public static Fragment create(String hostname, String _rid,String index) {
        Bundle args = new Bundle();
        args.putString("hostname", hostname);
        args.putString("_rid", _rid);
        args.putString("index", index);
        FragmentGetTask fragment = new FragmentGetTask();
        fragment.setArguments(args);
        return fragment;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hostname = getArguments().getString(HOSTNAME);
        _rid = getArguments().getString("_rid");
        index = getArguments().getString("index");
        methodCall = new MethodCallHelper(getContext(), hostname);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_get_task, container, false);

        viewPager = v.findViewById(R.id.viewPager);
        tabLayout = v.findViewById(R.id.tabLayout);
        toolbar =  v.findViewById(R.id.toolbar);
        taskId =  v.findViewById(R.id.taskId);
        taskId.setText("Задача #"+index);

        adapter = new TabAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(FragmentTaskTab.create(hostname, _rid,index),"Описание");
        adapter.addFragment(FragmentTaskMessage.create(hostname, _rid,index),"Сообщения");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        toolbar.setNavigationIcon(R.drawable.ic_back_vector);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.cont_item4, FragmentTask.create( RocketChatCache.INSTANCE.getSelectedServerHostname())).setReorderingAllowed(false)
                        .commit();
            }
        });

        return v;
    }






    @Override
    public void onResume(){
        super.onResume();

    }



}
