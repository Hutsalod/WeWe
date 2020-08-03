package chat.wewe.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import chat.wewe.core.models.Permission;
import chat.wewe.core.repositories.PermissionRepository;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.models.ddp.RealmPermission;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.RealmResults;

public class RealmPermissionRepository extends RealmRepository implements PermissionRepository {

  private final String hostname;

  public RealmPermissionRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Single<Optional<Permission>> getById(String id) {
    return Single.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> {
            if (pair.first == null) {
                return Flowable.empty();
            }
            return pair.first.where(RealmPermission.class)
                    .equalTo(RealmPermission.Columns.ID, id)
                    .findAll()
                    .<RealmResults<RealmPermission>>asFlowable();
        },
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid())
        .map(it -> {
          if (it.size() == 0) {
            return Optional.<Permission>absent();
          }
          return Optional.of(it.get(0).asPermission());
        })
        .first(Optional.absent()));
  }
}
