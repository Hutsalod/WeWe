package chat.wewe.core.interactors

import chat.wewe.core.SortDirection
import chat.wewe.core.models.Room
import chat.wewe.core.models.SpotlightRoom
import chat.wewe.core.repositories.RoomRepository
import chat.wewe.core.repositories.SpotlightRoomRepository
import chat.wewe.core.temp.TempSpotlightRoomCaller
import chat.wewe.core.utils.Pair
import chat.wewe.core.utils.Triple
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import java.util.*

class AutocompleteChannelInteractor(private val roomRepository: RoomRepository,
                                    private val spotlightRoomRepository: SpotlightRoomRepository,
                                    private val tempSpotlightRoomCaller: TempSpotlightRoomCaller) {

    fun getSuggestionsFor(name: String): Flowable<List<SpotlightRoom>> {
        return Flowable.zip<String, List<SpotlightRoom>, List<SpotlightRoom>, Triple<String, List<SpotlightRoom>, List<SpotlightRoom>>>(
                Flowable.just(name),
                roomRepository.getLatestSeen(5).map { toSpotlightRooms(it) },
                roomRepository.getSortedLikeName(name, SortDirection.DESC, 5).map { toSpotlightRooms(it) },
                Function3 { a, b, c -> Triple.create(a, b, c) }
        )

                .flatMap { triple ->
                    if (triple.first.isEmpty()) {
                        return@flatMap Flowable.just(triple.second)
                    }

                    val spotlightRooms = ArrayList<SpotlightRoom>()
                    spotlightRooms.addAll(triple.second.filter { it.name.contains(triple.first, true) })
                    spotlightRooms.addAll(triple.third)

                    val workedList = spotlightRooms.distinct().take(5)

                    if (workedList.size == 5) {
                        return@flatMap Flowable.just<List<SpotlightRoom>>(workedList)
                    }

                    tempSpotlightRoomCaller.search(triple.first)

                    spotlightRoomRepository.getSuggestionsFor(triple.first, SortDirection.DESC, 5)
                            .withLatestFrom<List<SpotlightRoom>, Pair<List<SpotlightRoom>, List<SpotlightRoom>>>(
                                    Flowable.just(workedList),
                                    BiFunction { a, b -> Pair.create(a, b) }
                            )
                            .map { pair ->
                                val spotlightRooms1 = ArrayList<SpotlightRoom>()
                                spotlightRooms1.addAll(pair.second)
                                spotlightRooms1.addAll(pair.first)

                                return@map spotlightRooms1.distinct().take(5)
                            }
                }
    }

    private fun toSpotlightRooms(rooms: List<Room>): List<SpotlightRoom> {
        val size = rooms.size
        val spotlightRooms = ArrayList<SpotlightRoom>(size)

        for (i in 0..size - 1) {
            val room = rooms[i]
            spotlightRooms.add(SpotlightRoom.builder()
                    .setId(room.id)
                    .setName(room.name)
                    .setType(room.type)
                    .build())
        }

        return spotlightRooms
    }
}
