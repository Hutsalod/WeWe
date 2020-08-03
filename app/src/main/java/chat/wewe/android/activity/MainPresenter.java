package chat.wewe.android.activity;

import android.support.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import chat.wewe.android.BackgroundLooper;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.log.RCLog;
import chat.wewe.android.service.ConnectivityManagerApi;
import chat.wewe.android.service.ServerConnectivity;
import chat.wewe.android.shared.BasePresenter;
import chat.wewe.android_ddp.DDPClient;
import chat.wewe.core.PublicSettingsConstants;
import chat.wewe.core.interactors.CanCreateRoomInteractor;
import chat.wewe.core.interactors.RoomInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.models.PublicSetting;
import chat.wewe.core.models.Session;
import chat.wewe.core.models.User;
import chat.wewe.core.repositories.PublicSettingRepository;
import chat.wewe.core.utils.Pair;
import hugo.weaving.DebugLog;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainPresenter extends BasePresenter<MainContract.View>
        implements MainContract.Presenter {

    private final CanCreateRoomInteractor canCreateRoomInteractor;
    private final RoomInteractor roomInteractor;
    private final SessionInteractor sessionInteractor;
    private final MethodCallHelper methodCallHelper;
    private final ConnectivityManagerApi connectivityManagerApi;
    private final PublicSettingRepository publicSettingRepository;

    public MainPresenter(RoomInteractor roomInteractor,
                         CanCreateRoomInteractor canCreateRoomInteractor,
                         SessionInteractor sessionInteractor,
                         MethodCallHelper methodCallHelper,
                         ConnectivityManagerApi connectivityManagerApi,
                         PublicSettingRepository publicSettingRepository) {
        this.roomInteractor = roomInteractor;
        this.canCreateRoomInteractor = canCreateRoomInteractor;
        this.sessionInteractor = sessionInteractor;
        this.methodCallHelper = methodCallHelper;
        this.connectivityManagerApi = connectivityManagerApi;
        this.publicSettingRepository = publicSettingRepository;
    }

    @Override
    public void bindViewOnly(@NonNull MainContract.View view) {
        super.bindView(view);
        subscribeToUnreadCount();
        subscribeToSession();
        setUserOnline();
    }

    @Override
    public void loadSignedInServers(@NonNull String hostname) {
        final Disposable disposable = publicSettingRepository.getById(PublicSettingsConstants.Assets.LOGO)
                .zipWith(publicSettingRepository.getById(PublicSettingsConstants.General.SITE_NAME), Pair::new)
                .map(this::getLogoAndSiteNamePair)
                .map(settings -> getServerList(hostname, settings))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                       // view::showSignedInServers,
                       // RCLog::e
                );

        addSubscription(disposable);
    }

    @Override
    public void bindView(@NonNull MainContract.View view) {
        super.bindView(view);

        if (shouldLaunchAddServerActivity()) {
            view.showAddServerScreen();
            return;
        }

        openRoom();

        subscribeToNetworkChanges();
        subscribeToUnreadCount();
        subscribeToSession();
    }

    @Override
    public void release() {
        if (RocketChatCache.INSTANCE.getSessionToken() != null) {
            setUserAway();
        }

        super.release();
    }

    @Override
    public void onOpenRoom(String hostname, String roomId) {
        final Disposable subscription = canCreateRoomInteractor.canCreate(roomId)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allowed -> {
                            if (allowed) {
                                view.showRoom(hostname, roomId);
                            } else {
                                view.showHome();
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void onRetryLogin() {
        final Disposable subscription = sessionInteractor.retryLogin()
                .subscribe();

        addSubscription(subscription);
    }

    @DebugLog
    @Override
    public void prepareToLogout() {
        clearSubscriptions();
    }

    private Pair<String, String> getLogoAndSiteNamePair(Pair<Optional<PublicSetting>, Optional<PublicSetting>> settingsPair) {
        String logoUrl = "";
        String siteName = "";
        if (settingsPair.first.isPresent()) {
            logoUrl = settingsPair.first.get().getValue();
        }
        if (settingsPair.second.isPresent()) {
            siteName = settingsPair.second.get().getValue();
        }
        return new Pair<>(logoUrl, siteName);
    }

    private List<Pair<String, Pair<String, String>>> getServerList(String hostname, Pair<String, String> serverInfoPair) throws JSONException {
        JSONObject jsonObject = new JSONObject(serverInfoPair.first);
        String logoUrl = (jsonObject.has("url")) ?
                jsonObject.optString("url") : jsonObject.optString("defaultUrl");
        String siteName = serverInfoPair.second;
        RocketChatCache.INSTANCE.addHostname(hostname.toLowerCase(), logoUrl, siteName);
        return RocketChatCache.INSTANCE.getServerList();
    }

    private void openRoom() {
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        String roomId = RocketChatCache.INSTANCE.getSelectedRoomId();

        if (roomId == null || roomId.length() == 0) {
            view.showHome();
            return;
        }

        onOpenRoom(hostname, roomId);
    }

    private void subscribeToUnreadCount() {
        final Disposable subscription = Flowable.combineLatest(
                roomInteractor.getTotalUnreadRoomsCount(),
                roomInteractor.getTotalUnreadMentionsCount(),
                (Pair::new)
        )
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> view.showUnreadCount(pair.first, pair.second),
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void subscribeToSession() {
        final Disposable subscription = sessionInteractor.getDefault()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sessionOptional -> {
                            Session session = sessionOptional.orNull();
                            if (session == null || session.getToken() == null) {
                                view.showLoginScreen();
                                return;
                            }

                            String error = session.getError();
                            if (error != null && error.length() != 0) {
                                view.showConnectionError();
                                return;
                            }

                            if (!session.isTokenVerified()) {
                                view.showConnecting();
                                return;
                            }
                            // TODO: Should we remove below and above calls to view?
                             view.showConnectionOk();
                            RocketChatCache.INSTANCE.setSessionToken(session.getToken());
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void subscribeToNetworkChanges() {
        Disposable disposable = connectivityManagerApi.getServerConnectivityAsObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        connectivity -> {
                            if (connectivity.state == ServerConnectivity.STATE_CONNECTED) {
                                //TODO: notify almost connected or something like that.
//                                view.showConnectionOk();
                            } else if (connectivity.state == ServerConnectivity.STATE_DISCONNECTED) {
                                if (connectivity.code == DDPClient.REASON_NETWORK_ERROR) {
                                    view.showConnectionError();
                                }
                            } else if (connectivity.state == ServerConnectivity.STATE_SESSION_ESTABLISHED) {
                                setUserOnline();
                                view.refreshRoom();
                                view.showConnectionOk();
                            } else {
                                view.showConnecting();
                            }
                        },
                        RCLog::e
                );

        addSubscription(disposable);
    }

    private void setUserOnline() {
        methodCallHelper.setUserPresence(User.STATUS_ONLINE)
                .continueWith(new LogIfError());
    }

    private void setUserAway() {
        methodCallHelper.setUserPresence(User.STATUS_AWAY)
                .continueWith(new LogIfError());
    }

    private boolean shouldLaunchAddServerActivity() {
        return connectivityManagerApi.getServerList().isEmpty();
    }
}
