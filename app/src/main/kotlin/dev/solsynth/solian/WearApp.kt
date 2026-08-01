package dev.solsynth.solian

import androidx.compose.runtime.*
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.ui.MainPagerScreen

@Composable
fun WearApp() {
    MainPagerScreen(
        onLogout = {
            TokenStore.clear()
        },
    )
}
