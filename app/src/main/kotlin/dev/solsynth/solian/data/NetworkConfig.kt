package dev.solsynth.solian.data

import android.util.Log
import okhttp3.CertificatePinner

/**
 * Shared network configuration for the Solar Network client.
 *
 * IMPORTANT: Certificate pinning is currently disabled because the placeholder
 * hashes cause all HTTPS connections to fail. To enable pinning:
 *
 * 2. Uncomment and replace the placeholders in [certificatePinner] below.
 * 3. Re-enable pinning in network_security_config.xml.
 */
object NetworkConfig {
    private const val TAG = "NetworkConfig"

    // TODO(#NNN): Replace these placeholder hashes with real production certificate
    // SHA-256 hashes before the next release.
    private const val PIN_SOLIAN_APP = "sha256/PLACEHOLDER_SOLIAN_APP_CERT_HASH"
    private const val PIN_NT_SOLIAN_APP = "sha256/PLACEHOLDER_NT_SOLIAN_APP_CERT_HASH"

    val certificatePinner: CertificatePinner = CertificatePinner.Builder().apply {
        val isPlaceholder = { pin: String ->
            pin.endsWith("PLACEHOLDER_SOLIAN_APP_CERT_HASH") ||
                    pin.endsWith("PLACEHOLDER_NT_SOLIAN_APP_CERT_HASH")
        }

        if (!isPlaceholder(PIN_SOLIAN_APP)) {
            add("solian.app", PIN_SOLIAN_APP)
        }
        if (!isPlaceholder(PIN_NT_SOLIAN_APP)) {
            add("nt.solian.app", PIN_NT_SOLIAN_APP)
        }

        if (isPlaceholder(PIN_SOLIAN_APP) || isPlaceholder(PIN_NT_SOLIAN_APP)) {
            Log.w(TAG, "Certificate pins contain placeholder hashes; pinning is disabled until real hashes are provided.")
        }
    }.build()
}
