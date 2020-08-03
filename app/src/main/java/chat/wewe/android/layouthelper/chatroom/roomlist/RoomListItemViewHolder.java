package chat.wewe.android.layouthelper.chatroom.roomlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;

import chat.wewe.android.helper.Logger;
import chat.wewe.android.widget.internal.RoomListItemView;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.RoomSidebar;
import chat.wewe.core.models.Spotlight;
import chat.wewe.core.models.User;

public class RoomListItemViewHolder extends RecyclerView.ViewHolder {
  private RoomListItemView itemView;
  Context context;

  public RoomListItemViewHolder(RoomListItemView itemView, RoomListAdapter.OnItemClickListener listener) {
    super(itemView);

    this.itemView = itemView;

    itemView.setOnClickListener(view -> {
      Object object = view.getTag();
      if (object instanceof RoomSidebar) {
        listener.onItemClick((RoomSidebar)object);
      } else if (object instanceof Spotlight) {
        listener.onItemClick((Spotlight)object);
      }
    });
  }

  public void bind(RoomSidebar roomSidebar) {
    itemView.setRoomId(roomSidebar.getRoomId());
    itemView.setRoomName(roomSidebar.getRoomName());
    itemView.setAlert(roomSidebar.isAlert());
    itemView.setUnreadCount(roomSidebar.getUnread());
    itemView.setTag(roomSidebar);
    itemView.setRoomTime(new SimpleDateFormat("HH:mm").format(new java.util.Date((long) roomSidebar.getLastSeen())));



    String roomType = roomSidebar.getType();
    if (roomType.equals(Room.TYPE_DIRECT_MESSAGE)) {
      showUserStatusIcon(roomSidebar.getUserStatus());
    } else {
      showRoomIcon(roomType);
    }
  }

  public void bind(Spotlight spotlight) {
    itemView.setRoomId(spotlight.getId());
    itemView.setRoomName(spotlight.getName());
    itemView.setAlert(false);
    itemView.setUnreadCount(0);
    itemView.setTag(spotlight);

    String roomType = spotlight.getType();
    if (roomType.equals(Room.TYPE_DIRECT_MESSAGE)) {
      showUserStatusIcon(spotlight.getStatus());
    } else {
      showRoomIcon(roomType);
    }
  }

  /**
   * Shows the user status icon.
   * @param userStatus The user status to show the correspondent icon.
   * @see User
   */
  private void showUserStatusIcon(String userStatus) {
    if (userStatus == null) {
      itemView.showOfflineUserStatusIcon();
    } else {
      switch (userStatus) {
        case User.STATUS_ONLINE:
          itemView.showOnlineUserStatusIcon();
          break;
        case User.STATUS_BUSY:
          itemView.showBusyUserStatusIcon();
          break;
        case User.STATUS_AWAY:
          itemView.showAwayUserStatusIcon();
          break;
        default:
          itemView.showOfflineUserStatusIcon();
          break;
      }
    }
  }

  private Bitmap showAvatar(String... urls){
    String imageURL = urls[0];
    Bitmap bimage = null;
    try {
      InputStream in = new java.net.URL("https://chat.weltwelle.com/avatar/"+imageURL).openStream();
      bimage = BitmapFactory.decodeStream(in);
    } catch (Exception e) {
      Log.e("Error Message", e.getMessage());
      e.printStackTrace();
    }

    return bimage;
  }

  /**
   * Only shows the room icon if it is a PRIVATE CHANNEL or PUBLIC CHANNEL, otherwise you should use {@link #showUserStatusIcon(String)} to show the icon.
   * @param roomType The type of Room.
   * @see Room
   */

  private void showRoomIcon(String roomType) {
    switch (roomType) {
      case Room.TYPE_CHANNEL:
        itemView.showPublicChannelIcon();
        break;
      case Room.TYPE_GROUP:
        itemView.showPrivateChannelIcon();
        break;
      case Room.TYPE_LIVECHAT:
        itemView.showLivechatChannelIcon();
        break;
      default: {
        itemView.showPrivateChannelIcon();
        Logger.INSTANCE.report(new AssertionError("Room type doesn't satisfies the method documentation. Room type is:" + roomType));
      }
    }
  }
}