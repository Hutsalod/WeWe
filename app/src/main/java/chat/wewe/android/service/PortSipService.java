package chat.wewe.android.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.portsip.OnPortSIPEvent;
import com.portsip.PortSipEnumDefine;
import com.portsip.PortSipErrorcode;
import com.portsip.PortSipSdk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.activity.Constants;
import chat.wewe.android.activity.Intro;
import chat.wewe.android.ui.IncomingActivity;
import chat.wewe.android.ui.MainActivity;
import chat.wewe.android.util.CallManager;
import chat.wewe.android.util.Contact;
import chat.wewe.android.util.ContactManager;
import chat.wewe.android.util.Ring;
import chat.wewe.android.util.Session;




public class PortSipService extends Service implements OnPortSIPEvent {
    public static final String ACTION_SIP_REGIEST = "PortSip.AndroidSample.Test.REGIEST";
    public static final String ACTION_SIP_UNREGIEST = "PortSip.AndroidSample.Test.UNREGIEST";
    public static final String ACTION_PUSH_MESSAGE = "PortSip.AndroidSample.Test.PushMessageIncoming";
    public static final String ACTION_PUSH_TOKEN = "PortSip.AndroidSample.Test.PushToken";

    public static final String INSTANCE_ID = "instanceid";

    public static final String USER_NAME = "user name";
    public static final String USER_PWD = "user pwd";
    public static final String SVR_HOST = "svr host";
    public static final String SVR_PORT = "svr port";

    public static final String USER_DOMAIN = "user domain";
    public static final String USER_DISPALYNAME = "user dispalay";
    public static final String USER_AUTHNAME = "user authname";
    public static final String STUN_HOST = "stun host";
    public static final String STUN_PORT = "stun port";

    public static final String TRANS = "TLS";
    public static final String SRTP = "srtp type";

    protected PowerManager.WakeLock mCpuLock;
    public static final String REGISTER_CHANGE_ACTION = "PortSip.AndroidSample.Test.RegisterStatusChagnge";
    public static final String CALL_CHANGE_ACTION = "PortSip.AndroidSample.Test.CallStatusChagnge";
    public static final String PRESENCE_CHANGE_ACTION = "PortSip.AndroidSample.Test.PRESENCEStatusChagnge";

    public static final String EXTRA_REGISTER_STATE = "RegisterStatus";
    public static final String EXTRA_CALL_SEESIONID = "SessionID";
    public static final String EXTRA_CALL_DESCRIPTION = "Description";
    public static final String EXTRA_PUSHTOKEN = "token";
    RocketChatApplication application;
    private final String APPID = "chat.wewe.android";
    private PortSipSdk mEngine;
    private RocketChatApplication applicaton;
    private  static final int SERVICE_NOTIFICATION  = 31414;
    public   static final int PENDINGCALL_NOTIFICATION = SERVICE_NOTIFICATION+1;
    private String channelID = "PortSipService";
    private String callChannelID = "Call Channel";
    private String pushToken;
    private NotificationManager mNotificationManager;
    HashMap<String, String> headers = new HashMap<String, String>();

    @Override
    public void onCreate() {
        super.onCreate();
        applicaton = (RocketChatApplication) getApplicationContext();

        mEngine = applicaton.mEngine;

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, "WeWe SIP", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel call = new NotificationChannel(callChannelID, "WeWe Call", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mNotificationManager.createNotificationChannel(call);
        }
      showServiceNotifiCation();
    }

