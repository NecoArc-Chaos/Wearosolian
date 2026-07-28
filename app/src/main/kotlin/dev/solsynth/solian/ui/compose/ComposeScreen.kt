package dev.solsynth.solian.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.PostRequest
import dev.solsynth.solian.theme.rememberIsScreenRound

@Composable
fun ComposeScreen() {
    var text by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)
    val isRound = rememberIsScreenRound()

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
        contentPadding = PaddingValues(
            top = if (isRound) 36.dp else 12.dp,
            bottom = if (isRound) 36.dp else 16.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Text("New Post", style = MaterialTheme.typography.titleSmall) }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("What's on your mind?") },
                singleLine = false,
                modifier = Modifier.fillMaxWidth(0.9f).height(80.dp),
                maxLines = 3,
                enabled = !isPosting,
            )
        }

        item { Spacer(Modifier.height(8.dp)) }

        if (result != null) {
            item {
                Text(result!!, style = MaterialTheme.typography.labelSmall,
                    color = if (result?.startsWith("Error") == true)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary)
            }
        }

        item {
            Button(
                onClick = {
                    scope.launch {
                        isPosting = true
                        result = null
                        try {
                            ApiClient.api.createPost(PostRequest(content = text))
                            text = ""
                            result = "Posted!"
                        } catch (e: Exception) {
                            result = "Error: ${e.message}"
                        } finally {
                            isPosting = false
                        }
                    }
                },
                enabled = text.isNotBlank() && !isPosting,
                modifier = Modifier.fillMaxWidth(0.8f),
            ) {
                if (isPosting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Publish")
                }
            }
        }
    }
}
