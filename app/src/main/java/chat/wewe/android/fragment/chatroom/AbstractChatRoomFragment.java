package chat.wewe.android.fragment.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.wewe.android.R;
import chat.wewe.android.activity.Success;
import chat.wewe.android.fragment.AbstractFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddTaskFragment;
import chat.wewe.android.widget.RoomToolbar;
import chat.wewe.core.models.User;

import static android.content.Context.MODE_PRIVATE;
import static org.webrtc.ContextUtils.getApplicationContext;

public abstract class AbstractChatRoomFragment extends AbstractFragment {
  private RoomToolbar roomToolbar;
  private String hostname, roomId, userId;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    roomToolbar = getActivity().findViewById(R.id.activity_main_toolbar);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  protected void setToolbarTitle(CharSequence title) {
    roomToolbar.hideChannelIcons();
    roomToolbar.setTitle(title);
    roomToolbar.setActionListener(new RoomToolbar.ActionListener() {
      @Override
      public void onClick(Boolean uid) {

        if (getActivity().getSharedPreferences("Sub", MODE_PRIVATE).getBoolean("Sub", false)==true || getActivity().getSharedPreferences("Sub", MODE_PRIVATE).getBoolean("SubApi", false)==true ) {
          Log.d("QQWEWE", "YES");
        startActivity(new Intent(getActivity(), chat.wewe.android.ui.MainActivity.class).putExtra("uid", uid).putExtra("name", title));
        }else {
          startActivity(new Intent(getActivity(), Success.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }
      }

      @Override
      public void onClickTask() {


      }
    });
  }

  protected void showToolbarPrivateChannelIcon() {
    roomToolbar.showPrivateChannelIcon();
  }

  protected void showToolbarPublicChannelIcon() {
    roomToolbar.showPublicChannelIcon();
  }

  protected void showToolbarLivechatChannelIcon() {
    roomToolbar.showLivechatChannelIcon();
  }

  protected void showToolbarUserStatuslIcon(@Nullable String status) {
    if (status == null) {
      roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_OFFLINE);
    } else {
      switch (status) {
        case User.STATUS_ONLINE:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_ONLINE);
          break;
        case User.STATUS_BUSY:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_BUSY);
          break;
        case User.STATUS_AWAY:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_AWAY);
          break;
        default:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_OFFLINE);
          break;
      }
    }
  }


}