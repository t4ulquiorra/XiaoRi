

package com.xiaori.extensions

import com.xiaori.models.MediaMetadata
import com.xiaori.models.PersistQueue
import com.xiaori.models.QueueData
import com.xiaori.models.QueueType
import com.xiaori.playback.queues.ListQueue
import com.xiaori.playback.queues.LocalAlbumRadio
import com.xiaori.playback.queues.Queue
import com.xiaori.playback.queues.YouTubeAlbumRadio
import com.xiaori.playback.queues.YouTubeQueue

fun Queue.toPersistQueue(
    title: String?,
    items: List<MediaMetadata>,
    mediaItemIndex: Int,
    position: Long
): PersistQueue {
    return when (this) {
        is ListQueue -> PersistQueue(
            title = title,
            items = items,
            mediaItemIndex = mediaItemIndex,
            position = position,
            queueType = QueueType.LIST
        )
        is YouTubeQueue -> {
            
            val endpoint = "youtube_queue"
            PersistQueue(
                title = title,
                items = items,
                mediaItemIndex = mediaItemIndex,
                position = position,
                queueType = QueueType.YOUTUBE,
                queueData = QueueData.YouTubeData(endpoint = endpoint)
            )
        }
        is YouTubeAlbumRadio -> {
            
            PersistQueue(
                title = title,
                items = items,
                mediaItemIndex = mediaItemIndex,
                position = position,
                queueType = QueueType.YOUTUBE_ALBUM_RADIO,
                queueData = QueueData.YouTubeAlbumRadioData(
                    playlistId = "youtube_album_radio"
                )
            )
        }
        is LocalAlbumRadio -> {
            
            PersistQueue(
                title = title,
                items = items,
                mediaItemIndex = mediaItemIndex,
                position = position,
                queueType = QueueType.LOCAL_ALBUM_RADIO,
                queueData = QueueData.LocalAlbumRadioData(
                    albumId = "local_album_radio",
                    startIndex = 0
                )
            )
        }
        else -> PersistQueue(
            title = title,
            items = items,
            mediaItemIndex = mediaItemIndex,
            position = position,
            queueType = QueueType.LIST
        )
    }
}

fun PersistQueue.toQueue(): Queue {
    return when (queueType) {
        is QueueType.LIST -> ListQueue(
            title = title,
            items = items.map { it.toMediaItem() },
            startIndex = mediaItemIndex,
            position = position
        )
        is QueueType.YOUTUBE -> {
            
            ListQueue(
                title = title,
                items = items.map { it.toMediaItem() },
                startIndex = mediaItemIndex,
                position = position
            )
        }
        is QueueType.YOUTUBE_ALBUM_RADIO -> {
            
            ListQueue(
                title = title,
                items = items.map { it.toMediaItem() },
                startIndex = mediaItemIndex,
                position = position
            )
        }
        is QueueType.LOCAL_ALBUM_RADIO -> {
            
            ListQueue(
                title = title,
                items = items.map { it.toMediaItem() },
                startIndex = mediaItemIndex,
                position = position
            )
        }
    }
}
