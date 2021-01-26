package chat.wewe.android.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SlidingPaneLayouts;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import chat.wewe.android.BuildConfig;
import chat.wewe.android.ConnectionStatusManager;
import chat.wewe.android.LaunchUtil;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.LogApi;
import chat.wewe.android.helper.VersionApp;
import chat.wewe.android.helper.VersionChecker;
import chat.wewe.android.api.DeviceGet;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.fragment.FragmentContact;
import chat.wewe.android.fragment.call_number.FragmentKeyboard;
import chat.wewe.android.fragment.chatroom.RoomFragment;
import chat.wewe.android.fragment.setting.FragmentSetting;
import chat.wewe.android.fragment.sidebar.SidebarMainFragment;
import chat.wewe.android.fragment.sidebar.dialog.FragmentTask;
import chat.wewe.android.helper.KeyboardHelper;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android.service.ConnectivityManagerApi;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.widget.RoomToolbar;
import chat.wewe.android.widget.helper.DebouncingOnClickListener;
import chat.wewe.core.interactors.CanCreateRoomInteractor;
import chat.wewe.core.interactors.RoomInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.repositories.PublicSettingRepository;
import chat.wewe.core.utils.Pair;
import chat.wewe.persistence.realm.repositories.RealmPublicSettingRepository;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import hugo.weaving.DebugLog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class MainActivity extends AbstractAuthedActivity implements MainContract.View {
    public RoomToolbar toolbar;
    public VersionApp versionApp = new VersionApp();
    public static SlidingPaneLayouts pane;
    public BottomNavigationView navigation;
    public MainContract.Presenter presenter;
    private volatile AtomicReference<Crouton> croutonStatusTicker = new AtomicReference<>();
    private View croutonView;
    private ImageView croutonTryAgainImage;
    private TextView croutonText;
    private AnimatedVectorDrawableCompat tryAgainSpinnerAnimatedDrawable;
    private LinearLayout call, setting, contacts, task,chat;
    private Boolean codeSet = false, status = false, update = false;
    private View notificationBadge;

    //Fragments
    public FragmentContact fragmentContact = new FragmentContact ();
    public FragmentKeyboard fragmentKeyboard = new FragmentKeyboard();
    public SidebarMainFragment fragmentSidebar = new SidebarMainFragment();
    public FragmentSetting fragmentSetting = new FragmentSetting();
    public FragmentTask fragmentTask = new FragmentTask();

    @Override
    public int getLayoutContainerForFragment() {
        return R.id.activity_main_container;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.activity_main_toolbar);
        pane = findViewById(R.id.sliding_pane);
        pane.setSliderFadeColor(Color.TRANSPARENT);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        call = findViewById(R.id.call);
        setting = findViewById(R.id.setting);
        contacts = findViewById(R.id.contacts);
        task = findViewById(R.id.task);
        chat = findViewById(R.id.chat);
        navigation.setSelectedItemId(R.id.action_chat);
        getSupportFragmentManager().executePendingTransactions();
        loadCroutonViewIfNeeded();
        setupToolbar();
        startSetting();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManagerApi connectivityManager = ConnectivityManager.getInstance(getApplicationContext());
        if (hostname == null || presenter == null) {
            String previousHostname = hostname;
            hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
            if (hostname == null) {
                showAddServerScreen();
            } else {
                onHostnameUpdated();
                if (!hostname.equalsIgnoreCase(previousHostname)) {
                    connectivityManager.resetConnectivityStateList();
                    connectivityManager.keepAliveServer();
                }
            }
        } else {
            connectivityManager.keepAliveServer();
            if(pane.isOpen())
            presenter.bindView(this);
            presenter.loadSignedInServers(hostname);
            roomId = RocketChatCache.INSTANCE.getSelectedRoomId();
            pane.setVisibility(VISIBLE);

        }
        RocketChatApplication.activityResumed();
    }

    @Override
    protected void onDestroy() {

        RocketChatApplication.activityPaused();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (presenter != null) {
            presenter.release();
        }
        Crouton.cancelAllCroutons();
        super.onPause();

    }

    @Override
    protected void onStop() {
        RocketChatApplication.activityPaused();
        super.onStop();
        if(codeSet==true) {
            String code = getSharedPreferences("pin", MODE_PRIVATE).getString("code", "");
            if (code != "") {
                codeSet = false;
                startActivity(new Intent(getApplication(), PinCodeLong.class));
            }
        }
        codeSet=true;
    }

    private void setupToolbar() {
        if (pane != null) {
            pane.setPanelSlideListener(new SlidingPaneLayouts.PanelSlideListener() {
                @Override
                public void onPanelSlide(@NonNull View view, float v) {
                    //Ref: ActionBarDrawerToggle#setProgress
                    // toolbar.setNavigationIconProgress(v);
                }

                @Override
                public void onPanelOpened(@NonNull View view) {
                    toolbar.setNavigationIconVerticalMirror(true);
                }
                @Override
                public void onPanelClosed(@NonNull View view) {


                    toolbar.setNavigationIconVerticalMirror(false);
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentById(R.id.sidebar_fragment_container);
                    if (fragment != null && fragment instanceof SidebarMainFragment) {
                        SidebarMainFragment sidebarMainFragment = (SidebarMainFragment) fragment;
                        sidebarMainFragment.toggleUserActionContainer(false);
                        sidebarMainFragment.showUserActionContainer(false);
                    }
                }
            });

            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(view -> {
                    if (pane.isSlideable() && !pane.isOpen()) {
                        pane.openPane();
                    }

                });
            }
        }
        closeSidebarIfNeeded();
    }

    private boolean closeSidebarIfNeeded() {
        if (pane != null && pane.isSlideable() && pane.isOpen()) {
         //   pane.closePane();
            return true;
        }
        return false;
    }

    @DebugLog
    @Override
    protected void onHostnameUpdated() {
        super.onHostnameUpdated();

        if (presenter != null) {
            presenter.release();
        }

        RoomInteractor roomInteractor = new RoomInteractor(new RealmRoomRepository(hostname));
        CanCreateRoomInteractor createRoomInteractor = new CanCreateRoomInteractor(
                new RealmUserRepository(hostname),
                new SessionInteractor(new RealmSessionRepository(hostname))
        );
        SessionInteractor sessionInteractor = new SessionInteractor(
                new RealmSessionRepository(hostname)
        );
        PublicSettingRepository publicSettingRepository = new RealmPublicSettingRepository(hostname);
        presenter = new MainPresenter(
                roomInteractor,
                createRoomInteractor,
                sessionInteractor,
                new MethodCallHelper(this, hostname),
                ConnectivityManager.getInstance(getApplicationContext()),
                publicSettingRepository
        );
        updateSidebarMainFragment();
        presenter.bindView(this);
        presenter.loadSignedInServers(hostname);
        roomId = RocketChatCache.INSTANCE.getSelectedRoomId();
    }

    private void updateSidebarMainFragment() {
        closeSidebarIfNeeded();

        String selectedServerHostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        Fragment sidebarFragment = findFragmentByTag(selectedServerHostname);
        if (sidebarFragment == null) {
            fragmentSidebar = SidebarMainFragment.create(selectedServerHostname);
            sidebarFragment = fragmentSidebar;
        }
            getSupportFragmentManager().beginTransaction().replace(R.id.sidebar_fragment_container, sidebarFragment, selectedServerHostname).commit();
            fragmentSetting = FragmentSetting.create(RocketChatCache.INSTANCE.getSelectedServerHostname(), RocketChatCache.INSTANCE.getUserId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.cont_item3, fragmentSetting)
                    .replace(R.id.cont, fragmentContact)
                    .replace(R.id.cont_item2, fragmentKeyboard)
                    .commit();

            fragmentTask = FragmentTask.create(RocketChatCache.INSTANCE.getSelectedServerHostname(), status);
            getSupportFragmentManager().beginTransaction().replace(R.id.cont_item4, fragmentTask).commit();

    }

    @Override
    protected void onRoomIdUpdated() {
        super.onRoomIdUpdated();
        presenter.onOpenRoom(hostname, roomId);

    }

    @Override
    protected boolean onBackPress() {
        return closeSidebarIfNeeded() || super.onBackPress();
    }

    @Override
    public void showHome() {
        if (pane != null) {
       //     pane.openPane();
        }

    }

    @Override
    public void showRoom(String hostname, String roomId) {
        showFragment(RoomFragment.create(hostname, roomId));
        closeSidebarIfNeeded();
        KeyboardHelper.hideSoftKeyboard(this);
    }

    @Override
    public void showUnreadCount(long roomsCount, int mentionsCount) {
        toolbar.setUnreadBadge((int) roomsCount, mentionsCount);
    }

    @Override
    public void showAddServerScreen() {
        LaunchUtil.showAddServerActivity(this);
    }

    @Override
    public void showLoginScreen() {
        codeSet=false;
        LaunchUtil.showLoginActivity(this, hostname);
        showConnectionOk();
    }

    @Override
    public void showConnectionError() {
        fragmentContact.noConnect();
        fragmentKeyboard.noConnect();
        fragmentSidebar.noConnect();
        fragmentSetting.noConnect();
        fragmentTask.noConnect();
        status = false;
        retryConnectionTime();
    }

    @Override
    public void showConnecting() {
        status = true;
        fragmentContact.Connecting();
        fragmentKeyboard.Connecting();
        fragmentSidebar.Connecting();
        fragmentSetting.Connecting();
        fragmentTask.Connecting();
    }

    @Override
    public void showConnectionOk() {
        status = true;
        ConnectionStatusManager.INSTANCE.setOnline(this::dismissStatusTickerIfShowing);
        fragmentContact.okConnect();
        fragmentKeyboard.okConnect();
        fragmentSidebar.okConnect();
        fragmentSetting.okConnect();
        fragmentTask.okConnect();

    }


    private void loadCroutonViewIfNeeded() {
        if (croutonView == null) {
            croutonView = LayoutInflater.from(this).inflate(R.layout.crouton_status_ticker, null);
            croutonTryAgainImage = croutonView.findViewById(R.id.try_again_image);
            croutonText = croutonView.findViewById(R.id.text_view_status);
            tryAgainSpinnerAnimatedDrawable =
                    AnimatedVectorDrawableCompat.create(this, R.drawable.ic_loading_animated);
            croutonTryAgainImage.setImageDrawable(tryAgainSpinnerAnimatedDrawable);
            updateCrouton();
        }
    }

    private void updateCrouton() {
        Configuration configuration = new Configuration.Builder()
                .setDuration(Configuration.DURATION_INFINITE).build();

        Crouton crouton = Crouton.make(this, croutonView, getLayoutContainerForFragment())
                .setConfiguration(configuration);
        croutonStatusTicker.set(crouton);

    }

    private void dismissStatusTickerIfShowing(boolean success) {
        if (success && croutonStatusTicker.get() != null) {
            croutonStatusTicker.get().hide();
        }
    }

    private void retryConnection() {
        croutonStatusTicker.set(null);
        ConnectivityManager.getInstance(getApplicationContext()).keepAliveServer();
    }

    @Override
    public void showSignedInServers(List<Pair<String, Pair<String, String>>> serverList) {

    }

    @Override
    public void refreshRoom() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(getLayoutContainerForFragment());
        if (fragment != null && fragment instanceof RoomFragment) {
            RoomFragment roomFragment = (RoomFragment) fragment;
            roomFragment.loadMissedMessages();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_chat:
                    call.setVisibility(GONE);
                    contacts.setVisibility(GONE);
                    task.setVisibility(GONE);
                    setting.setVisibility(GONE);
                    chat.setVisibility(VISIBLE);
                    return true;
                case R.id.action_call:
                    chat.setVisibility(GONE);
                    contacts.setVisibility(GONE);
                    task.setVisibility(GONE);
                    setting.setVisibility(GONE);
                    call.setVisibility(VISIBLE);
                    return true;
                case R.id.action_group:
                    chat.setVisibility(GONE);
                    call.setVisibility(GONE);
                    task.setVisibility(GONE);
                    setting.setVisibility(GONE);
                    contacts.setVisibility(VISIBLE);
                    return true;
                case R.id.action_task:
                    chat.setVisibility(GONE);
                    call.setVisibility(GONE);
                    contacts.setVisibility(GONE);
                    setting.setVisibility(GONE);
                    task.setVisibility(VISIBLE);
                    fragmentTask = FragmentTask.create(RocketChatCache.INSTANCE.getSelectedServerHostname(),status);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.cont_item4, fragmentTask).commit();
                    return true;
                case R.id.action_setting:
                    chat.setVisibility(GONE);
                    call.setVisibility(GONE);
                    contacts.setVisibility(GONE);
                    task.setVisibility(GONE);
                    setting.setVisibility(VISIBLE);

                    if (update == true) {
                        versionApp.showUpdate(MainActivity.this).show();
                        update = false;
                        notificationBadge.setVisibility(GONE);
                    }
                    return true;
            }

            return false;
        }
    };

    @DebugLog
    public void onLogout() {
        presenter.prepareToLogout();
        codeSet=false;
        if (RocketChatCache.INSTANCE.getSelectedServerHostname() == null) {
            finish();
            LaunchUtil.showMainActivity(this);
        } else {
            onHostnameUpdated();
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (navigation.getSelectedItemId() == R.id.action_chat && pane.isOpen() == true) {
                        finish();
                    } else {
                        navigation.setSelectedItemId(R.id.action_chat);
                        pane.openPane();

                    }
                    return false;
            }

        }
        return super.dispatchKeyEvent(event);
    }

    private void sendSettings(String token){
        UtilsApi.getAPIService().getSettings("KEY:"+token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getString("status").equals("200")){
                                    new LogApi().logPost(token);
                                    if (jsonRESULTS.getJSONObject("result").getString("INNER_GROUP").equals("true")) {
                                        getSharedPreferences("Sub", Context.MODE_PRIVATE).edit().putBoolean("SubApi", true).commit();
                                      startServiceSip();
                                    }else if (getSharedPreferences("Sub", MODE_PRIVATE).getBoolean("Sub", false)==true){
                                      startServiceSip();
                                    } else  {
                                        getSharedPreferences("Sub", Context.MODE_PRIVATE).edit().putBoolean("SubApi", false).commit();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });

    }



    public void startServiceSip(){
        if (getSharedPreferences("SIP", Context.MODE_PRIVATE).getString("UF_SIP_NUMBER", null) != null){
            Intent onLineIntent = new Intent(this, PortSipService.class);
            onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(onLineIntent);
            } else {
                startService(onLineIntent);
            }
        }

    }

    public void bindView() {
        presenter.bindView(this);
    }


    public void getVersion(){
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(4);
        notificationBadge = LayoutInflater.from(this).inflate( R.layout.view_notification_badge, menuView, false);
        try {
            String versionApp = getPackageManager().getPackageInfo(getPackageName(),0).versionName.substring(0, 5);
            String latestVersion = new VersionChecker().execute().get();
            if(!versionApp.contains(latestVersion) && Calendar.getInstance()
                    .get(Calendar.DAY_OF_MONTH)!=getSharedPreferences("vDay", MODE_PRIVATE).getInt("vDay",0)) {
                if (latestVersion != "0.0.0") {
                    update = true;
                    itemView.addView(notificationBadge);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void retryConnectionTime(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (status == false)
                    retryConnection();
            }
        }, 15000);
    }

    public void startSetting(){
        if(RocketChatCache.INSTANCE.getSessionToken() != null){
            String code = getSharedPreferences("pin", MODE_PRIVATE).getString("code", "");
            if (code != "")
            startActivity(new Intent(getApplication(), PinCodeLong.class));
            sendSettings(getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""));
            getVersion();
            DeviceGet.getDevice(getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""));
        }

    }


}
