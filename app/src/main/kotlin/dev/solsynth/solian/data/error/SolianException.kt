package dev.solsynth.solian.data.error

import retrofit2.HttpException
import java.io.IOException

sealed class SolianException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause) {

    data class NetworkError(
        override val message: String = "Network error",
        override val cause: Throwable? = null,
    ) : SolianException(message, cause)

    data class HttpError(
        val code: Int,
        override val message: String,
        override val cause: Throwable? = null,
    ) : SolianException(message, cause)

    data class AuthError(
        override val message: String = "Authentication failed",
        override val cause: Throwable? = null,
    ) : SolianException(message, cause)

    data class ParseError(
        override val message: String = "Failed to parse response",
        override val cause: Throwable? = null,
    ) : SolianException(message, cause)

    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null,
    ) : SolianException(message, cause)

    data class TimeoutError(
        override val message: String = "Request timed out",
        override val cause: Throwable? = null,
    ) : SolianException(message, cause)

    data class RateLimitError(
        val retryAfter: Long? = null,
        override val message: String = "Rate limit exceeded",
        override val cause: Throwable? = null,
    ) : SolianException(message, cause)
}
