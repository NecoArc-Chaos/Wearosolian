package dev.solsynth.solian.data

import okhttp3.CertificatePinner

/**
 * Shared network configuration for the Solar Network client.
 *
 * Contains certificate pins for official domains.
 *
 * IMPORTANT: Replace the placeholder SHA-256 hashes with actual production
 * certificate hashes. To extract:
 *
 *   echo | openssl s_client -connect solian.app:443 -servername solian.app 2>/dev/null \
 *     | openssl x509 -pubkey -noout \
 *     | openssl pkey -pubin -outform der \
 *     | openssl dgst -sha256 -binary \
 *     | openssl enc -base64
 */
object NetworkConfig {
    /**
     * Certificate pinner for official Solar Network domains.
     *
     * The official instance uses Let's Encrypt. Pin the intermediate or leaf
     * certificate hash, NOT the root CA, to allow for certificate rotation.
     */
    val certificatePinner: CertificatePinner = CertificatePinner.Builder()
        .add("solian.app", "sha256/PLACEHOLDER_SOLIAN_APP_CERT_HASH")
        .add("nt.solian.app", "sha256/PLACEHOLDER_NT_SOLIAN_APP_CERT_HASH")
        .build()
}
