

package echo.music.iad1tya.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import echo.music.iad1tya.ui.screens.Screens

/**
 * Xevrae Navigation Bar ported for portrait and landscape orientations.
 */
@Composable
fun AppBottomNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    isLandscape: Boolean = false,
    isTranslucentBackground: Boolean = false,
    containerColor: Color? = null,
    showLabels: Boolean = true,
    onItemClick: (Screens, Boolean) -> Unit,
) {
    val resolvedContainerColor = containerColor ?: if (isLandscape) {
        Color(0xFF1F1F1F)
    } else if (isTranslucentBackground) {
        Color.Transparent
    } else {
        Color(0xFF121212)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isLandscape) {
                    Modifier
                        .padding(horizontal = 48.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                } else if (isTranslucentBackground) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF121212).copy(alpha = 0.6f),
                                Color(0xFF121212).copy(alpha = 0.9f),
                                Color(0xFF121212),
                            )
                        )
                    )
                } else {
                    Modifier.background(Color(0xFF121212))
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        NavigationBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = resolvedContainerColor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            navigationItems.forEach { screen ->
                val isSelected = remember(currentRoute, screen.route) {
                    currentRoute == screen.route ||
                        (currentRoute?.startsWith("${screen.route}/") == true &&
                            navigationItems.any { it.route == screen.route })
                }

                val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive

                NavigationBarItem(
                    selected = isSelected,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    ),
                    onClick = {
                        onItemClick(screen, isSelected)
                    },
                    label = if (showLabels) {
                        {
                            Text(
                                text = stringResource(screen.titleId),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            )
                        }
                    } else null,
                    icon = {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = stringResource(screen.titleId),
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        )
                    },
                )
            }
        }
    }
}
