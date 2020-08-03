package chat.wewe.android.fragment.sidebar;

import android.support.annotation.NonNull;

import java.util.List;

import bolts.Continuation;
import chat.wewe.android.shared.BaseContract;
import chat.wewe.core.models.RoomSidebar;
import chat.wewe.core.models.Spotlight;
import chat.wewe.core.models.User;
import io.reactivex.Flowable;

public interface SidebarMainContract {

  interface View extends BaseContract.View {

    void showScreen();

    void showEmptyScreen();

    void showRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList);

    void filterRoomSidebarList(CharSequence term);

    void show(User user);

    void onPreparedToLogOut();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onRoomSelected(RoomSidebar roomSidebar);

    void onSpotlightSelected(Spotlight spotlight);

    Flowable<List<Spotlight>> searchSpotlight(String term);

    void disposeSubscriptions();

    void onUserOnline();

    void onUserAway();

    void onUserBusy();

    void onUserOffline();

    void onLogout(Continuation<Void, Object> continuation);

    void prepareToLogOut();
  }
}