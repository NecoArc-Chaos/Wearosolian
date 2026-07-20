package dev.solsynth.solian

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.solsynth.solian.ui.scaffold.WearAppScaffold
import dev.solsynth.solian.ui.timeline.TimelineScreen
import dev.solsynth.solian.ui.chat.ChatListScreen
import dev.solsynth.solian.ui.settings.SettingsScreen

/**
 * Root composable — Wear-optimized scaffold with swipe-to-dismiss navigation.
 */
@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    WearAppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "timeline"
        ) {
            composable("timeline") {
                TimelineScreen(onNavigateToChat = { navController.navigate("chat") })
            }
            composable("chat") {
                ChatListScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}
