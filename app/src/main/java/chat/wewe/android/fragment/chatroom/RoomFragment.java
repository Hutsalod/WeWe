package chat.wewe.android.fragment.chatroom;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.widget.SlidingPaneLayouts;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import chat.wewe.persistence.encrypt.Cryptor;
import chat.wewe.android.BackgroundLooper;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.activity.ShowUserDetailed;
import chat.wewe.android.activity.room.RoomActivity;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.fragment.chatroom.dialog.FileUploadProgressDialogFragment;
import chat.wewe.android.fragment.sidebar.SidebarMainFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddTaskFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddUsersDialogFragment;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.helper.FileUploadHelper;
import chat.wewe.android.helper.LoadMoreScrollListener;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.OnBackPressListener;
import chat.wewe.android.helper.RecyclerViewAutoScrollManager;
import chat.wewe.android.helper.RecyclerViewScrolledToBottomListener;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.android.layouthelper.chatroom.AbstractNewMessageIndicatorManager;
import chat.wewe.android.layouthelper.chatroom.MessageFormManager;
import chat.wewe.android.layouthelper.chatroom.MessageListAdapter;
import chat.wewe.android.layouthelper.chatroom.MessagePopup;
import chat.wewe.android.layouthelper.chatroom.ModelListAdapter;
import chat.wewe.android.layouthelper.chatroom.PairedMessage;
import chat.wewe.android.layouthelper.extra_action.AbstractExtraActionItem;
import chat.wewe.android.layouthelper.extra_action.MessageExtraActionBehavior;
import chat.wewe.android.layouthelper.extra_action.upload.AbstractUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.AudioUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.FileUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.ImageUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.VideoUploadActionItem;
import chat.wewe.android.log.RCLog;
import chat.wewe.android.renderer.RocketChatUserStatusProvider;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android.service.temp.DeafultTempSpotlightRoomCaller;
import chat.wewe.android.service.temp.DefaultTempSpotlightUserCaller;
import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.android.widget.RoomToolbar;
import chat.wewe.android.widget.internal.ExtraActionPickerDialogFragment;
import chat.wewe.android.widget.message.MessageFormLayout;
import chat.wewe.android.widget.message.autocomplete.AutocompleteManager;
import chat.wewe.android.widget.message.autocomplete.channel.ChannelSource;
import chat.wewe.android.widget.message.autocomplete.user.UserSource;
import chat.wewe.core.interactors.AutocompleteChannelInteractor;
import chat.wewe.core.interactors.AutocompleteUserInteractor;
import chat.wewe.core.interactors.MessageInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.models.Message;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.User;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.repositories.RealmMessageRepository;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmSpotlightRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmSpotlightUserRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static chat.wewe.persistence.encrypt.Cryptor.privatChat;

/**
 * Chat room screen.
 */
