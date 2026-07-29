package dev.solsynth.solian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.ambient.AmbientModeSupport
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.theme.WearosolianTheme

class MainActivity : ComponentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenStore.init(applicationContext)
        ambientController = AmbientModeSupport.attach(this)

        setContent {
            val isAmbient = ambientController.isAmbient
            WearosolianTheme(isAmbient = isAmbient) {
                WearApp()
            }
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return object : AmbientModeSupport.AmbientCallback() {
            override fun onEnterAmbient(ambientDetails: Bundle?) {
                // Enter ambient mode - reduce updates, disable animations
            }

            override fun onExitAmbient() {
                // Exit ambient mode - restore normal UI
            }

            override fun onUpdateAmbient() {
                // Update ambient UI (e.g., refresh time)
            }
        }
    }
}
