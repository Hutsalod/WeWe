package chat.wewe.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import chat.wewe.android.BuildConfig;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.StatusConnect;
import chat.wewe.android.activity.Intro;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.activity.PinCode;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.DeviceGet;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.fragment.AbstractFragment;
import chat.wewe.android.fragment.sidebar.dialog.EditPasswordDialogFragment;
import chat.wewe.android.fragment.sidebar.dialog.EmailFragment;
import chat.wewe.android.fragment.sidebar.dialog.LogDialogFragment;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.layouthelper.chatroom.roomlist.RoomListAdapter;
import chat.wewe.android.renderer.UserRenderer;
import chat.wewe.android.service.PortSipService;
import chat.wewe.core.interactors.RoomInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.models.RoomSidebar;
import chat.wewe.core.models.User;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmSpotlightRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import hugo.weaving.DebugLog;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class FragmentSetting extends AbstractFragment implements SidebarMainContract.View, StatusConnect {

    private NestedScrollView actionContainers,languageLayaut,secyrityLayaut,blaclistScrol;
    private SharedPreferences SipData,Preferences;
    private  SwitchCompat switch1;
    private   TextView stan_user_name,statusConnetc;
    private BaseApiService mApiServiceChat,mApiService;
    private  final Integer REQUEST_GET_SINGLE_FILE = 51;
    private String device = "0";
    static private SidebarMainContract.Presenter presenter;
    private RoomListAdapter adapter;
    private SearchView searchView;
    private TextView loadMoreResultsText;
    private List<RoomSidebar> roomSidebarList = Collections.emptyList();
    private Disposable spotlightDisposable;
    private String hostname,userId;
    public  MethodCallHelper methodCallHelper;
    public ImageView stanUsers;
    private boolean dialogSet = false;
    private static final String HOSTNAME = "hostname",USERID = "userId";
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;
    @Override
    protected int getLayout() {
        return R.layout.fragment_setting;
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    protected void onSetupView() {

        switch1 = (SwitchCompat) rootView.findViewById(R.id.deviceprivat);
        setupVersionInfo();
        setupUserStatusButtons();
        setupLogoutButton();
        setupUserActionToggle();
        actionContainers = (NestedScrollView) rootView.findViewById(R.id.user_action_outer_container);
        languageLayaut = (NestedScrollView) rootView.findViewById(R.id.languageLayaut);
        secyrityLayaut = (NestedScrollView) rootView.findViewById(R.id.secyrityLayaut);
        blaclistScrol = (NestedScrollView) rootView.findViewById(R.id.blaclistScrol);

        if(switch1!=null) {
            DeviceGet.getSettings(getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""), switch1);
        }
    }

    public static FragmentSetting create(String hostname, String userId) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        args.putString(USERID, userId);

        FragmentSetting fragment = new FragmentSetting();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hostname = getArguments().getString(HOSTNAME);
        userId = getArguments().getString(USERID);
        RealmUserRepository userRepository = new RealmUserRepository(hostname);

        AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname))
        );


        presenter = new SidebarMainPresenter(
                hostname,
                new RoomInteractor(new RealmRoomRepository(hostname)),
                userRepository,
                absoluteUrlHelper,
                new MethodCallHelper(getContext(), hostname),
                new RealmSpotlightRepository(hostname)
        );

        methodCallHelper = new MethodCallHelper(getContext(), hostname);
        SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
        Preferences = getActivity().getSharedPreferences("Setting", MODE_PRIVATE);
        mApiServiceChat = UtilsApiChat.getAPIService();
        mApiService = UtilsApi.getAPIService();




    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        stanUsers = view.findViewById(R.id.stanUsers);
        stan_user_name = view.findViewById(R.id.current_user_name);
        statusConnetc = view.findViewById(R.id.status_connetc);
        presenter.bindView(this);



        return view;
    }

    @Override
    public void onDestroyView() {
        presenter.release();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
       // userStatus();

    }

    @Override
    public void onPause() {

        super.onPause();
    }


    private void setupVersionInfo() {
        TextView versionInfoView = (TextView) rootView.findViewById(R.id.version_info);
        versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
    }

    static public  void setLogout(){

        Log.d("QQWEWE", "!33");


                    presenter.prepareToLogOut();

    }

    private void setupUserStatusButtons() {
        rootView.findViewById(R.id.btn_language).setOnClickListener(view -> {
            actionContainers.setVisibility(View.GONE);
            languageLayaut.setVisibility(View.VISIBLE);
            secyrityLayaut.setVisibility(View.GONE);
            blaclistScrol.setVisibility(View.GONE);
        });


        rootView.findViewById(R.id.deviceprivat).setOnClickListener(view -> {
            if(switch1.isChecked())
                device = "1";
            else
                device = "0";
            DeviceGet.postDevice(getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""),device);
        });

        rootView.findViewById(R.id.exetGoogle).setOnClickListener(view -> {
            DeleteGooglePay();
        });

        rootView.findViewById(R.id.supportConnect).setOnClickListener(view -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","support@weltwelle.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support on Android application");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Support");
            startActivity(Intent.createChooser(emailIntent, "Support"));
        });

        rootView.findViewById(R.id.blaclist).setOnClickListener(view -> {

            languageLayaut.setVisibility(View.GONE);
            actionContainers.setVisibility(View.GONE);
            secyrityLayaut.setVisibility(View.GONE);
            blaclistScrol.setVisibility(View.VISIBLE);
        });

        rootView.findViewById(R.id.friends).setOnClickListener(view -> {
            Uri uri = Uri.parse("smsto:");
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra("sms_body", "Hey, I'm using rocket to chat and call. Join me! Apple - https://apps.apple.com/ua/app/rocket-phone/id1386715295, Android - https://play.google.com/store/apps/details?id=chat.wewe.android");
            startActivity(intent);
        });

        rootView.findViewById(R.id.log).setOnClickListener(view -> {
            LogDialogFragment di = new LogDialogFragment().create();
            di.show(getActivity().getSupportFragmentManager(), "example dialog");
        });

        rootView.findViewById(R.id.edit_password).setOnClickListener(view -> {
            EditPasswordDialogFragment di = new EditPasswordDialogFragment().create();
            di.show(getActivity().getSupportFragmentManager(), "example dialog");
        });



        rootView.findViewById(R.id.edit_email).setOnClickListener(view -> {
             new EmailFragment().create().show(getActivity().getSupportFragmentManager(), "EmailFragment");
        });

        rootView.findViewById(R.id.policy_mss).setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://weltwelle.com/privpolicy.html"));
            startActivity(i);
        });
        rootView.findViewById(R.id.btn_language).setOnClickListener(view -> {
            actionContainers.setVisibility(View.GONE);
            languageLayaut.setVisibility(View.VISIBLE);
            secyrityLayaut.setVisibility(View.GONE);
            blaclistScrol.setVisibility(View.GONE);
        });
        rootView.findViewById(R.id.switch2).setOnClickListener(view -> {
            Toast.makeText(getActivity(), ""+getString(R.string.str_12),
                    Toast.LENGTH_SHORT).show();
        });

      rootView.findViewById(R.id.current_user_avatar).setOnClickListener(view -> {
            openAvatarFile();
        });

        rootView.findViewById(R.id.switch3).setOnClickListener(view -> {
            SharedPreferences.Editor ed = Preferences.edit();
            if(Preferences.getBoolean("VIDEO_C", false)==true)
                ed.putBoolean("VIDEO_C", false);
            else
                ed.putBoolean("VIDEO_C", true);
            ed.commit();
        });

        rootView.findViewById(R.id.nazadq).setOnClickListener(view -> {
            languageLayaut.setVisibility(View.GONE);
            actionContainers.setVisibility(View.VISIBLE);
            secyrityLayaut.setVisibility(View.GONE);
            blaclistScrol.setVisibility(View.GONE);
        });

        rootView.findViewById(R.id.nazad2).setOnClickListener(view -> {
            languageLayaut.setVisibility(View.GONE);
            actionContainers.setVisibility(View.VISIBLE);
            secyrityLayaut.setVisibility(View.GONE);
            blaclistScrol.setVisibility(View.GONE);
        });

        rootView.findViewById(R.id.securityBtn).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), PinCode.class));
        });

        rootView.findViewById(R.id.noKey).setOnClickListener(view -> {
            languageLayaut.setVisibility(View.GONE);
            actionContainers.setVisibility(View.VISIBLE);
            secyrityLayaut.setVisibility(View.GONE);
            blaclistScrol.setVisibility(View.GONE);
        });

        rootView.findViewById(R.id.okKey).setOnClickListener(view -> {
            languageLayaut.setVisibility(View.GONE);
            actionContainers.setVisibility(View.VISIBLE);
            secyrityLayaut.setVisibility(View.GONE);
            blaclistScrol.setVisibility(View.GONE);
        });

        rootView.findViewById(R.id.languageru).setOnClickListener(view -> {
            SharedPreferences.Editor ed = SipData.edit();
            ed.putString("LANG_APP", "RU");
            ed.commit();
            this.getActivity().finish();
            startActivity(new Intent(getActivity(), Intro.class));
        });

        rootView.findViewById(R.id.languageua).setOnClickListener(view -> {
            SharedPreferences.Editor ed = SipData.edit();
            ed.putString("LANG_APP", "UK");
            ed.commit();
            this.getActivity().finish();
            startActivity(new Intent(getActivity(), Intro.class));
        });

        rootView.findViewById(R.id.languageen).setOnClickListener(view -> {
            SharedPreferences.Editor ed = SipData.edit();
            ed.putString("LANG_APP", "KV");
            ed.commit();
            this.getActivity().finish();
            startActivity(new Intent(getActivity(), Intro.class));
        });

        rootView.findViewById(R.id.languagede).setOnClickListener(view -> {
            SharedPreferences.Editor ed = SipData.edit();
            ed.putString("LANG_APP", "DE");
            ed.commit();
            this.getActivity().finish();
            startActivity(new Intent(getActivity(), Intro.class));

        });


        rootView.findViewById(R.id.deleye_mss).setOnClickListener(view -> {


            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            methodCallHelper.hideAndEraseRooms(RocketChatCache.INSTANCE.getUserId());
                            Log.d("jsonRESULTS","ttt"+userId);
                            closeUserActionContainer();
                            // Clear relative data and set new hostname if any.
                            presenter.prepareToLogOut();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getString(R.string.delet_message)+" ?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });

        rootView.findViewById(R.id.btn_status_online).setOnClickListener(view -> {
            presenter.onUserOnline();
            closeUserActionContainer();
        });
        rootView.findViewById(R.id.btn_status_away).setOnClickListener(view -> {
            presenter.onUserAway();
            closeUserActionContainer();
        });
        rootView.findViewById(R.id.btn_status_busy).setOnClickListener(view -> {
            presenter.onUserBusy();
            closeUserActionContainer();
        });
        rootView.findViewById(R.id.btn_status_invisible).setOnClickListener(view -> {
            presenter.onUserOffline();
            closeUserActionContainer();
        });

    }

    @SuppressLint("RxLeakedSubscription")
    private void setupUserActionToggle() {
        final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        toggleUserAction.setFocusableInTouchMode(false);

        rootView.findViewById(R.id.user_info_container).setOnClickListener(view -> toggleUserAction.toggle());

        RxCompoundButton.checkedChanges(toggleUserAction)
                .compose(bindToLifecycle())
                .subscribe(
                        this::showUserActionContainer,
                        Logger.INSTANCE::report
                );
    }

    public void showUserActionContainer(boolean show) {
        rootView.findViewById(R.id.user_action_visible)
                .setVisibility(show ? View.VISIBLE : View.GONE);

        rootView.findViewById(R.id.user_action_outer_container).setOnClickListener(view -> {
            presenter.onUserAway();
            closeUserActionContainer();
        });
    }

    public void openAvatarFile() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);

        startActivityForResult(intent, REQUEST_GET_SINGLE_FILE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_GET_SINGLE_FILE) {
            if (data != null) {

            //  setAvatarFile(mFileUri);


            }
        }
    }



    public void DeleteGooglePay(){
        mApiService.removeTransactionid("KEY:"+getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("jsonRESULTS",""+response.toString());
                        if (response.isSuccessful()){
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getJSONObject("result").getString("SUCCESS").equals("true")){
                                    Toast.makeText(getActivity(), "Текущий пользователь WeWe успешно отвязан от Appleid",
                                            Toast.LENGTH_SHORT).show();
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
                        //   Log.e("debug", "onFailure: ERROR > " + t.toString());

                    }
                });
    }



    private void setupLogoutButton() {
        rootView.findViewById(R.id.btn_logout).setOnClickListener(view -> {

            getActivity().getSharedPreferences("SIP", Context.MODE_PRIVATE).edit().clear().commit();
            getActivity().getSharedPreferences("pin", Context.MODE_PRIVATE).edit().clear().commit();
            getActivity().getSharedPreferences("Setting", Context.MODE_PRIVATE).edit().clear().commit();
            Intent offLineIntent = new Intent(getActivity(), PortSipService.class);
            offLineIntent.setAction(PortSipService.ACTION_SIP_UNREGIEST);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().startService(offLineIntent);
            }else{
                getActivity().startService(offLineIntent);
            }
            closeUserActionContainer();
            // Clear relative data and set new hostname if any.
            presenter.prepareToLogOut();
        });
    }


    public void closeUserActionContainer() {
        final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        if (toggleUserAction != null && toggleUserAction.isChecked()) {
            toggleUserAction.setChecked(false);
        }
    }

    private void onRenderCurrentUser(User user) {
        Log.d("QQRRXX","Test");
        if (user != null) {
            UserRenderer userRenderer = new UserRenderer(user);
            userRenderer.showAvatar(rootView.findViewById(R.id.current_user_avatar), hostname);
            userRenderer.showUsername(rootView.findViewById(R.id.current_user_name));
            userRenderer.showStatusColor(rootView.findViewById(R.id.current_user_status));
            Log.d("QQRRXX","33");

        }

        if(RocketChatCache.INSTANCE.getSessionToken() != null) {
            if (getActivity().getSharedPreferences("Sub", MODE_PRIVATE).getBoolean("Sub", false) == true || getActivity().getSharedPreferences("Sub", MODE_PRIVATE).getBoolean("SubApi", false) == true) {
                if (dialogSet == false) {
                    dialogSet = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (!Settings.canDrawOverlays(getActivity())) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("Для нормального отображение звонков, нужно разрешить настройки!")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            testPermission();
                                        }
                                    });
                            builder.create().show();

                        }
                    }
                }
            }
        }
    }



    @Override
    public void showScreen() {

    }

    @Override
    public void showEmptyScreen() {

    }

    @Override
    public void showRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList) {

    }

    @Override
    public void filterRoomSidebarList(CharSequence term) {

    }

    @Override
    public void show(User user) {
        onRenderCurrentUser(user);

    }

    @DebugLog
    @Override
    public void onPreparedToLogOut() {
        final Activity activity = getActivity();
        if (activity != null && activity instanceof MainActivity) {
            ((MainActivity) activity).onLogout();
        }
    }




    public void testPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            if (!Settings.canDrawOverlays(getActivity().getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getActivity().getPackageName()));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                }
            }


        }
    }

    @Override
    public void noConnect() {
        stanUsers.setImageResource(R.drawable.s000);
        statusConnetc.setText("Chat: Офлайн SIP: Офлайн");
    }

    @Override
    public void Connecting() {
        this.stanUsers.setImageResource(R.drawable.s112);
        statusConnetc.setText("Chat: Онлайн SIP: Онлайн");
    }

    @Override
    public void okConnect() {
        this.stanUsers.setImageResource(R.drawable.s222);
        statusConnetc.setText("Chat: Онлайн SIP: Онлайн");
    }


}
