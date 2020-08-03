package chat.wewe.android.service.ddp.base;

import android.content.Context;

import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.models.ddp.RealmMeteorLoginServiceConfiguration;
import io.realm.RealmObject;

/**
 * meteor.loginServiceConfiguration subscriber
 */
public class LoginServiceConfigurationSubscriber extends AbstractBaseSubscriber {
  public LoginServiceConfigurationSubscriber(Context context, String hostname,
                                             RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
  }

  @Override
  protected String getSubscriptionName() {
    return "meteor.loginServiceConfiguration";
  }

  @Override
  protected String getSubscriptionCallbackName() {
    return "meteor_accounts_loginServiceConfiguration";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RealmMeteorLoginServiceConfiguration.class;
  }
}
