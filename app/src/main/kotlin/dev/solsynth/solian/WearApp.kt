package dev.solsynth.solian

import androidx.compose.runtime.*
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.ui.scaffold.WearAppScaffold
import dev.solsynth.solian.ui.login.LoginScreen
import dev.solsynth.solian.ui.MainPagerScreen

@Composable
fun WearApp() {
    var isLoggedIn by remember { mutableStateOf(TokenStore.isLoggedIn) }

    if (!isLoggedIn) {
        WearAppScaffold {
            LoginScreen(onLoginSuccess = { isLoggedIn = true })
        }
    } else {
        WearAppScaffold {
            MainPagerScreen(
                onLogout = {
                    TokenStore.token = null
                    isLoggedIn = false
                },
            )
        }
    }
}
