package chat.wewe.android.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import java.util.Locale;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;


public class Intro extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

                RocketChatCache.INSTANCE.setSelectedRoomId(null);
                setLocale(getSharedPreferences("SIP", MODE_PRIVATE).getString("LANG_APP", "ru"));
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.cancelAll();

                    if(getSharedPreferences("Setting", MODE_PRIVATE).getInt("privary", '0')=='1' ||
                            getSharedPreferences("Setting", MODE_PRIVATE).getBoolean("PrivaryPolicy", false) == true){

                        startActivity(new Intent(Intro.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        Intro.this.finish();

                    }else{

                        startActivity(new Intent(Intro.this, PrivaryPolicy.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        Intro.this.finish();
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




    public void setLocale(String language_code){
        Activity activity = this;
        Resources res = activity.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(language_code.toLowerCase());
        res.updateConfiguration(conf, dm);
    }


}