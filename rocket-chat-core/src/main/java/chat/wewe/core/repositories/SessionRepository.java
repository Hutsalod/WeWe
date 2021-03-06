package chat.wewe.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.wewe.core.models.Session;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface SessionRepository {

  Flowable<Optional<Session>> getById(int id);

  Single<Boolean> save(Session session);
}
