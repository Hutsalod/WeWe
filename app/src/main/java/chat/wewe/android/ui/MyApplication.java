package chat.wewe.android.ui;

import android.app.Application;

import com.portsip.PortSipSdk;

public class MyApplication extends Application {
	public boolean mConference= false;
	public PortSipSdk mEngine;
	public boolean mUseFrontCamera= false;

	@Override
	public void onCreate() {
		super.onCreate();
		mEngine = new PortSipSdk();
	}
}
