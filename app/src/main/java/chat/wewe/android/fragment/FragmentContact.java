package chat.wewe.android.fragment;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import chat.wewe.android.R;
import chat.wewe.android.StatusConnect;
import chat.wewe.android.activity.ContactAdapter;
import chat.wewe.android.activity.ContactModel;

public class FragmentContact extends AbstractFragment implements StatusConnect {

    private List<ContactModel> contactModelList = new ArrayList<>();
    private  String[] PERMISSION_CONTACT = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
    private  final int REQUEST_CONTACT = 1;
    private  RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    public ImageView stanUsers;


    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void onSetupView() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        recyclerView =  v.findViewById(R.id.rv);
        stanUsers = v.findViewById(R.id.userStatus);
        setDataToAdapter();
        requestContactsPermissions();
        return v;
    }

    private void setDataToAdapter(){
        contactAdapter = new ContactAdapter(getActivity(),contactModelList);
        initRecyclerView();
    }

    private void initRecyclerView(){
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(contactAdapter);
    }
    private void getContactInfo(){
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PHONE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String PHONE_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI,null,null,null,DISPLAY_NAME);

        if (cursor.getCount() > 0){
            while (cursor.moveToNext()){
                String CONTACT_ID = cursor.getString(cursor.getColumnIndex(ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(HAS_PHONE_NUMBER));
                ContactModel contactModel = new ContactModel();
                if (hasPhoneNumber > 0){
                    contactModel.setName(name);

                    Cursor phoneCursor = contentResolver.query(PHONE_URI, new String[]{NUMBER},PHONE_ID+" = ?",new String[]{CONTACT_ID},null);
                    List<String> contactList = new ArrayList<>();
                    phoneCursor.moveToFirst();
                    while (!phoneCursor.isAfterLast()){
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)).replace(" ","");
                        contactList.add(phoneNumber);
                        phoneCursor.moveToNext();
                    }
                    contactModel.setNumber(contactList);
                    contactModelList.add(contactModel);
                    phoneCursor.close();
                }
            }
            contactAdapter.notifyDataSetChanged();
        }
    }


    public void requestContactsPermissions(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_CONTACTS)){


            } else {
                ActivityCompat.requestPermissions(getActivity(),PERMISSION_CONTACT,REQUEST_CONTACT);
            }
        } else {
            getContactInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults){
            if (result == PackageManager.PERMISSION_GRANTED){
                getContactInfo();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

     //   userStatus();
    }



    @Override
    public void noConnect() {
       stanUsers.setImageResource(R.drawable.s000);

        Log.d("TWEWET","s000");
    }

    @Override
    public void Connecting() {
       this.stanUsers.setImageResource(R.drawable.s112);

        Log.d("TWEWET","s112");
    }

    @Override
    public void okConnect() {
        this.stanUsers.setImageResource(R.drawable.s222);

        Log.d("TWEWET","222");
    }


}
