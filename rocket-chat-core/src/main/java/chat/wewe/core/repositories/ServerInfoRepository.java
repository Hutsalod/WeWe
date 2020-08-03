package chat.wewe.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.wewe.core.models.ServerInfo;
import io.reactivex.Flowable;

public interface ServerInfoRepository {

  Flowable<Optional<ServerInfo>> getByHostname(String hostname);
}
