package echo.music.iad1tya.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.innertube.models.*
import echo.music.iad1tya.LocalDatabase
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.LocalPlayerConnection
import echo.music.iad1tya.R
import echo.music.iad1tya.db.entities.Album
import echo.music.iad1tya.db.entities.Artist
import echo.music.iad1tya.db.entities.LocalItem
import echo.music.iad1tya.db.entities.Song
import echo.music.iad1tya.models.MediaMetadata
import echo.music.iad1tya.models.toMediaMetadata
import echo.music.iad1tya.playback.queues.YouTubeQueue
import echo.music.iad1tya.ui.component.*
import echo.music.iad1tya.ui.component.shimmer.GridItemPlaceHolder
import echo.music.iad1tya.ui.component.shimmer.ShimmerHost
import echo.music.iad1tya.ui.component.shimmer.TextPlaceholder
import echo.music.iad1tya.ui.menu.YouTubeAlbumMenu
import echo.music.iad1tya.ui.menu.YouTubeArtistMenu
import echo.music.iad1tya.ui.menu.YouTubePlaylistMenu
import echo.music.iad1tya.ui.menu.YouTubeSongMenu
import echo.music.iad1tya.viewmodels.DailyDiscoverItem
import echo.music.iad1tya.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalTime
import kotlin.math.*

