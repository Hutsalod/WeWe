package chat.wewe.android.service.observer;

import android.content.Context;

import java.util.List;

import chat.wewe.android.helper.GcmPushSettingHelper;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.models.ddp.RealmPublicSetting;
import chat.wewe.persistence.realm.models.internal.GcmPushRegistration;
import io.realm.Realm;
import io.realm.RealmResults;

public class PushSettingsObserver extends AbstractModelObserver<RealmPublicSetting> {

  public PushSettingsObserver(Context context, String hostname,
                              RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
  }

  @Override
  public RealmResults<RealmPublicSetting> queryItems(Realm realm) {
    return GcmPushSettingHelper.queryForGcmPushEnabled(realm);
  }

  @Override
  public void onUpdateResults(List<RealmPublicSetting> results) {
    boolean gcmPushEnabled = GcmPushSettingHelper.isGcmPushEnabled(results);

    if (gcmPushEnabled) {
      realmHelper.executeTransaction(realm ->
          GcmPushRegistration.updateGcmPushEnabled(realm, gcmPushEnabled));
    }
  }
}
