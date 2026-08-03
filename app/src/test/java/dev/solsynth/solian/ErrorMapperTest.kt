package dev.solsynth.solian.data.error

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class ErrorMapperTest {

    @Test
    fun `maps IOException to NetworkError`() {
        val result = ErrorMapper.map(IOException("boom"))
        assertTrue(result is SolianException.NetworkError)
    }

    @Test
    fun `maps SocketTimeoutException to TimeoutError`() {
        val result = ErrorMapper.map(SocketTimeoutException("slow"))
        assertTrue(result is SolianException.TimeoutError)
    }

    @Test
    fun `maps UnknownHostException to NetworkError`() {
        val result = ErrorMapper.map(UnknownHostException("nope"))
        assertTrue(result is SolianException.NetworkError)
    }

    @Test
    fun `maps 401 to AuthError with server message`() {
        val result = ErrorMapper.map(httpException(401, """{"message":"Bad credentials"}"""))
        assertTrue(result is SolianException.AuthError)
        assertEquals("Bad credentials", result.message)
    }

    @Test
    fun `maps 429 to RateLimitError`() {
        val result = ErrorMapper.map(httpException(429, """{"message":"Too many requests"}"""))
        assertTrue(result is SolianException.RateLimitError)
    }

    @Test
    fun `maps 500 to HttpError with code`() {
        val result = ErrorMapper.map(httpException(500, """{"error":"internal"}"""))
        assertTrue(result is SolianException.HttpError)
        assertEquals(500, (result as SolianException.HttpError).code)
    }

    @Test
    fun `maps HttpException without parseable body to generic HttpError`() {
        val result = ErrorMapper.map(httpException(503, ""))
        assertTrue(result is SolianException.HttpError)
        assertEquals(503, (result as SolianException.HttpError).code)
    }

    @Test
    fun `maps unknown exception to UnknownError`() {
        val result = ErrorMapper.map(IllegalStateException("weird"))
        assertTrue(result is SolianException.UnknownError)
    }

    @Test
    fun `returns existing SolianException unchanged`() {
        val original = SolianException.AuthError(message = "keep me")
        val result = ErrorMapper.map(original)
        assertEquals(original, result)
    }

    @Test
    fun `maps empty body gracefully without crashing`() {
        val result = ErrorMapper.map(httpException(400, null))
        assertNotNull(result)
        assertTrue(result is SolianException.HttpError)
    }

    private fun httpException(code: Int, jsonBody: String?): HttpException {
        val body = (jsonBody ?: "").toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }
}