    private void showServiceNotifiCation(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0/*requestCode*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this,channelID).setOnlyAlertOnce(true);
        }else{
            builder = new Notification.Builder(this);

        }

        startForeground(SERVICE_NOTIFICATION, builder.setSmallIcon(R.drawable.ic_logo_vector).setContentTitle(getString(R.string.app_name)).setContentText("WeWe Call").build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if (ACTION_PUSH_MESSAGE.equals(intent.getAction()) && !CallManager.Instance().regist) {
                registerToServer();
            } else if (ACTION_SIP_REGIEST.equals(intent.getAction()) && !CallManager.Instance().regist) {
                registerToServer();
            } else if (ACTION_SIP_UNREGIEST.equals(intent.getAction()) && CallManager.Instance().regist) {
                unregisterToServer();
            }else if (ACTION_PUSH_TOKEN.equals(intent.getAction())) {
                pushToken = intent.getStringExtra(EXTRA_PUSHTOKEN);

                if (!TextUtils.isEmpty(pushToken) && CallManager.Instance().regist)
                {
                    String pushMessage = "device-os=android;device-uid=" + pushToken + ";allow-call-push=true;allow-message-push=true;app-id=" + APPID;
                    mEngine.addSipMessageHeader(-1, "ALL", 1, "token", pushMessage);
                }
            }

        }

        mNotificationManager.cancelAll();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
           mNotificationManager.deleteNotificationChannel(channelID);
        }
        mNotificationManager.cancel(SERVICE_NOTIFICATION);
        return result;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mEngine.destroyConference();

        if (mCpuLock != null) {
            mCpuLock.release();
        }
        mNotificationManager.cancelAll();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotificationManager.deleteNotificationChannel(channelID);
            mNotificationManager.deleteNotificationChannel(callChannelID);
        }
        mNotificationManager = null;
        Log.d("WEWE_CALL"," onDestroy");

        mEngine.removeUser();
        mEngine.DeleteCallManager();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void registerToServer() {


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Random rm = new Random();
        int localPort = 5060 + rm.nextInt(60000);

        int transType = preferences.getInt(TRANS, 1);
        int srtpType = preferences.getInt(SRTP, 1);
        String userName = preferences.getString(USER_NAME, "");
        String password = preferences.getString(USER_PWD, "");
        String displayName = preferences.getString(USER_DISPALYNAME, "");
        String authName = preferences.getString(USER_AUTHNAME, "");
        String userDomain = preferences.getString(USER_DOMAIN, "");


        String sipServer = preferences.getString(SVR_HOST, "sip.weltwelle.com");
        String serverPort = preferences.getString(SVR_PORT, "5061");
        String stunServer = preferences.getString(STUN_HOST, "");
        String stunPort = preferences.getString(STUN_PORT, "3478");

        int sipServerPort = Integer.parseInt(serverPort);
        int stunServerPort = Integer.parseInt(stunPort);


        if(TextUtils.isEmpty(userName)){
            showTipMessage("Please enter user name!");
            return;
        }

        if(TextUtils.isEmpty(password)){
            showTipMessage("Please enter password!");
            return;
        }

        if(TextUtils.isEmpty(sipServer)){
            showTipMessage("Please enter SIP Server!");
            return;
        }

        if(TextUtils.isEmpty(serverPort)){
            showTipMessage("Please enter Server Port!");
            return;
        }

        int result = 0;
        mEngine.DeleteCallManager();
        mEngine.CreateCallManager(applicaton);
        mEngine.setOnPortSIPEvent(this);
        String dataPath = getExternalFilesDir(null).getAbsolutePath();

        result = mEngine.initialize(PortSipEnumDefine.ENUM_TRANSPORT_TLS, "0.0.0.0", localPort,
                PortSipEnumDefine.ENUM_LOG_LEVEL_NONE, dataPath,
                8, "", 0, 0, dataPath, "", false, null);
        if(result != PortSipErrorcode.ECoreErrorNone) {
            showTipMessage("initialize failure ErrorCode = " + result);
            mEngine.DeleteCallManager();
            CallManager.Instance().resetAll();
            return;
        }
        mEngine.addSipMessageHeader(-1, "REGISTER", 1, "tokenYA", FirebaseInstanceId.getInstance().getToken());
        mEngine.addSipMessageHeader(-1, "INVITE", 1, "tokenYA", FirebaseInstanceId.getInstance().getToken());


        result = mEngine.setUser(userName, displayName, authName, password,
                userDomain, sipServer, sipServerPort, stunServer, stunServerPort, null, 5061);

        if(result != PortSipErrorcode.ECoreErrorNone)
        {
            showTipMessage("setUser failure ErrorCode = " + result);
            mEngine.DeleteCallManager();
            CallManager.Instance().resetAll();
            return;
        }

        result = mEngine.setLicenseKey("2ANDRB4xQ0ExNkJCMUM4OUU1ODY2QTg5MTZDMDNGNzIwNUU4Q0BFMjg2QzhEQjVDMjA3QUM0Q0VGNjlCQ0M5NEJBRDIyM0A5MDJBRkYyQzdBMThGNzhGNTg4M0I2RTUzRTA4RjhGRkBCMEVDNkZFNkQxQTI5NEQ0MDAyQzhBRTY0NTZGNUY1Rg");
        if (result == PortSipErrorcode.ECoreWrongLicenseKey) {
            Log.d("TEST_WEWE","start");
            showTipMessage("The wrong license key was detected, please check with sales@portsip.com or support@portsip.com");
            return;
        }
        else if (result  == PortSipErrorcode.ECoreTrialVersionLicenseKey)
        {
            Log.d("TEST_WEWE","stop");
            Log.w("Trial Version","This trial version SDK just allows short conversation, you can't hearing anything after 2-3 minutes, contact us: sales@portsip.com to buy official version.");
            showTipMessage("This Is Trial Version");
        }

        mEngine.setAudioDevice(PortSipEnumDefine.AudioDevice.SPEAKER_PHONE);
        mEngine.setVideoDeviceId(1);
        mEngine.setSrtpPolicy(1);
        ConfigPreferences(this, preferences, mEngine);

        mEngine.enable3GppTags(false);

        if (!TextUtils.isEmpty(pushToken)) {
            String pushMessage = "device-os=android;device-uid=" + pushToken + ";allow-call-push=true;allow-message-push=true;app-id=" + APPID;
            mEngine.addSipMessageHeader(-1, "REGISTER", 1, "token", pushMessage);
        }

        mEngine.setInstanceId(getInstanceID());

        result = mEngine.registerServer(90, 0);
        if(result != PortSipErrorcode.ECoreErrorNone)
        {
            showTipMessage("registerServer failure ErrorCode =" + result);
            mEngine.unRegisterServer();
            mEngine.DeleteCallManager();
            CallManager.Instance().resetAll();
        }


    }

    private void showTipMessage(String tipMessage)
    {
        Log.d("SIPTEST","TEST "+tipMessage);
    }

    public static void ConfigPreferences(Context context, SharedPreferences preferences, PortSipSdk sdk) {
        sdk.clearAudioCodec();
        if (preferences.getBoolean(context.getString(R.string.MEDIA_G722), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_G722);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_PCMA), true)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_PCMA);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_PCMU), true)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_PCMU);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_G729), true)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_G729);
        }

        if (preferences.getBoolean(context.getString(R.string.MEDIA_GSM), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_GSM);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_ILBC), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_ILBC);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_AMR), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_AMR);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_AMRWB), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_AMRWB);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_SPEEX), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_SPEEX);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_SPEEXWB), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_SPEEXWB);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_ISACWB), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_ISACWB);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_ISACSWB), false)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_ISACSWB);
        }
//        if (preferences.getBoolean(context.getString(R.string.MEDIA_G7221), false)) {
//            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_G7221);
//        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_OPUS), true)) {
            sdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_OPUS);
        }

        sdk.clearVideoCodec();
        if (preferences.getBoolean(context.getString(R.string.MEDIA_H264), true)) {
            sdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_H264);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_VP8), true)) {
            sdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_VP8);
        }
        if (preferences.getBoolean(context.getString(R.string.MEDIA_VP9), true)) {
            sdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_VP9);
        }

		sdk.enableAEC(preferences.getBoolean(context.getString(R.string.MEDIA_AEC), true));
        sdk.enableAGC(preferences.getBoolean(context.getString(R.string.MEDIA_AGC), true));
        sdk.enableCNG(preferences.getBoolean(context.getString(R.string.MEDIA_CNG), true));
        sdk.enableVAD(preferences.getBoolean(context.getString(R.string.MEDIA_VAD), true));
        sdk.enableANS(preferences.getBoolean(context.getString(R.string.MEDIA_ANS), false));

        boolean foward = preferences.getBoolean(context.getString(R.string.str_fwopenkey), false);
        boolean fowardBusy = preferences.getBoolean(context.getString(R.string.str_fwbusykey), false);
        String fowardto = preferences.getString(context.getString(R.string.str_fwtokey), null);
        if (foward && !TextUtils.isEmpty(fowardto)) {
            sdk.enableCallForward(fowardBusy, fowardto);
        }

        sdk.enableReliableProvisional(preferences.getBoolean(context.getString(R.string.str_pracktitle), false));

        String resolution = preferences.getString((context.getString(R.string.str_resolution)), "CIF");
        int width = 352;
        int height = 288;
        if (resolution.equals("QCIF")) {
            width = 176;
            height = 144;
        } else if (resolution.equals("CIF")) {
            width = 352;
            height = 288;
        } else if (resolution.equals("VGA")) {
            width = 640;
            height = 480;
        } else if (resolution.equals("720P")) {
            width = 1280;
            height = 720;
        } else if (resolution.equals("1080P")) {
            width = 1920;
            height = 1080;
        }

        sdk.setVideoResolution(width, height);
    }

    private int getTransType(int select) {
        switch (select) {
            case 0:
                return PortSipEnumDefine.ENUM_TRANSPORT_UDP;
            case 1:
                return PortSipEnumDefine.ENUM_TRANSPORT_TLS;
            case 2:
                return PortSipEnumDefine.ENUM_TRANSPORT_TCP;
            case 3:
                return PortSipEnumDefine.ENUM_TRANSPORT_PERS_UDP;
            case 4:
                return PortSipEnumDefine.ENUM_TRANSPORT_PERS_TCP;
        }
        return PortSipEnumDefine.ENUM_TRANSPORT_UDP;
    }

    String getInstanceID() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String insanceid = preferences.getString(INSTANCE_ID, "");
        if (TextUtils.isEmpty(insanceid)) {
            insanceid = UUID.randomUUID().toString();
            preferences.edit().putString(INSTANCE_ID, insanceid).commit();
        }
        return insanceid;
    }

    public void UnregisterToServerWithoutPush() {


        if (!TextUtils.isEmpty(pushToken)) {
            String pushMessage = "device-os=android;device-uid=" + pushToken + ";allow-call-push=false;allow-message-push=false;app-id=" + APPID;
            mEngine.addSipMessageHeader(-1, "REGISTER", 1, "tokenYA", pushMessage);
        }

        mEngine.unRegisterServer();
        mEngine.DeleteCallManager();
        CallManager.Instance().regist = false;
    }

    public void unregisterToServer() {

        mEngine.unRegisterServer();
        mEngine.DeleteCallManager();
        CallManager.Instance().regist = false;
    }

    //--------------------
    @Override
    public void onRegisterSuccess(String statusText, int statusCode, String sipMessage) {
        CallManager.Instance().regist = true;
        Intent broadIntent = new Intent(REGISTER_CHANGE_ACTION);
        broadIntent.putExtra(EXTRA_REGISTER_STATE,statusText);
        sendPortSipMessage("onRegisterSuccess", broadIntent);
        keepCpuRun(true);
    }

    @Override
    public void onRegisterFailure(String statusText, int statusCode, String sipMessage) {
        Intent broadIntent = new Intent(REGISTER_CHANGE_ACTION);
        broadIntent.putExtra(EXTRA_REGISTER_STATE, statusText);
        sendPortSipMessage("onRegisterFailure" + statusCode, broadIntent);
        CallManager.Instance().regist=false;
        CallManager.Instance().resetAll();

        keepCpuRun(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onInviteIncoming(long sessionId,
                                 String callerDisplayName,
                                 String caller,
                                 String calleeDisplayName,
                                 String callee,
                                 String audioCodecNames,
                                 String videoCodecNames,
                                 boolean existsAudio,
                                 boolean existsVideo,
                                 String sipMessage) {


        if(CallManager.Instance().findIncomingCall()!=null){
            applicaton.mEngine.rejectCall(sessionId,486);//busy
            return;
        }
        Session session = CallManager.Instance().findIdleSession();
        session.state = Session.CALL_STATE_FLAG.INCOMING;
        session.hasVideo = existsVideo;
        session.sessionID = sessionId;
        session.remote = caller;
        session.displayName = callerDisplayName;

        Intent activityIntent = new Intent(this, IncomingActivity.class);
        activityIntent.putExtra("incomingSession",sessionId);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Ring.getInstance(this).startRingTone();
        if(isForeground()){
            startActivity(activityIntent);
        }else{

            showPendingCallNotification(this, callerDisplayName, session.displayName, activityIntent);
        }
        Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
        broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);

        String description = session.lineName + " onInviteIncoming";
        broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);


        sendPortSipMessage(description, broadIntent);



    }

    @Override
    public void onInviteTrying(long sessionId) {
    }

    @Override
    public void onInviteSessionProgress(
            long sessionId,
            String audioCodecNames,
            String videoCodecNames,
            boolean existsEarlyMedia,
            boolean existsAudio,
            boolean existsVideo,
            String sipMessage) {
    }

    @Override
    public void onInviteRinging(long sessionId, String statusText, int statusCode, String sipMessage) {
    }

    @Override
    public void onInviteAnswered(long sessionId,
                                 String callerDisplayName,
                                 String caller,
                                 String calleeDisplayName,
                                 String callee,
                                 String audioCodecNames,
                                 String videoCodecNames,
                                 boolean existsAudio,
                                 boolean existsVideo,
                                 String sipMessage) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);


        if (session != null) {
            session.state = Session.CALL_STATE_FLAG.CONNECTED;
            session.hasVideo = existsVideo;

            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);

            String description = session.lineName + " onInviteAnswered";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);

            sendPortSipMessage(description, broadIntent);
        }

        Ring.getInstance(this).stopRingBackTone();
    }

    @Override
    public void onInviteFailure(long sessionId, String reason, int code, String sipMessage) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);
        if (session != null) {
            session.state = Session.CALL_STATE_FLAG.FAILED;
            session.sessionID = sessionId;

            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);
            String description = session.lineName + " onInviteFailure";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);
            Log.d("TEST_WEWE","onInviteFailure session");
            sendPortSipMessage(description, broadIntent);
        }
        Log.d("TEST_WEWE","onInviteFailure ");
        Ring.getInstance(this).stopRingBackTone();
    }

    @Override
    public void onInviteUpdated(
            long sessionId,
            String audioCodecNames,
            String videoCodecNames,
            boolean existsAudio,
            boolean existsVideo,
            String sipMessage) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);

        if (session != null) {
            session.state = Session.CALL_STATE_FLAG.CONNECTED;
            session.hasVideo = existsVideo;

            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);
            String description = session.lineName + " OnInviteUpdated";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);

            sendPortSipMessage(description, broadIntent);
        }
    }

    @Override
    public void onInviteConnected(long sessionId) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);
        if (session != null) {
            session.state = Session.CALL_STATE_FLAG.CONNECTED;
            session.sessionID = sessionId;

            if (applicaton.mConference)
            {
                applicaton.mEngine.joinToConference(session.sessionID);
                applicaton.mEngine.sendVideo(session.sessionID, true);
            }

            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);
            String description = session.lineName + " OnInviteConnected";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);

            sendPortSipMessage(description, broadIntent);
            mNotificationManager.cancel(PENDINGCALL_NOTIFICATION);
            Log.d("TEST_WEWE","onInviteConnected session");
        }
        Log.d("TEST_WEWE","onInviteConnected ");
        CallManager.Instance().setSpeakerOn(applicaton.mEngine, CallManager.Instance().isSpeakerOn());
    }

    @Override
    public void onInviteBeginingForward(String forwardTo) {
    }

    @Override
    public void onInviteClosed(long sessionId) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);
        if (session != null) {
            session.state = Session.CALL_STATE_FLAG.CLOSED;
            session.sessionID = sessionId;

            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);
            String description = session.lineName +  " OnInviteClosed";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);
            sendPortSipMessage(description, broadIntent);

        }
        unregisterToServer();
        Ring.getInstance(this).stopRingTone();
        mNotificationManager.cancel(PENDINGCALL_NOTIFICATION);


    }

    @Override
    public void onDialogStateUpdated(String BLFMonitoredUri,
                                     String BLFDialogState,
                                     String BLFDialogId,
                                     String BLFDialogDirection) {
        String text = "The user ";
        text += BLFMonitoredUri;
        text += " dialog state is updated: ";
        text += BLFDialogState;
        text += ", dialog id: ";
        text += BLFDialogId;
        text += ", direction: ";
        text += BLFDialogDirection;
    }

    @Override
    public void onRemoteUnHold(
            long sessionId,
            String audioCodecNames,
            String videoCodecNames,
            boolean existsAudio,
            boolean existsVideo) {
    }

    @Override
    public void onRemoteHold(long sessionId) {
    }

    @Override
    public void onReceivedRefer(
            long sessionId,
            long referId,
            String to,
            String referFrom,
            String referSipMessage) {
    }

    @Override
    public void onReferAccepted(long sessionId) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);
        if (session != null) {
            session.state = Session.CALL_STATE_FLAG.CLOSED;
            session.sessionID = sessionId;

            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);
            String description = session.lineName + " onReferAccepted";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);

            sendPortSipMessage(description, broadIntent);

        }
        Log.d("TEST_WEWE","onReferAccepted session");
        Ring.getInstance(this).stopRingTone();
    }

    @Override
    public void onReferRejected(long sessionId, String reason, int code) {
    }

    @Override
    public void onTransferTrying(long sessionId) {
    }

    @Override
    public void onTransferRinging(long sessionId) {
    }

    @Override
    public void onACTVTransferSuccess(long sessionId) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);
        if (session != null) {
            session.state = Session.CALL_STATE_FLAG.CLOSED;
            session.sessionID = sessionId;

            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);
            String description = session.lineName + " Transfer succeeded, call closed";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);
            Log.d("TEST_WEWE","onACTVTransferSuccess session");
            sendPortSipMessage(description, broadIntent);
            // Close the call after succeeded transfer the call
            mEngine.hangUp(sessionId);
        }
    }

    @Override
    public void onACTVTransferFailure(long sessionId, String reason, int code) {
        Session session = CallManager.Instance().findSessionBySessionID(sessionId);
        if (session != null) {
            Intent broadIntent = new Intent(CALL_CHANGE_ACTION);
            broadIntent.putExtra(EXTRA_CALL_SEESIONID, sessionId);
            String description = session.lineName + " Transfer failure!";
            broadIntent.putExtra(EXTRA_CALL_DESCRIPTION, description);

            sendPortSipMessage(description, broadIntent);

        }
    }

    @Override
    public void onReceivedSignaling(long sessionId, String signaling) {
    }

    @Override
    public void onSendingSignaling(long sessionId, String signaling) {
    }

    @Override
    public void onWaitingVoiceMessage(
            String messageAccount,
            int urgentNewMessageCount,
            int urgentOldMessageCount,
            int newMessageCount,
            int oldMessageCount) {
    }

    @Override
    public void onWaitingFaxMessage(
            String messageAccount,
            int urgentNewMessageCount,
            int urgentOldMessageCount,
            int newMessageCount,
            int oldMessageCount) {
    }

    @Override
    public void onRecvDtmfTone(long sessionId, int tone) {
    }

    @Override
    public void onRecvOptions(String optionsMessage) {
    }

    @Override
    public void onRecvInfo(String infoMessage) {
    }

    @Override
    public void onRecvNotifyOfSubscription(long sessionId, String notifyMessage, byte[] messageData, int messageDataLength) {
    }

    //Receive a new subscribe
    @Override
    public void onPresenceRecvSubscribe(
            long subscribeId,
            String fromDisplayName,
            String from,
            String subject) {
        Contact contact = ContactManager.Instance().findContactBySipAddr(from);
        if (contact == null) {
            contact = new Contact();
            contact.sipAddr = from;
            ContactManager.Instance().addContact(contact);
        }

        contact.subRequestDescription = subject;
        contact.subId = subscribeId;
        switch (contact.state) {
            case ACCEPTED://This subscribe has accepted
                applicaton.mEngine.presenceAcceptSubscribe(subscribeId);
                break;
            case REJECTED://This subscribe has rejected
                applicaton.mEngine.presenceRejectSubscribe(subscribeId);
                break;
            case UNSETTLLED:
                break;
            case UNSUBSCRIBE:
                contact.state = Contact.SUBSCRIBE_STATE_FLAG.UNSETTLLED;
                break;
        }
        Intent broadIntent = new Intent(PRESENCE_CHANGE_ACTION);
        sendPortSipMessage("OnPresenceRecvSubscribe", broadIntent);
    }

    //update online status
    @Override
    public void onPresenceOnline(String fromDisplayName, String from, String stateText) {
        Contact contact = ContactManager.Instance().findContactBySipAddr(from);
        if (contact == null) {

        } else {
            contact.subDescription = stateText;
        }

        Intent broadIntent = new Intent(PRESENCE_CHANGE_ACTION);
        sendPortSipMessage("OnPresenceRecvSubscribe", broadIntent);
    }

    //update offline status
    @Override
    public void onPresenceOffline(String fromDisplayName, String from) {
        Contact contact = ContactManager.Instance().findContactBySipAddr(from);
        if (contact == null) {

        } else {
            contact.subDescription = "Offline";
        }

        Intent broadIntent = new Intent(PRESENCE_CHANGE_ACTION);
        sendPortSipMessage("OnPresenceRecvSubscribe", broadIntent);
    }

    @Override
    public void onRecvMessage(
            long sessionId,
            String mimeType,
            String subMimeType,
            byte[] messageData,
            int messageDataLength) {
    }

    @Override
    public void onRecvOutOfDialogMessage(
            String fromDisplayName,
            String from,
            String toDisplayName,
            String to,
            String mimeType,
            String subMimeType,
            byte[] messageData,
            int messageDataLengthsipMessage,
            String sipMessage) {
        if ("text".equals(mimeType) && "plain".equals(subMimeType)) {
            Toast.makeText(this,"you have a mesaage from: "+from+ "  "+new String(messageData),Toast.LENGTH_SHORT).show();
        }else{
        }
    }

    @Override
    public void onSendMessageSuccess(long sessionId, long messageId) {
    }

    @Override
    public void onSendMessageFailure(long sessionId, long messageId, String reason, int code) {
    }

    @Override
    public void onSendOutOfDialogMessageSuccess(long messageId,
                                                String fromDisplayName,
                                                String from,
                                                String toDisplayName,
                                                String to) {
    }

    @Override
    public void onSendOutOfDialogMessageFailure(
            long messageId,
            String fromDisplayName,
            String from,
            String toDisplayName,
            String to,
            String reason,
            int code) {
    }

    @Override
    public void onSubscriptionFailure(long subscribeId, int statusCode) {
    }

    @Override
    public void onSubscriptionTerminated(long subscribeId) {
    }

    @Override
    public void onPlayAudioFileFinished(long sessionId, String fileName) {
    }

    @Override
    public void onPlayVideoFileFinished(long sessionId) {
    }

    @Override
    public void onReceivedRTPPacket(
            long sessionId,
            boolean isAudio,
            byte[] RTPPacket,
            int packetSize) {
    }

    @Override
    public void onSendingRTPPacket(long l, boolean b, byte[] bytes, int i) {

    }

    @Override
    public void onAudioRawCallback(
            long sessionId,
            int callbackType,
            byte[] data,
            int dataLength,
            int samplingFreqHz) {
    }

    @Override
    public void onVideoRawCallback(long l, int i, int i1, int i2, byte[] bytes, int i3) {

    }


    //--------------------
    public void sendPortSipMessage(String message, Intent broadIntent) {
        Intent intent = new Intent(this, chat.wewe.android.activity.MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this,channelID);
        }else{
            builder = new Notification.Builder(this);
        }

        Log.d("WEWE_CALL"," "+message);
        builder.setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Sip Notify")
                .setContentText(message)
                .setContentIntent(contentIntent)
                .build();
       // mNotificationManager.notify(1, builder.build());
        sendBroadcast(broadIntent);
    }


    public int outOfDialogRefer(int replaceSessionId, String replaceMethod, String target, String referTo) {
        return 0;
    }

    public void keepCpuRun(boolean keepRun) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (keepRun == true) { //open
            if (mCpuLock == null) {
                if ((mCpuLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SipSample:CpuLock.")) == null) {
                    return;
                }
                mCpuLock.setReferenceCounted(false);
            }

            synchronized (mCpuLock) {
                if (!mCpuLock.isHeld()) {
                    mCpuLock.acquire();
                }
            }

            headers.put("guid",    UUID.randomUUID().toString());
            headers.put("token",    FirebaseInstanceId.getInstance().getToken());


            for (HashMap.Entry<String, String> entry : headers.entrySet()) {
                applicaton.mEngine.addSipMessageHeader(-1, "INVITE", 1, entry.getKey(), entry.getValue());
            }
            applicaton.mEngine.sendVideo(applicaton.mEngine.call("wewe_token", true, false), true);
        } else {//close
            if (mCpuLock != null) {
                synchronized (mCpuLock) {
                    if (mCpuLock.isHeld()) {
                        mCpuLock.release();
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean isForeground(){
        String[] activitys;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            activitys = getActivePackages(this);
        }else{
            activitys = getActivePackagesCompat(this);
        }
        if(activitys.length>0){
            String packagename = getPackageName();
            //String processName= getProcessName();||activityname.contains(processName)
            for(String activityname:activitys){

                if(activityname.contains(packagename)){
                    return true;
                }
            }
            return false;
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private String[] getActivePackagesCompat(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> taskInfo = mActivityManager.getRunningTasks(1);
        final ComponentName componentName = taskInfo.get(0).topActivity;
        final String[] activePackages = new String[1];
        activePackages[0] = componentName.getPackageName();
        return activePackages;
    }

    private String[] getActivePackages(Context context) {
        final Set<String> activePackages = new HashSet<String>();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processInfos = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                activePackages.addAll(Arrays.asList(processInfo.pkgList));
            }
        }
        return activePackages.toArray(new String[activePackages.size()]);
    }


    private PendingIntent openParkingViolationScreen(int notificationId) {


        Intent fullScreenIntent = new Intent(this, IncomingActivity.class);
        fullScreenIntent.putExtra("incomingSession",notificationId);
        return PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    }




    public void showPendingCallNotification(Context context, String contenTitle,String contenText,Intent intent) {

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, callChannelID)
                .setContentTitle("Входящий звонок")
                .setContentText(contenTitle)
                .setSmallIcon(R.drawable.ic_call)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(0,"Принять", PendingIntent.getActivity(context, 1, intent.putExtra("TRYING", 1), PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(R.drawable.ic_finish_call,
                        "Отказать", PendingIntent.getActivity(context, 2, intent.putExtra("TRYING", 2),PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setShowWhen(true)
                .setContentIntent(contentIntent)
                .setFullScreenIntent(contentIntent, true);
        mNotificationManager.notify(PENDINGCALL_NOTIFICATION, builder.build());

    }
}

