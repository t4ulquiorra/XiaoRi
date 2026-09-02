package echo.music.iad1tya.ui.player

/**
 * Pure helpers for the artwork pager on NowPlayingScreen.
 */

internal fun deriveOrderIndex(
    queueMediaIds: List<String>,
    currentMediaId: String?,
): Int {
    if (queueMediaIds.isEmpty() || currentMediaId.isNullOrEmpty()) return 0
    return queueMediaIds
        .indexOfLast { it == currentMediaId }
        .coerceAtLeast(0)
}

internal sealed interface ArtworkSeekAction {
    data object Next : ArtworkSeekAction
    data object Previous : ArtworkSeekAction
    data class Skip(val index: Int) : ArtworkSeekAction
    data object NoOp : ArtworkSeekAction
}

internal fun computeSeekAction(
    newPage: Int,
    currentOrderIndex: Int,
): ArtworkSeekAction =
    when (newPage - currentOrderIndex) {
        0 -> ArtworkSeekAction.NoOp
        1 -> ArtworkSeekAction.Next
        -1 -> ArtworkSeekAction.Previous
        else -> ArtworkSeekAction.Skip(newPage)
    }
