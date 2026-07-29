package dev.solsynth.solian.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import dev.solsynth.solian.R
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun HomeScreen() {
    WearScreen {
        item {
            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.home_checkin_title), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.home_checkin_fortune),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.home_checkin_advice),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.home_countdown_title), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.home_countdown_christmas), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.home_countdown_days), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.home_notifications_title), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.home_notifications_unread),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
