package dev.solsynth.solian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.ambient.AmbientModeSupport
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.theme.WearosolianTheme

class MainActivity : ComponentActivity(), AmbientModeSupport.AmbientModeCallback {

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ambientController = AmbientModeSupport.attach(this)
        TokenStore.init(applicationContext)

        setContent {
            WearosolianTheme {
                WearApp()
            }
        }
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        // Enter always-on: reduce updates
    }

    override fun onExitAmbient() {
        // Exit always-on: resume normal updates
    }

    override fun onUpdateAmbient() {
        // Update ambient UI if needed
    }
}
