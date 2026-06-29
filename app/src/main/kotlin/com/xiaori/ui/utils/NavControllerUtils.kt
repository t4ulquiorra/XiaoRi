

package t4ulquiorra.xiaori.ui.utils

import androidx.navigation.NavController
import t4ulquiorra.xiaori.ui.screens.Screens

fun NavController.backToMain() {
    val mainRoutes = Screens.MainScreens.map { it.route }

    while (previousBackStackEntry != null &&
        currentBackStackEntry?.destination?.route !in mainRoutes
    ) {
        popBackStack()
    }
}
