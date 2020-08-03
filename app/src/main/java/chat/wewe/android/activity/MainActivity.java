package chat.wewe.android.activity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SlidingPaneLayouts;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import chat.wewe.android.ConnectionStatusManager;
import chat.wewe.android.LaunchUtil;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.fragment.FragmentContact;
import chat.wewe.android.fragment.call_number.FragmentKeyboard;
import chat.wewe.android.fragment.chatroom.HomeFragment;
import chat.wewe.android.fragment.chatroom.RoomFragment;
import chat.wewe.android.fragment.sidebar.FragmentSetting;
import chat.wewe.android.fragment.sidebar.SidebarMainFragment;
import chat.wewe.android.fragment.sidebar.dialog.FragmentTask;
import chat.wewe.android.helper.KeyboardHelper;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android.service.ConnectivityManagerApi;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.widget.RoomToolbar;
import chat.wewe.android.widget.helper.DebouncingOnClickListener;
import chat.wewe.android.widget.helper.FrescoHelper;
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
    public SlidingPaneLayouts pane;
    private MainContract.Presenter presenter;
    private volatile AtomicReference<Crouton> croutonStatusTicker = new AtomicReference<>();
    private View croutonView;
    private ImageView croutonTryAgainImage;
    private TextView croutonText;
    private AnimatedVectorDrawableCompat tryAgainSpinnerAnimatedDrawable;
    private LinearLayout call, setting, contacts, task,chat;
    public BottomNavigationView navigation;
    private Boolean codeSet = false;
    private SharedPreferences Preferences;


    //Fragments
    FragmentContact fragmentContact = new FragmentContact ();
    FragmentKeyboard fragmentKeyboard = new FragmentKeyboard();
    SidebarMainFragment fragmentSidebar = new SidebarMainFragment();
    FragmentSetting fragmentSetting = new FragmentSetting();
    FragmentTask fragmentTask = new FragmentTask();



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

        getSupportFragmentManager().executePendingTransactions();

        loadCroutonViewIfNeeded();
        setupToolbar();

        navigation.setSelectedItemId(R.id.action_chat);

        Preferences = getSharedPreferences("pin", MODE_PRIVATE);
        String code = Preferences.getString("code", "");
        if (!code.equals(""))
            startActivity(new Intent(getApplication(), PinCodeLong.class));

        if(!getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", "").equals("")){
        getSettings(getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""));
        }


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
            presenter.bindView(this);
            presenter.loadSignedInServers(hostname);
            roomId = RocketChatCache.INSTANCE.getSelectedRoomId();

        }
        RocketChatApplication.activityResumed();
    }

    @Override
    protected void onPause() {
        if (presenter != null) {
            presenter.release();
        }
        Crouton.cancelAllCroutons();
        if(codeSet==true) {
            String code = Preferences.getString("code", "");
            if (!code.equals(""))
                startActivity(new Intent(getApplication(), PinCodeLong.class));
        }codeSet=true;
        super.onPause();
        RocketChatApplication.activityPaused();
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
        // REMARK: Tablet UI doesn't have SlidingPane!
        if (pane != null && pane.isSlideable() && pane.isOpen()) {
            pane.closePane();
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sidebar_fragment_container, sidebarFragment, selectedServerHostname)
                .commit();

        fragmentSetting = FragmentSetting.create(RocketChatCache.INSTANCE.getSelectedServerHostname(), RocketChatCache.INSTANCE.getUserId());
        getSupportFragmentManager().beginTransaction().replace(R.id.cont_item3, fragmentSetting)
                .commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.cont, fragmentContact).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.cont_item2, fragmentKeyboard).commit();



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
            if (pane.isOpen()) {
                pane.openPane();
            } else {
                pane.openPane();
            }
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
        LaunchUtil.showLoginActivity(this, hostname);
        showConnectionOk();
    }

    @Override
    public void showConnectionError() {
        ConnectionStatusManager.INSTANCE.setConnectionError(this::showConnectionErrorCrouton);
        fragmentContact.noConnect();
        fragmentKeyboard.noConnect();
        fragmentSidebar.noConnect();
        fragmentSetting.noConnect();
        fragmentTask.noConnect();
    }

    @Override
    public void showConnecting() {
        ConnectionStatusManager.INSTANCE.setConnecting(this::showConnectingCrouton);
        fragmentContact.okConnect();
        fragmentKeyboard.okConnect();
        fragmentSidebar.okConnect();
        fragmentSetting.okConnect();
        fragmentTask.okConnect();
    }

    @Override
    public void showConnectionOk() {
        ConnectionStatusManager.INSTANCE.setOnline(this::dismissStatusTickerIfShowing);
        fragmentContact.okConnect();
        fragmentKeyboard.okConnect();
        fragmentSidebar.okConnect();
        fragmentSetting.okConnect();
        fragmentTask.okConnect();
    }

    private void showConnectingCrouton(boolean success) {
        if (success) {
            croutonText.setText(R.string.server_config_activity_authenticating);
            croutonTryAgainImage.setOnClickListener(null);
            tryAgainSpinnerAnimatedDrawable.start();
            Crouton.cancelAllCroutons();
            updateCrouton();
            croutonStatusTicker.get().show();
        }
    }

    private void showConnectionErrorCrouton(boolean success) {
        if (success) {
            tryAgainSpinnerAnimatedDrawable.stop();
            croutonText.setText(R.string.fragment_retry_login_error_title);

            croutonTryAgainImage.setOnClickListener(new DebouncingOnClickListener() {
                @Override
                public void doClick(View v) {
                    retryConnection();
                }
            });

            Crouton.cancelAllCroutons();
            updateCrouton();
            croutonStatusTicker.get().show();
        }
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
        showConnecting();
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

    private void changeServerIfNeeded(String serverHostname) {
        if (!hostname.equalsIgnoreCase(serverHostname)) {
            RocketChatCache.INSTANCE.setSelectedServerHostname(serverHostname);
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
                    fragmentTask = FragmentTask.create(RocketChatCache.INSTANCE.getSelectedServerHostname());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.cont_item4, fragmentTask).commit();
                    return true;
                case R.id.action_setting:
                    chat.setVisibility(GONE);
                    call.setVisibility(GONE);
                    contacts.setVisibility(GONE);
                    task.setVisibility(GONE);
                    setting.setVisibility(VISIBLE);
                    return true;
            }

            return false;
        }
    };

    @DebugLog
    public void onLogout() {
        presenter.prepareToLogout();
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
                    navigation.setSelectedItemId(R.id.action_chat);
                    pane.openPane();
                    return true;
            }

        }
        return super.dispatchKeyEvent(event);
    }

    private void getSettings(String token){

        UtilsApi.getAPIService().getSettings(" KEY:"+token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getString("status").equals("200")){
                                    Log.d("QQWEWE", "!33");
                                    if (jsonRESULTS.getJSONObject("result").getString("INNER_GROUP").equals("true")) {
                                        Log.d("QQWEWE", "YES_SAVE");
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
        Intent onLineIntent = new Intent(this, PortSipService.class);
        onLineIntent.setAction(PortSipService.ACTION_PUSH_MESSAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(onLineIntent);
        }else{
           startService(onLineIntent);
        }

    }


}