@RuntimePermissions
public class RoomFragment extends AbstractChatRoomFragment implements
        OnBackPressListener,
        ExtraActionPickerDialogFragment.Callback,
        ModelListAdapter.OnItemLongClickListener<PairedMessage>,
        ModelListAdapter.OnItemClickListener<PairedMessage>,
        RoomContract.View {

    private static final int DIALOG_ID = 1;
    private static final String HOSTNAME = "hostname";
    private static final String ROOM_ID = "roomId";
    private boolean privat = false;
    private String privatKey = "", publiKey = "";

    private String hostname;
    private String token;
    private String userId;
    private String roomId;
    private String roomType;
    private String nameRoom = "";
    private LoadMoreScrollListener scrollListener;
    private MessageFormManager messageFormManager;
    private RecyclerView messageRecyclerView;
    private RecyclerViewAutoScrollManager recyclerViewAutoScrollManager;
    protected AbstractNewMessageIndicatorManager newMessageIndicatorManager;
    protected Snackbar unreadIndicator;
    private boolean previousUnreadMessageExists;
    private MessageListAdapter messageListAdapter;
    private AutocompleteManager autocompleteManager;
    private SharedPreferences Preferences;

    private List<AbstractExtraActionItem> extraActionItems;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected RoomContract.Presenter presenter;

    private RealmRoomRepository roomRepository;
    private RealmUserRepository userRepository;
    private MethodCallHelper methodCallHelper;
    private AbsoluteUrlHelper absoluteUrlHelper;

    private Message editingMessage = null;

    private RoomToolbar toolbar;

    private Optional<SlidingPaneLayouts> optionalPane;
    private SidebarMainFragment sidebarFragment;
    private float dX,dY;


    public RoomFragment() {
    }

    /**
     * build fragment with roomId.
     */
    public static RoomFragment create(String hostname, String roomId) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        args.putString(ROOM_ID, roomId);

        RoomFragment fragment = new RoomFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        hostname = args.getString(HOSTNAME);
        roomId = args.getString(ROOM_ID);

        roomRepository = new RealmRoomRepository(hostname);

        MessageInteractor messageInteractor = new MessageInteractor(
                new RealmMessageRepository(hostname),
                roomRepository
        );

        userRepository = new RealmUserRepository(hostname);

        absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname))
        );

        methodCallHelper = new MethodCallHelper(getContext(), hostname);

        presenter = new RoomPresenter(
                roomId,
                userRepository,
                messageInteractor,
                roomRepository,
                absoluteUrlHelper,
                methodCallHelper,
                ConnectivityManager.getInstance(getContext())
        );

        if (savedInstanceState == null) {
            presenter.loadMessages();
        }

        privatKey = getActivity().getSharedPreferences("Sip", MODE_PRIVATE).getString("CHAT_PRIVAT", "");
        publiKey = getActivity().getSharedPreferences("Sip", MODE_PRIVATE).getString("CHAT_PUBLIC", "");
        Log.d("CHAT_PRIVAT","CHAT_PRIVAT "+privatKey);
        Log.d("CHAT_PRIVAT","CHAT_PUBLIC "+publiKey);
        if (privatKey.isEmpty()) {

            KeyPair kp = Cryptor.getKeyPair();

            PublicKey publicKey = kp.getPublic();
            byte[] publicKeyBytes = publicKey.getEncoded();
            publiKey = new String(Base64.encode(publicKeyBytes, Base64.DEFAULT));

            PrivateKey privateKey = kp.getPrivate();
            byte[] privateKeyBytes = privateKey.getEncoded();
            privatKey = new String(Base64.encode(privateKeyBytes, Base64.DEFAULT));
            setPrivatKey(getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""), privatKey);

            getActivity().getSharedPreferences("Sip", MODE_PRIVATE).edit()
                    .putString("CHAT_PUBLIC", publiKey).putString("CHAT_PRIVAT", privatKey).commit();
        }

        Preferences = getActivity().getSharedPreferences("Setting", MODE_PRIVATE);

    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_room;
    }

    @Override
    protected void onSetupView() {
        optionalPane = Optional.ofNullable(getActivity().findViewById(R.id.sliding_pane));
        messageRecyclerView = rootView.findViewById(R.id.messageRecyclerView);

        messageListAdapter = new MessageListAdapter(getContext(), hostname);
        messageRecyclerView.setAdapter(messageListAdapter);
        messageListAdapter.setOnItemLongClickListener(this);
        messageListAdapter.setOnItemClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        recyclerViewAutoScrollManager = new RecyclerViewAutoScrollManager(linearLayoutManager) {
            @Override
            protected void onAutoScrollMissed() {
                if (newMessageIndicatorManager != null) {
                    presenter.onUnreadCount();
                }
            }
        };
        messageListAdapter.registerAdapterDataObserver(recyclerViewAutoScrollManager);

        scrollListener = new LoadMoreScrollListener(linearLayoutManager, 1) {
            @Override
            public void requestMoreItem() {
                presenter.loadMoreMessages();
            }
        };
        messageRecyclerView.addOnScrollListener(scrollListener);
        messageRecyclerView.addOnScrollListener(new RecyclerViewScrolledToBottomListener(linearLayoutManager, 1, this::markAsReadIfNeeded));

        newMessageIndicatorManager = new AbstractNewMessageIndicatorManager() {
            @Override
            protected void onShowIndicator(int count, boolean onlyAlreadyShown) {
                if ((onlyAlreadyShown && unreadIndicator != null && unreadIndicator.isShown()) || !onlyAlreadyShown) {
                    unreadIndicator = getUnreadCountIndicatorView(count);
                    unreadIndicator.show();
                }
            }

            @Override
            protected void onHideIndicator() {
                if (unreadIndicator != null && unreadIndicator.isShown()) {
                    unreadIndicator.dismiss();
                }
            }
        };


    //    getPrivatKey(getActivity().getSharedPreferences("SIP", MODE_PRIVATE).getString("TOKEN_WE", ""),RocketChatCache.INSTANCE.getUserName());

        setupToolbar();
        setupSidebar();
        setupMessageComposer();
        setupMessageActions();
    }

    private void setupMessageActions() {
        extraActionItems = new ArrayList<>(4); // fixed number as of now
        extraActionItems.add(new ImageUploadActionItem());
        extraActionItems.add(new AudioUploadActionItem());
        extraActionItems.add(new VideoUploadActionItem(Preferences.getBoolean("VIDEO_C", true),getContext()));
        extraActionItems.add(new FileUploadActionItem());
    }

    private void scrollToLatestMessage() {
        if (messageRecyclerView != null)
            messageRecyclerView.scrollToPosition(0);
    }

    protected Snackbar getUnreadCountIndicatorView(int count) {
        // TODO: replace with another custom View widget, not to hide message composer.
        final String caption = getResources().getQuantityString(
                R.plurals.fmt_dialog_view_latest_message_title, count, count);

        return Snackbar.make(rootView, caption, Snackbar.LENGTH_LONG)
                .setAction(R.string.dialog_view_latest_message_action, view -> scrollToLatestMessage());
    }

    @Override
    public void onDestroyView() {
        RecyclerView.Adapter adapter = messageRecyclerView.getAdapter();
        if (adapter != null)
            adapter.unregisterAdapterDataObserver(recyclerViewAutoScrollManager);

        compositeDisposable.clear();

        if (autocompleteManager != null) {
            autocompleteManager.dispose();
            autocompleteManager = null;
        }

        super.onDestroyView();
    }

    @Override
    public boolean onItemLongClick(PairedMessage pairedMessage) {
        presenter.onMessageSelected(pairedMessage.target);
        return true;
    }

    @Override
    public void onItemClick(PairedMessage pairedMessage) {
        presenter.onMessageTap(pairedMessage.target);
    }

    private void setupToolbar() {
        toolbar = getActivity().findViewById(R.id.activity_main_toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_room);

        optionalPane.ifPresent(pane -> toolbar.setNavigationOnClickListener(view -> {
            if (pane.isSlideable() && !pane.isOpen()) {
                pane.openPane();
            }
        }));

        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_pinned_messages:
                    showRoomListFragment(R.id.action_pinned_messages);
                    break;
                case R.id.action_favorite_messages:
                    showRoomListFragment(R.id.action_favorite_messages);
                    break;
                case R.id.action_file_list:
                    showRoomListFragment(R.id.action_file_list);
                    break;
                case R.id.action_member_list:
                    showRoomListFragment(R.id.action_member_list);
                    break;
                case R.id.add_user_group:
                    addUserGroup();
                    break;
                case R.id.privat_chat:
                    privatChat();
                    break;
                default:
                    return super.onOptionsItemSelected(menuItem);
            }
            return true;
        });
        toolbar.taskAdd.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("QQQEEE",""+hostname+""+roomId+""+ RocketChatCache.INSTANCE.getUserId());
            AddTaskFragment task = new AddTaskFragment().create(hostname,roomId,userId);
            task.setActionListener(new AddTaskFragment.ActionListener() {
                @Override
                public void onClick(String uid) {
                    showRoom(uid);
                }
            });
            task.show(getActivity().getSupportFragmentManager(), "example dialog");
        }
        });
    }

    private void setupSidebar() {
        SlidingPaneLayouts subPane = getActivity().findViewById(R.id.sub_sliding_pane);
        sidebarFragment = (SidebarMainFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.sidebar_fragment_container);

        optionalPane.ifPresent(pane -> pane.setPanelSlideListener(new SlidingPaneLayouts.PanelSlideListener() {
                @Override
                public void onPanelSlide(@NonNull View view, float v) {
                    messageFormManager.enableComposingText(false);
                    sidebarFragment.clearSearchViewFocus();
                    //Ref: ActionBarDrawerToggle#setProgress
                    toolbar.setNavigationIconProgress(v);
                }

                @Override
                public void onPanelOpened(@NonNull View view) {
                    toolbar.setNavigationIconVerticalMirror(true);
                }

                @Override
                public void onPanelClosed(@NonNull View view) {
                    messageFormManager.enableComposingText(true);
                    toolbar.setNavigationIconVerticalMirror(false);
                    subPane.closePane();
                    closeUserActionContainer();
                }
            }));
    }

    public void closeUserActionContainer() {
        sidebarFragment.closeUserActionContainer();
    }

    private void setupMessageComposer() {
        final MessageFormLayout messageFormLayout = rootView.findViewById(R.id.messageComposer);
        messageFormManager = new MessageFormManager(messageFormLayout, this::showExtraActionSelectionDialog);
        messageFormManager.setSendMessageCallback(this::sendMessage);
        messageFormManager.getRoomName(nameRoom);
        messageFormLayout.setEditTextCommitContentListener(this::onCommitContent);

        autocompleteManager = new AutocompleteManager(rootView.findViewById(R.id.messageListRelativeLayout));

        autocompleteManager.registerSource(
                new ChannelSource(
                        new AutocompleteChannelInteractor(
                                roomRepository,
                                new RealmSpotlightRoomRepository(hostname),
                                new DeafultTempSpotlightRoomCaller(methodCallHelper)
                        ),
                        AndroidSchedulers.from(BackgroundLooper.get()),
                        AndroidSchedulers.mainThread()
                )
        );

        Disposable disposable = Single.zip(
                absoluteUrlHelper.getRocketChatAbsoluteUrl(),
                roomRepository.getById(roomId).first(Optional.absent()),
                Pair::create
        )
                .subscribe(
                        pair -> {
                            if (pair.first.isPresent() && pair.second.isPresent()) {
                                autocompleteManager.registerSource(
                                        new UserSource(
                                                new AutocompleteUserInteractor(
                                                        pair.second.get(),
                                                        userRepository,
                                                        new RealmMessageRepository(hostname),
                                                        new RealmSpotlightUserRepository(hostname),
                                                        new DefaultTempSpotlightUserCaller(methodCallHelper)
                                                ),
                                                pair.first.get(),
                                                RocketChatUserStatusProvider.INSTANCE,
                                                AndroidSchedulers.from(BackgroundLooper.get()),
                                                AndroidSchedulers.mainThread()
                                        )
                                );
                            }
                        },
                        throwable -> {
                        }
                );

        compositeDisposable.add(disposable);

        autocompleteManager.bindTo(
                messageFormLayout.getEditText(),
                messageFormLayout
        );


        sendShare();
    }

    public void sendShare(){
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {


                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                dialog.dismiss();
                                handleSendImage(intent);  // Handle multiple images being sent
                                getActivity().getIntent().setAction(null);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                intent.setAction(null);
                                break;

                            case DialogInterface.BUTTON_NEUTRAL:
                                intent.setAction(null);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Отправить файл " + RocketChatCache.INSTANCE.getUserName() + " ?").setPositiveButton("Да", dialogClickListener)
                        .setNegativeButton("Нет", dialogClickListener).show();

            }

    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            uploadFile(imageUri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != AbstractUploadActionItem.RC_UPL || resultCode != Activity.RESULT_OK) {
            return;
        }

        if (data == null || data.getData() == null) {
            return;
        }

        uploadFile(data.getData());
    }

    private void uploadFile(Uri uri) {
        String uplId = new FileUploadHelper(getContext(), RealmStore.get(hostname))
                .requestUploading(roomId, uri);
        if (!TextUtils.isEmpty(uplId)) {
            FileUploadProgressDialogFragment.create(hostname, roomId, uplId)
                    .show(getFragmentManager(), "FileUploadProgressDialogFragment");

        } else {

        }
    }

    private void markAsReadIfNeeded() {
        presenter.onMarkAsRead();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.bindView(this);
    }

    @Override
    public void onPause() {
        presenter.release();
        super.onPause();
    }

    private void showExtraActionSelectionDialog() {
        final DialogFragment fragment = ExtraActionPickerDialogFragment
                .create(new ArrayList<>(extraActionItems),Preferences.getBoolean("VIDEO_C", true));
        fragment.setTargetFragment(this, DIALOG_ID);
        fragment.show(getFragmentManager(), "ExtraActionPickerDialogFragment");
    }

    @Override
    public void onItemSelected(int itemId) {
        for (AbstractExtraActionItem extraActionItem : extraActionItems) {
            if (extraActionItem.getItemId() == itemId) {
                RoomFragmentPermissionsDispatcher.onExtraActionSelectedWithCheck(RoomFragment.this, extraActionItem);
                return;
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (editingMessage != null) {
            editingMessage = null;
            messageFormManager.clearComposingText();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RoomFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    protected void onExtraActionSelected(MessageExtraActionBehavior action) {
        action.handleItemSelectedOnFragment(RoomFragment.this);
    }

    private boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                    Bundle opts, String[] supportedMimeTypes) {
        boolean supported = false;
        for (final String mimeType : supportedMimeTypes) {
            if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                supported = true;
                break;
            }
        }

        if (!supported) {
            return false;
        }

        if (BuildCompat.isAtLeastNMR1()
                && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                return false;
            }
        }

        Uri linkUri = inputContentInfo.getLinkUri();
        if (linkUri == null) {
            return false;
        }

        sendMessage(linkUri.toString());

        try {
            inputContentInfo.releasePermission();
        } catch (Exception e) {
            RCLog.e(e);
            Logger.INSTANCE.report(e);
        }

        return true;
    }



    private void sendMessage(String messageText) {
        if (editingMessage == null) {
            if(privatChat == true) {
                String encrypted = Cryptor.encryptRSAToString(messageText, publiKey);
                presenter.sendMessage("\uD83D\uDD11 " +  encrypted);
            }else{
                presenter.sendMessage(messageText);
            }
        } else {
            presenter.updateMessage(editingMessage, messageText);
        }
    }

    private void privatChat(){
        if(privatChat == false) {
            presenter.sendMessage("\uD83D\uDD12 *ВХОД В ПРИВАТНЫЙ ЧАТ*");
            Log.d("CHAT_PRIVAT","CHAT_PRIVAT "+privatKey);
            methodCallHelper.usersMessage(true, "2", privatKey, roomId.replaceAll(userId, ""), roomId);
            privatChat = true;
        }else {
            presenter.sendMessage("\uD83D\uDD13 *ВЫХОД С ПРИВАТНОГО ЧАТА*");
            privatChat = false;
        }
    }


    @Override
    public void setupWith(@NonNull RocketChatAbsoluteUrl rocketChatAbsoluteUrl) {
        if (rocketChatAbsoluteUrl != null) {
            token = rocketChatAbsoluteUrl.getToken();
            userId = rocketChatAbsoluteUrl.getUserId();
            messageListAdapter.setAbsoluteUrl(rocketChatAbsoluteUrl);
        }
    }

    @Override
    public void render(@NonNull Room room) {
        roomType = room.getType();
        nameRoom = room.getName();
        setToolbarTitle(room.getName());

        boolean unreadMessageExists = room.isAlert();
        if (newMessageIndicatorManager != null && previousUnreadMessageExists && !unreadMessageExists) {
            newMessageIndicatorManager.reset();
        }
        previousUnreadMessageExists = unreadMessageExists;

        if (room.isChannel()) {
            showToolbarPublicChannelIcon();
            return;
        }

        if (room.isPrivate()) {
            showToolbarPrivateChannelIcon();
        }

        if (room.isLivechat()) {
            showToolbarLivechatChannelIcon();
        }
    }

    @Override
    public void showUserStatus(@NonNull User user) {
        showToolbarUserStatuslIcon(user.getStatus());
    }

    @Override
    public void updateHistoryState(boolean hasNext, boolean isLoaded) {
        if (messageRecyclerView == null || !(messageRecyclerView.getAdapter() instanceof MessageListAdapter)) {
            return;
        }

        MessageListAdapter adapter = (MessageListAdapter) messageRecyclerView.getAdapter();
        if (isLoaded) {
            scrollListener.setLoadingDone();
        }
        adapter.updateFooter(hasNext, isLoaded);
    }

    @Override
    public void onMessageSendSuccessfully() {
        scrollToLatestMessage();
        messageFormManager.onMessageSend();
        editingMessage = null;
    }

    @Override
    public void disableMessageInput() {
        messageFormManager.enableComposingText(false);
    }

    @Override
    public void enableMessageInput() {
        messageFormManager.enableComposingText(true);
    }

    @Override
    public void showUnreadCount(int count) {
        newMessageIndicatorManager.updateNewMessageCount(count);
    }

    @Override
    public void showMessages(@NonNull List<? extends Message> messages) {
        if (messageListAdapter == null) {
            return;
        }

        messageListAdapter.updateData((List<Message>) messages);
    }

    @Override
    public void showMessageSendFailure(@NonNull Message message) {
        new AlertDialog.Builder(getContext())
                .setPositiveButton(R.string.resend,
                        (dialog, which) -> presenter.resendMessage(message))
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.discard,
                        (dialog, which) -> presenter.deleteMessage(message))
                .show();
    }

    @Override
    public void showMessageDeleteFailure(@NonNull Message message) {
        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.failed_to_delete))
                .setMessage(getContext().getString(R.string.failed_to_delete_message))
                .setPositiveButton(R.string.ok, (dialog, which) -> presenter.acceptMessageDeleteFailure(message))
                .show();
    }

    @Override
    public void autoloadImages() {
        messageListAdapter.setAutoloadImages(true);
    }

    @Override
    public void manualLoadImages() {
        messageListAdapter.setAutoloadImages(false);
    }

    @Override
    public void onReply(@NonNull AbsoluteUrl absoluteUrl, @NonNull String markdown, @NonNull Message message) {
        messageFormManager.setReply(absoluteUrl, markdown, message);
    }

    @Override
    public void onCopy(@NonNull String message) {
        RocketChatApplication context = RocketChatApplication.getInstance();
        ClipboardManager clipboardManager =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("message", message));
    }

    @Override
    public void showMessageActions(@NonNull Message message) {
        Activity context = getActivity();
        if (context != null && context instanceof MainActivity) {
            MessagePopup.take(message)
                    .setProfil(this::showUsersDetail)
                    .setReplyAction(msg -> presenter.replyMessage(message, false))
                    .setEditAction(this::onEditMessage)
                    .setCopyAction(msg -> onCopy(message.getMessage()))
                    .setQuoteAction(msg -> presenter.replyMessage(message, true))
                    .setDeleteAction(this::onDeleteMessage)
                    .setForwardAction(this::openDialogUserss)
                    .showWith(context);
        }
    }


    public void showUsersDetail(Message message) {
        Activity context = getActivity();
        if (context != null && context instanceof MainActivity) {

            showUserDetailed(message);
        }
    }

    private void onEditMessage(Message message) {
        editingMessage = message;
        messageFormManager.setEditMessage(message.getMessage());
    }

    public void openDialogUserss(Message message) {
        AddUsersDialogFragment di = new AddUsersDialogFragment().create(hostname,roomId,userId,1);
        di.setActionListener(new AddUsersDialogFragment.ActionListener() {
            @Override
            public void onClick(String uid) {
                methodCallHelper.forwardMessage(message.getId(),uid,roomId);
                showRoom(uid);
            }
        });
        di.show(getActivity().getSupportFragmentManager(), "example dialog");


    }

    public void addUserGroup() {

        AddUsersDialogFragment di = new AddUsersDialogFragment().create(hostname,roomId,RocketChatCache.INSTANCE.getUserId(),0);
        di.setActionListener(new AddUsersDialogFragment.ActionListener() {
            @Override
            public void onClick(String uid) {
                sendUsers(uid);
            }
        });
        di.show(getActivity().getSupportFragmentManager(), "example dialog");


    }


    public void showRoom(String roomId){
        ((MainActivity)getActivity()).showRoom(hostname,roomId);
    }

    public void onDeleteMessage(Message message) {
        presenter.deleteMessage(message);
    }


    private void showRoomListFragment(int actionId) {
        //TODO: oddly sometimes getActivity() yields null. Investigate the situations this might happen
        //and fix it, removing this null-check
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), RoomActivity.class)
                    .putExtra("actionId", actionId)
                    .putExtra("roomId", roomId)
                    .putExtra("roomType", roomType)
                    .putExtra("hostname", hostname)
                    .putExtra("token", token)
                    .putExtra("userId", userId);
            startActivity(intent);
        }
    }

    private void showUserDetailed(Message message) {
       String email = (message.getUser().getEmails().toArray().length>0) ? message.getUser().getEmails().get(0).getAddress() : "";
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), ShowUserDetailed.class)
                    .putExtra("name",message.getUser().getName())
                    .putExtra("status",message.getUser().getStatus())
                    .putExtra("username",message.getUser().getUsername())
                    .putExtra("rols",email)
                    .putExtra("roomId", roomId)
                    .putExtra("roomType", roomType)
                    .putExtra("hostname", hostname)
                    .putExtra("token", token)
                    .putExtra("userId", userId);

            startActivity(intent);
        }
    }


    private void setPrivatKey(String token,String key){
        Map<String, Object> jsonParams = new ArrayMap<>();
        jsonParams.put("UF_PUBLIC_KEY", key);
        UtilsApi.getAPIService().setPublicKey(" KEY:"+token,jsonParams)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){

                            Log.d("TEST_CRYPT"," "+response.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }

    private void getPrivatKey(String token,String name){

        UtilsApi.getAPIService().getPublicKey(" KEY:"+token,name)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            try {
                            JSONObject jsonRESULTS = new JSONObject(response.body().string());

                                getActivity().getSharedPreferences("Sip", MODE_PRIVATE).edit()
                                        .putString(name, jsonRESULTS.getString("status")).commit();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });

    }



    public void sendUsers(String msg){
        presenter.sendMessage("@"+msg + " Добавить");
    }

    public void loadMissedMessages() {
        presenter.loadMissedMessages();
    }

}