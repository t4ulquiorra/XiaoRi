package echo.music.iad1tya.ui.player

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.extensions.metadata
import echo.music.iad1tya.ui.component.DimIconButton
import echo.music.iad1tya.ui.component.PlayerControlLayout
import echo.music.iad1tya.ui.component.PlayerControlState
import echo.music.iad1tya.ui.component.PlayerRepeatMode
import echo.music.iad1tya.ui.component.PlayerUIEvent
import echo.music.iad1tya.utils.makeTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreenContent(
    navController: NavController,
    pureBlack: Boolean = false,
    onDismiss: () -> Unit = {},
    onShowQueue: () -> Unit = {},
    onShowMenu: () -> Unit = {},
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val coroutineScope = rememberCoroutineScope()

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    val isShuffle = playerConnection.player.shuffleModeEnabled
    val playerRepeatMode = when (repeatMode) {
        Player.REPEAT_MODE_OFF -> PlayerRepeatMode.NONE
        Player.REPEAT_MODE_ALL -> PlayerRepeatMode.ALL
        Player.REPEAT_MODE_ONE -> PlayerRepeatMode.ONE
        else -> PlayerRepeatMode.NONE
    }

    val controlState = remember(isPlaying, isShuffle, playerRepeatMode, canSkipPrevious, canSkipNext) {
        PlayerControlState(
            isPlaying = isPlaying,
            isShuffle = isShuffle,
            repeatMode = playerRepeatMode,
            isPreviousAvailable = canSkipPrevious,
            isNextAvailable = canSkipNext,
        )
    }

    // Dynamic Palette & Gradient
    val defaultDarkBg = Color(0xFF121212)
    val startColor = remember { Animatable(defaultDarkBg) }
    val endColor = remember { Animatable(defaultDarkBg) }
    var spotShadowColor by remember { mutableStateOf(Color.White) }
    val gradientOffset = remember { GradientOffset(GradientAngle.CW135) }

    LaunchedEffect(mediaMetadata?.id) {
        val currentMeta = mediaMetadata
        if (currentMeta?.thumbnailUrl != null) {
            withContext(Dispatchers.IO) {
                val req = ImageRequest.Builder(context)
                    .data(currentMeta.thumbnailUrl)
                    .size(100, 100)
                    .allowHardware(false)
                    .build()
                val result = runCatching { context.imageLoader.execute(req) }.getOrNull()
                val bitmap = result?.image?.toBitmap()
                if (bitmap != null) {
                    val palette = runCatching { Palette.from(bitmap).generate() }.getOrNull()
                    val extractedColor = palette.getColorFromPalette()
                    spotShadowColor = extractedColor
                    startColor.animateTo(extractedColor, tween(600))
                    endColor.animateTo(if (pureBlack) Color.Black else defaultDarkBg, tween(600))
                }
            }
        } else {
            startColor.animateTo(defaultDarkBg, tween(400))
            endColor.animateTo(if (pureBlack) Color.Black else defaultDarkBg, tween(400))
        }
    }

    // Playback Progress & Duration
    var sliderDraggingPosition by remember { mutableStateOf<Float?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying, mediaMetadata?.id) {
        while (isActive) {
            if (sliderDraggingPosition == null) {
                currentPositionMs = playerConnection.player.currentPosition
                totalDurationMs = playerConnection.player.duration.coerceAtLeast(0L)
            }
            delay(200)
        }
    }

    // Artwork Pager
    val queueSize = playerConnection.player.mediaItemCount
    val currentMediaItemIndex = playerConnection.player.currentMediaItemIndex
    val artworkPagerState = rememberPagerState(
        initialPage = currentMediaItemIndex.coerceAtLeast(0),
        pageCount = { queueSize.coerceAtLeast(1) },
    )
    var isAnimatingFromPlayer by remember { mutableStateOf(false) }
    var isUserDraggingActive by remember { mutableStateOf(false) }

    LaunchedEffect(artworkPagerState) {
        snapshotFlow {
            artworkPagerState.isScrollInProgress to isAnimatingFromPlayer
        }.collect { (scrolling, animating) ->
            isUserDraggingActive = scrolling && !animating
        }
    }

    // Player -> Pager sync
    LaunchedEffect(currentMediaItemIndex, queueSize) {
        if (!isUserDraggingActive &&
            queueSize > 0 &&
            currentMediaItemIndex in 0 until queueSize &&
            currentMediaItemIndex != artworkPagerState.currentPage
        ) {
            isAnimatingFromPlayer = true
            try {
                artworkPagerState.animateScrollToPage(currentMediaItemIndex)
            } finally {
                isAnimatingFromPlayer = false
            }
        }
    }

    // Pager -> Player sync
    LaunchedEffect(artworkPagerState, currentMediaItemIndex, queueSize) {
        snapshotFlow { artworkPagerState.settledPage }
            .distinctUntilChanged()
            .collect { settled ->
                if (isAnimatingFromPlayer || queueSize <= 0 || settled !in 0 until queueSize || settled == currentMediaItemIndex) return@collect
                runCatching {
                    when (val action = computeSeekAction(settled, currentMediaItemIndex)) {
                        ArtworkSeekAction.Next -> playerConnection.player.seekToNextMediaItem()
                        ArtworkSeekAction.Previous -> playerConnection.player.seekToPreviousMediaItem()
                        is ArtworkSeekAction.Skip -> playerConnection.player.seekToDefaultPosition(action.index)
                        ArtworkSeekAction.NoOp -> Unit
                    }
                }
            }
    }

    val mainScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (pureBlack) Brush.verticalGradient(listOf(Color.Black, Color.Black))
                else Brush.linearGradient(
                    colors = listOf(startColor.value, endColor.value),
                    start = gradientOffset.start,
                    end = gradientOffset.end,
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(mainScrollState)
        ) {
            // Header Top Bar
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    DimIconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = onDismiss,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp,
                            ),
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        val subtitleText = mediaMetadata?.album?.title ?: "Echo Music"
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            maxLines = 1,
                            modifier = Modifier
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    animationMode = MarqueeAnimationMode.Immediately,
                                )
                                .focusable(),
                        )
                    }
                },
                actions = {
                    DimIconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = onShowMenu,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Artwork Pager Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                HorizontalPager(
                    state = artworkPagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    key = { idx -> "artwork_page_$idx" },
                ) { page ->
                    val isCurrentPage = page == currentMediaItemIndex
                    val itemMetadata = if (isCurrentPage) mediaMetadata else {
                        runCatching {
                            playerConnection.player.getMediaItemAt(page).metadata
                        }.getOrNull()
                    }
                    val thumbUrl = itemMetadata?.thumbnailUrl

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .shadow(
                                elevation = if (isCurrentPage) 12.dp else 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = if (isCurrentPage) spotShadowColor.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.3f),
                                ambientColor = Color.Transparent,
                            )
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(thumbUrl)
                                .crossfade(400)
                                .build(),
                            contentDescription = itemMetadata?.title,
                            placeholder = ColorPainter(Color(0xFF2A2A2A)),
                            error = ColorPainter(Color(0xFF2A2A2A)),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Track Title, Artists & Favorite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = mediaMetadata?.title ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            )
                            .focusable(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val artistNames = mediaMetadata?.artists?.joinToString { it.name }.orEmpty()
                    Text(
                        text = artistNames,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.7f),
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            )
                            .focusable()
                            .clickable {
                                val firstArtistId = mediaMetadata?.artists?.firstOrNull()?.id
                                if (!firstArtistId.isNullOrEmpty()) {
                                    onDismiss()
                                    navController.navigate("artist/$firstArtistId")
                                }
                            },
                    )
                }

                // Heart / Favorite button
                val isFavorite = currentSong?.song?.inLibrary != null
                DimIconButton(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(start = 8.dp),
                    onClick = {
                        playerConnection.toggleLike()
                    },
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                        contentDescription = "Favorite",
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress Slider & Timestamps
            val sliderValue = sliderDraggingPosition ?: if (totalDurationMs > 0L) {
                currentPositionMs.toFloat() / totalDurationMs.toFloat()
            } else 0f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
            ) {
                Slider(
                    value = sliderValue.coerceIn(0f, 1f),
                    onValueChange = { sliderDraggingPosition = it },
                    onValueChangeFinished = {
                        val seekTargetMs = ((sliderDraggingPosition ?: 0f) * totalDurationMs).toLong()
                        playerConnection.player.seekTo(seekTargetMs)
                        currentPositionMs = seekTargetMs
                        sliderDraggingPosition = null
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val displayedPosition = if (sliderDraggingPosition != null) {
                        (sliderDraggingPosition!! * totalDurationMs).toLong()
                    } else currentPositionMs
                    Text(
                        text = makeTimeString(displayedPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Text(
                        text = makeTimeString(totalDurationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls (PlayerControlLayout)
            PlayerControlLayout(
                controllerState = controlState,
                accentColor = MaterialTheme.colorScheme.primary,
                onUIEvent = { event ->
                    when (event) {
                        PlayerUIEvent.PlayPause -> {
                            if (isPlaying) playerConnection.player.pause()
                            else playerConnection.player.play()
                        }
                        PlayerUIEvent.Previous -> playerConnection.player.seekToPreviousMediaItem()
                        PlayerUIEvent.Next -> playerConnection.player.seekToNextMediaItem()
                        PlayerUIEvent.Shuffle -> playerConnection.player.shuffleModeEnabled = !isShuffle
                        PlayerUIEvent.Repeat -> {
                            val nextMode = when (repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                            playerConnection.player.repeatMode = nextMode
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Actions (Queue button, etc.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DimIconButton(
                    onClick = onShowQueue,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        tint = Color.White.copy(alpha = 0.7f),
                        contentDescription = "Queue",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
