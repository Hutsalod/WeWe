package chat.wewe.android.fragment.chatroom;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import chat.wewe.android.BackgroundLooper;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.log.RCLog;
import chat.wewe.android.service.ConnectivityManagerApi;
import chat.wewe.android.shared.BasePresenter;
import chat.wewe.core.SyncState;
import chat.wewe.core.interactors.MessageInteractor;
import chat.wewe.core.models.Message;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.Settings;
import chat.wewe.core.models.User;
import chat.wewe.core.repositories.RoomRepository;
import chat.wewe.core.repositories.UserRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RoomPresenter extends BasePresenter<RoomContract.View>
        implements RoomContract.Presenter {

    private final String roomId;
    private final MessageInteractor messageInteractor;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final AbsoluteUrlHelper absoluteUrlHelper;
    private final MethodCallHelper methodCallHelper;
    private final ConnectivityManagerApi connectivityManagerApi;
    private Room currentRoom;

    /* package */RoomPresenter(String roomId,
                               UserRepository userRepository,
                               MessageInteractor messageInteractor,
                               RoomRepository roomRepository,
                               AbsoluteUrlHelper absoluteUrlHelper,
                               MethodCallHelper methodCallHelper,
                               ConnectivityManagerApi connectivityManagerApi) {
        this.roomId = roomId;
        this.userRepository = userRepository;
        this.messageInteractor = messageInteractor;
        this.roomRepository = roomRepository;
        this.absoluteUrlHelper = absoluteUrlHelper;
        this.methodCallHelper = methodCallHelper;
        this.connectivityManagerApi = connectivityManagerApi;
    }

    @Override
    public void bindView(@NonNull RoomContract.View view) {
        super.bindView(view);
        refreshRoom();
    }

    @Override
    public void refreshRoom() {
        getRoomRoles();
        getRoomInfo();
        getRoomHistoryStateInfo();
        getMessages();
        getUserPreferences();
    }

    @Override
    public void loadMessages() {
        final Disposable subscription = getSingleRoom()
                .flatMap(messageInteractor::loadMessages)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (!success) {
                                connectivityManagerApi.keepAliveServer();
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void loadMoreMessages() {
        final Disposable subscription = getSingleRoom()
                .flatMap(messageInteractor::loadMoreMessages)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (!success) {
                                connectivityManagerApi.keepAliveServer();
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void onMessageSelected(@Nullable Message message) {
        if (message == null) {
            return;
        }

        if (message.getSyncState() == SyncState.DELETE_FAILED) {
            view.showMessageDeleteFailure(message);
        } else if (message.getSyncState() == SyncState.FAILED) {
            view.showMessageSendFailure(message);
        } else if (message.getType() == null && message.getSyncState() == SyncState.SYNCED) {
            // If message is not a system message show applicable actions.
            view.showMessageActions(message);
        }
    }

    @Override
    public void onMessageTap(@Nullable Message message) {
        if (message == null) {
            return;
        }

        if (message.getSyncState() == SyncState.FAILED) {
            view.showMessageSendFailure(message);
        }
    }

    @Override
    public void replyMessage(@NonNull Message message, boolean justQuote) {
        final Disposable subscription = this.absoluteUrlHelper.getRocketChatAbsoluteUrl()
                .cache()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        serverUrl -> {
                            if (serverUrl.isPresent()) {
                                RocketChatAbsoluteUrl absoluteUrl = serverUrl.get();
                                String baseUrl = absoluteUrl.getBaseUrl();
                                view.onReply(absoluteUrl, buildReplyOrQuoteMarkdown(baseUrl, message, justQuote), message);
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    public void acceptMessageDeleteFailure(Message message) {
        final Disposable subscription = messageInteractor.acceptDeleteFailure(message)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        addSubscription(subscription);
    }

    @Override
    public void loadMissedMessages() {
        RocketChatApplication appContext = RocketChatApplication.getInstance();
        JSONObject openedRooms = RocketChatCache.INSTANCE.getOpenedRooms();
        if (openedRooms.has(roomId)) {
            try {
                JSONObject room = openedRooms.getJSONObject(roomId);
                String rid = room.optString("rid");
                long ls = room.optLong("ls");
                methodCallHelper.loadMissedMessages(rid, ls)
                        .continueWith(new LogIfError());
            } catch (JSONException e) {
                RCLog.e(e);
            }
        }
    }

    private String buildReplyOrQuoteMarkdown(String baseUrl, Message message, boolean justQuote) {
        if (currentRoom == null || message.getUser() == null) {
            return "";
        }

        if (currentRoom.isDirectMessage()) {
            return String.format("[ ](%s/direct/%s?msg=%s) ", baseUrl,
                    message.getUser().getUsername(),
                    message.getId());
        } else {
            return String.format("[ ](%s/channel/%s?msg=%s) %s", baseUrl,
                    currentRoom.getName(),
                    message.getId(),
                    justQuote ? "" : "@" + message.getUser().getUsername() + " ");
        }
    }

    @Override
    public void sendMessage(String messageText) {
        view.disableMessageInput();
        final Disposable subscription = getRoomUserPair()
                .flatMap(pair -> messageInteractor.send(pair.first, pair.second, messageText))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (success) {
                                view.onMessageSendSuccessfully();
                            }
                            view.enableMessageInput();
                        },
                        throwable -> {
                            view.enableMessageInput();
                            Logger.INSTANCE.report(throwable);
                        }
                );

        addSubscription(subscription);
    }

    @Override
    public void resendMessage(@NonNull Message message) {
        final Disposable subscription = getCurrentUser()
                .flatMap(user -> messageInteractor.resend(message, user))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        addSubscription(subscription);
    }

    @Override
    public void updateMessage(@NonNull Message message, String content) {
        view.disableMessageInput();
        final Disposable subscription = getCurrentUser()
                .flatMap(user -> messageInteractor.update(message, user, content))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (success) {
                                view.onMessageSendSuccessfully();
                            }
                            view.enableMessageInput();
                        },
                        throwable -> {
                            view.enableMessageInput();
                            Logger.INSTANCE.report(throwable);
                        }
                );

        addSubscription(subscription);
    }

    @Override
    public void deleteMessage(@NonNull Message message) {
        final Disposable subscription = messageInteractor.delete(message)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        addSubscription(subscription);
    }

    @Override
    public void onUnreadCount() {
        final Disposable subscription = getRoomUserPair()
                .flatMap(roomUserPair -> messageInteractor
                        .unreadCountFor(roomUserPair.first, roomUserPair.second))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> view.showUnreadCount(count),
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void onMarkAsRead() {
        final Disposable subscription = roomRepository.getById(roomId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .filter(Room::isAlert)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        room -> methodCallHelper.readMessages(room.getRoomId())
                                .continueWith(new LogIfError()),
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void getRoomRoles() {
        methodCallHelper.getRoomRoles(roomId);
    }

    private void getRoomInfo() {
        final Disposable subscription = roomRepository.getById(roomId)
                .distinctUntilChanged()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processRoom, Logger.INSTANCE::report);
        addSubscription(subscription);
    }

    private void processRoom(Room room) {
        this.currentRoom = room;
        view.render(room);

        if (room.isDirectMessage()) {
            getUserByUsername(room.getName());
        }
    }

    private void getUserByUsername(String username) {
        final Disposable disposable = userRepository.getByUsername(username)
                .distinctUntilChanged()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showUserStatus, Logger.INSTANCE::report);
        addSubscription(disposable);
    }

    private void getRoomHistoryStateInfo() {
        final Disposable subscription = roomRepository.getHistoryStateByRoomId(roomId)
                .distinctUntilChanged()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        roomHistoryState -> {
                            int syncState = roomHistoryState.getSyncState();
                            view.updateHistoryState(
                                    !roomHistoryState.isComplete(),
                                    syncState == SyncState.SYNCED || syncState == SyncState.FAILED
                            );
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void getMessages() {
        final Disposable subscription = Flowable.zip(roomRepository.getById(roomId),
                absoluteUrlHelper.getRocketChatAbsoluteUrl().toFlowable().cache(), Pair::new)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .map(pair -> {
                    view.setupWith(pair.second.orNull());
                    return pair.first;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(room -> {
                    RocketChatCache.INSTANCE.addOpenedRoom(room.getRoomId(), room.getLastSeen());
                    return room;
                })
                .flatMap(messageInteractor::getAllFrom)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::showMessages,
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void getUserPreferences() {
        final Disposable subscription = userRepository.getCurrent()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> user.getSettings() != null)
                .map(User::getSettings)
                .filter(settings -> settings.getPreferences() != null)
                .map(Settings::getPreferences)
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        preferences -> {
                            if (preferences.isAutoImageLoad()) {
                                view.autoloadImages();
                            } else {
                                view.manualLoadImages();
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void getAbsoluteUrl() {
        final Disposable subscription = absoluteUrlHelper.getRocketChatAbsoluteUrl()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        it -> view.setupWith(it.orNull()),
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private Single<Pair<Room, User>> getRoomUserPair() {
        return Single.zip(
                getSingleRoom(),
                getCurrentUser(),
                Pair::new
        );
    }

    private Single<Room> getSingleRoom() {
        return roomRepository.getById(roomId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

    private Single<User> getCurrentUser() {
        return userRepository.getCurrent()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }
}