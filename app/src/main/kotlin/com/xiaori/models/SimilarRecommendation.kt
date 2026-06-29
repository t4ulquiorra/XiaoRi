

package com.xiaori.models

import com.music.innertube.models.YTItem
import com.xiaori.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
