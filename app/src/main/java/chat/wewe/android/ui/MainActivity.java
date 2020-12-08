package chat.wewe.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import chat.wewe.android.R;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.PortSipService;


public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    public PortMessageReceiver receiver = null;
    private CountDownTimer countDownTimer;
    private final int REQ_DANGERS_PERMISSION = 2;
    public String name = null;
    public Boolean typeCall = false;

    RadioGroup menuGroup;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        receiver = new PortMessageReceiver();

        Intent intent = getIntent();

         name = intent.getStringExtra("name");
        typeCall = intent.getBooleanExtra("uid",false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager!= null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }else {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        setContentView(R.layout.main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PortSipService.REGISTER_CHANGE_ACTION);
        filter.addAction(PortSipService.CALL_CHANGE_ACTION);
        filter.addAction(PortSipService.PRESENCE_CHANGE_ACTION);
        registerReceiver(receiver, filter);


        menuGroup = findViewById(R.id.tab_menu);
        menuGroup.setOnCheckedChangeListener(this);

            ((RadioButton)menuGroup.getChildAt(1)).setChecked(true);
            countDownTimer = new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }
                @Override
                public void onFinish() {
                    ((RadioButton)menuGroup.getChildAt(2)).setChecked(true);
                    countDownTimer.cancel();
                }
            };
            countDownTimer.start();


    }



    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions (this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_DANGERS_PERMISSION:
                int i=0;
                for(int result:grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                      //  Toast.makeText(this, "you must grant the permission "+permissions[i], Toast.LENGTH_SHORT).show();
						i++;
                        stopService(new Intent(this, PortSipService.class));
                        System.exit(0);
                    }
                }
                break;
        }
    }
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (checkedId) {
            case R.id.tab_login:
                switchContent(R.id.login_fragment);
                break;
            case R.id.tab_numpad:
                switchContent(R.id.numpad_fragment);
                break;
            case R.id.tab_video:
                switchContent(R.id.video_fragment);
                break;
            case R.id.tab_message:
                switchContent(R.id.message_fragment);
                break;
            case R.id.tab_setting:
                switchContent(R.id.setting_fragment);
                break;
        }

    }

    public void switchContent(@IdRes int fragmentId) {
        Fragment fragment = getFragmentManager().findFragmentById(fragmentId);
        Fragment login_fragment = getFragmentManager().findFragmentById(R.id.login_fragment);
        Fragment numpad_fragment = getFragmentManager().findFragmentById(R.id.numpad_fragment);
        Fragment video_fragment = getFragmentManager().findFragmentById(R.id.video_fragment);
        Fragment setting_fragment = getFragmentManager().findFragmentById(R.id.setting_fragment);
        Fragment message_fragment = getFragmentManager().findFragmentById(R.id.message_fragment);

        FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
        fTransaction.hide(login_fragment).hide(numpad_fragment).hide(video_fragment).hide(setting_fragment).hide(message_fragment);
        if(fragment!=null){
            fTransaction.show( fragment).commit();
        }
    }

    public void requestPermissions(Activity activity) {
        // Check if we have write permission
        if(	PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ||PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                ||PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO))
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},
                    REQ_DANGERS_PERMISSION);
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
