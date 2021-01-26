package chat.wewe.android.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import java.util.Calendar;

import chat.wewe.android.R;

import static android.content.Context.MODE_PRIVATE;

public class VersionApp {


    public AlertDialog showUpdate(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.vers_name)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                context.getSharedPreferences("vDay", MODE_PRIVATE).edit().putInt("vDay", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).commit();
                            }
                        }
                ).setPositiveButton("ОБНОВИТЬ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=chat.wewe.android")));
            }
        });
        return builder.create();
    }


}
