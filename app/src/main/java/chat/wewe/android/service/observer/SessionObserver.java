package chat.wewe.android.service.observer;

import android.content.Context;

import java.util.List;

import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.RaixPushHelper;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android.service.internal.StreamRoomMessageManager;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.models.internal.GetUsersOfRoomsProcedure;
import chat.wewe.persistence.realm.models.internal.LoadMessageProcedure;
import chat.wewe.persistence.realm.models.internal.MethodCall;
import chat.wewe.persistence.realm.models.internal.RealmSession;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Observes user is logged into server.
 */
public class SessionObserver extends AbstractModelObserver<RealmSession> {
  private final StreamRoomMessageManager streamNotifyMessage;
  private final RaixPushHelper pushHelper;
  private int count;

  /**
   * constructor.
   */
  public SessionObserver(Context context, String hostname,
                         RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
    count = 0;

    streamNotifyMessage =
        new StreamRoomMessageManager(context, hostname, realmHelper);
    pushHelper = new RaixPushHelper(realmHelper);
  }

  @Override
  public RealmResults<RealmSession> queryItems(Realm realm) {
    return realm.where(RealmSession.class)
        .isNotNull(RealmSession.TOKEN)
        .equalTo(RealmSession.TOKEN_VERIFIED, true)
        .isNull(RealmSession.ERROR)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<RealmSession> results) {
    int origCount = count;
    count = results.size();
    if (origCount > 0 && count > 0) {
      return;
    }

    if (count == 0) {
      if (origCount > 0) {
        onLogout();
      }
      return;
    }

    if (origCount == 0 && count > 0) {
      onLogin();
    }
  }

  @DebugLog
  private void onLogin() {
    streamNotifyMessage.register();

    // update push info
    pushHelper
        .pushSetUser(RocketChatCache.INSTANCE.getOrCreatePushId())
        .continueWith(new LogIfError());

    ConnectivityManager.getInstance(context).notifySessionEstablished(hostname);
  }

  @DebugLog
  private void onLogout() {
    streamNotifyMessage.unregister();

    realmHelper.executeTransaction(realm -> {
      // remove all tables. ONLY INTERNAL TABLES!.
      realm.delete(MethodCall.class);
      realm.delete(LoadMessageProcedure.class);
      realm.delete(GetUsersOfRoomsProcedure.class);
      return null;
    }).continueWith(new LogIfError());
  }
}
