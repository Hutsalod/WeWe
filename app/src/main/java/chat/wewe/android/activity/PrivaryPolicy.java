package chat.wewe.android.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import chat.wewe.android.R;

public class PrivaryPolicy extends AppCompatActivity {
    private final int REQ_DANGERS_PERMISSION = 2;

    SwitchCompat privary;
    SharedPreferences sPref,sPrefs,SipData;
    String code;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        privary = (SwitchCompat)findViewById(R.id.switch1);
        textView = (TextView)findViewById(R.id.textView);
        SpannableString ss = new SpannableString(textView.getText().toString());
        SipData = getSharedPreferences("SIP", MODE_PRIVATE);
        sPref = getSharedPreferences("Setting", MODE_PRIVATE);
        sPrefs = getSharedPreferences("pin", MODE_PRIVATE);
        code = sPrefs.getString("code", "");
        if(sPref.getInt("privary", '0')=='1'){
            if(code=="") {
                finish();
               startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

            } else{
                finish();
           startActivity(new Intent(getApplicationContext(), PinCodeLong.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

            }
        }
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://weltwelle.com/privpolicy.html")));
            }
        };

                ss.setSpan(clickableSpan, 79, 127, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        requestPermissions (this);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public void privaryOK(View view) {
        sPref = getSharedPreferences("Setting", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("privary", '1');
        ed.commit();
        if (privary.isChecked()==true){
            if(code=="") {
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }else{
                finish();
                startActivity(new Intent(getApplicationContext(), PinCodeLong.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
}
        }else {
            Toast.makeText(this, "Для пользования WeWe примите политику конфиденцильности!" ,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void requestPermissions(Activity activity) {
        // Check if we have write permission
        if(	PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                || PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
        || PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS)
        || PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS))
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
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