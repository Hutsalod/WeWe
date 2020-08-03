package chat.wewe.android.layouthelper.extra_action.upload;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;

import chat.wewe.android.R;

public class VideoUploadActionItem extends AbstractUploadActionItem {

  public boolean videoQuality = true;
    private Context context;

    public VideoUploadActionItem(boolean video,Context context) {
    this.videoQuality=video;
      this.context=context;
  }

  @Override
  public int getItemId() {
    return 12;
  }

  @Override
  protected Intent getIntentForPickFile() {
    Intent intent = new Intent();
    intent.setType("video/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);

    Intent recordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    if(videoQuality)
    recordVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); //low quality.
    else
    recordVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); //low quality.

    recordVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 105678400L);

    Intent chooserIntent = Intent.createChooser(intent, "Select Video to Upload");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { recordVideoIntent });


    return chooserIntent;
  }

  @Override
  public int getIcon() {
    return R.drawable.ic_video_call_white_24dp;
  }

  @Override
  public int getTitle() {
    return R.string.video_upload_message_spec_title;
  }
}
