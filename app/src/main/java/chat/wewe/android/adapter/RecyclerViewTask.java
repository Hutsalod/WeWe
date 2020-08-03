package chat.wewe.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import chat.wewe.android.R;
import chat.wewe.android.fragment.call_number.FragmentKeyboard;


public class RecyclerViewTask extends RecyclerView.Adapter<RecyclerViewTask.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";
    Button buttonAd;
    private ArrayList<String> mImageNames = new ArrayList<>();
    private ArrayList<String> mPosition = new ArrayList<>();
    private ArrayList<String> mCreatedBy= new ArrayList<>();
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<Integer> mNumberId = new ArrayList<>();
    private ArrayList<Boolean> mClosed = new ArrayList<>();
    private ArrayList<String> mRid= new ArrayList<>();
    private ArrayList<AdapterTaskList> adapter;
    private int set = 0;
    private Context mContext;

    public RecyclerViewTask(Context context, ArrayList<String> imageNames, ArrayList<String> position, ArrayList<String> createdBy, ArrayList<String> data, ArrayList<Integer> numberId,ArrayList<Boolean>  closed,ArrayList<String> rid) {
        mPosition = position;
        mImageNames = imageNames;
        mCreatedBy = createdBy;
        mData = data;
        mNumberId = numberId;
        mClosed = closed;
        mRid = rid;
        mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
       holder.name.setText("Задача #"+mNumberId.get(position)+" - "+mImageNames.get(position));
        holder.uid.setText(mPosition.get(position));
        holder._createdBy.setText(mCreatedBy.get(position));
        holder.data.setText(mData.get(position));

        if(mClosed.get(position)) {
            holder.linerm.setBackgroundColor(Color.parseColor("#F3F3F3"));
        }else {

            holder.linerm.setBackgroundColor(Color.parseColor("#DCF1DF"));
        }
        holder.linerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onGet(mNumberId.get(position),mRid.get(position));
            }
        });

        holder.red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener !=null) {
                    mListener.onRed(mPosition.get(position),mNumberId.get(position));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView name,uid,data,_createdBy;
        LinearLayout parentLayout,linerm;
        ListView list;
        int id;
        Button closed,red;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.task_name);
            uid = itemView.findViewById(R.id._taskText);
            data = itemView.findViewById(R.id.date);
            _createdBy = itemView.findViewById(R.id._createdBy);
            linerm = itemView.findViewById(R.id.linerm);
            closed = itemView.findViewById(R.id.closed);
            red = itemView.findViewById(R.id.red);


            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }

    public interface ActionListener{
        void onClick(String uid, int position);
        void onRed(String uid, int position);
        void onGet(int position, String rid);
    }

    private ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }
}















