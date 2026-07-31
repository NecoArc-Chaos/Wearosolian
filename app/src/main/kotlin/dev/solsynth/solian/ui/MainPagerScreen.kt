package dev.solsynth.solian.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.MaterialTheme
import dev.solsynth.solian.ui.home.HomeScreen
import dev.solsynth.solian.ui.explore.ExploreScreen
import dev.solsynth.solian.ui.compose.ComposeScreen
import dev.solsynth.solian.ui.chat.ChatScreen
import dev.solsynth.solian.ui.account.AccountScreen

@Composable
fun MainPagerScreen(
    onShowLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 5 }, initialPage = 0)

    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> HomeScreen()
                1 -> ExploreScreen()
                2 -> ComposeScreen()
                3 -> ChatScreen()
                4 -> AccountScreen(onLogout = onLogout, onShowLogin = onShowLogin)
            }
        }

        HorizontalPageIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .graphicsLayer { clip = false },
        )
    }
}
