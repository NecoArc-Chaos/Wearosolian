package dev.solsynth.solian

import androidx.compose.runtime.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.ui.scaffold.WearAppScaffold
import dev.solsynth.solian.ui.login.LoginScreen
import dev.solsynth.solian.ui.timeline.TimelineScreen
import dev.solsynth.solian.ui.chat.ChatListScreen

@Composable
fun WearApp() {
    var isLoggedIn by remember { mutableStateOf(TokenStore.isLoggedIn) }
    val navController = rememberSwipeDismissableNavController()

    if (!isLoggedIn) {
        WearAppScaffold {
            LoginScreen(
                onLoginSuccess = { isLoggedIn = true }
            )
        }
    } else {
        WearAppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "timeline",
            ) {
                composable("timeline") {
                    TimelineScreen(
                        onNavigateToChat = { navController.navigate("chat") },
                        onLogout = {
                            TokenStore.token = null
                            isLoggedIn = false
                        },
                    )
                }
                composable("chat") {
                    ChatListScreen()
                }
            }
        }
    }
}
