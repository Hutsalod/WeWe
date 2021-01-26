package chat.wewe.android.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.portsip.PortSipErrorcode;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.util.CallManager;
import chat.wewe.android.util.Ring;
import chat.wewe.android.util.Session;

public class IncomingActivity extends Activity implements PortMessageReceiver.BroadcastListener, View.OnClickListener {

    public PortMessageReceiver receiver = null;
    private RocketChatApplication application;
    private TextView tips;
    private Button btnVideo;
    private long mSessionid;
    private int mState;
    public boolean exit = false;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final Window win = getWindow();
        win.addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.incomingview);

        tips = findViewById(R.id.sessiontips);
        btnVideo = findViewById(R.id.answer_video);
        receiver = new PortMessageReceiver();
        IntentFilter filter = new IntentFilter();

        filter.addAction(PortSipService.REGISTER_CHANGE_ACTION);
        filter.addAction(PortSipService.CALL_CHANGE_ACTION);
        filter.addAction(PortSipService.PRESENCE_CHANGE_ACTION);

        registerReceiver(receiver, filter);
        receiver.broadcastReceiver =this;
        Intent intent = getIntent();

        mSessionid = intent.getLongExtra("incomingSession", PortSipErrorcode.INVALID_SESSION_ID);
        Session session = CallManager.Instance().findSessionBySessionID(mSessionid);
        if(mSessionid== PortSipErrorcode.INVALID_SESSION_ID||session ==null||session.state!= Session.CALL_STATE_FLAG.INCOMING){
            this.finish();
            return;
        }

        application = (RocketChatApplication) getApplication();
        tips.setText(session.displayName);
        setVideoAnswerVisibility(session);

        findViewById(R.id.hangup_call).setOnClickListener(this);
        findViewById(R.id.answer_audio).setOnClickListener(this);
        btnVideo.setOnClickListener(this);

        mState = intent.getIntExtra("TRYING",0);

        switch (mState){
            case 0:
                break;
            case 1: findViewById(R.id.answer_audio).performClick();
                break;
            case 2: findViewById(R.id.hangup_call).performClick();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        long sessionid = intent.getLongExtra("incomingSession", PortSipErrorcode.INVALID_SESSION_ID);
        Session session = CallManager.Instance().findSessionBySessionID(sessionid);
        if(mSessionid!= PortSipErrorcode.INVALID_SESSION_ID&&session !=null){
            mSessionid = sessionid;
            setVideoAnswerVisibility(session);
            tips.setText(session.lineName+"   "+session.remote );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if(exit==true)
            startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onBroadcastReceiver(Intent intent) {
        String action = intent.getAction();
        if (PortSipService.CALL_CHANGE_ACTION.equals(action))
        {
            long sessionId = intent.getLongExtra(PortSipService.EXTRA_CALL_SEESIONID, Session.INVALID_SESSION_ID);
            String status = intent.getStringExtra(PortSipService.EXTRA_CALL_DESCRIPTION);
            Session session = CallManager.Instance().findSessionBySessionID(sessionId);
            if (session != null)
            {
                switch (session.state)
                {
                    case INCOMING:
                        break;
                    case TRYING:
                        break;
                    case CONNECTED:
                    case FAILED:
                    case CLOSED:
                        Session anOthersession = CallManager.Instance().findIncomingCall();
                        if(anOthersession==null) {

                        }else{
                            setVideoAnswerVisibility(anOthersession);
                            tips.setText(anOthersession.displayName);
                            mSessionid = anOthersession.sessionID;
                        }
                        break;

                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(application.mEngine!=null){
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(PortSipService.PENDINGCALL_NOTIFICATION);
            Session currentLine = CallManager.Instance().findSessionBySessionID(mSessionid);
            switch (view.getId()){
                case R.id.answer_audio:
                case R.id.answer_video:
                    if (currentLine.state != Session.CALL_STATE_FLAG.INCOMING) {
                        return;
                    }
                    Ring.getInstance(this).stopRingTone();
                    currentLine.state = Session.CALL_STATE_FLAG.CONNECTED;
                    application.mEngine.answerCall(mSessionid,view.getId()== R.id.answer_video);
                    if(application.mConference){
                        application.mEngine.joinToConference(currentLine.sessionID);
                    }
                    exit=true;
                    break;
                case R.id.hangup_call:
                    Ring.getInstance(this).stop();
                    if (currentLine.state == Session.CALL_STATE_FLAG.INCOMING) {
                        application.mEngine.rejectCall(currentLine.sessionID, 486);
                        currentLine.Reset();
                    }
                    break;
            }
        }

        Session anOthersession = CallManager.Instance().findIncomingCall();
        if(anOthersession==null) {
            this.finish();
        }else{
            mSessionid = anOthersession.sessionID;
            setVideoAnswerVisibility(anOthersession);
        }

    }

    private void setVideoAnswerVisibility(Session session){
        if(session == null)
            return;
        if(session.hasVideo){
            btnVideo.setVisibility(View.VISIBLE);
        }else{
            btnVideo.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }
}
