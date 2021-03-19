package chat.wewe.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.portsip.PortSIPVideoRenderer;
import com.portsip.PortSipEnumDefine;
import com.portsip.PortSipSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.fragment.chatroom.RoomContract;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.util.CallManager;
import chat.wewe.android.util.Ring;
import chat.wewe.android.util.Session;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


public class NumpadFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener , PortMessageReceiver.BroadcastListener{
    private EditText etSipNum;
    private TextView mtips;
    private Spinner spline;
    private boolean callSet = true;

    protected RoomContract.Presenter presenter;
    CheckBox cbSendVideo, cbRecvVideo, cbConference, cbSendSdp;
    RocketChatApplication application;
    MainActivity activity;
    HashMap<String, String> headers = new HashMap<String, String>();
    SharedPreferences SipData;
    private PortSIPVideoRenderer remoteRenderScreen = null;
    private PortSIPVideoRenderer localRenderScreen = null;
    private String name = "";
    private Boolean typeCall = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        application = (RocketChatApplication) activity.getApplicationContext();


        return inflater.inflate(R.layout.numpad, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etSipNum =  view.findViewById(R.id.etsipaddress);
        cbSendSdp =  view.findViewById(R.id.sendSdp);
        cbConference =  view.findViewById(R.id.conference);
        cbSendVideo =  view.findViewById(R.id.sendVideo);
        cbRecvVideo =  view.findViewById(R.id.acceptVideo);

        name = ((MainActivity) getActivity()).name;
        typeCall = ((MainActivity) getActivity()).typeCall;

        spline =  view.findViewById(R.id.sp_lines);

        TableLayout dtmfPad =  view.findViewById(R.id.dtmf_pad);
        TableLayout functionPad =  view.findViewById(R.id.function_pad);


        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.lines, android.R.layout.simple_list_item_1);
        spline.setAdapter(spinnerAdapter);
        spline.setSelection(CallManager.Instance().CurrentLine);
        spline.setOnItemSelectedListener(this);

        view.findViewById(R.id.dial).setOnClickListener(this);
        view.findViewById(R.id.pad).setOnClickListener(this);
        view.findViewById(R.id.delete).setOnClickListener(this);
        mtips = (TextView) view.findViewById(R.id.txtips);

        cbConference.setChecked(application.mConference);
        cbConference.setOnCheckedChangeListener(this);
        SetTableItemClickListener(functionPad);
        SetTableItemClickListener(dtmfPad);
        onHiddenChanged(false);
        etSipNum.setText(name);
        headers.put("guid",    UUID.randomUUID().toString());
        headers.put("loginTO",    name);
        headers.put("token",    FirebaseInstanceId.getInstance().getToken());


