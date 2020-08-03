package chat.wewe.android.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.utils.NotificationUtils;
import chat.wewe.android.vo.NotificationVO;

import static chat.wewe.android.push.PushConstants.ALERT;
import static chat.wewe.android.push.PushConstants.BADGE;
import static chat.wewe.android.push.PushConstants.COUNT;
import static chat.wewe.android.push.PushConstants.GCM_N;
import static chat.wewe.android.push.PushConstants.GCM_NOTIFICATION;
import static chat.wewe.android.push.PushConstants.GCM_NOTIFICATION_BODY;
import static chat.wewe.android.push.PushConstants.LOC_DATA;
import static chat.wewe.android.push.PushConstants.LOC_KEY;
import static chat.wewe.android.push.PushConstants.MSGCNT;
import static chat.wewe.android.push.PushConstants.PARSE_COM_DATA;
import static chat.wewe.android.push.PushConstants.SOUND;
import static chat.wewe.android.push.PushConstants.SOUNDNAME;
import static chat.wewe.android.push.PushConstants.SUMMARY_TEXT;
import static chat.wewe.android.push.PushConstants.TWILIO_BODY;
import static chat.wewe.android.push.PushConstants.TWILIO_SOUND;
import static chat.wewe.android.push.PushConstants.TWILIO_TITLE;
import static chat.wewe.android.push.PushConstants.UA_PREFIX;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgingService";
    private static final String TITLE = "title";
    private static final String EMPTY = "";
    private static final String MESSAGE = "message";
    private static final String BODY = "body";
    private static final String PARAMS = "params";
    private static final String ejson = "ejson";
    private static final String IMAGE = "image";
    private static final String ACTION = "action";
    private static final String WEWE = "WEWE";
    private static final String DATA = "notification";
    private static final String ACTION_DESTINATION = "action_destination";
    private static final String LOG_TAG = "GCMIntentService";


  /*  public void handleIntent(Intent intent){
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if(action.equals("com.google.android.c2dm.intent.RECEIVE")&&intent.getStringExtra("message_type")==null) {
            Bundle bundle = intent.getExtras();

            if ("call".equals(bundle.getString("msg_type")))
            {
                Intent srvIntent = new Intent(this, PortSipService.class);
                srvIntent.setAction(ACTION_PUSH_MESSAGE);
                startService(srvIntent);
            }

            if ("im".equals(bundle.getString("msg_type")))
            {
                String content = bundle.getString("msg_content");
                String from = bundle.getString("send_from");
                String to = bundle.getString("send_to");
                String pushid = bundle.getString("portsip-push-id");
                Intent srvIntent = new Intent(this, PortSipService.class);
                srvIntent.setAction(ACTION_PUSH_MESSAGE);
                startService(srvIntent);
            }
        }

        if (extras == null) {
            return;
        }

        Context applicationContext = getApplicationContext();

        extras = normalizeExtras(applicationContext, extras);

        PushNotificationHandler pushNotificationHandler = new PushNotificationHandler();

        pushNotificationHandler.showNotificationIfPossible(applicationContext, extras);
    }*/

    private Bundle normalizeExtras(Context context, Bundle extras) {
        Log.d(LOG_TAG, "normalize extras");
        Iterator<String> keyIterator = extras.keySet().iterator();
        Bundle newExtras = new Bundle();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            Log.d(LOG_TAG, "key = " + key);

            // If normalizeKeythe key is "data" or "message" and the value is a json object extract
            // This is to support parse.com and other services. Issue #147 and pull #218
            if (key.equals(PARSE_COM_DATA) || key.equals(MESSAGE)) {
                Object json = extras.get(key);
                // Make sure data is json object stringified
                if (json instanceof String && ((String) json).startsWith("{")) {
                    Log.d(LOG_TAG, "extracting nested message data from key = " + key);
                    try {
                        // If object contains message keys promote each value to the root of the bundle
                        JSONObject data = new JSONObject((String) json);
                        if (data.has(ALERT) || data.has(MESSAGE) || data.has(BODY) || data.has(TITLE)) {
                            Iterator<String> jsonIter = data.keys();
                            while (jsonIter.hasNext()) {
                                String jsonKey = jsonIter.next();

                                Log.d(LOG_TAG, "key = data/" + jsonKey);

                                String value = data.getString(jsonKey);
                                jsonKey = normalizeKey(jsonKey);
                                value = localizeKey(context, jsonKey, value);

                                newExtras.putString(jsonKey, value);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "normalizeExtras: JSON exception");
                    }
                }
            } else if (key.equals(("notification"))) {
                Bundle value = extras.getBundle(key);
                Iterator<String> iterator = value.keySet().iterator();
                while (iterator.hasNext()) {
                    String notifkey = iterator.next();

                    Log.d(LOG_TAG, "notifkey = " + notifkey);
                    String newKey = normalizeKey(notifkey);
                    Log.d(LOG_TAG, "replace key " + notifkey + " with " + newKey);

                    String valueData = value.getString(notifkey);
                    valueData = localizeKey(context, newKey, valueData);

                    newExtras.putString(newKey, valueData);
                }
                continue;
            }

            String newKey = normalizeKey(key);
            Log.d(LOG_TAG, "replace key " + key + " with " + newKey);
            replaceKey(context, key, newKey, extras, newExtras);

        } // while

        return newExtras;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
           handleData(data);



        } else if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification());
        }// Check if message contains a notification payload.




    }



    private void handleNotification(RemoteMessage.Notification RemoteMsgNotification) {
        String message = RemoteMsgNotification.getBody();
        String title = RemoteMsgNotification.getTitle();
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setTitle(title);
        notificationVO.setMessage(message);

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationVO, resultIntent);

    }

    private void handleData(Map<String, String> data) {

        if(!data.containsKey(BODY)) {

            String param = "1";

            String title = data.get(TITLE);
            String message = data.get(MESSAGE);
            String rid = data.get(ejson);

            if (data.containsKey(PARAMS))
                param = data.get(PARAMS);
            String wewe = data.toString();
            String iconUrl = data.get(IMAGE);
            String action = data.get(ACTION);
            String actionDestination = data.get(ACTION_DESTINATION);


            NotificationVO notificationVO = new NotificationVO();
            notificationVO.setTitle(title);
            notificationVO.setMessage(message);
            notificationVO.setIconUrl(iconUrl);
            notificationVO.setAction(action);
            //resultIntent.putExtra("exitt",true);

            notificationVO.setActionDestination(actionDestination);

            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            if (!param.equals("1")) {
                try {
                    String params = new JSONObject(param).getString("devid");
                    String idmodel = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    if (params.equals(idmodel) != true)
                        resultIntent.putExtra("exit", true);
                    else
                        resultIntent.putExtra("exit", false);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    try {
                        // Perform the operation associated with our pendingIntent
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            } else {
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                resultIntent.putExtra("startRoom", true);
                notificationUtils.displayNotification(notificationVO, resultIntent);
            }

        }else{
            SaveUserInfo();
        }
    }
    /*
     * Normalize localization for key
     */
    private String localizeKey(Context context, String key, String value) {
        if (key.equals(TITLE) || key.equals(MESSAGE) || key.equals(SUMMARY_TEXT)) {
            try {
                JSONObject localeObject = new JSONObject(value);

                String localeKey = localeObject.getString(LOC_KEY);

                ArrayList<String> localeFormatData = new ArrayList<>();
                if (!localeObject.isNull(LOC_DATA)) {
                    String localeData = localeObject.getString(LOC_DATA);
                    JSONArray localeDataArray = new JSONArray(localeData);
                    for (int i = 0, size = localeDataArray.length(); i < size; i++) {
                        localeFormatData.add(localeDataArray.getString(i));
                    }
                }

                String packageName = context.getPackageName();
                Resources resources = context.getResources();

                int resourceId = resources.getIdentifier(localeKey, "string", packageName);

                if (resourceId != 0) {
                    return resources.getString(resourceId, localeFormatData.toArray());
                } else {
                    Log.d(LOG_TAG, "can't find resource for locale key = " + localeKey);

                    return value;
                }
            } catch (JSONException e) {
                Log.d(LOG_TAG, "no locale found for key = " + key + ", error " + e.getMessage());

                return value;
            }
        }

        return value;
    }

    /*
     * Replace alternate keys with our canonical value
     */
    private String normalizeKey(String key) {
        if (key.equals(BODY) || key.equals(ALERT) || key.equals(GCM_NOTIFICATION_BODY) || key
                .equals(TWILIO_BODY)) {
            return MESSAGE;
        } else if (key.equals(TWILIO_TITLE)) {
            return TITLE;
        } else if (key.equals(MSGCNT) || key.equals(BADGE)) {
            return COUNT;
        } else if (key.equals(SOUNDNAME) || key.equals(TWILIO_SOUND)) {
            return SOUND;
        } else if (key.startsWith(GCM_NOTIFICATION)) {
            return key.substring(GCM_NOTIFICATION.length() + 1, key.length());
        } else if (key.startsWith(GCM_N)) {
            return key.substring(GCM_N.length() + 1, key.length());
        } else if (key.startsWith(UA_PREFIX)) {
            key = key.substring(UA_PREFIX.length() + 1, key.length());
            return key.toLowerCase();
        } else {
            return key;
        }
    }
    private void replaceKey(Context context, String oldKey, String newKey, Bundle extras,
                            Bundle newExtras) {
        Object value = extras.get(oldKey);
        if (value == null) {
            return;
        }

        if (value instanceof String) {
            value = localizeKey(context, newKey, (String) value);

            newExtras.putString(newKey, (String) value);
        } else if (value instanceof Boolean) {
            newExtras.putBoolean(newKey, (Boolean) value);
        } else if (value instanceof Number) {
            newExtras.putDouble(newKey, ((Number) value).doubleValue());
        } else {
            newExtras.putString(newKey, String.valueOf(value));
        }
    }

    public void SaveUserInfo() {

        Intent onLineIntent = new Intent(getApplicationContext(), PortSipService.class);
        onLineIntent.putExtra(PortSipService.EXTRA_PUSHTOKEN, FirebaseInstanceId.getInstance().getToken());
        onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startService(onLineIntent);
        }else{
            getApplicationContext().startService(onLineIntent);
        }
    }

}
