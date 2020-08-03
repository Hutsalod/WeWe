package chat.wewe.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import chat.wewe.android.R;

public class PinCodeLong extends AppCompatActivity {

    SharedPreferences sPref;
    EditText EditTextName;
    String code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.pin_code_long);

        sPref = getSharedPreferences("pin", MODE_PRIVATE);
        SharedPreferences.Editor ed =  sPref.edit();
        code = sPref.getString("code", "");
        ed.commit();
        EditTextName = (EditText)findViewById(R.id.EditTextName);
    }


    public void helpB(View v) {
        Button clickedButton = (Button) v;
        switch (clickedButton.getId()) {
            case R.id.btn1:
                EditTextName.setText(EditTextName.getText()+"1");
                EditTextName();
                break;
            case R.id.btn2:
                EditTextName.setText(EditTextName.getText()+"2");
                EditTextName();
                break;
            case R.id.btn3:
                EditTextName.setText(EditTextName.getText()+"3");
                EditTextName();
                break;
            case R.id.btn4:
                EditTextName.setText(EditTextName.getText()+"4");
                EditTextName();
                break;
            case R.id.btn5:
                EditTextName.setText(EditTextName.getText()+"5");
                EditTextName();
                break;
            case R.id.btn6:
                EditTextName.setText(EditTextName.getText()+"6");
                EditTextName();
                break;
            case R.id.btn7:
                EditTextName.setText(EditTextName.getText()+"7");
                EditTextName();
                break;
            case R.id.btn8:
                EditTextName.setText(EditTextName.getText()+"8");
                EditTextName();
                break;
            case R.id.btn9:
                EditTextName.setText(EditTextName.getText()+"9");
                EditTextName();
                break;
            case R.id.btn11:
                EditTextName.setText(EditTextName.getText()+"0");
                EditTextName();
                break;
            case R.id.btn15:
                if(!EditTextName.getText().toString().equals(""))
                    EditTextName.setText(new StringBuffer(EditTextName.getText().toString()).delete(EditTextName.getText().toString().length()-1,EditTextName.getText().toString().length()));
                break;
        }

    }


    private void EditTextName(){
        if(EditTextName.getText().toString().matches(code)){
           // startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
             }
        if(EditTextName.getText().length()>=5)
            EditTextName.setText(new StringBuffer(EditTextName.getText().toString()).delete(EditTextName.getText().toString().length()-1,EditTextName.getText().toString().length()));
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