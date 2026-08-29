package echo.music.iad1tya.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.innertube.models.*
import echo.music.iad1tya.R
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.menu.YouTubeAlbumMenu
import echo.music.iad1tya.ui.menu.YouTubeArtistMenu
import echo.music.iad1tya.ui.menu.YouTubePlaylistMenu
import echo.music.iad1tya.ui.menu.YouTubeSongMenu
import kotlinx.coroutines.launch

fun Modifier.pressClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        }
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    if (!enabled) return@detectTapGestures
                    scope.launch { scale.animateTo(0.96f, tween(80)) }
                    scope.launch { alpha.animateTo(0.75f, tween(80)) }
                    val released = tryAwaitRelease()
                    scope.launch { scale.animateTo(1f, tween(80)) }
                    scope.launch { alpha.animateTo(1f, tween(80)) }
                    if (released) onClick()
                }
            )
        }
}

fun Modifier.lightPressClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        }
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    if (!enabled) return@detectTapGestures
                    scope.launch { scale.animateTo(0.98f, tween(80)) }
                    scope.launch { alpha.animateTo(0.75f, tween(80)) }
                    val released = tryAwaitRelease()
                    scope.launch { scale.animateTo(1f, tween(80)) }
                    scope.launch { alpha.animateTo(1f, tween(80)) }
                    if (released) onClick()
                }
            )
        }
}

/**
 * Xevrae Home Section Container: displays section title, optional subtitle, and a snapping horizontal row
 */
