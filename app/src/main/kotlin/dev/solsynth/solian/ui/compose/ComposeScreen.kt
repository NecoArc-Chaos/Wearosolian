package dev.solsynth.solian.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import dev.solsynth.solian.R
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.PostRequest
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun ComposeScreen() {
    var text by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    WearScreen {
        item { Text(stringResource(R.string.compose_title), style = MaterialTheme.typography.titleSmall) }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            // Wear Compose Material3 does not provide OutlinedTextField;
            // the standard Material3 component is used here and works on Wear OS.
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.compose_hint)) },
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
                            result = context.getString(R.string.compose_success)
                        } catch (e: Exception) {
                            result = context.getString(R.string.compose_error, e.message ?: "")
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
                    Text(stringResource(R.string.compose_button))
                }
            }
        }
    }
}
