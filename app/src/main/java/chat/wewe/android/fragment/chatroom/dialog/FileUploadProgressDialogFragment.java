package chat.wewe.android.fragment.chatroom.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import chat.wewe.android.R;
import chat.wewe.android.renderer.FileUploadingRenderer;
import chat.wewe.core.SyncState;
import chat.wewe.persistence.realm.RealmObjectObserver;
import chat.wewe.persistence.realm.models.internal.FileUploading;

/**
 * dialog fragment to display progress of file uploading.
 */
public class FileUploadProgressDialogFragment extends AbstractChatRoomDialogFragment {

  private String uplId;
  private RealmObjectObserver<FileUploading> fileUploadingObserver;

  public FileUploadProgressDialogFragment() {
  }

  public static FileUploadProgressDialogFragment create(String hostname,
                                                        String roomId, String uplId) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    args.putString("roomId", roomId);
    args.putString("uplId", uplId);

    FileUploadProgressDialogFragment fragment = new FileUploadProgressDialogFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  protected void handleArgs(@NonNull Bundle args) {
    super.handleArgs(args);
    uplId = args.getString("uplId");
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("TEST_WEWE","TUPDATE5");
    fileUploadingObserver = realmHelper
        .createObjectObserver(realm -> realm.where(FileUploading.class).equalTo("uplId", uplId))
        .setOnUpdateListener(this::onRenderFileUploadingState);
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_file_uploading;
  }

  @Override
  protected void onSetupDialog() {

  }

  private void onRenderFileUploadingState(FileUploading state) {
    if (state == null) {
      return;
    }

    int syncstate = state.getSyncState();
    if (syncstate == SyncState.SYNCED) {
      Log.d("TEST_WEWE","TUPDATE8");
      dismiss();
    } else if (syncstate == SyncState.FAILED) {

      //TODO: prompt retry.
      Log.d("TEST_WEWE","TUPDATE7");
      dismiss();
    } else {
      final Dialog dialog = getDialog();
      if (dialog != null) {
        new FileUploadingRenderer(getContext(), state)
                .progressInto( dialog.findViewById(R.id.progressBar))
                .progressTextInto(
                        dialog.findViewById(R.id.txt_filesize_uploaded),
                        dialog.findViewById(R.id.txt_filesize_total));
        Log.d("TEST_WEWE","TUPDATE3");
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    fileUploadingObserver.sub();
  }

  @Override
  public void onPause() {
    fileUploadingObserver.unsub();
    super.onPause();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    //TODO: should cancel uploading? or continue with showing notification with progress?
  }
}
