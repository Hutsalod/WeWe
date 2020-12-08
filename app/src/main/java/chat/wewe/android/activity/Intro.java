package chat.wewe.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.Locale;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.UtilsApiChat;


public class Intro extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    public static boolean callSet,subscription;
    private SharedPreferences SipData,sPref;
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.intro);


       // this.getSharedPreferences("caches", 0).edit().clear().apply();

        RocketChatCache.INSTANCE.setSelectedRoomId(null);
        SipData = getSharedPreferences("SIP", MODE_PRIVATE);
        code = getSharedPreferences("pin", MODE_PRIVATE).getString("code", "");
        sPref = getSharedPreferences("Setting", MODE_PRIVATE);
        setLocale(SipData.getString("LANG_APP", "ru"));




                    if(sPref.getInt("privary", '0')=='1'){
                            startActivity(new Intent(getApplication(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    }else{
                        startActivity(new Intent(getApplicationContext(), PrivaryPolicy.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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