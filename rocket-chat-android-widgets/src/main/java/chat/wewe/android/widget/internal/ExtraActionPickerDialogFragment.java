package chat.wewe.android.widget.internal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import chat.wewe.android.widget.R;
import chat.wewe.android.widget.layouthelper.MessageExtraActionListAdapter;
import chat.wewe.android.widget.message.MessageExtraActionItemPresenter;

public class ExtraActionPickerDialogFragment extends BottomSheetDialogFragment {

  private List<MessageExtraActionItemPresenter> actionItems;
  public boolean videoQuality = true;


  public static ExtraActionPickerDialogFragment create(
      List<MessageExtraActionItemPresenter> actionItems,boolean videoQuality) {
    ExtraActionPickerDialogFragment fragment = new ExtraActionPickerDialogFragment();
    fragment.setActionItems(actionItems,videoQuality);

    return fragment;
  }




  public void setActionItems(List<MessageExtraActionItemPresenter> actionItems,boolean videoQuality) {
    this.actionItems = actionItems;
    this.videoQuality = videoQuality;
  }

  @Override
  public final void setupDialog(Dialog dialog, int style) {
    super.setupDialog(dialog, style);
    dialog.setContentView(R.layout.dialog_message_extra_action_picker);

    MessageExtraActionListAdapter adapter = new MessageExtraActionListAdapter(actionItems);
    adapter.setOnItemClickListener(new MessageExtraActionListAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(final int itemId) {

        if(videoQuality){
        if(itemId==12) {
          AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
          builder.setMessage("Для отправки видеофайл будет сжат. Сжатие может занять некоторое время. Начать процесс?");
          builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              callbackOnItemSelected(itemId);
            }
          });
          builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
          });
          AlertDialog dialog = builder.create();
          dialog.show();
        }
        else
          callbackOnItemSelected(itemId);
        }else {
          callbackOnItemSelected(itemId);
        }


        dismiss();
      }
    });

    RecyclerView recyclerView =
        (RecyclerView) dialog.findViewById(R.id.message_extra_action_listview);
    recyclerView.setAdapter(adapter);
  }

  private void callbackOnItemSelected(int itemId) {
    final Fragment fragment = getTargetFragment();
    if (fragment instanceof Callback) {
      ((Callback) fragment).onItemSelected(itemId);
    }
  }

  public interface Callback {
    void onItemSelected(int itemId);
  }
}