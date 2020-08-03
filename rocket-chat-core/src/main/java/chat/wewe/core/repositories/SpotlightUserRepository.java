package chat.wewe.core.repositories;

import java.util.List;

import chat.wewe.core.SortDirection;
import chat.wewe.core.models.SpotlightUser;
import io.reactivex.Flowable;

public interface SpotlightUserRepository {

  Flowable<List<SpotlightUser>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
