package chat.wewe.core.interactors

import chat.wewe.core.SortDirection
import chat.wewe.core.models.Room
import chat.wewe.core.repositories.RoomRepository
import io.reactivex.Flowable

class RoomInteractor(private val roomRepository: RoomRepository) {

    fun getTotalUnreadMentionsCount(): Flowable<Int> {
        return roomRepository.all
                .flatMap { rooms ->
                    Flowable.fromIterable(rooms)
                            .filter { room -> room.isOpen && room.isAlert }
                            .map { it.unread }
                            .defaultIfEmpty(0)
                            .reduce { unreadCount, unreadCount2 -> unreadCount + unreadCount2 }
                            .toFlowable()
                }
    }

    fun getTotalUnreadRoomsCount(): Flowable<Long> {
        return roomRepository.all
                .flatMap { rooms ->
                    Flowable.fromIterable(rooms)
                            .filter { room -> room.isOpen && room.isAlert }
                            .count()
                            .toFlowable()
                }
    }

    fun getOpenRooms(): Flowable<List<Room>> {
        return roomRepository.all
                .flatMap { rooms ->
                    Flowable.fromIterable(rooms)
                            .filter { it.isOpen }
                            .toList()
                            .toFlowable()
                }
    }

    fun getRoomsWithNameLike(name: String): Flowable<List<Room>> {
        return roomRepository.getSortedLikeName(name, SortDirection.DESC, 5)
    }
}
