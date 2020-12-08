package chat.wewe.android.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.wewe.android.R;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.widget.RocketChatAvatar;
import chat.wewe.android.widget.helper.AvatarHelper;

public class ShowUserDetailed extends AppCompatActivity {

    private  MethodCallHelper methodCall;


    public static final String HOSTNAME = "hostname",USERID = "userId";
    private LinearLayout btnAudio,btnVideo;
    private TextView textName,texStatus,texRols;
    private RocketChatAvatar avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar  toolbar = findViewById(R.id.toolbar);
        textName = findViewById(R.id.name);
        btnAudio = findViewById(R.id.btnAudio);
        btnVideo = findViewById(R.id.btnVideo);

        texStatus = findViewById(R.id.status);
        avatar = findViewById(R.id.avatar);
        texRols = findViewById(R.id.rols);



        Intent intent = getIntent();
        String hostname = intent.getStringExtra("hostname");
        String userId = intent.getStringExtra("userId");
        String name = intent.getStringExtra("name");
        String status = intent.getStringExtra("status");
        String username = intent.getStringExtra("username");
        String rols = intent.getStringExtra("rols");

        toolbar.setTitle(username);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textName.setText(name);
        texStatus.setText("Статус: "+status);
        texRols.setText("Email: "+rols);
        methodCall = new MethodCallHelper(getApplicationContext(), hostname);

        if (avatar != null) {

            Drawable placeholderDrawable = AvatarHelper.INSTANCE.getTextDrawable(name, getApplicationContext());
            avatar.loadImage("https://chat.weltwelle.com/avatar/"+username, placeholderDrawable);
        }

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callVoice();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callVideoVoice();
            }
        });
    }

    public void callVoice(){
        startActivity(new Intent(this, chat.wewe.android.ui.MainActivity.class).putExtra("uid", false).putExtra("name", ""));
    }

    public void callVideoVoice(){
        startActivity(new Intent(this, chat.wewe.android.ui.MainActivity.class).putExtra("uid", true).putExtra("name", ""));
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return false;
    }
}
