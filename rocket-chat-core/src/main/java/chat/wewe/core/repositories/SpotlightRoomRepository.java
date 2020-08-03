package chat.wewe.core.repositories;

import java.util.List;

import chat.wewe.core.SortDirection;
import chat.wewe.core.models.SpotlightRoom;
import io.reactivex.Flowable;

public interface SpotlightRoomRepository {

  Flowable<List<SpotlightRoom>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
