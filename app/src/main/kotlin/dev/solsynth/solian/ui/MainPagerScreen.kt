package dev.solsynth.solian.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.*
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.ui.account.AccountScreen
import dev.solsynth.solian.ui.chat.ChatScreen
import dev.solsynth.solian.ui.compose.ComposeScreen
import dev.solsynth.solian.ui.explore.ExploreScreen
import dev.solsynth.solian.ui.home.HomeScreen
import dev.solsynth.solian.ui.login.LoginScreen

@Composable
fun MainPagerScreen(
    onLogout: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 5 }, initialPage = 0)
    var isLoggedIn by remember { mutableStateOf(TokenStore.isLoggedIn) }

    // Standard M3 Wear layout with AppScaffold at the very top.
    AppScaffold(
        timeText = { TimeText() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
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
                    4 -> if (isLoggedIn) {
                        AccountScreen(
                            onLogout = {
                                onLogout()
                                isLoggedIn = false
                            },
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = { isLoggedIn = true },
                        )
                    }
                }
            }

            // M3 Guideline: Page indicator at the bottom center.
            // Requirement: Hide indicator on Explore page (index 1) to avoid collision with EdgeButton.
            val indicatorAlpha by animateFloatAsState(
                targetValue = if (pagerState.currentPage == 1) 0f else 1f,
                label = "IndicatorVisibility"
            )

            HorizontalPageIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .alpha(indicatorAlpha),
            )
        }
    }
}
