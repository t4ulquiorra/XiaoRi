

package t4ulquiorra.xiaori.lyrics

import android.content.Context
import t4ulquiorra.xiaori.betterlyrics.BetterLyrics
import t4ulquiorra.xiaori.constants.EnableBetterLyricsKey
import t4ulquiorra.xiaori.utils.dataStore
import t4ulquiorra.xiaori.utils.get

object BetterLyricsProvider : LyricsProvider {
    override val name = "BetterLyrics"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableBetterLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> = BetterLyrics.getLyrics(title, artist, duration, album)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
        callback: (String) -> Unit,
    ) {
        BetterLyrics.getAllLyrics(title, artist, duration, album, callback)
    }
}