@Composable
fun HomeItem(
    title: String,
    subtitle: String? = null,
    channelId: String? = null,
    thumbnailUrl: String? = null,
    items: List<YTItem>,
    navController: NavController,
    onItemClick: ((YTItem) -> Unit)? = null,
    onItemMoreClick: ((YTItem) -> Unit)? = null,
) {
    if (items.isEmpty()) return

    val menuState = LocalMenuState.current
    val lazyListState = rememberLazyListState()
    val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyListState = lazyListState, snapPosition = SnapPosition.Start))

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val widthDp = maxWidth
        val dynamicThumbSize = (160.dp).coerceAtLeast(120.dp)

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .then(
                        if (channelId != null) {
                            Modifier.pressClickable {
                                navController.navigate("artist/$channelId")
                            }
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = ColorPainter(Color(0xFF2A2A2A)),
                        error = ColorPainter(Color(0xFF2A2A2A)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (!subtitle.isNullOrEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA8A8A8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            LazyRow(
                state = lazyListState,
                flingBehavior = snapperFlingBehavior,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(items, key = { it.id + it.title }) { item ->
                    when (item) {
                        is PlaylistItem -> {
                            HomeItemContentPlaylist(
                                onClick = {
                                    if (onItemClick != null) onItemClick(item)
                                    else navController.navigate("online_playlist/${item.id}")
                                },
                                onMoreClick = {
                                    if (onItemMoreClick != null) onItemMoreClick(item)
                                    else menuState.show {
                                        YouTubePlaylistMenu(
                                            playlist = item,
                                            navController = navController,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                                title = item.title,
                                subtitle = item.author?.name ?: stringResource(R.string.playlist),
                                thumbnailUrl = item.thumbnail,
                                thumbSize = dynamicThumbSize,
                            )
                        }

                        is AlbumItem -> {
                            HomeItemContentPlaylist(
                                onClick = {
                                    if (onItemClick != null) onItemClick(item)
                                    else navController.navigate("album/${item.id}")
                                },
                                onMoreClick = {
                                    if (onItemMoreClick != null) onItemMoreClick(item)
                                    else menuState.show {
                                        YouTubeAlbumMenu(
                                            albumItem = item,
                                            navController = navController,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                                title = item.title,
                                subtitle = item.authors?.joinToString { it.name } ?: stringResource(R.string.album),
                                thumbnailUrl = item.thumbnail,
                                thumbSize = dynamicThumbSize,
                            )
                        }

                        is ArtistItem -> {
                            HomeItemArtist(
                                onClick = {
                                    if (onItemClick != null) onItemClick(item)
                                    else navController.navigate("artist/${item.id}")
                                },
                                name = item.title,
                                subscribers = item.subscribers,
                                thumbnailUrl = item.thumbnail,
                                thumbSize = dynamicThumbSize,
                            )
                        }

                        is SongItem -> {
                            HomeItemContent(
                                onClick = {
                                    if (onItemClick != null) onItemClick(item)
                                },
                                onMoreClick = {
                                    if (onItemMoreClick != null) onItemMoreClick(item)
                                    else menuState.show {
                                        YouTubeSongMenu(
                                            song = item,
                                            navController = navController,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                                title = item.title,
                                subtitle = item.artists.joinToString { it.name },
                                thumbnailUrl = item.thumbnail,
                                thumbSize = dynamicThumbSize,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Xevrae Playlist / Album square card item
 */
@Composable
fun HomeItemContentPlaylist(
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    thumbSize: Dp = 160.dp,
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .focusable(true)
            .pressClickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                placeholder = ColorPainter(Color(0xFF2A2A2A)),
                error = ColorPainter(Color(0xFF2A2A2A)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(thumbSize)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(thumbSize)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        animationMode = MarqueeAnimationMode.Immediately,
                    ),
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA8A8A8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(thumbSize),
            )
        }
    }
}

/**
 * Xevrae General Content item card (Song / Single)
 */
@Composable
fun HomeItemContent(
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    thumbSize: Dp = 160.dp,
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .focusable(true)
            .pressClickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                placeholder = ColorPainter(Color(0xFF2A2A2A)),
                error = ColorPainter(Color(0xFF2A2A2A)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(thumbSize)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(thumbSize)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        animationMode = MarqueeAnimationMode.Immediately,
                    ),
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA8A8A8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(thumbSize),
            )
        }
    }
}

/**
 * Xevrae Circular Artist item card
 */
@Composable
fun HomeItemArtist(
    onClick: () -> Unit,
    name: String,
    subscribers: String? = null,
    thumbnailUrl: String?,
    thumbSize: Dp = 160.dp,
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .focusable(true)
            .pressClickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                placeholder = ColorPainter(Color(0xFF2A2A2A)),
                error = ColorPainter(Color(0xFF2A2A2A)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(thumbSize)
                    .aspectRatio(1f)
                    .clip(CircleShape),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(thumbSize),
            )

            if (!subscribers.isNullOrEmpty()) {
                Text(
                    text = subscribers,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA8A8A8),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(thumbSize),
                )
            }
        }
    }
}

/**
 * Xevrae 4-row Quick Picks horizontal grid item
 */
@Composable
fun QuickPicksItem(
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    title: String,
    artist: String,
    explicit: Boolean = false,
    thumbnailUrl: String?,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    widthDp: Dp,
) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .width(widthDp - 48.dp)
            .focusable(true)
            .lightPressClickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = ColorPainter(Color(0xFF2A2A2A)),
                    error = ColorPainter(Color(0xFF2A2A2A)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (explicit) {
                        Icon(
                            painter = painterResource(R.drawable.explicit),
                            contentDescription = "Explicit",
                            tint = Color(0xFFA8A8A8),
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp),
                        )
                    }
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA8A8A8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (onMoreClick != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onMoreClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = "More",
                        tint = Color(0xFFA8A8A8),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/**
 * Xevrae Top Artist Chart item (with rank number badge)
 */
@Composable
fun ItemArtistChart(
    onClick: () -> Unit,
    title: String,
    subscribers: String? = null,
    thumbnailUrl: String?,
    rank: Int = 1,
    widthDp: Dp,
) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .width(widthDp - 48.dp)
            .focusable(true)
            .pressClickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) MaterialTheme.colorScheme.primary else Color(0xFFA8A8A8),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                placeholder = ColorPainter(Color(0xFF2A2A2A)),
                error = ColorPainter(Color(0xFF2A2A2A)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!subscribers.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subscribers,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA8A8A8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
