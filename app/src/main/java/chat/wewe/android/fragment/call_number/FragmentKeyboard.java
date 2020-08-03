package chat.wewe.android.fragment.call_number;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.StatusConnect;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.fragment.AbstractFragment;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FragmentKeyboard extends AbstractFragment implements  View.OnClickListener, StatusConnect {

    private TextInputEditText EditTextName;
    private RecyclerView mRecyclerView;
    private String getName;
    private ArrayList<ExampleItem> mExampleList;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout kayboardLayout,openKey;
    private ExampleAdapter mAdapters;
    public ImageView stanUsers;
    private SharedPreferences SipData;

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TableLayout dtmfPad =  view.findViewById(R.id.dtmf_pad);
        SetTableItemClickListener(dtmfPad);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_keyboard;
    }

    @Override
    protected void onSetupView() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        EditTextName = v.findViewById(R.id.EditTextName);
        mRecyclerView = v.findViewById(R.id.recyclerv_view);
        kayboardLayout = v.findViewById(R.id.kayboardLayout);
        stanUsers = v.findViewById(R.id.stanUsers);
        SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);



        openKey = v.findViewById(R.id.openKey);
        openKey.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                kayboardLayout.setVisibility(VISIBLE);
                mRecyclerView.setVisibility(GONE);
                openKey.setVisibility(GONE);
            }
        });

        TextView closed =  v.findViewById(R.id.textView2);
        closed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                kayboardLayout.setVisibility(GONE);
                mRecyclerView.setVisibility(VISIBLE);
                openKey.setVisibility(VISIBLE);

            }
        });
    //    userStatus();
        return v;
    }

    public void status(){
        stanUsers.setImageResource(R.drawable.s000);
    }

    private void SwitchPanel(){
        View dtmfView = getView().findViewById(R.id.dtmf_pad);
        if (dtmfView.getVisibility() == View.VISIBLE) {
            dtmfView.setVisibility(View.INVISIBLE);
        } else {
            dtmfView.setVisibility(View.VISIBLE);
        }
    }

    private void SetTableItemClickListener(TableLayout table) {

        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow tableRow = (TableRow) table.getChildAt(i);
            int line = tableRow.getChildCount();
            for (int index = 0; index < line; index++) {
                tableRow.getChildAt(index).setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn1:
            case R.id.btn2:
            case R.id.btn3:
            case R.id.btn4:
            case R.id.btn5:
            case R.id.btn6:
            case R.id.btn7:
            case R.id.btn8:
            case R.id.btn9:
            case R.id.btn10:
            case R.id.btn11:
            case R.id.btn12: {
                String numberString = ((Button) view).getText().toString();

                EditTextName.append(numberString);
             break;

    }
            case R.id.btn13:
                ((MainActivity) getActivity()).navigation.setSelectedItemId(R.id.action_group);
                break;
            case R.id.btn14:
                startActivity(new Intent(getActivity(), chat.wewe.android.ui.MainActivity.class).putExtra("uid", RocketChatCache.INSTANCE.getUserId()).putExtra("name",EditTextName.getText().toString()));
                break;
            case R.id.btn15:
                    if(!EditTextName.getText().toString().equals(""))
                        EditTextName.setText(new StringBuffer(EditTextName.getText().toString()).delete(EditTextName.getText().toString().length()-1,EditTextName.getText().toString().length()));
                break;
        }}
/*

    public void helpB(View v) {
        Button clickedButton = (Button) v;
        switch (clickedButton.getId()) {
            case R.id.btn1:
                EditTextName.setText(EditTextName.getText()+"1");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn2:
                EditTextName.setText(EditTextName.getText()+"2");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn3:
                EditTextName.setText(EditTextName.getText()+"3");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn4:
                EditTextName.setText(EditTextName.getText()+"4");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn5:
                EditTextName.setText(EditTextName.getText()+"5");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn6:
                EditTextName.setText(EditTextName.getText()+"6");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn7:
                EditTextName.setText(EditTextName.getText()+"7");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn8:
                EditTextName.setText(EditTextName.getText()+"8");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn9:
                EditTextName.setText(EditTextName.getText()+"9");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn10:
                EditTextName.setText(EditTextName.getText()+"*");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn11:
                EditTextName.setText(EditTextName.getText()+"0");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn12:
                EditTextName.setText(EditTextName.getText()+"#");
                EditTextName.setSelection(EditTextName.getText().length());
                break;
            case R.id.btn13:
                break;
            case R.id.btn14:
                getName = EditTextName.getText().toString().replace("+", "").replaceAll("[^0-9\\.]", "");
                if(!EditTextName.getText().toString().substring(1,2).equals("0") && EditTextName.getText().toString().length()>3) {
                    if(SipData.getString("INNER_GROUP", "false").equals("true")) {
                        if(EditTextName.getText().toString().indexOf("+")<0)
                            EditTextName.setText("+"+EditTextName.getText().toString());
                        setInsertButton();
                    }else {
                        Toast.makeText(getActivity().getApplicationContext(),"У вас нет доступа!",Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getActivity().getApplicationContext(), "Неверный формат международного телефонного номера",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btn15:
                if(!EditTextName.getText().toString().equals(""))
                    EditTextName.setText(new StringBuffer(EditTextName.getText().toString()).delete(EditTextName.getText().toString().length()-1,EditTextName.getText().toString().length()));
                break;
        }
    }

 */

    private void setInsertButton() {
        long date = System.currentTimeMillis();
        SimpleDateFormat datas = new SimpleDateFormat("H:m:s yyyy-MM-dd");
        String time = datas.format(date);
        insertItem(""+getName, ""+time);
        Log.d("XSWQAZ","ADD ");
    }

    private void insertItem(String line1, String line2) {
        mExampleList.add(new ExampleItem(line1, line2));
        mAdapters.notifyItemInserted(mExampleList.size());

    }

    private void animateHide(final View view) {
        view.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(GONE);
            }
        });
    }

    private void animateShow(final View view) {
        view.animate().scaleX(1).scaleY(1).setDuration(150).withStartAction(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(VISIBLE);
            }
        });
    }

    public void setText(int item) {
        ImageView view = getView().findViewById(R.id.stanUsers);
        view.setVisibility(GONE);
    }

    private void buildRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapters = new ExampleAdapter(mExampleList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapters);
    }

    @Override
    public void noConnect() {
        stanUsers.setImageResource(R.drawable.s000);
    }

    @Override
    public void Connecting() {
        this.stanUsers.setImageResource(R.drawable.s112);
    }

    @Override
    public void okConnect() {
        this.stanUsers.setImageResource(R.drawable.s222);
    }

}
