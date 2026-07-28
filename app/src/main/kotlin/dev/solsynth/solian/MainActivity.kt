package dev.solsynth.solian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.theme.WearosolianTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenStore.init(applicationContext)

        setContent {
            WearosolianTheme {
                WearApp()
            }
        }
    }
}
