package chat.wewe.core.repositories

import chat.wewe.core.models.Spotlight
import io.reactivex.Flowable

interface SpotlightRepository {

    fun getSuggestionsFor(term: String, limit: Int): Flowable<List<Spotlight>>
}