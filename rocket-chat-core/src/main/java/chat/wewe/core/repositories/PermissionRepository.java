package chat.wewe.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.wewe.core.models.Permission;
import io.reactivex.Single;

public interface PermissionRepository {

  Single<Optional<Permission>> getById(String id);
}
