package dev.solsynth.solian.data

import okhttp3.CertificatePinner

/**
 * Shared network configuration for the Solar Network client.
 *
 * IMPORTANT: Certificate pinning is currently disabled because the placeholder
 * hashes cause all HTTPS connections to fail. To enable pinning:
 *
 * 1. Extract the real SHA-256 SPKI hash from the production server:
 *
 *    echo | openssl s_client -connect solian.app:443 -servername solian.app 2>/dev/null \
 *      | openssl x509 -pubkey -noout \
 *      | openssl pkey -pubin -outform der \
 *      | openssl dgst -sha256 -binary \
 *      | openssl enc -base64
 *
 * 2. Uncomment and replace the placeholders in [certificatePinner] below.
 * 3. Re-enable pinning in network_security_config.xml.
 */
object NetworkConfig {
    /**
     * Certificate pinner for official Solar Network domains.
     *
     * Currently disabled (empty builder) because the placeholder hashes
     * break connectivity. Replace with real hashes before release.
     */
    val certificatePinner: CertificatePinner = CertificatePinner.Builder()
        // .add("solian.app", "sha256/REAL_HASH_HERE")
        // .add("nt.solian.app", "sha256/REAL_HASH_HERE")
        .build()
}
