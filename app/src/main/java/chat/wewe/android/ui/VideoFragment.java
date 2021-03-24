package chat.wewe.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.portsip.PortSIPVideoRenderer;
import com.portsip.PortSipEnumDefine;
import com.portsip.PortSipErrorcode;
import com.portsip.PortSipSdk;

import java.util.Timer;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.chatroom.RoomContract;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.util.CallManager;
import chat.wewe.android.util.Ring;
import chat.wewe.android.util.Session;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;

import static android.content.Context.MODE_PRIVATE;

public class VideoFragment extends BaseFragment implements View.OnClickListener , PortMessageReceiver.BroadcastListener{
	private RocketChatApplication application;
	private MainActivity activity;
	private int t = 0,m;
	public  int callSed = 0;
	private boolean setNumber = false;
	private PortSIPVideoRenderer remoteRenderScreen = null;
	private PortSIPVideoRenderer localRenderScreen = null;
	private PortSIPVideoRenderer.ScalingType scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_BALANCED;// SCALE_ASPECT_FIT or SCALE_ASPECT_FILL;
	private ImageView imgSwitchCamera = null;
	private ImageView imgScaleType = null,finish_car= null,ic_video_call= null,ic_audio_call= null,ic_number = null;

	public TextView usersName,statucConnect;

	private EditText etSipNum;
	private AbsoluteUrlHelper absoluteUrlHelper;
	protected RoomContract.Presenter presenter;
	private String userId,roomId,hostname;
	private MethodCallHelper methodCallHelper;
	private SharedPreferences SipData;
	private RealmRoomRepository roomRepository;
	private RealmUserRepository userRepository;
	private FrameLayout ramkaVideo;
	private String name;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		activity = (MainActivity)getActivity();
		application = (RocketChatApplication)activity.getApplication();

		return inflater.inflate(R.layout.video, container, false);


	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		etSipNum =  view.findViewById(R.id.etsipaddress);
		SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
		userId = SipData.getString("ID_RC","");
		roomId = SipData.getString("RM_ID","");
		hostname = SipData.getString("hostname","");
		TableLayout dtmfPad =  view.findViewById(R.id.dtmf_pad);



		imgSwitchCamera = (ImageView)view.findViewById(R.id.ibcamera);
		imgScaleType = (ImageView)view.findViewById(R.id.ibscale);
		finish_car = (ImageView)view.findViewById(R.id.finish_car);
		ic_video_call = (ImageView)view.findViewById(R.id.ic_video_call);
		ic_audio_call = (ImageView)view.findViewById(R.id.ic_audio_call);
		ic_number = (ImageView)view.findViewById(R.id.ic_number);
		Timer time = new Timer();
		imgScaleType.setOnClickListener(this);
		imgSwitchCamera.setOnClickListener(this);
		finish_car.setOnClickListener(this);
		ic_video_call.setOnClickListener(this);
		ic_audio_call.setOnClickListener(this);
		ic_number.setOnClickListener(this);
		SetTableItemClickListener(dtmfPad);
		name = ((MainActivity) getActivity()).name;
		usersName = (TextView) view.findViewById(R.id.usersName);
		usersName.setText(name);
		statucConnect = (TextView)view.findViewById(R.id.statucConnect);

		localRenderScreen = (PortSIPVideoRenderer)view.findViewById(R.id.local_video_view);
		remoteRenderScreen = (PortSIPVideoRenderer)view.findViewById(R.id.remote_video_view);
		ramkaVideo = view.findViewById(R.id.ramka_video);


		scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FIT;//
		remoteRenderScreen.setScalingType(scalingType);
		activity.receiver.broadcastReceiver =this ;


		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				++t;
				if(t>59){
					t=0;
					++m;
				}
				statucConnect.setText(""+m+":"+((t>=10)? "" : "0")+t);
				handler.postDelayed(this, 1000);
			}
		});

		Session currentLine = CallManager.Instance().getCurrentSession();

		usersName.setText(currentLine.displayName);
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		PortSipSdk portSipLib = application.mEngine;
		if(localRenderScreen!=null){
			if(portSipLib!=null) {
				portSipLib.displayLocalVideo(false);

			}
			localRenderScreen.release();
		}


		CallManager.Instance().setRemoteVideoWindow(application.mEngine,-1,null);//set
		if(remoteRenderScreen!=null){
			remoteRenderScreen.release();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (hidden) {
			localRenderScreen.setVisibility(View.INVISIBLE);
			stopVideo(application.mEngine);
		}
		else
		{
			updateVideo(application.mEngine);
			activity.receiver.broadcastReceiver = this;
			localRenderScreen.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View view)
	{
		if (application.mEngine == null)
			return;
		PortSipSdk portSipLib = application.mEngine;
		Session currentLine = CallManager.Instance().getCurrentSession();



		switch (view.getId())
		{

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
				Log.d("XSWQAZ",""+numberString);
				char number = numberString.charAt(0);
				//	etSipNum.append(numberString);
				if (CallManager.Instance().regist && currentLine.state == Session.CALL_STATE_FLAG.CONNECTED) {
					if (number == '*') {
						portSipLib.sendDtmf(currentLine.sessionID, PortSipEnumDefine.ENUM_DTMF_MOTHOD_RFC2833, 10,
								160, true);
						return;
					}
					if (number == '#') {
						portSipLib.sendDtmf(currentLine.sessionID, PortSipEnumDefine.ENUM_DTMF_MOTHOD_RFC2833, 11,
								160, true);
						return;
					}
					int sum = Integer.parseInt(numberString);// 0~9
					portSipLib.sendDtmf(currentLine.sessionID, PortSipEnumDefine.ENUM_DTMF_MOTHOD_RFC2833, sum,
							160, true);
				}
			}
			break;
			case R.id.ibcamera:
				application.mUseFrontCamera = !application.mUseFrontCamera;
				SetCamera(portSipLib, application.mUseFrontCamera);
				break;
			case R.id.ic_number:
				SwitchPanel();

				break;
			case R.id.ibscale:
				if (scalingType == PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FIT)
				{
					imgScaleType.setImageResource(R.drawable.aspect_fill);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FILL;
				}
				else if (scalingType == PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FILL)
				{
					imgScaleType.setImageResource(R.drawable.aspect_balanced);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_BALANCED;
				}
				else
				{
					imgScaleType.setImageResource(R.drawable.aspect_fit);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FIT;
				}

				localRenderScreen.setScalingType(scalingType);
				remoteRenderScreen.setScalingType(scalingType);
				updateVideo(portSipLib);
				break;

			case R.id.finish_car:
				Ring.getInstance(getActivity()).stop();
				switch (currentLine.state) {
					case INCOMING:
						portSipLib.rejectCall(currentLine.sessionID, 486);
						break;
					case CONNECTED:
					case TRYING:
						portSipLib.hangUp(currentLine.sessionID);
						break;
				}
				getActivity().finish();
				//startActivity(Home);

				currentLine.Reset();


				break;
			case R.id.ic_video_call:
			{
				if (currentLine.bMute) {
					portSipLib.muteSession(currentLine.sessionID, false,
							false, false, false);
					currentLine.bMute = false;
					ic_video_call.setImageResource(R.drawable.ic_video_call);
				} else {
					portSipLib.muteSession(currentLine.sessionID, false,
							true, true, true);
					currentLine.bMute = true;
					ic_video_call.setImageResource(R.drawable.ic_video_call_off);
				}
			}
			break;
			case R.id.ic_audio_call:
				if (currentLine.bDenam)
				{
					ic_audio_call.setImageResource(R.drawable.ic_audio_call);
					portSipLib.muteSession(currentLine.sessionID, false,
							false, false, false);
					currentLine.bDenam = false;

				} else {

					ic_audio_call.setImageResource(R.drawable.ic_audio_call_off);
					portSipLib.muteSession(currentLine.sessionID, true,
							false, true, true);
					currentLine.bDenam = true;
				}
				break;
		}
	}



	private void SetCamera(PortSipSdk portSipLib, boolean userFront)
	{
		if (userFront)
		{
			portSipLib.setVideoDeviceId(1);
		}
		else
		{
			portSipLib.setVideoDeviceId(0);
		}
	}

	private void stopVideo(PortSipSdk portSipLib)
	{
		Session cur = CallManager.Instance().getCurrentSession();
		if(portSipLib!=null) {
			portSipLib.displayLocalVideo(false);
			portSipLib.setLocalVideoWindow(null);
			CallManager.Instance().setRemoteVideoWindow(portSipLib,cur.sessionID,null);
			CallManager.Instance().setConferenceVideoWindow(portSipLib,null);
		}
	}

	public void updateVideo(PortSipSdk portSipLib)
	{
		CallManager callManager = CallManager.Instance();
		if (application.mConference)
		{
			callManager.setConferenceVideoWindow(portSipLib,remoteRenderScreen);
		}else {
			Session cur = CallManager.Instance().getCurrentSession();
			if (cur != null && !cur.IsIdle()
					&& cur.sessionID != PortSipErrorcode.INVALID_SESSION_ID
					&& cur.hasVideo) {

				callManager.setRemoteVideoWindow(portSipLib,cur.sessionID, remoteRenderScreen);

				portSipLib.setLocalVideoWindow(localRenderScreen);
				portSipLib.displayLocalVideo(true); // display Local video
				portSipLib.sendVideo(cur.sessionID, true);
			} else {
				portSipLib.displayLocalVideo(false);
				callManager.setRemoteVideoWindow(portSipLib,cur.sessionID, null);
				portSipLib.setLocalVideoWindow(null);
			}
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();

	}


	private void SwitchPanel(){
		View dtmfView = getView().findViewById(R.id.dtmf_pad);
		if (dtmfView.getVisibility() == View.VISIBLE) {
			dtmfView.setVisibility(View.INVISIBLE);
		} else {
			dtmfView.setVisibility(View.VISIBLE);
		}
	}

	public void onBroadcastReceiver(Intent intent)
	{

		String action = intent == null ? "" : intent.getAction();
		if (PortSipService.CALL_CHANGE_ACTION.equals(action))
		{

			long sessionId = intent.getLongExtra(PortSipService.EXTRA_CALL_SEESIONID, Session.INVALID_SESSION_ID);

			Log.d("SIPCALL","sessionId "+sessionId);
			String status = intent.getStringExtra(PortSipService.EXTRA_CALL_DESCRIPTION);
			Log.d("SIPCALL","sessionId "+status);
			if(status.equals("1")) {
				getActivity().finish();
			}

			Session session = CallManager.Instance().findSessionBySessionID(sessionId);
			if (session.state != null)
			{
				switch (session.state)
				{
					case INCOMING:
						break;
					case TRYING:
						break;
					case CONNECTED:
						t = 0;
						m = 0;
						setNumber = true;
						break;
					case FAILED:
					case CLOSED:
						if (setNumber == true)
							getActivity().finish();
						updateVideo(application.mEngine);
						break;

				}
			}
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
}
