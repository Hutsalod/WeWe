package chat.wewe.android.layouthelper.chatroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import chat.wewe.android.BackgroundLooper;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.helper.Logger;
import chat.wewe.core.interactors.EditMessageInteractor;
import chat.wewe.core.interactors.PermissionInteractor;
import chat.wewe.core.models.Message;
import chat.wewe.core.repositories.MessageRepository;
import chat.wewe.core.repositories.PermissionRepository;
import chat.wewe.core.repositories.PublicSettingRepository;
import chat.wewe.core.repositories.RoomRepository;
import chat.wewe.core.repositories.RoomRoleRepository;
import chat.wewe.core.repositories.UserRepository;
import chat.wewe.persistence.realm.repositories.RealmMessageRepository;
import chat.wewe.persistence.realm.repositories.RealmPermissionRepository;
import chat.wewe.persistence.realm.repositories.RealmPublicSettingRepository;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmRoomRoleRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MessagePopup {
    private static volatile MessagePopup singleton = null;
    private static final Action PROFILL_ACTION_INFO = new Action("Профиль", null, true);
    private static final Action REPLY_ACTION_INFO = new Action("Ответить  сообщение", null, true);
    private static final Action QUOTE_ACTION_INFO = new Action("Цитата сообщение", null, true);
    private static final Action FORWARD_ACTION_INFO = new Action("Переслать сообщение", null, true);
    private static final Action EDIT_ACTION_INFO = new Action("Редактировать сообщение", null, true);
    private static final Action COPY_ACTION_INFO = new Action("Копировать сообщение", null, true);
    private static final Action DELETE_ACTION_INFO = new Action("Удалить сообщение", null, false);
    private final List<Action> defaultActions = new ArrayList<>(5);
    private final List<Action> otherActions = new ArrayList<>();
    private Message message;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();



    private MessagePopup(Message message) {
        this.message = message;
    }

    private void showAvailableActionsOnly(Context context) {
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();

        EditMessageInteractor editMessageInteractor = getEditMessageInteractor(hostname);

        MessageRepository messageRepository = new RealmMessageRepository(hostname);
        Disposable disposable = messageRepository.getById(singleton.message.getId())
                .flatMap(it -> {
                    if (!it.isPresent()) {
                        return Single.just(Pair.<Message, Boolean>create(null, false));
                    }

                    Message message = it.get();

                    return Single.zip(
                            Single.just(message),
                            editMessageInteractor.isAllowed(message),
                            Pair::create
                    );
                })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> {
                            EDIT_ACTION_INFO.allowed = pair.second;
                            DELETE_ACTION_INFO.allowed = pair.second;
                            List<Action> allActions = singleton.defaultActions;
                            List<Action> allowedActions = new ArrayList<>(3);
                            for (int i = 0; i < allActions.size(); i++) {
                                Action action = allActions.get(i);
                                if (action.allowed) {
                                    allowedActions.add(action);
                                }
                            }
                            allowedActions.addAll(singleton.otherActions);
                            CharSequence[] items = new CharSequence[allowedActions.size()];
                            for (int j = 0; j < items.length; j++) {
                                items[j] = allowedActions.get(j).actionName;
                            }
                            new AlertDialog.Builder(context)
                                    .setItems(items, (dialog, index) -> {
                                        Action action = allowedActions.get(index);
                                        ActionListener actionListener = action.actionListener;
                                        if (actionListener != null) {
                                            actionListener.execute(singleton.message);
                                        }
                                    })
                                    .setOnCancelListener(dialog -> compositeDisposable.clear())
                                    .setOnDismissListener(dialog1 -> compositeDisposable.clear())
                                    .create()
                                    .show();
                        },
                        Logger.INSTANCE::report
                );
        compositeDisposable.add(disposable);
    }

    private void addDefaultActions() {
        singleton.defaultActions.add(PROFILL_ACTION_INFO);
        singleton.defaultActions.add(REPLY_ACTION_INFO);
        singleton.defaultActions.add(QUOTE_ACTION_INFO);
        singleton.defaultActions.add(FORWARD_ACTION_INFO);
        singleton.defaultActions.add(EDIT_ACTION_INFO);
        singleton.defaultActions.add(COPY_ACTION_INFO);
        singleton.defaultActions.add(DELETE_ACTION_INFO);
    }

    public static MessagePopup take(Message message) {
        if (singleton == null) {
            synchronized (MessagePopup.class) {
                if (singleton == null) {
                    singleton = new Builder(message).build();
                    singleton.addDefaultActions();
                }
            }
        }
        singleton.message = message;
        singleton.otherActions.clear();
        return singleton;
    }

    private Action getActionIfExists(Action action) {
        if (singleton.otherActions.contains(action)) {
            return singleton.otherActions.get(singleton.otherActions.indexOf(action));
        }
        if (singleton.defaultActions.contains(action)) {
            return singleton.defaultActions.get(singleton.defaultActions.indexOf(action));
        }
        return null;
    }

    public MessagePopup addAction(@NonNull CharSequence actionName, ActionListener actionListener) {
        List<Action> actions = singleton.otherActions;
        Action newAction = new Action(actionName, actionListener, true);
        Action existingAction = getActionIfExists(newAction);
        if (existingAction != null) {
            existingAction.actionListener = actionListener;
        } else {
            actions.add(newAction);
        }
        return singleton;
    }

    public MessagePopup setReplyAction(ActionListener actionListener) {
        REPLY_ACTION_INFO.actionListener = actionListener;
        return singleton;
    }

    public MessagePopup setEditAction(ActionListener actionListener) {
        EDIT_ACTION_INFO.actionListener = actionListener;
        return singleton;
    }

    public MessagePopup setCopyAction(ActionListener actionListener) {
        COPY_ACTION_INFO.actionListener = actionListener;
        return singleton;
    }

    public MessagePopup setProfil(ActionListener actionListener) {
        PROFILL_ACTION_INFO.actionListener = actionListener;
        return singleton;
    }

    public MessagePopup setDeleteAction(ActionListener actionListener) {
        DELETE_ACTION_INFO.actionListener = actionListener;
        return singleton;
    }

    public MessagePopup setForwardAction(ActionListener actionListener) {
        FORWARD_ACTION_INFO.actionListener = actionListener;
        return singleton;
    }

    public MessagePopup setQuoteAction(ActionListener actionListener) {
        QUOTE_ACTION_INFO.actionListener = actionListener;
        return singleton;
    }

    public void showWith(Context context) {
        showAvailableActionsOnly(context);
    }

    private EditMessageInteractor getEditMessageInteractor(String hostname) {
        UserRepository userRepository = new RealmUserRepository(hostname);
        RoomRoleRepository roomRoleRepository = new RealmRoomRoleRepository(hostname);
        PermissionRepository permissionRepository = new RealmPermissionRepository(hostname);

        PermissionInteractor permissionInteractor = new PermissionInteractor(
                userRepository,
                roomRoleRepository,
                permissionRepository
        );

        MessageRepository messageRepository = new RealmMessageRepository(hostname);
        RoomRepository roomRepository = new RealmRoomRepository(hostname);
        PublicSettingRepository publicSettingRepository = new RealmPublicSettingRepository(hostname);

        return new EditMessageInteractor(
                permissionInteractor,
                userRepository,
                messageRepository,
                roomRepository,
                publicSettingRepository
        );
    }

    private static class Builder {
        private final Message message;

        Builder(Message message) {
            if (message == null) {
                throw new IllegalArgumentException("Message must not be null");
            }
            this.message = message;
        }

        public MessagePopup build() {
            Message message = this.message;
            return new MessagePopup(message);
        }
    }

    public static class Action {
        private CharSequence actionName;
        private ActionListener actionListener;
        private boolean allowed;

        public Action(CharSequence actionName, ActionListener actionListener, boolean allowed) {
            this.actionName = actionName;
            this.actionListener = actionListener;
            this.allowed = allowed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Action that = (Action) o;

            return actionName.equals(that.actionName);
        }

        @Override
        public int hashCode() {
            return actionName.hashCode();
        }
    }

    public interface ActionListener {
        void execute(Message message);
    }
}
