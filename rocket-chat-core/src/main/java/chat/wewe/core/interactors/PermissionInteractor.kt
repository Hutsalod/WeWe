package chat.wewe.core.interactors

import chat.wewe.core.models.Permission
import chat.wewe.core.models.Room
import chat.wewe.core.models.RoomRole
import chat.wewe.core.repositories.PermissionRepository
import chat.wewe.core.repositories.RoomRoleRepository
import chat.wewe.core.repositories.UserRepository
import chat.wewe.core.utils.Pair
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class PermissionInteractor(private val userRepository: UserRepository,
                           private val roomRoleRepository: RoomRoleRepository,
                           private val permissionRepository: PermissionRepository) {

    fun isAllowed(permissionId: String, room: Room): Single<Boolean> {
        return userRepository.getCurrent()
                .first(Optional.absent())
                .flatMap {
                    if (!it.isPresent) {
                        return@flatMap Single.just(false)
                    }

                    Single.zip<Optional<RoomRole>, Optional<Permission>, Pair<Optional<RoomRole>, Optional<Permission>>>(
                            roomRoleRepository.getFor(room, it.get()),
                            permissionRepository.getById(permissionId),
                            BiFunction { a, b -> Pair.create(a, b) }
                    )
                            .flatMap innerFlatMap@ {
                                if (!it.first.isPresent || !it.second.isPresent) {
                                    return@innerFlatMap Single.just(false)
                                }

                                val commonRoles = it.first.get().roles.intersect(
                                        it.second.get().roles
                                )

                                Single.just(commonRoles.isNotEmpty())
                            }
                }
    }
}
