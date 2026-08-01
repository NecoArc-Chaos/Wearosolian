package dev.solsynth.solian.data.error

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {

    fun map(throwable: Throwable): SolianException {
        return when (throwable) {
            is SolianException -> throwable
            is SocketTimeoutException -> SolianException.TimeoutError(cause = throwable)
            is UnknownHostException -> SolianException.NetworkError(cause = throwable)
            is IOException -> SolianException.NetworkError(
                message = throwable.message ?: "Network error",
                cause = throwable,
            )
            is HttpException -> mapHttpException(throwable)
            else -> SolianException.UnknownError(
                message = throwable.message ?: "Unknown error",
                cause = throwable,
            )
        }
    }

    private fun mapHttpException(exception: HttpException): SolianException {
        val code = exception.code()
        val message = extractErrorMessage(exception) ?: exception.message() ?: "HTTP error $code"
        return when (code) {
            401 -> SolianException.AuthError(message = message, cause = exception)
            429 -> SolianException.RateLimitError(message = message, cause = exception)
            else -> SolianException.HttpError(code = code, message = message, cause = exception)
        }
    }

    private fun extractErrorMessage(exception: HttpException): String? {
        return try {
            val body: ResponseBody? = exception.response()?.errorBody()
            val bodyString = body?.string() ?: return null
            val json = JSONObject(bodyString)
            json.optString("message", null)
                ?: json.optString("error", null)
                ?: json.optString("error_message", null)
        } catch (_: Exception) {
            null
        }
    }
}
