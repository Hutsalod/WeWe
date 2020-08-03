package chat.wewe.android.renderer.optional;

public interface Optional {
  void onDataExists(String key);

  void onNoData(String key);
}
