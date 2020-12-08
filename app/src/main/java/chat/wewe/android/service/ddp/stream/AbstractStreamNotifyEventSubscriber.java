package chat.wewe.android.service.ddp.stream;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.android.log.RCLog;
import chat.wewe.android.service.ddp.AbstractDDPDocEventSubscriber;
import chat.wewe.android_ddp.DDPSubscription;
import chat.wewe.persistence.realm.RealmHelper;

import static chat.wewe.android.activity.MainActivity.pane;

public abstract class AbstractStreamNotifyEventSubscriber extends AbstractDDPDocEventSubscriber {
  protected AbstractStreamNotifyEventSubscriber(Context context, String hostname,
                                                RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
  }

  @Override
  protected final boolean shouldTruncateTableOnInitialize() {
    return false;
  }

  @Override
  protected final boolean isTarget(String callbackName) {
    return getSubscriptionName().equals(callbackName);
  }

  protected abstract String getSubscriptionParam();

  @Override
  protected final JSONArray getSubscriptionParams() throws JSONException {
    return new JSONArray().put(getSubscriptionParam()).put(false);
  }

  protected abstract String getPrimaryKeyForModel();

  @Override
  protected final void onDocumentAdded(DDPSubscription.Added docEvent) {
    // do nothing.
  }

  @Override
  protected final void onDocumentRemoved(DDPSubscription.Removed docEvent) {
    // do nothing.
  }

  @Override
  protected final void onDocumentChanged(DDPSubscription.Changed docEvent) {
    try {
      if (!docEvent.fields.getString("eventName").equals(getSubscriptionParam())) {
        return;
      }

      handleArgs(docEvent.fields.getJSONArray("args"));
    } catch (Exception exception) {
      RCLog.w(exception, "failed to save stream-notify event.");
    }
  }

  protected void handleArgs(JSONArray args) throws JSONException {
    String msg = args.length() > 0 ? args.getString(0) : null;
    JSONObject target = args.getJSONObject(args.length() - 1);
    if ("removed".equals(msg)) {

      pane.openPane();

      Log.d("QQWEWE", "removed");
      realmHelper.executeTransaction(realm ->
          realm.where(getModelClass())
              .equalTo(getPrimaryKeyForModel(), target.getString(getPrimaryKeyForModel()))
              .findAll().deleteAllFromRealm()
      ).continueWith(new LogIfError());
    } else { //inserted, updated
      realmHelper.executeTransaction(realm ->
          realm.createOrUpdateObjectFromJson(getModelClass(), customizeFieldJson(target))
      ).continueWith(new LogIfError());
    }
  }


}


