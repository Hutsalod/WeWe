package chat.wewe.android.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.service.PortSipService;

public class SettingFragment extends PreferenceFragment {
	RocketChatApplication application;
	MainActivity activity;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		application = (RocketChatApplication)activity.getApplication();
		addPreferencesFromResource(R.xml.setting);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.setBackgroundColor(getResources().getColor(R.color.white));
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
			PortSipService.ConfigPreferences(getActivity(), preferences, application.mEngine);
		}else{
			activity.receiver.broadcastReceiver =null;
		}
	}
}
