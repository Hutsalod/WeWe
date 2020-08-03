package chat.wewe.android.service;

/**
 * interface for observer and ddp_subscription.
 */
public interface Registrable {
  /**
   * register.
   */
  void register();

  /**
   * unregister.
   */
  void unregister();
}