private fun Modifier.angledGradientBackground(
    colors: List<Color>,
    degrees: Float,
): Modifier = this.then(
    if (colors.size < 2) {
        Modifier
    } else {
        Modifier.drawBehind {
            val (x, y) = size
            val gamma = atan2(y, x)
            if (gamma == 0f || gamma == (PI / 2).toFloat()) return@drawBehind

            val degreesNormalised = (degrees % 360).let { if (it < 0) it + 360 else it }
            val alpha = (degreesNormalised * PI / 180).toFloat()
            val gradientLength = when (alpha) {
                in 0f..gamma, in (2 * PI - gamma)..2 * PI -> x / cos(alpha)
                in gamma..(PI - gamma).toFloat() -> y / sin(alpha)
                in (PI - gamma)..(PI + gamma) -> x / -cos(alpha)
                in (PI + gamma)..(2 * PI - gamma) -> y / -sin(alpha)
                else -> hypot(x, y)
            }

            val centerOffsetX = cos(alpha) * gradientLength / 2
            val centerOffsetY = sin(alpha) * gradientLength / 2

            drawRect(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = center - androidx.compose.ui.geometry.Offset(centerOffsetX, centerOffsetY),
                    end = center + androidx.compose.ui.geometry.Offset(centerOffsetX, centerOffsetY),
                )
            )
        }
    }
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState? = null,
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val homePage by viewModel.homePage.collectAsState()
    val quickPicks by viewModel.quickPicks.collectAsState()
    val speedDialItems by viewModel.speedDialItems.collectAsState()
    val dailyDiscover by viewModel.dailyDiscover.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()

    val scrollState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()
    val chipRowState = rememberScrollState()

    // Pagination trigger
    LaunchedEffect(scrollState) {
        snapshotFlow {
            val totalItems = scrollState.layoutInfo.totalItemsCount
            val lastVisibleIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 3 && totalItems > 0
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && !isLoading && homePage?.continuation != null) {
                    viewModel.loadMoreYouTubeItems(homePage?.continuation)
                }
            }
    }

    // Dynamic top ambient color
    val topAmbientColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
        animationSpec = tween(500),
        label = "ambientColor"
    )

    // Filter out non-functional Station / TV / Listen Together sections
    val excludedKeywords = remember {
        listOf("station", "stations", "shows for you", "listen together", "tv", "live")
    }
    val filteredSections = remember(homePage) {
        homePage?.sections?.filter { section ->
            !excludedKeywords.any { kw -> section.title.contains(kw, ignoreCase = true) }
        }.orEmpty()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        state = pullRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color(0xFF242424),
                color = MaterialTheme.colorScheme.primary,
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Top Atmospheric Angled Mesh Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .angledGradientBackground(
                        listOf(topAmbientColor, Color(0xFF121212)),
                        25f
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x75000000),
                                    Color(0xFF121212),
                                )
                            ),
                        ),
                )
            }

            // 2. Content gated behind Loading / Refreshing state via Crossfade
            val isContentLoading = isRefreshing || (isLoading && homePage == null && quickPicks == null)

            Crossfade(
                targetState = isContentLoading,
                animationSpec = tween(300),
                label = "Home Content Crossfade",
            ) { loading ->
                if (loading) {
                    // Shimmer skeleton placeholder with scrolling disabled
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = 130.dp,
                                bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding() + 48.dp
                            )
                    ) {
                        HomeShimmerLayout()
                    }
                } else {
                    // Fully loaded content list with stable items
                    LazyColumn(
                        state = scrollState,
                        contentPadding = PaddingValues(
                            top = 130.dp,
                            bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding() + 48.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Quick Picks Section (4-row horizontal snapping grid)
                        if (!quickPicks.isNullOrEmpty()) {
                            item(key = "xevrae_quick_picks_section") {
                                val picks = quickPicks ?: emptyList()
                                val quickPicksGridState = rememberLazyGridState()
                                val quickPicksSnapper = rememberSnapFlingBehavior(
                                    SnapLayoutInfoProvider(lazyGridState = quickPicksGridState, snapPosition = SnapPosition.Start)
                                )

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val widthDp = maxWidth
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)) {
                                            Text(
                                                text = "Let's start with a radio",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFA8A8A8),
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = stringResource(R.string.quick_picks),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
                                        }

                                        LazyHorizontalGrid(
                                            rows = GridCells.Fixed(4),
                                            modifier = Modifier.height(256.dp),
                                            state = quickPicksGridState,
                                            flingBehavior = quickPicksSnapper,
                                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            items(picks, key = { it.song.id }) { song ->
                                                QuickPicksItem(
                                                    title = song.song.title,
                                                    artist = song.artists.joinToString { it.name },
                                                    explicit = song.song.explicit,
                                                    thumbnailUrl = song.song.thumbnailUrl,
                                                    isActive = song.song.id == mediaMetadata?.id,
                                                    isPlaying = isPlaying,
                                                    widthDp = widthDp,
                                                    onClick = {
                                                        if (song.song.id == mediaMetadata?.id) {
                                                            playerConnection.togglePlayPause()
                                                        } else {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    endpoint = WatchEndpoint(videoId = song.song.id),
                                                                    preloadItem = song.toMediaMetadata()
                                                                )
                                                            )
                                                        }
                                                    },
                                                    onMoreClick = {
                                                        menuState.show {
                                                            YouTubeSongMenu(
                                                                song = SongItem(
                                                                    id = song.song.id,
                                                                    title = song.song.title,
                                                                    artists = song.artists.map { com.music.innertube.models.Artist(id = it.id, name = it.name) },
                                                                    album = null,
                                                                    duration = null,
                                                                    thumbnail = song.song.thumbnailUrl.orEmpty(),
                                                                    explicit = song.song.explicit
                                                                ),
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Daily Discover Card
                        dailyDiscover?.firstOrNull()?.let { discover ->
                            item(key = "xevrae_daily_discover_section") {
                                val rec = discover.recommendation
                                val recSong = rec as? SongItem
                                val recId = rec.id
                                val recTitle = rec.title
                        // Daily Discover Carousel / Banner
                        if (dailyDiscover != null) {
                            val discover = dailyDiscover
                            if (discover != null) {
                                item(key = "xevrae_daily_discover_section") {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                                            Text(
                                                text = "Hand-picked for today",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFA8A8A8),
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Daily Discover",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
                                        }

                                        ItemSongFullWidth(
                                            onClick = {
                                                if (discover.song.id == mediaMetadata?.id) {
                                                    playerConnection.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue(
                                                            endpoint = WatchEndpoint(videoId = discover.song.id),
                                                            preloadItem = discover.toMediaMetadata()
                                                        )
                                                    )
                                                }
                                            },
                                            onMoreClick = {
                                                menuState.show {
                                                    YouTubeSongMenu(
                                                        song = SongItem(
                                                            id = discover.song.id,
                                                            title = discover.song.title,
                                                            artists = discover.artists.map { com.music.innertube.models.Artist(id = it.id, name = it.name) },
                                                            album = null,
                                                            duration = null,
                                                            thumbnail = discover.song.thumbnailUrl.orEmpty(),
                                                            explicit = discover.song.explicit
                                                        ),
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            },
                                            title = discover.song.title,
                                            artists = discover.artists.map { com.music.innertube.models.Artist(id = it.id, name = it.name) },
                                            thumbnailUrl = discover.song.thumbnailUrl,
                                            album = null,
                                            duration = null,
                                            explicit = discover.song.explicit,
                                        )
                                    }
                                }
                            }
                        }

                        // Speed Dial / Keep Listening Recommendations
                        if (!speedDialItems.isNullOrEmpty()) {
                            item(key = "xevrae_speed_dial_section") {
                                val speedDials = speedDialItems ?: emptyList()
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                                        Text(
                                            text = "Jump back in",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA8A8A8),
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Keep Listening",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        speedDials.take(5).forEach { rec ->
                                            val recSong = rec as? SongItem
                                            val recAlbum = rec as? AlbumItem
                                            val recArtist = rec as? ArtistItem
                                            val recPlaylist = rec as? PlaylistItem

                                            val recTitle = recSong?.title ?: recAlbum?.title ?: recArtist?.title ?: recPlaylist?.title ?: ""
                                            val recSubtitle = recSong?.artists?.joinToString { it.name }
                                                ?: recAlbum?.artists?.joinToString { it.name }
                                                ?: recPlaylist?.author?.name
                                                ?: "Artist"
                                            val recThumb = rec.thumbnail
                                            val recId = rec.id

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        when (rec) {
                                                            is SongItem -> {
                                                                playerConnection.playQueue(
                                                                    YouTubeQueue(
                                                                        rec.endpoint ?: WatchEndpoint(videoId = rec.id),
                                                                        rec.toMediaMetadata()
                                                                    )
                                                                )
                                                            }
                                                            is AlbumItem -> navController.navigate("album/${rec.id}")
                                                            is ArtistItem -> navController.navigate("artist/${rec.id}")
                                                            is PlaylistItem -> navController.navigate("online_playlist/${rec.id}")
                                                        }
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(recThumb)
                                                        .crossfade(true)
                                                        .build(),
                                                    placeholder = ColorPainter(Color(0xFF2A2A2A)),
                                                    error = ColorPainter(Color(0xFF2A2A2A)),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(if (rec is ArtistItem) CircleShape else RoundedCornerShape(8.dp)),
                                                )

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = recTitle,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    Text(
                                                        text = recSubtitle,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFFA8A8A8),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                    .clickable {
                                                        if (recId == mediaMetadata?.id) {
                                                            playerConnection.togglePlayPause()
                                                        } else if (recSong != null) {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    recSong.endpoint ?: WatchEndpoint(videoId = recSong.id),
                                                                    recSong.toMediaMetadata()
                                                                )
                                                            )
                                                        }
                                                    },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(
                                                            if (recId == mediaMetadata?.id && isPlaying) R.drawable.pause else R.drawable.play
                                                        ),
                                                        contentDescription = "Play",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Forgotten Favorites Carousel
                        if (!forgottenFavorites.isNullOrEmpty()) {
                            item(key = "xevrae_forgotten_favorites_section") {
                                val favs = forgottenFavorites ?: emptyList()
                                val lazyListState = rememberLazyListState()
                                val snapper = rememberSnapFlingBehavior(
                                    SnapLayoutInfoProvider(lazyListState = lazyListState, snapPosition = SnapPosition.Start)
                                )

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                                        Text(
                                            text = "Rediscover your old gems",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA8A8A8),
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Forgotten Favorites",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )
                                    }

                                    LazyRow(
                                        state = lazyListState,
                                        flingBehavior = snapper,
                                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        itemsIndexed(favs, key = { index, song -> "${song.song.id}_$index" }) { _, song ->
                                            HomeItemContent(
                                                onClick = {
                                                    if (song.song.id == mediaMetadata?.id) {
                                                        playerConnection.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue(
                                                                endpoint = WatchEndpoint(videoId = song.song.id),
                                                                preloadItem = song.toMediaMetadata()
                                                            )
                                                        )
                                                    }
                                                },
                                                onMoreClick = {
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = SongItem(
                                                                id = song.song.id,
                                                                title = song.song.title,
                                                                artists = song.artists.map { com.music.innertube.models.Artist(id = it.id, name = it.name) },
                                                                album = null,
                                                                duration = null,
                                                                thumbnail = song.song.thumbnailUrl.orEmpty(),
                                                                explicit = song.song.explicit
                                                            ),
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                },
                                                title = song.song.title,
                                                subtitle = song.artists.joinToString { it.name },
                                                thumbnailUrl = song.song.thumbnailUrl,
                                                thumbSize = 160.dp,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Dynamic YouTube Music Home Sections (excluding Station / TV / Listen Together)
                        itemsIndexed(
                            items = filteredSections,
                            key = { index, section -> "section_${section.title}_${section.endpoint?.params.orEmpty()}_${section.items.firstOrNull()?.id.orEmpty()}_$index" }
                        ) { _, section ->
                            HomeItem(
                                title = section.title,
                                subtitle = null,
                                channelId = null,
                                thumbnailUrl = null,
                                items = section.items,
                                navController = navController,
                                onItemClick = { item ->
                                    when (item) {
                                        is SongItem -> {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                                    item.toMediaMetadata()
                                                )
                                            )
                                        }
                                        is AlbumItem -> navController.navigate("album/${item.id}")
                                        is ArtistItem -> navController.navigate("artist/${item.id}")
                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                    }
                                }
                            )
                        }

                        // Pagination Spinner
                        if (isLoading && homePage != null) {
                            item(key = "pagination_loading") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Floating Pinned Top Bar & Scrolling Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF121212).copy(alpha = 0.95f),
                                Color(0xFF121212).copy(alpha = 0.80f),
                                Color.Transparent,
                            )
                        )
                    )
            ) {
                // Top App Bar
                HomeTopAppBar(
                    navController = navController,
                    appName = stringResource(R.string.app_name),
                    onSearchClick = { navController.navigate("search") },
                    onHistoryClick = { navController.navigate("history") },
                    onSettingsClick = { navController.navigate("settings") },
                )

                // Chips Row
                val chips = homePage?.chips
                if (!chips.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(chipRowState)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // "All" chip
                        Chip(
                            isSelected = selectedChip == null,
                            text = "All",
                            onClick = {
                                if (selectedChip != null) {
                                    viewModel.toggleChip(null)
                                }
                            }
                        )

                        chips.forEach { chip ->
                            Chip(
                                isSelected = selectedChip == chip,
                                text = chip.title,
                                onClick = { viewModel.toggleChip(chip) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Xevrae Top App Bar Header with time-of-day greeting
 */
@Composable
fun HomeTopAppBar(
    navController: NavController,
    appName: String,
    onSearchClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val hour = remember { LocalTime.now().hour }
    val greeting = remember(hour) {
        when (hour) {
            in 6..12 -> "Good morning"
            in 13..17 -> "Good afternoon"
            in 18..23 -> "Good evening"
            else -> "Good night"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA8A8A8),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242424))
                    .clickable { onSearchClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242424))
                    .clickable { onHistoryClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = "History",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242424))
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Xevrae Home Shimmer Loading Layout matching Xevrae's Shimmer.kt
 * Scrolling is completely disabled while placeholder is displayed.
 */
@Composable
private fun HomeShimmerLayout() {
    ShimmerHost {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Quick Picks Shimmer Skeleton
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextPlaceholder(modifier = Modifier.width(150.dp).height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GridItemPlaceHolder(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextPlaceholder(modifier = Modifier.width(220.dp).height(16.dp))
                                TextPlaceholder(modifier = Modifier.width(150.dp).height(14.dp))
                            }
                        }
                    }
                }
            }

            // Section 1 Shimmer Skeleton (Carousels)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextPlaceholder(modifier = Modifier.width(160.dp).height(24.dp))
                LazyRow(
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(4) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            GridItemPlaceHolder(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            TextPlaceholder(modifier = Modifier.width(130.dp).height(16.dp))
                            TextPlaceholder(modifier = Modifier.width(90.dp).height(14.dp))
                        }
                    }
                }
            }

            // Section 2 Shimmer Skeleton (Carousels)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextPlaceholder(modifier = Modifier.width(180.dp).height(24.dp))
                LazyRow(
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(4) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            GridItemPlaceHolder(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            TextPlaceholder(modifier = Modifier.width(130.dp).height(16.dp))
                            TextPlaceholder(modifier = Modifier.width(90.dp).height(14.dp))
                        }
                    }
                }
            }
        }
    }
}
