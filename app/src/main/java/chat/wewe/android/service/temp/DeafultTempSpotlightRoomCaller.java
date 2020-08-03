package chat.wewe.android.service.temp;

import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.core.temp.TempSpotlightRoomCaller;

public class DeafultTempSpotlightRoomCaller implements TempSpotlightRoomCaller {

  private final MethodCallHelper methodCallHelper;

  public DeafultTempSpotlightRoomCaller(MethodCallHelper methodCallHelper) {
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void search(String term) {
    methodCallHelper.searchSpotlightRooms(term);
  }
}
