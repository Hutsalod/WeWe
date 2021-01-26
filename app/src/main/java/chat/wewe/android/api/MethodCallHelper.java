package chat.wewe.android.api;

import android.content.Context;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.helper.CheckSum;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.android.log.RCLog;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android_ddp.DDPClient;
import chat.wewe.android_ddp.DDPClientCallback;
import chat.wewe.core.PublicSettingsConstants;
import chat.wewe.core.SyncState;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.models.ddp.RealmMessage;
import chat.wewe.persistence.realm.models.ddp.RealmPermission;
import chat.wewe.persistence.realm.models.ddp.RealmPublicSetting;
import chat.wewe.persistence.realm.models.ddp.RealmRoom;
import chat.wewe.persistence.realm.models.ddp.RealmRoomRole;
import chat.wewe.persistence.realm.models.ddp.RealmSpotlight;
import chat.wewe.persistence.realm.models.ddp.RealmSpotlightRoom;
import chat.wewe.persistence.realm.models.ddp.RealmSpotlightUser;
import chat.wewe.persistence.realm.models.internal.MethodCall;
import chat.wewe.persistence.realm.models.internal.RealmSession;
import hugo.weaving.DebugLog;
import io.realm.RealmQuery;
import okhttp3.HttpUrl;

/**
 * Utility class for creating/handling MethodCall or RPC.
 * <p>
 * TODO: separate method into several manager classes (SubscriptionManager, MessageManager, ...).
 */
public class MethodCallHelper {

    protected static final long TIMEOUT_MS = 25000;
    protected static final Continuation<String, Task<JSONObject>> CONVERT_TO_JSON_OBJECT =
            task -> Task.forResult(new JSONObject(task.getResult()));
    protected static final Continuation<String, Task<JSONArray>> CONVERT_TO_JSON_ARRAY =
            task -> Task.forResult(new JSONArray(task.getResult()));
    protected final Context context;
    protected final RealmHelper realmHelper;

    /**
     * initialize with Context and hostname.
     */
    public MethodCallHelper(Context context, String hostname) {
        this.context = context.getApplicationContext();
        this.realmHelper = RealmStore.getOrCreate(hostname);
    }

    /**
     * initialize with RealmHelper and DDPClient.
     */
    public MethodCallHelper(RealmHelper realmHelper) {
        this.context = null;
        this.realmHelper = realmHelper;
    }

    public MethodCallHelper(Context context, RealmHelper realmHelper) {
        this.context = context.getApplicationContext();
        this.realmHelper = realmHelper;
    }

    @DebugLog
    private Task<String> executeMethodCall(String methodName, String param, long timeout) {
        if (DDPClient.get() != null) {
            return DDPClient.get().rpc(UUID.randomUUID().toString(), methodName, param, timeout)
                    .onSuccessTask(task -> Task.forResult(task.getResult().result))
                    .continueWithTask(task_ -> {
                        if (task_.isFaulted()) {
                            return Task.forError(task_.getError());
                        }
                        return Task.forResult(task_.getResult());
                    });
        } else {
            return MethodCall.execute(realmHelper, methodName, param, timeout)
                    .onSuccessTask(task -> {
                        ConnectivityManager.getInstance(context.getApplicationContext())
                                .keepAliveServer();
                        return task;
                    });
        }
    }

