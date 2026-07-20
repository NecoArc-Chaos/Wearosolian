package dev.solsynth.solian.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.PostRequest

@Composable
fun ComposeScreen() {
    var text by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("New Post", style = MaterialTheme.typography.titleSmall)

        Spacer(Modifier.height(8.dp))

        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("What's on your mind?") },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            maxLines = 3,
            enabled = !isPosting,
        )

        Spacer(Modifier.height(8.dp))

        if (result != null) {
            Text(result!!, style = MaterialTheme.typography.labelSmall,
                color = if (result?.startsWith("Error") == true)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = {
                scope.launch {
                    isPosting = true
                    result = null
                    try {
                        val token = "Bearer ${TokenStore.token}"
                        ApiClient.api.createPost(token, PostRequest(content = text))
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
