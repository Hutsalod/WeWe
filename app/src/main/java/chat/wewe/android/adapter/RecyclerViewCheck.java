package chat.wewe.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.ArrayList;

import chat.wewe.android.R;


public class RecyclerViewCheck extends RecyclerView.Adapter<RecyclerViewCheck.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";
    Button buttonAd;
    private ArrayList<Integer> mitemId = new ArrayList<>();
    private ArrayList<String> mitemText = new ArrayList<>();
    private ArrayList<Boolean> mcheck = new ArrayList<>();
    private ArrayList<Integer> mNumberIdCheck = new ArrayList<>();
    private int set = 0;
    private Context mContext;

    public RecyclerViewCheck(Context context, ArrayList<Integer> itemId, ArrayList<String> itemText,  ArrayList<Boolean> check,ArrayList<Integer> numberIdCheck) {
        mitemId = itemId;
        mitemText = itemText;
        mcheck = check;
        mNumberIdCheck = numberIdCheck;
        mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.iteam_check_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.checkBox.setChecked(mcheck.get(position));
        holder.checkBox.setText(mitemText.get(position));

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener !=null) {
                    mListener.onClick(mitemId.get(position),mNumberIdCheck.get(position),holder.checkBox.isChecked());
                }
            }
        });


        holder.closed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener !=null) {
                    mListener.onClosed(mitemId.get(position),mNumberIdCheck.get(position));
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mitemText.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox checkBox;
        Button closed;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            closed = itemView.findViewById(R.id.closed);
        }
    }

    public interface ActionListener{
        void onClick(int position, int mNumberIdCheck, Boolean cheak);
        void onClosed(int position, int cheak);
    }

    private ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }
}















