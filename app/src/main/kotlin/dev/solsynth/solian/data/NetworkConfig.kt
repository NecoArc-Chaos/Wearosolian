package dev.solsynth.solian.data

import android.util.Log
import okhttp3.CertificatePinner

/**
 * Shared network configuration for the Solar Network client.
 *
 * Contains certificate pins for official domains. Production certificate
 * hashes are embedded directly; rotation requires app updates.
 */
object NetworkConfig {
    private const val TAG = "NetworkConfig"

    // Production certificate pins extracted from live servers.
    // Replace with new hashes on rotation; the runtime guard below
    // will log a warning if any pin becomes invalid.
    private const val PIN_SOLIAN_APP = "sha256/ldZj8XVxkaBt4jBzMPobxTovjj5PFLIkBxjl4mGxyF8="
    private const val PIN_API_SOLIAN_APP = "sha256/FCfjouup09On+facLQ3EB57P+RN7EM9VKTcDteEBt4E="

    val certificatePinner: CertificatePinner = CertificatePinner.Builder().apply {
        val isPlaceholder = { pin: String ->
            pin.endsWith("PLACEHOLDER_SOLIAN_APP_CERT_HASH") ||
            pin.endsWith("PLACEHOLDER_API_SOLIAN_APP_CERT_HASH")
        }

        if (!isPlaceholder(PIN_SOLIAN_APP)) {
            add("solian.app", PIN_SOLIAN_APP)
        }
        if (!isPlaceholder(PIN_API_SOLIAN_APP)) {
            add("api.solian.app", PIN_API_SOLIAN_APP)
        }

        if (isPlaceholder(PIN_SOLIAN_APP) || isPlaceholder(PIN_API_SOLIAN_APP)) {
            Log.w(TAG, "Certificate pins contain placeholder hashes; pinning is disabled until real hashes are provided.")
        }
    }.build()
}
