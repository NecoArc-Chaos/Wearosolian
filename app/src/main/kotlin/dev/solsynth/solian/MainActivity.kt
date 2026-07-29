package dev.solsynth.solian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.wear.ambient.AmbientLifecycleObserver
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.theme.WearosolianTheme

class MainActivity : ComponentActivity(), LifecycleEventObserver {

    private var ambientObserver: AmbientLifecycleObserver? = null
    private var isAmbient by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenStore.init(applicationContext)

        ambientObserver = AmbientLifecycleObserver(
            activity = this,
            callbacks = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
                override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
                    isAmbient = true
                }

                override fun onExitAmbient() {
                    isAmbient = false
                }

                override fun onUpdateAmbient() {
                    // Update ambient UI (e.g., refresh time)
                }
            },
        )

        lifecycle.addObserver(this)

        setContent {
            WearosolianTheme(isAmbient = isAmbient) {
                WearApp()
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            ambientObserver?.let { lifecycle.addObserver(it) }
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            ambientObserver?.let { lifecycle.removeObserver(it) }
        }
    }
}
