package chat.wewe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chat.wewe.android.R;


/**
 * Created by incred-dev on 6/7/18.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder>{

    private List<ContactModel> contactModelList;
    static int a_chars = 0;
    LinearLayout teamContact;
    Intent callInt ;
    private Context mContext;

    public ContactAdapter(Context context, List<ContactModel> contactModelList) {
        this.contactModelList = contactModelList;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ContactModel model = contactModelList.get(position);
        if (model != null){
            if (model.getName() != null){
                holder.name.setText(model.getName());
                char a_char = model.getName().replace(" ","").replace("-","").replace("+","").charAt(0);
                holder.iconContact.setText(""+a_char);


            }

            if (model.getNumber() != null){
                StringBuffer buffer = new StringBuffer();
                for (String number:model.getNumber()){
                    buffer.append(number).append("\n");
                    ++a_chars;
                    if(a_chars>=10)
                        a_chars = 1;
                    if(a_chars==1)
                        holder.ImageContact.setImageResource(R.drawable.list_users);
                    if(a_chars==2)
                        holder.ImageContact.setImageResource(R.drawable.list_usersq);
                    if(a_chars==3)
                        holder.ImageContact.setImageResource(R.drawable.list_usersw);
                    if(a_chars==4)
                        holder.ImageContact.setImageResource(R.drawable.list_userse);
                    if(a_chars==5)
                        holder.ImageContact.setImageResource(R.drawable.list_usersr);
                    if(a_chars==6)
                        holder.ImageContact.setImageResource(R.drawable.list_users);
                    if(a_chars==7)
                        holder.ImageContact.setImageResource(R.drawable.list_usersq);
                    if(a_chars==8)
                        holder.ImageContact.setImageResource(R.drawable.list_usersw);
                    if(a_chars==9)
                        holder.ImageContact.setImageResource(R.drawable.list_userse);
                }

                holder.number.setText(buffer);
                teamContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        }


    }

    @Override
    public int getItemCount() {
        return contactModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, number, iconContact;
        ImageView ImageContact;

        public MyViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tvName);
            number = (TextView) itemView.findViewById(R.id.tvNumber);
            iconContact = (TextView) itemView.findViewById(R.id.iconContact);
            ImageContact = (ImageView) itemView.findViewById(R.id.ImageContact);
            teamContact = (LinearLayout) itemView.findViewById(R.id.teamContact);

        }
    }

    public void filterList(ArrayList<ContactModel> filteredList) {
        contactModelList = filteredList;
        notifyDataSetChanged();
    }
}
