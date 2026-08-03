package dev.solsynth.solian.data.api

import android.content.Context
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.model.SnAttachment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class ApiClientResolveUrlTest {

    @Before
    fun setUp() {
        val prefs = RuntimeEnvironment.getApplication()
            .getSharedPreferences("solian_auth_url_test", Context.MODE_PRIVATE)
        val field = TokenStore::class.java.getDeclaredField("prefs")
        field.isAccessible = true
        field.set(null, prefs)
        TokenStore.serverUrl = "https://api.solian.app"
    }

    @Test
    fun `null url and null attachment returns null`() {
        assertNull(ApiClient.resolveUrl(null, null))
    }

    @Test
    fun `null url with attachment id builds drive url`() {
        val attachment = SnAttachment(
            id = "01HQZ3VTJ6Y3Z6Y3Z6Y3Z6Y3Z6Y",
            type = 0,
            url = null,
            previewUrl = null,
        )
        assertEquals(
            "https://api.solian.app/drive/files/01HQZ3VTJ6Y3Z6Y3Z6Y3Z6Y3Z6Y",
            ApiClient.resolveUrl(null, attachment),
        )
    }

    @Test
    fun `null url with attachment url uses attachment url`() {
        val attachment = SnAttachment(
            id = "id",
            type = 0,
            url = "https://cdn.example.com/a.png",
            previewUrl = "https://cdn.example.com/a.png",
        )
        assertEquals("https://cdn.example.com/a.png", ApiClient.resolveUrl(null, attachment))
    }

    @Test
    fun `absolute http url returned unchanged`() {
        assertEquals(
            "https://cdn.example.com/a.png",
            ApiClient.resolveUrl("https://cdn.example.com/a.png"),
        )
    }

    @Test
    fun `bare id without slash or dot maps to drive endpoint`() {
        assertEquals(
            "https://api.solian.app/drive/files/abc123",
            ApiClient.resolveUrl("abc123"),
        )
    }

    @Test
    fun `leading slash url joined to base`() {
        assertEquals(
            "https://api.solian.app/assets/logo.png",
            ApiClient.resolveUrl("/assets/logo.png"),
        )
    }

    @Test
    fun `relative path joined with slash`() {
        assertEquals(
            "https://api.solian.app/files/a.png",
            ApiClient.resolveUrl("files/a.png"),
        )
    }

    @Test
    fun `relative path with query preserved`() {
        assertEquals(
            "https://api.solian.app/files/a.png?v=1",
            ApiClient.resolveUrl("files/a.png?v=1"),
        )
    }

    @Test
    fun `custom server base is honored`() {
        TokenStore.serverUrl = "https://instance.example.com/"
        assertEquals(
            "https://instance.example.com/drive/files/xyz",
            ApiClient.resolveUrl("xyz"),
        )
    }
}