    public Task<Void> deleteRooms(final String roomId,final String UF_ROCKET_ID) {
        return call("eraseRoom", TIMEOUT_MS, () -> new JSONArray().put(roomId).put(UF_ROCKET_ID))
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray roomRoles = task.getResult();
                    for (int i = 0; i < roomRoles.length(); i++) {
                        RealmRoomRole.customizeJson(roomRoles.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmRoomRole.class);
                        realm.createOrUpdateAllFromJson(RealmRoomRole.class, roomRoles);
                        return null;
                    });
                });
    }

    private Task<String> injectErrorHandler(Task<String> task) {
        return task.continueWithTask(_task -> {
            if (_task.isFaulted()) {
                Exception exception = _task.getError();
                if (exception instanceof MethodCall.Error || exception instanceof DDPClientCallback.RPC.Error) {
                    String errMessageJson;
                    if (exception instanceof DDPClientCallback.RPC.Error) {
                        errMessageJson = ((DDPClientCallback.RPC.Error) exception).error.toString();
                    } else {
                        errMessageJson = exception.getMessage();
                    }
                    if (TextUtils.isEmpty(errMessageJson)) {
                        return Task.forError(exception);
                    }
                    String errType = new JSONObject(errMessageJson).optString("error");
                    String errMessage = new JSONObject(errMessageJson).getString("message");

                    if (TwoStepAuthException.TYPE.equals(errType)) {
                        return Task.forError(new TwoStepAuthException(errMessage));
                    }
                    return Task.forError(new Exception(errMessage));
                } else if (exception instanceof DDPClientCallback.RPC.Timeout) {
                    return Task.forError(new MethodCall.Timeout());
                } else if (exception instanceof DDPClientCallback.Closed) {
                    return Task.forError(new Exception(exception.getMessage()));
                } else {
                    return Task.forError(exception);
                }
            } else {
                return _task;
            }
        });
    }

    protected final Task<String> call(String methodName, long timeout) {
        return injectErrorHandler(executeMethodCall(methodName, null, timeout));
    }

    protected final Task<String> call(String methodName, long timeout, ParamBuilder paramBuilder) {
        try {
            final JSONArray params = paramBuilder.buildParam();
            return injectErrorHandler(executeMethodCall(methodName,
                    params != null ? params.toString() : null, timeout));
        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    /**
     * Register RealmUser.
     */
    public Task<String> registerUser(final String name, final String email,
                                     final String password, final String confirmPassword) {
        return call("registerUser", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
                .put("name", name)
                .put("email", email)
                .put("pass", password)
                .put("confirm-pass", confirmPassword))); // nothing to do.
    }

    private Task<Void> saveToken(Task<String> task) {
        return realmHelper.executeTransaction(realm ->
                realm.createOrUpdateObjectFromJson(RealmSession.class, new JSONObject()
                        .put("sessionId", RealmSession.DEFAULT_ID)
                        .put("token", task.getResult())
                        .put("tokenVerified", true)
                        .put("error", JSONObject.NULL)
                ));
    }

    /**
     * set current user's name.
     */
    public Task<String> setUsername(final String username) {
        return call("setUsername", TIMEOUT_MS, () -> new JSONArray().put(username));
    }

    public Task<Void> joinDefaultChannels() {
        return call("joinDefaultChannels", TIMEOUT_MS)
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void> joinRoom(String roomId) {
        return call("joinRoom", TIMEOUT_MS, () -> new JSONArray().put(roomId))
                .onSuccessTask(task -> Task.forResult(null));
    }

    /**
     * Login with username/email and password.
     */
    public Task<Void> loginWithEmail(final String usernameOrEmail, final String password) {
        return call("login", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
                param.put("user", new JSONObject().put("email", usernameOrEmail));
            } else {
                param.put("user", new JSONObject().put("username", usernameOrEmail));
            }
            param.put("password", new JSONObject()
                    .put("digest", CheckSum.sha256(password))
                    .put("algorithm", "sha-256"));
            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken);
    }

    public Task<Void> loginWithLdap(final String username, final String password) {
        return call("login", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();

            param.put("ldap", true);
            param.put("username", username);
            param.put("ldapPass", password);
            param.put("ldapOptions", new JSONObject());

            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken);
    }

    /**
     * Login with OAuth.
     */
    public Task<Void> loginWithOAuth(final String credentialToken,
                                     final String credentialSecret) {
        return call("login", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
                .put("oauth", new JSONObject()
                        .put("credentialToken", credentialToken)
                        .put("credentialSecret", credentialSecret))
        )).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken);
    }

    /**
     * Login with token.
     */
    public Task<Void> loginWithToken(final String token) {
        return call("login", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
                .put("resume", token)
        )).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken)
                .continueWithTask(task -> {
                    if (task.isFaulted()) {
                        RealmSession.logError(realmHelper, task.getError());
                    }
                    return task;
                });
    }


    public Task<Void> twoStepCodeLogin(final String usernameOrEmail, final String password,
                                       final String twoStepCode) {
        return call("login", TIMEOUT_MS, () -> {
            JSONObject loginParam = new JSONObject();
            if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
                loginParam.put("user", new JSONObject().put("email", usernameOrEmail));
            } else {
                loginParam.put("user", new JSONObject().put("username", usernameOrEmail));
            }
            loginParam.put("password", new JSONObject()
                    .put("digest", CheckSum.sha256(password))
                    .put("algorithm", "sha-256"));

            JSONObject twoStepParam = new JSONObject();
            twoStepParam.put("login", loginParam);
            twoStepParam.put("code", twoStepCode);

            JSONObject param = new JSONObject();
            param.put("totp", twoStepParam);

            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken);
    }

    /**
     * Logout.
     */
    public Task<Void> logout() {
        return call("logout", TIMEOUT_MS).onSuccessTask(task -> {
            if (task.isFaulted()) {
                return Task.forError(task.getError());
            }
            return null;
        });
    }

    /**
     * request "subscriptions/get".
     */
    public Task<Void> getRoomSubscriptions() {
        return call("subscriptions/get", TIMEOUT_MS).onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray result = task.getResult();
                    try {
                        for (int i = 0; i < result.length(); i++) {
                            RealmRoom.customizeJson(result.getJSONObject(i));
                        }

                        return realmHelper.executeTransaction(realm -> {
                            realm.delete(RealmRoom.class);
                            realm.createOrUpdateAllFromJson(
                                    RealmRoom.class, result);

                            JSONObject openedRooms = RocketChatCache.INSTANCE.getOpenedRooms();

                            RealmQuery<RealmRoom> query = realm.where(RealmRoom.class);
                            Iterator<String> keys = openedRooms.keys();
                            while (keys.hasNext()) {
                                String rid = keys.next();
                                RealmRoom realmRoom = query.equalTo(RealmRoom.ID, rid).findFirst();
                                if (realmRoom == null) {
                                    RocketChatCache.INSTANCE.removeOpenedRoom(rid);
                                } else {
                                    loadMissedMessages(rid, realmRoom.getLastSeen())
                                            .continueWithTask(task1 -> {
                                                if (task1.isFaulted()) {
                                                    Exception error = task1.getError();
                                                    RCLog.e(error);
                                                }
                                                return null;
                                            });
                                }
                            }
                            return null;
                        });
                    } catch (JSONException exception) {
                        return Task.forError(exception);
                    }
                });
    }

    public Task<JSONArray> loadMissedMessages(final String roomId, final long timestamp) {

        return call("loadMissedMessages", TIMEOUT_MS, () -> new JSONArray()
                .put(roomId)
                .put(timestamp > 0 ? new JSONObject().put("$date", timestamp) : JSONObject.NULL)
        ).onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    JSONArray result = task.getResult();
                    for (int i = 0; i < result.length(); i++) {
                        RealmMessage.customizeJson(result.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        if (timestamp == 0) {
                            realm.where(RealmMessage.class)
                                    .equalTo("rid", roomId)
                                    .equalTo("syncstate", SyncState.SYNCED)
                                    .findAll().deleteAllFromRealm();
                        }
                        if (result.length() > 0) {
                            realm.createOrUpdateAllFromJson(RealmMessage.class, result);
                        }
                        return null;
                    }).onSuccessTask(_task -> Task.forResult(result));
                });
    }

    /**
     * Load messages for room.
     */
    public Task<JSONArray> loadHistory(final String roomId, final long timestamp,
                                       final int count, final long lastSeen) {
        return call("loadHistory", TIMEOUT_MS, () -> new JSONArray()
                .put(roomId)
                .put(timestamp > 0 ? new JSONObject().put("$date", timestamp) : JSONObject.NULL)
                .put(count)
                .put(lastSeen > 0 ? new JSONObject().put("$date", lastSeen) : JSONObject.NULL)
        ).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> {
                    JSONObject result = task.getResult();
                    final JSONArray messages = result.getJSONArray("messages");
                    for (int i = 0; i < messages.length(); i++) {
                        RealmMessage.customizeJson(messages.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        if (timestamp == 0) {
                            realm.where(RealmMessage.class)
                                    .equalTo("rid", roomId)
                                    .equalTo("syncstate", SyncState.SYNCED)
                                    .findAll().deleteAllFromRealm();
                        }
                        if (messages.length() > 0) {
                            realm.createOrUpdateAllFromJson(RealmMessage.class, messages);
                        }
                        return null;
                    }).onSuccessTask(_task -> Task.forResult(messages));
                });
    }

    /**
     * update user's status.
     */
    public Task<Void> setUserStatus(final String status) {
        return call("UserPresence:setDefaultStatus", TIMEOUT_MS, () -> new JSONArray().put(status))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void> setUserPresence(final String status) {
        return call("UserPresence:" + status, TIMEOUT_MS)
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<JSONObject> getUsersOfRoom(final String roomId, final boolean showAll) {
        return call("getUsersOfRoom", TIMEOUT_MS, () -> new JSONArray().put(roomId).put(showAll))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    public Task<Void> createChannel(final String name,final String fname, final boolean readOnly) {
        return call("createChannel", TIMEOUT_MS, () -> new JSONArray()
                .put(name)
                .put(new JSONArray())
                .put(readOnly)
                .put(fname))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void> createPrivateGroup(final String name, final String fname, final boolean readOnly) {
        return call("createPrivateGroup", TIMEOUT_MS, () -> new JSONArray()
                .put(name)
                .put(new JSONArray())
                .put(fname)
                .put(readOnly))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<String> createDirectMessage(final String username) {
        return call("createDirectMessage", TIMEOUT_MS, () -> new JSONArray().put(username))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("rid")));
    }

    /**
     * send message.
     */
    public Task<Void> sendMessage(String messageId, String roomId, String msg, long editedAt) {
        try {
            JSONObject messageJson = new JSONObject()
                    .put("_id", messageId)
                    .put("rid", roomId)
                    .put("msg", msg);

            if (editedAt == 0) {
                return sendMessage(messageJson);
            } else {
                return updateMessage(messageJson);
            }
        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    public Task<Void> deleteMessage(String messageID) {
        try {
            JSONObject messageJson = new JSONObject()
                    .put("_id", messageID);

            return deleteMessage(messageJson);
        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    /**
     * Send message object.
     */
    private Task<Void> sendMessage(final JSONObject messageJson) {
        return call("sendMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> updateMessage(final JSONObject messageJson) {
        return call("updateMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> deleteMessage(final JSONObject messageJson) {
        return call("deleteMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    /**
     * mark all messages are read in the room.
     */
    public Task<Void> readMessages(final String roomId) {
        return call("readMessages", TIMEOUT_MS, () -> new JSONArray().put(roomId))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void>  hideAndEraseRooms(final String Id) {
        return call("hideAndEraseRooms", TIMEOUT_MS, () -> new JSONArray().put(Id))
                .onSuccessTask(task -> Task.forResult(null));
    }

        public Task<Void> getPublicSettings(String currentHostname) {
        return call("public-settings/get", TIMEOUT_MS)
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray settings = task.getResult();
                    String siteUrl = null;
                    String siteName = null;
                    for (int i = 0; i < settings.length(); i++) {
                        JSONObject jsonObject = settings.getJSONObject(i);
                        RealmPublicSetting.customizeJson(jsonObject);
                        if (isPublicSetting(jsonObject, PublicSettingsConstants.General.SITE_URL)) {
                            siteUrl = jsonObject.getString(RealmPublicSetting.VALUE);
                        } else if (isPublicSetting(jsonObject, PublicSettingsConstants.General.SITE_NAME)) {
                            siteName = jsonObject.getString(RealmPublicSetting.VALUE);
                        }
                    }

                    if (siteName != null && siteUrl != null) {
                        HttpUrl httpSiteUrl = HttpUrl.parse(siteUrl);
                        if (httpSiteUrl != null) {
                            String host = httpSiteUrl.host();
                            RocketChatCache.INSTANCE.addSiteUrl(host, currentHostname);
                            RocketChatCache.INSTANCE.addSiteName(currentHostname, siteName);
                        }
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmPublicSetting.class);
                        realm.createOrUpdateAllFromJson(RealmPublicSetting.class, settings);
                        return null;
                    });
                });
    }

    private boolean isPublicSetting(JSONObject jsonObject, String id) {
        return jsonObject.optString(RealmPublicSetting.ID).equalsIgnoreCase(id);
    }

    public Task<Void> getPermissions() {
        return call("permissions/get", TIMEOUT_MS)
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray permissions = task.getResult();
                    for (int i = 0; i < permissions.length(); i++) {
                        RealmPermission.customizeJson(permissions.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmPermission.class);
                        realm.createOrUpdateAllFromJson(RealmPermission.class, permissions);
                        return null;
                    });
                });
    }

    public Task<Void> getRoomRoles(final String roomId) {
        return call("getRoomRoles", TIMEOUT_MS, () -> new JSONArray().put(roomId))
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray roomRoles = task.getResult();
                    for (int i = 0; i < roomRoles.length(); i++) {
                        RealmRoomRole.customizeJson(roomRoles.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmRoomRole.class);
                        realm.createOrUpdateAllFromJson(RealmRoomRole.class, roomRoles);
                        return null;
                    });
                });
    }

    public Task<Void> searchSpotlightUsers(String term) {
        return searchSpotlight(RealmSpotlightUser.class, "users", term);
    }

    public Task<Void> searchSpotlightRooms(String term) {
        return searchSpotlight(RealmSpotlightRoom.class, "rooms", term);
    }

    public Task<Void> searchSpotlight(String term) {
        return call("spotlight", TIMEOUT_MS, () ->
                new JSONArray()
                        .put(term)
                        .put(JSONObject.NULL)
                        .put(new JSONObject().put("rooms", true).put("users", true))
        ).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> {
                    String jsonString = null;
                    final JSONObject result = task.getResult();

                    JSONArray roomJsonArray = (JSONArray) result.get("rooms");
                    int roomTotal = roomJsonArray.length();
                    if (roomTotal > 0) {
                        for (int i = 0; i < roomTotal; ++i) {
                            RealmSpotlight.Companion.customizeRoomJSONObject(roomJsonArray.getJSONObject(i));
                        }
                        jsonString = roomJsonArray.toString();
                    }

                    JSONArray userJsonArray = (JSONArray) result.get("users");
                    int usersTotal = userJsonArray.length();
                    if (usersTotal > 0) {
                        for (int i = 0; i < usersTotal; ++i) {
                            RealmSpotlight.Companion.customizeUserJSONObject(userJsonArray.getJSONObject(i));
                        }

                        if (jsonString == null) {
                            jsonString = userJsonArray.toString();
                        } else {
                            jsonString = jsonString.replace("]", "") + "," + userJsonArray.toString().replace("[", "");
                        }
                    }

                    if (jsonString != null) {
                        String jsonStringResults = jsonString;
                        realmHelper.executeTransaction(realm -> {
                            realm.delete(RealmSpotlight.class);
                            realm.createOrUpdateAllFromJson(RealmSpotlight.class, jsonStringResults);
                            return null;
                        });
                    }
                    return null;
                });
    }

    private Task<Void> searchSpotlight(Class clazz, String key, String term) {
        return call("spotlight", TIMEOUT_MS, () -> new JSONArray()
                .put(term)
                .put(JSONObject.NULL)
                .put(new JSONObject().put(key, true)))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> {
                    final JSONObject result = task.getResult();
                    if (!result.has(key)) {
                        return null;
                    }

                    Object items = result.get(key);
                    if (!(items instanceof JSONArray)) {
                        return null;
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(clazz);
                        realm.createOrUpdateAllFromJson(clazz, (JSONArray) items);
                        return null;
                    });
                });
    }

    //Переслать сообщение
    public Task<Void> forwardMessage(String id, String ridDestination,String ridSourse) {
        try {
            JSONObject messageJson = new JSONObject()
                    .put("id", id)
                    .put("ridDestination",ridDestination)
                    .put("ridSourse", ridSourse);
            return forwardMessage(messageJson);

        } catch (JSONException exception) {
            Log.d("XSWQAZ","ERRRO"+exception);
            return Task.forError(exception);
        }
    }

    //Добавить задачу
    public Task<Void> AddTask(String roomId, String uid,String name, String taskText, String username,String responsible,int priority,int day,int time) {
        try {

            Log.d("XSWQAZ",""+RealmSession.ID);
            JSONObject messageJson = new JSONObject()
                    .put("rid", roomId)
                    .put("uid",uid)
                    .put("name", name)
                    .put("taskText", taskText)
                    .put("username", username)
                    .put("responsible", responsible)
                    .put("priority", priority)
                    .put("deadline", new JSONArray().put(day).put(time));
            Log.d("XSWQAZ",""+messageJson);
            return AddTask(messageJson);

        } catch (JSONException exception) {
            Log.d("XSWQAZ","ERRRO"+exception);
            return Task.forError(exception);
        }
    }

    //Получить список задач для канала





    //Закрыть задачу
    public Task<Void> closeTask(String roomId,Integer numberId,String username) {
        try {

            Log.d("XSWQAZ",""+RealmSession.ID);
            JSONObject messageJson = new JSONObject()
                    .put("rid", roomId)
                    .put("numberId", numberId)
                    .put("username", username);
            return closeTask(messageJson);

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    //Закрыть задачу
    public Task<Void> removeTask(String roomId,Integer numberId,String username) {
        try {

            Log.d("XSWQAZ",""+RealmSession.ID);
            JSONObject messageJson = new JSONObject()
                    .put("rid", roomId)
                    .put("numberId", numberId)
                    .put("uid", username);
            return removeTask(messageJson);

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    //Закрыть задачу
    public Task<Void> addCheckListItem(String rid,String uid,int numberId,int itemId,String itemText) {
        try {

            Log.d("XSWQAZ",""+RealmSession.ID);
            JSONObject messageJson = new JSONObject()
                    .put("rid", rid)
                    .put("uid", uid)
                    .put("numberId", numberId)
                    .put("itemId", itemId)
                    .put("itemText", itemText);
            return addCheckListItem(messageJson);

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    //Закрыть задачу
    public Task<Void> removeCheckListItem(String rid,String uid,Integer numberId,Integer itemId) {
        try {

            Log.d("XSWQAZ",""+RealmSession.ID);
            JSONObject messageJson = new JSONObject()
                    .put("rid", rid)
                    .put("uid", uid)
                    .put("numberId", numberId)
                    .put("itemId", itemId);
            return removeCheckListItem(messageJson);

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    //Закрыть задачу
    public Task<Void> checkUncheckItem(String rid,String uid,int numberId,int itemId,Boolean isCheck) {
        try {

            Log.d("XSWQAZ",""+RealmSession.ID);
            JSONObject messageJson = new JSONObject()
                    .put("rid", rid)
                    .put("uid", uid)
                    .put("numberId", numberId)
                    .put("itemId", itemId)
                    .put("isCheck", isCheck);
            return checkUncheckItem(messageJson);

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    //Вспомогательные методы
 /*   public Task<JSONObject> getLastMessageByRoomId(String roomId) {
        try {

            JSONObject messageJson = new JSONObject()
                    .put("rid", roomId);
            return getLastMessageByRoomId(messageJson);

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }*/


    public Task<JSONObject> AddMsgToTask(String rid, int numberId, String msgText, String uid) {
        try {
            JSONObject messageJson = new JSONObject()
                    .put("rid", rid)
                    .put("numberId", numberId)
                    .put("msgText", msgText)
                    .put("uid", uid);


            return AddMsgToTask(messageJson);

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    public Task<Void> sendMessageTime(String messageId, String roomId, String msg) {
        try {

            JSONObject messageJson = new JSONObject()
                    .put("_id", messageId)
                    .put("rid", roomId)
                    .put("msg", msg);


            return sendMessageTime(messageJson,RocketChatCache.INSTANCE.getUserId());

        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }



    public Task<Void> usersMessage(Boolean success,String type,String fromUserId,String toUserId,String rid) {
        try {
            JSONObject messageJson = new JSONObject()
                    .put("success", success)
                    .put("type", type)
                    .put("fromUserId", fromUserId)
                    .put("toUserId", toUserId)
                    .put("rid", rid);
            Log.d("XSWQAZ",""+messageJson);
            return usersMessage(messageJson);
        } catch (JSONException exception) {

            return Task.forError(exception);

        }
    }

    private Task<Void> AddTask(final JSONObject messageJson) {
        return call("addTask", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> forwardMessage(final JSONObject messageJson) {
        return call("forwardMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<JSONArray> getTask(final String status) {
        return call("getTasks", TIMEOUT_MS, () -> new JSONArray().put(status))
                .onSuccessTask(CONVERT_TO_JSON_ARRAY);
    }

    public Task<JSONObject> getOneTask(final String rid,final int numberId) {
        return call("getTask", TIMEOUT_MS, () -> new JSONArray().put(rid).put(numberId))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    public Task<JSONArray> rooms_get() {
        return call("subscriptions/get", TIMEOUT_MS, () -> new JSONArray())
                .onSuccessTask(CONVERT_TO_JSON_ARRAY);
    }

    public Task<JSONObject> getAllTaskAndUsersByUserId(final String status) {
        return call("getAllTaskAndUsersByUserId", TIMEOUT_MS, () -> new JSONArray().put(status))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    private Task<JSONObject> AddMsgToTask(final JSONObject messageJson) {
        return call("AddMsgToTask", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    public Task<Void> updateTask(final JSONObject messageJson) {
        return call("updateTask", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> closeTask(final JSONObject messageJson) {
        return call("closeTask", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> removeTask(final JSONObject messageJson) {
        return call("removeTask", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> addCheckListItem(final JSONObject messageJson) {
        return call("addCheckListItem", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> removeCheckListItem(final JSONObject messageJson) {
        return call("removeCheckListItem", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    private Task<Void> checkUncheckItem(final JSONObject messageJson) {
        return call("checkUncheckItem", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<JSONObject> getLastMessageByRoomId(final String messageJson) {
        return call("getLastMessageByRoomId", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }




    public Task<JSONArray> getUsersByRoomId(final String messageJson) {
        return call("getUsersByRoomId", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(CONVERT_TO_JSON_ARRAY);
    }

    private Task<Void> sendMessageTime(final JSONObject messageJson,final  String Id) {
        Log.d("TEST_WEWE",""+messageJson + " " + Id);
        return call("sendMessageToDeleteAfterRead", TIMEOUT_MS, () -> new JSONArray().put(messageJson).put(Id))
                .onSuccessTask(task -> Task.forResult(null));
    }


    private Task<Void> usersMessage(final JSONObject messageJson) {


        return call("sendInfoCallMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }


    protected interface ParamBuilder {
        JSONArray buildParam() throws JSONException;
    }
}
