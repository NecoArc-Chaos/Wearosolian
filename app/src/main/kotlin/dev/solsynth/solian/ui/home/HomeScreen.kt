package dev.solsynth.solian.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.solsynth.solian.R
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val checkInResult by viewModel.checkInResult.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val countdownDays by viewModel.countdownDays.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    WearScreen { spec ->
        item {
            TitleCard(
                onClick = { viewModel.refreshData() },
                title = {
                    Text(
                        text = stringResource(R.string.home_checkin_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(0.9f).transformedHeight(this, spec),
                transformation = SurfaceTransformation(spec)
            ) {
                if (isLoading && checkInResult == null) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize))
                } else {
                    Text(
                        text = checkInResult?.fortuneReport?.poem
                            ?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.home_checkin_fortune),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = checkInResult?.fortuneReport?.summary
                            ?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.home_checkin_advice),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            TitleCard(
                onClick = { viewModel.refreshCountdown() },
                title = {
                    Text(
                        text = stringResource(R.string.home_countdown_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(0.9f).transformedHeight(this, spec),
                transformation = SurfaceTransformation(spec)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(R.string.home_countdown_christmas),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(R.string.home_countdown_days, countdownDays),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }

        item {
            TitleCard(
                onClick = {},
                title = {
                    Text(
                        text = stringResource(R.string.home_notifications_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(0.9f).transformedHeight(this, spec),
                transformation = SurfaceTransformation(spec)
            ) {
                Text(
                    text = if (unreadCount > 0) {
                        "$unreadCount ${stringResource(R.string.home_notifications_unread_suffix)}"
                    } else {
                        stringResource(R.string.home_notifications_unread_none)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
