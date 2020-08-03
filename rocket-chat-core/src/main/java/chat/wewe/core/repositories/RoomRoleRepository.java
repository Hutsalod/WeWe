package chat.wewe.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.wewe.core.models.Room;
import chat.wewe.core.models.RoomRole;
import chat.wewe.core.models.User;
import io.reactivex.Single;

public interface RoomRoleRepository {

  Single<Optional<RoomRole>> getFor(Room room, User user);
}
