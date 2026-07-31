package dev.solsynth.solian

import androidx.compose.runtime.*
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.ui.login.LoginScreen
import dev.solsynth.solian.ui.MainPagerScreen

@Composable
fun WearApp() {
    var showLogin by remember { mutableStateOf(false) }

    if (showLogin) {
        LoginScreen(onLoginSuccess = { showLogin = false })
    } else {
        MainPagerScreen(
            onShowLogin = { showLogin = true },
            onLogout = {
                TokenStore.clear()
            },
        )
    }
}
}
