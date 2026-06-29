

package t4ulquiorra.xiaori.models

import com.music.innertube.models.YTItem
import t4ulquiorra.xiaori.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
