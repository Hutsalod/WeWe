package chat.wewe.android.widget.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import chat.wewe.android.widget.R;
import chat.wewe.android.widget.helper.FrescoHelper;


public  class ShowImageUrls extends AppCompatActivity {

    public String urlIntent = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_url);
        SimpleDraweeView image =  findViewById(R.id.image);
        Button closed =  findViewById(R.id.closed);
        urlIntent = getIntent().getStringExtra("url");

        FrescoHelper.INSTANCE.loadImageWithCustomization(image, urlIntent);

        closed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
