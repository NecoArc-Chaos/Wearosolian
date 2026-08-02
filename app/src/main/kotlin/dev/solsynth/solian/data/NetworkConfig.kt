package dev.solsynth.solian.data

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.CertificatePinner

/**
 * Shared network configuration for the Solar Network client.
 *
 * Contains certificate pins for official domains. Supports dynamic pin rotation
 * by fetching updated pins from the backend when available.
 */
object NetworkConfig {
    private const val TAG = "NetworkConfig"

    // Production certificate pins extracted from live servers.
    // These serve as fallback pins if dynamic pin fetch fails.
    private const val PIN_SOLIAN_APP = "sha256/ldZj8XVxkaBt4jBzMPobxTovjj5PFLIkBxjl4mGxyF8="
    private const val PIN_API_SOLIAN_APP = "sha256/FCfjouup09On+facLQ3EB57P+RN7EM9VKTcDteEBt4E="

    private val _certificatePinner = MutableStateFlow(createCertificatePinner())
    val certificatePinner: StateFlow<CertificatePinner> = _certificatePinner

    fun getCertificatePinner(): CertificatePinner = _certificatePinner.value

    fun updatePins(pins: Map<String, List<String>>) {
        val builder = CertificatePinner.Builder()
        pins.forEach { (host, pinList) ->
            pinList.forEach { pin ->
                builder.add(host, pin)
            }
        }
        _certificatePinner.value = builder.build()
        Log.i(TAG, "Certificate pins updated for ${pins.keys.joinToString()}")
    }

    fun createCertificatePinner(): CertificatePinner {
        val isPlaceholder = { pin: String ->
            pin.endsWith("PLACEHOLDER_SOLIAN_APP_CERT_HASH") ||
                pin.endsWith("PLACEHOLDER_API_SOLIAN_APP_CERT_HASH")
        }

        val builder = CertificatePinner.Builder()
        if (!isPlaceholder(PIN_SOLIAN_APP)) {
            builder.add("solian.app", PIN_SOLIAN_APP)
        }
        if (!isPlaceholder(PIN_API_SOLIAN_APP)) {
            builder.add("api.solian.app", PIN_API_SOLIAN_APP)
        }

        if (isPlaceholder(PIN_SOLIAN_APP) || isPlaceholder(PIN_API_SOLIAN_APP)) {
            Log.w(TAG, "Certificate pins contain placeholder hashes; pinning is disabled until real hashes are provided.")
        }

        return builder.build()
    }
}
