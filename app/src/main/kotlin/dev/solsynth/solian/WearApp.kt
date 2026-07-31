package dev.solsynth.solian

import androidx.compose.runtime.*
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.ui.login.LoginScreen
import dev.solsynth.solian.ui.MainPagerScreen

@Composable
fun WearApp() {
    var isLoggedIn by remember { mutableStateOf(TokenStore.isLoggedIn) }

    key(isLoggedIn) {
        if (!isLoggedIn) {
            LoginScreen(onLoginSuccess = { isLoggedIn = true })
        } else {
            MainPagerScreen(
                onLogout = {
                    TokenStore.clear()
                    isLoggedIn = false
                },
            )
        }
    }
}
}
