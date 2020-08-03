package chat.wewe.android.service.temp;

import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.core.temp.TempSpotlightUserCaller;

public class DefaultTempSpotlightUserCaller implements TempSpotlightUserCaller {

  private final MethodCallHelper methodCallHelper;

  public DefaultTempSpotlightUserCaller(MethodCallHelper methodCallHelper) {
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void search(String term) {
    methodCallHelper.searchSpotlightUsers(term);
  }
}
