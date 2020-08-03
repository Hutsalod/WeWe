package chat.wewe.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import chat.wewe.android.R;


public class TaskMessage extends RecyclerView.Adapter<TaskMessage.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";
    Button buttonAd;
    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<String> mMessage = new ArrayList<>();
    private Context mContext;

    public TaskMessage(Context context,  ArrayList<String> name, ArrayList<String> data,ArrayList<String> message) {
        mName = name;
        mData = data;
        mMessage = message;
        mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.name.setText(mName.get(position));
        holder.uid.setText(mData.get(position));



    }

    @Override
    public int getItemCount() {
        return mName.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name,uid,data,_createdBy;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_view_cat_name);
            uid = itemView.findViewById(R.id.text_view_message);


        }
    }


}















