package echo.music.iad1tya.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import echo.music.iad1tya.R

/**
 * Xevrae Full-width Song Row
 */
@Composable
fun ItemSongFullWidth(
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    title: String,
    artist: String,
    duration: String? = null,
    explicit: Boolean = false,
    thumbnailUrl: String?,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    index: Int? = null,
    isLiked: Boolean = false,
    onLikeClick: (() -> Unit)? = null,
    isDownloaded: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    rightView: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(true)
            .lightPressClickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (index != null) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFFA8A8A8),
                    modifier = Modifier.width(28.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
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
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
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
                                .size(13.dp)
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

            if (duration != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA8A8A8),
                )
            }

            if (rightView != null) {
                rightView()
            } else if (onMoreClick != null) {
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
 * Xevrae Full-width Album Row
 */
@Composable
fun ItemAlbumFullWidth(
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    title: String,
    artist: String,
    year: String? = null,
    thumbnailUrl: String?,
    songCount: Int? = null,
    rightView: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(true)
            .pressClickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp)),
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

                Spacer(modifier = Modifier.height(2.dp))

                val subtitleParts = listOfNotNull(
                    artist.takeIf { it.isNotEmpty() },
                    year,
                    songCount?.let { "$it songs" }
                )
                Text(
                    text = subtitleParts.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA8A8A8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (rightView != null) {
                rightView()
            } else if (onMoreClick != null) {
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
 * Xevrae Full-width Artist Row
 */
@Composable
fun ItemArtistFullWidth(
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    name: String,
    subscribers: String? = null,
    thumbnailUrl: String?,
    rightView: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(true)
            .pressClickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                    .size(56.dp)
                    .clip(CircleShape),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = name,
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

            if (rightView != null) {
                rightView()
            } else if (onMoreClick != null) {
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
 * Xevrae Full-width Playlist Row
 */
@Composable
fun ItemPlaylistFullWidth(
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    title: String,
    author: String? = null,
    songCount: String? = null,
    thumbnailUrl: String?,
    rightView: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(true)
            .pressClickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp)),
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

                Spacer(modifier = Modifier.height(2.dp))

                val subtitleParts = listOfNotNull(
                    author.takeIf { !it.isNullOrEmpty() },
                    songCount.takeIf { !it.isNullOrEmpty() }
                )
                Text(
                    text = subtitleParts.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA8A8A8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (rightView != null) {
                rightView()
            } else if (onMoreClick != null) {
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
