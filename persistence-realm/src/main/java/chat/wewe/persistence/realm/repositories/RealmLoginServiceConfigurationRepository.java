package chat.wewe.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.List;

import chat.wewe.core.models.LoginServiceConfiguration;
import chat.wewe.core.repositories.LoginServiceConfigurationRepository;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.models.ddp.RealmMeteorLoginServiceConfiguration;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.RealmResults;

public class RealmLoginServiceConfigurationRepository extends RealmRepository
    implements LoginServiceConfigurationRepository {

  private final String hostname;

  public RealmLoginServiceConfigurationRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Single<Optional<LoginServiceConfiguration>> getByName(String serviceName) {
    return Single.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> {
          if (pair.first == null) {
            return Flowable.empty();
          }

          return pair.first.where(RealmMeteorLoginServiceConfiguration.class)
                          .equalTo(RealmMeteorLoginServiceConfiguration.SERVICE, serviceName)
                          .findAll()
                          .<RealmResults<RealmMeteorLoginServiceConfiguration>>asFlowable();
        },
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid() && it.size() > 0)
        .map(it -> Optional.of(it.get(0).asLoginServiceConfiguration()))
        .first(Optional.absent()));
  }

  @Override
  public Flowable<List<LoginServiceConfiguration>> getAll() {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> {
          if (pair.first == null) {
            return Flowable.empty();
          }

          return pair.first.where(RealmMeteorLoginServiceConfiguration.class)
                          .findAll()
                          .asFlowable();
        },
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid())
        .map(this::toList));
  }

  private List<LoginServiceConfiguration> toList(
      RealmResults<RealmMeteorLoginServiceConfiguration> realmConfigurations) {
    final int total = realmConfigurations.size();
    final List<LoginServiceConfiguration> serviceConfigurations = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      serviceConfigurations.add(realmConfigurations.get(i).asLoginServiceConfiguration());
    }

    return serviceConfigurations;
  }
}