        ((MainActivity) getActivity()).startServiceSip();


        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
            if(name!=null) {
              if (application.mEngine == null)
                  return;
              PortSipSdk portSipSdk = application.mEngine;
              Session currentLine = CallManager.Instance().getCurrentSession();
              String callTo = name;
              if (callTo.length() <= 0) {
                  showTips("The phone number is empty.");
                  return;
              }
              if (!currentLine.IsIdle()) {
                  showTips("Current line is busy now, please switch a line.");
                  return;
              }

              // Ensure that we have been added one audio codec at least
              if (portSipSdk.isAudioCodecEmpty()) {
                  showTips("Audio Codec Empty,add audio codec at first");
                  return;
              }

              // Usually for 3PCC need to make call without SDP
              long sessionId = portSipSdk.call(callTo, cbSendSdp.isChecked(), typeCall);
              if (sessionId <= 0) {
                  showTips("Call failure");
                  return;
              }

              for (HashMap.Entry<String, String> entry : headers.entrySet()) {
                  portSipSdk.addSipMessageHeader(-1, "INVITE", 1, entry.getKey(), entry.getValue());
              }

              portSipSdk.sendVideo(sessionId, true);

              currentLine.remote = callTo;

              currentLine.sessionID = sessionId;
              currentLine.state = Session.CALL_STATE_FLAG.TRYING;
              currentLine.hasVideo = typeCall;
              showTips(currentLine.lineName + ": Calling...");

      }
        }
    }, 4000);
    }



    public void onBroadcastReceiver(Intent intent) {
        String action = intent == null ? "" : intent.getAction();
        if (PortSipService.CALL_CHANGE_ACTION.equals(action)) {
            long sessionId = intent.getLongExtra(PortSipService.EXTRA_CALL_SEESIONID, Session.INVALID_SESSION_ID);
            String status = intent.getStringExtra(PortSipService.EXTRA_CALL_DESCRIPTION);
            showTips(status);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ShowCurrentLineState();
            activity.receiver.broadcastReceiver = this;
        }
    }

    private void ShowCurrentLineState() {
        Session current = CallManager.Instance().getCurrentSession();
        switch (current.state) {
            case CLOSED:
            case FAILED:
                showTips(current.lineName + ": Idle");

                break;
            case CONNECTED:
                showTips(current.lineName + ": CONNECTED");

             //    ((RadioButton) activity.menuGroup.getChildAt(2)).setChecked(true);

                break;
            case INCOMING:
                showTips(current.lineName + ": INCOMING");
                break;
            case TRYING:
                showTips(current.lineName + ": TRYING");
           //     ((RadioButton) activity.menuGroup.getChildAt(2)).setChecked(true);

                break;
        }
        Button mute = getView().findViewById(R.id.mute);
        if (current.bMute) {
            mute.setText("Mute");
        } else {
            mute.setText("UnMute");
        }

        Button mic = getView().findViewById(R.id.mic);
        if(CallManager.Instance().isSpeakerOn()){
            mic.setText("SpeakOn");
        }else {
            mic.setText("SpeakOff");
        }
    }

    private void SetTableItemClickListener(TableLayout table) {

        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow tableRow = (TableRow) table.getChildAt(i);
            int line = tableRow.getChildCount();
            for (int index = 0; index < line; index++) {
                tableRow.getChildAt(index).setOnClickListener(this);
            }
        }
    }

    private void showTips(String text) {
        mtips.setText(text);
       // Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    private void SwitchPanel() {
        View view = getView().findViewById(R.id.function_pad);
        View dtmfView = getView().findViewById(R.id.dtmf_pad);
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.INVISIBLE);
            dtmfView.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
            dtmfView.setVisibility(View.INVISIBLE);
        }
    }


    AlertDialog TransferDLG;
    AlertDialog AttendTransferDLG;

    private void showTransferDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View textEntryView = factory.inflate(R.layout.transferdialog, null);
        builder.setIcon(R.drawable.ic_contacts);
        builder.setTitle("Transfer input");
        builder.setView(textEntryView);

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleTransfer();
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleNegativeButtonClick();
            }
        });
        TransferDLG = builder.create();
        TransferDLG.show();
    }

    private void showAttendTransferDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View textEntryView = factory.inflate(R.layout.attendtransferdialog, null);
        builder.setIcon(R.drawable.ic_contacts);
        builder.setTitle("Transfer input");
        builder.setView(textEntryView);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleAttendTransfer();
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleNegativeButtonClick();
            }
        });
        AttendTransferDLG = builder.create();
        AttendTransferDLG.show();
    }

    private void handleNegativeButtonClick() {
        AttendTransferDLG = null;
        TransferDLG = null;
    }

    private void handleTransfer() {
        Session currentLine = CallManager.Instance().getCurrentSession();
        if (currentLine.state != Session.CALL_STATE_FLAG.CONNECTED) {
            showTips("current line must be connected ");
            return;
        }
        EditText transferTo =  TransferDLG.findViewById(R.id.ettransferto);
        String referTo = transferTo.getText().toString();
        if (TextUtils.isEmpty(referTo)) {
            showTips("The transfer number is empty");
            return;
        }
        int rt = application.mEngine.refer(currentLine.sessionID, referTo);
        if (rt != 0) {
            showTips(currentLine.lineName + ": failed to transfer");
        } else {
            showTips(currentLine.lineName + " success transfered");
        }
        TransferDLG = null;

    }

    private void handleAttendTransfer() {
        Session currentLine = CallManager.Instance().getCurrentSession();
        if (currentLine.state != Session.CALL_STATE_FLAG.CONNECTED) {
            showTips("current line must be connected ");
            return;
        }
        EditText transferTo =  AttendTransferDLG.findViewById(R.id.ettransferto);
        EditText transferLine =  AttendTransferDLG.findViewById(R.id.ettransferline);
        String referTo = transferTo.getText().toString();
        if (TextUtils.isEmpty(referTo)) {
            showTips("The transfer number is empty");
            return;
        }
        String lineString = transferLine.getText().toString();

        int line = Integer.parseInt(lineString);


        if (line < 0 || line >= CallManager.MAX_LINES) {
            showTips("The replace line out of range");
            return;
        }
        Session replaceSession = CallManager.Instance().findSessionByIndex(line);
        if (replaceSession.state != Session.CALL_STATE_FLAG.CONNECTED) {
            showTips("The replace line does not established yet");
            return;
        }

        if (replaceSession.sessionID == currentLine.sessionID) {
            showTips("The replace line can not be current line");
            return;
        }

        int rt = application.mEngine.attendedRefer(currentLine.sessionID, replaceSession.sessionID, referTo);

        if (rt != 0) {
            showTips(currentLine.lineName + ": failed to Attend transfer");
        } else {
            showTips(currentLine.lineName + ": Transferring");
        }
        AttendTransferDLG = null;
    }



    @Override
    public void onClick(View view) {

        if (application.mEngine == null)
            return;
        PortSipSdk portSipSdk = application.mEngine;
        Session currentLine = CallManager.Instance().getCurrentSession();

        switch (view.getId()) {
            case R.id.zero:
            case R.id.one:
            case R.id.two:
            case R.id.three:
            case R.id.four:
            case R.id.five:
            case R.id.six:
            case R.id.seven:
            case R.id.eight:
            case R.id.nine:
            case R.id.star:
            case R.id.sharp: {
                String numberString = ((Button) view).getText().toString();

                char number = numberString.charAt(0);
                etSipNum.append(numberString);
                if (CallManager.Instance().regist && currentLine.state == Session.CALL_STATE_FLAG.CONNECTED) {
                    if (number == '*') {
                        portSipSdk.sendDtmf(currentLine.sessionID, PortSipEnumDefine.ENUM_DTMF_MOTHOD_RFC2833, 10,
                                160, true);
                        return;
                    }
                    if (number == '#') {
                        portSipSdk.sendDtmf(currentLine.sessionID, PortSipEnumDefine.ENUM_DTMF_MOTHOD_RFC2833, 11,
                                160, true);
                        return;
                    }
                    int sum = Integer.parseInt(numberString);// 0~9
                    portSipSdk.sendDtmf(currentLine.sessionID, PortSipEnumDefine.ENUM_DTMF_MOTHOD_RFC2833, sum,
                            160, true);
                }
            }
            break;
            case R.id.delete:
                int cursorpos = etSipNum.getSelectionStart();
                if (cursorpos - 1 >= 0) {
                    etSipNum.getText().delete(cursorpos - 1, cursorpos);
                }
                break;

            case R.id.pad:
                SwitchPanel();
                break;

            case R.id.dial: {
               // ((RadioButton)activity.menuGroup.getChildAt(2)).setChecked(true);
               String callTo = etSipNum.getText().toString();
                if (callTo.length() <= 0) {
                    showTips("The phone number is empty.");
                    return;
                }
                if (!currentLine.IsIdle()) {
                    showTips("Current line is busy now, please switch a line.");
                    return;
                }

                // Ensure that we have been added one audio codec at least
                if (portSipSdk.isAudioCodecEmpty()) {
                    showTips("Audio Codec Empty,add audio codec at first");
                    return;
                }

                // Usually for 3PCC need to make call without SDP
                long sessionId = portSipSdk.call(callTo, cbSendSdp.isChecked(), callSet);
                if (sessionId <= 0) {
                    showTips("Call failure");
                    return;
                }
                //default send video
                portSipSdk.sendVideo(sessionId, true);

                currentLine.remote = callTo;

                currentLine.sessionID = sessionId;
                currentLine.state = Session.CALL_STATE_FLAG.TRYING;
                currentLine.hasVideo = callSet;
                showTips(currentLine.lineName + ": Calling...");
            }
            break;
            case R.id.hangup: {
                Ring.getInstance(getActivity()).stop();
                switch (currentLine.state) {
                    case INCOMING:
                        portSipSdk.rejectCall(currentLine.sessionID, 486);
                        showTips(currentLine.lineName + ": Rejected call");
                        break;
                    case CONNECTED:
                    case TRYING:
                        portSipSdk.hangUp(currentLine.sessionID);
                        showTips(currentLine.lineName + ": Hang up");
                        break;
                }
                currentLine.Reset();


                break;
            }
            case R.id.answer: {

                if (currentLine.state != Session.CALL_STATE_FLAG.INCOMING) {
                    showTips("No incoming call on current line, please switch a line.");
                    return;
                }

                currentLine.state = Session.CALL_STATE_FLAG.CONNECTED;
                Ring.getInstance(getActivity()).stopRingTone();//stop ring
                portSipSdk.answerCall(currentLine.sessionID, cbRecvVideo.isChecked());//answer call
                if(application.mConference){
                    portSipSdk.joinToConference(currentLine.sessionID);
                }
            }

            break;
            case R.id.reject: {

                if (currentLine.state == Session.CALL_STATE_FLAG.INCOMING) {
                    portSipSdk.rejectCall(currentLine.sessionID, 486);
                    currentLine.Reset();
                    Ring.getInstance(getActivity()).stop();
                    showTips(currentLine.lineName + ": Rejected call");
                    return;
                }
                break;
            }

            case R.id.hold: {

                if (!(currentLine.state == Session.CALL_STATE_FLAG.CONNECTED) || currentLine.bHold) {
                    return;
                }

                int rt = portSipSdk.hold(currentLine.sessionID);
                if (rt != 0) {
                    showTips("hold operation failed.");
                    return;
                }
                currentLine.bHold = true;
            }
            break;
            case R.id.unhold: {

                if (!(currentLine.state == Session.CALL_STATE_FLAG.CONNECTED) || !currentLine.bHold) {
                    return;
                }

                int rt = portSipSdk.unHold(currentLine.sessionID);
                if (rt != 0) {
                    currentLine.bHold = false;
                    showTips(currentLine.lineName + ": Un-Hold Failure.");
                    return;
                }

                currentLine.bHold = false;
                showTips(currentLine.lineName + ": Un-Hold");
            }
            break;
            case R.id.attenttransfer: {

                if (!(currentLine.state == Session.CALL_STATE_FLAG.CONNECTED)) {
                    showTips("Need to make the call established first");
                    return;
                }
                showAttendTransferDialog();
            }
            break;
            case R.id.transfer: {

                if (!(currentLine.state == Session.CALL_STATE_FLAG.CONNECTED)) {
                    showTips("Need to make the call established first");
                    return;
                }
                showTransferDialog();
            }
            break;

            case R.id.mic:
                if(CallManager.Instance().setSpeakerOn(portSipSdk,!CallManager.Instance().isSpeakerOn())){
                    ((Button) view).setText("SpeakOn");
                }else {
                    ((Button) view).setText("SpeakOff");
                }
                break;
            case R.id.mute: {

                if (currentLine.bMute) {
                    portSipSdk.muteSession(currentLine.sessionID, false,
                            false, false, false);
                    currentLine.bMute = false;
                    Log.d("BBBRRR","++");
                    ((Button) view).setText("Mute");
                } else {
                    portSipSdk.muteSession(currentLine.sessionID, true,
                            true, true, true);
                    currentLine.bMute = true;
                    ((Button) view).setText("UnMute");
                    Log.d("BBBRRR","--");
                }
            }
            break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if (CallManager.Instance().CurrentLine == position) {
            ShowCurrentLineState();
            return;
        }


        if (cbConference.isChecked()) {
            CallManager.Instance().CurrentLine = position;
            mtips.setText("");
        } else {
            // To switch the line, must hold currently line first
            Session currentLine = CallManager.Instance().getCurrentSession();
            if (currentLine.state == Session.CALL_STATE_FLAG.CONNECTED && !currentLine.bHold) {
                application.mEngine.hold(currentLine.sessionID);
                currentLine.bHold = true;
                showTips(currentLine.lineName + ": Hold");
            }

            CallManager.Instance().CurrentLine = position;
            currentLine = CallManager.Instance().getCurrentSession();

            // If target line was in hold state, then un-hold it
            if (currentLine.IsIdle()) {
                showTips(currentLine.lineName + ": Idle");
            } else if (currentLine.state == Session.CALL_STATE_FLAG.CONNECTED && currentLine.bHold) {
                application.mEngine.unHold(currentLine.sessionID);
                currentLine.bHold = false;
                showTips(currentLine.lineName + ": UnHold - call established");
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.conference:
                if(application.mConference == b)
                    return;
                application.mConference = b;
                if(b){
                    application.mEngine.createVideoConference(null,320,240,true);
                    CallManager.Instance().addActiveSessionToConfrence(application.mEngine);
                }else {
                    application.mEngine.destroyConference();
                }
                break;
        }
    }


}