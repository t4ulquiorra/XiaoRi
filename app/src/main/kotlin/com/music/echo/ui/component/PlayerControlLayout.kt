package echo.music.iad1tya.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import echo.music.iad1tya.R

enum class PlayerRepeatMode {
    NONE, ALL, ONE
}

data class PlayerControlState(
    val isPlaying: Boolean = false,
    val isShuffle: Boolean = false,
    val repeatMode: PlayerRepeatMode = PlayerRepeatMode.NONE,
    val isPreviousAvailable: Boolean = true,
    val isNextAvailable: Boolean = true,
)

sealed interface PlayerUIEvent {
    data object PlayPause : PlayerUIEvent
    data object Previous : PlayerUIEvent
    data object Next : PlayerUIEvent
    data object Shuffle : PlayerUIEvent
    data object Repeat : PlayerUIEvent
}

fun Modifier.dimClickable(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier = this.clickable(
    interactionSource = interactionSource,
    indication = null,
    onClick = onClick,
)

@Composable
fun DimIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "dim_alpha")

    Box(
        modifier = modifier
            .dimClickable(interactionSource, onClick)
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun PlayerControlLayout(
    controllerState: PlayerControlState,
    isSmallSize: Boolean = false,
    accentColor: Color = Color.White,
    onUIEvent: (PlayerUIEvent) -> Unit,
) {
    val height = if (isSmallSize) 48.dp else 96.dp
    val smallIcon = if (isSmallSize) 16.dp to 31.dp else 26.dp to 46.dp
    val mediumIcon = if (isSmallSize) 31.dp to 42.dp else 46.dp to 57.dp
    val bigIcon = if (isSmallSize) 42.dp to 53.dp else 79.dp to 106.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 6.dp),
    ) {
        // Shuffle
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "shuffle_alpha")
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(smallIcon.second)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .dimClickable(interactionSource) { onUIEvent(PlayerUIEvent.Shuffle) }
                    .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = controllerState.isShuffle, label = "Shuffle Button") { isShuffle ->
                    if (!isShuffle) {
                        Icon(
                            painter = painterResource(R.drawable.shuffle),
                            tint = Color.White.copy(alpha = 0.6f),
                            contentDescription = "Shuffle Off",
                            modifier = Modifier.size(smallIcon.first),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.shuffle_on),
                            tint = accentColor,
                            contentDescription = "Shuffle On",
                            modifier = Modifier.size(smallIcon.first),
                        )
                    }
                }
            }
        }
        // Previous
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "prev_alpha")
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(mediumIcon.second)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .dimClickable(interactionSource) {
                        if (controllerState.isPreviousAvailable) onUIEvent(PlayerUIEvent.Previous)
                    }
                    .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    tint = if (controllerState.isPreviousAvailable) Color.White else Color.Gray,
                    contentDescription = "Previous",
                    modifier = Modifier.size(mediumIcon.first),
                )
            }
        }
        // Play/Pause
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "playpause_alpha")
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(bigIcon.second)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .dimClickable(interactionSource) { onUIEvent(PlayerUIEvent.PlayPause) }
                    .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = controllerState.isPlaying, label = "PlayPause") { isPlaying ->
                    if (!isPlaying) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircle,
                            tint = Color.White,
                            contentDescription = "Play",
                            modifier = Modifier.size(bigIcon.first),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.PauseCircle,
                            tint = Color.White,
                            contentDescription = "Pause",
                            modifier = Modifier.size(bigIcon.first),
                        )
                    }
                }
            }
        }
        // Next
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "next_alpha")
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(mediumIcon.second)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .dimClickable(interactionSource) {
                        if (controllerState.isNextAvailable) onUIEvent(PlayerUIEvent.Next)
                    }
                    .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    tint = if (controllerState.isNextAvailable) Color.White else Color.Gray,
                    contentDescription = "Next",
                    modifier = Modifier.size(mediumIcon.first),
                )
            }
        }
        // Repeat
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "repeat_alpha")
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(smallIcon.second)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .dimClickable(interactionSource) { onUIEvent(PlayerUIEvent.Repeat) }
                    .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = controllerState.repeatMode, label = "Repeat Button") { rm ->
                    when (rm) {
                        PlayerRepeatMode.NONE -> {
                            Icon(
                                painter = painterResource(R.drawable.repeat),
                                tint = Color.White.copy(alpha = 0.6f),
                                contentDescription = "Repeat Off",
                                modifier = Modifier.size(smallIcon.first),
                            )
                        }
                        PlayerRepeatMode.ALL -> {
                            Icon(
                                painter = painterResource(R.drawable.repeat_on),
                                tint = accentColor,
                                contentDescription = "Repeat All",
                                modifier = Modifier.size(smallIcon.first),
                            )
                        }
                        PlayerRepeatMode.ONE -> {
                            Icon(
                                painter = painterResource(R.drawable.repeat_one_on),
                                tint = accentColor,
                                contentDescription = "Repeat One",
                                modifier = Modifier.size(smallIcon.first),
                            )
                        }
                    }
                }
            }
        }
    }
}
