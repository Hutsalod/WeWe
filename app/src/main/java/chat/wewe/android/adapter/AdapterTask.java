package chat.wewe.android.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import chat.wewe.android.R;


public class AdapterTask extends RecyclerView.Adapter<AdapterTask.ViewHolder>{


    private ArrayList<AdapterTaskList> adapterTask;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public  ImageView image;
        public TextView name,uid,data,_createdBy,textView16;
        public LinearLayout parentLayout,linerm;
        public ListView list;
        public int id;
        public Button closed,red;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.task_name);
            uid = itemView.findViewById(R.id._taskText);
            data = itemView.findViewById(R.id.date);
            _createdBy = itemView.findViewById(R.id._createdBy);
            linerm = itemView.findViewById(R.id.linerm);
            closed = itemView.findViewById(R.id.closed);
            red = itemView.findViewById(R.id.red);
            textView16 = itemView.findViewById(R.id.textView16);

            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }

    public AdapterTask(ArrayList<AdapterTaskList> adapters) {
        adapterTask = adapters;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        final AdapterTaskList currentItems = adapterTask.get(i);

       holder.name.setText("Задача #"+currentItems.mNumberId()+" - "+currentItems.mImageNames());
        holder.uid.setText(currentItems.mPosition());
        holder._createdBy.setText(currentItems.mCreatedBy());
        holder.data.setText(currentItems.mData());
        holder.textView16.setText("Низкий приоритет");
        if(currentItems.mClosed())
            holder.linerm.setBackgroundColor(Color.parseColor("#FFFFFF"));
        else
            holder.linerm.setBackgroundColor(Color.parseColor("#3DB3E5C8"));

        if(currentItems.mPriority().equals("0")) {
            holder.textView16.setVisibility(View.GONE);
        }
        if(currentItems.mPriority().equals("1")) {
            holder.textView16.setTextColor(Color.parseColor("#F2994A"));
            holder.textView16.setText("Низкий приоритет");
            holder.textView16.setBackgroundResource(R.drawable.item_task_grean);
        }
        if(currentItems.mPriority().equals("2")){
            holder.textView16.setBackgroundResource(R.drawable.item_button_task);
            holder.textView16.setText("Высокий приоритет");
            holder.textView16.setTextColor(Color.parseColor("#EB5757"));
        }


        holder.linerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onGet(String.valueOf(currentItems.mNumberId()),currentItems.mRid());
            }
        });



    }

    @Override
    public int getItemCount() {
        return adapterTask.size();
    }



    public interface ActionListener{
        void onGet(String position, String rid);
    }

    public ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }
}















