package chat.wewe.android;

import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.portsip.PortSipSdk;

import java.util.List;

import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.OkHttpHelper;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android.widget.RocketChatWidgets;
import chat.wewe.android_ddp.DDPClient;
import chat.wewe.core.models.ServerInfo;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.RocketChatPersistenceRealm;
import io.fabric.sdk.android.Fabric;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Customized Application-class for Rocket.Chat
 */
public class RocketChatApplication extends MultiDexApplication {

    private static RocketChatApplication instance;

    public static RocketChatApplication getInstance() {
        return instance;
    }
    public boolean mConference= false;
    public PortSipSdk mEngine;
    public boolean mUseFrontCamera= false;

    @Override
    public void onCreate() {
        super.onCreate();
        mEngine = new PortSipSdk();
        RocketChatCache.INSTANCE.initialize(this);
        JobManager.create(this).addJobCreator(new RocketChatJobCreator());
        DDPClient.initialize(OkHttpHelper.INSTANCE.getClientForWebSocket());
        Fabric.with(this, new Crashlytics());

        RocketChatPersistenceRealm.init(this);

        List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(this).getServerList();
        for (ServerInfo serverInfo : serverInfoList) {
            RealmStore.put(serverInfo.getHostname());
        }

        RocketChatWidgets.initialize(this, OkHttpHelper.INSTANCE.getClientForDownloadFile());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            Logger.INSTANCE.report(e);
        });

        instance = this;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible = false;
}