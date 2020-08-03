package chat.wewe.android.push.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import java.util.List;

import chat.wewe.android.helper.GcmPushSettingHelper;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.core.models.ServerInfo;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.models.ddp.RealmPublicSetting;
import chat.wewe.persistence.realm.models.internal.GcmPushRegistration;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

  @Override
  public void onTokenRefresh() {
    List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(getApplicationContext())
        .getServerList();
    for (ServerInfo serverInfo : serverInfoList) {
      RealmHelper realmHelper = RealmStore.get(serverInfo.getHostname());
      if (realmHelper != null) {
        updateGcmToken(realmHelper);
      }
    }
  }

  private void updateGcmToken(RealmHelper realmHelper) {
    final List<RealmPublicSetting> results = realmHelper.executeTransactionForReadResults(
        GcmPushSettingHelper::queryForGcmPushEnabled);
    final boolean gcmPushEnabled = GcmPushSettingHelper.isGcmPushEnabled(results);

    if (gcmPushEnabled) {
      realmHelper.executeTransaction(realm ->
          GcmPushRegistration.updateGcmPushEnabled(realm, gcmPushEnabled));
    }
  }
}