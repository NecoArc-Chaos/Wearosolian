package dev.solsynth.solian

import com.google.gson.Gson
import dev.solsynth.solian.data.model.AuthConstants
import dev.solsynth.solian.data.model.RoomSyncResponse
import dev.solsynth.solian.data.model.SnAccountStatus
import dev.solsynth.solian.data.model.SnCheckInResult
import dev.solsynth.solian.data.model.SnChatSummary
import dev.solsynth.solian.data.model.SnFortuneTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolParsingTest {

    private val gson = Gson()

    private fun checkInJson(): String {
        return """
        {
          "id": "01JEXAMPLE000000000000000001",
          "level": 5,
          "tips": [
            {"is_positive": true, "title": "Lucky", "content": "A good day ahead"}
          ],
          "fortune_report": {
            "version": 2,
            "poem": "长风破浪会有时",
            "summary": "宜出行，忌拖延",
            "summary_detail": "细节",
            "wish": "愿望",
            "love": "桃花",
            "study": "学业",
            "career": "事业",
            "health": "健康",
            "lost_item": "失物",
            "lucky_color": "红",
            "lucky_direction": "东",
            "lucky_time": "辰时",
            "lucky_item": "玉佩",
            "lucky_action": "运动",
            "avoid_action": "熬夜",
            "ritual": "仪式"
          },
          "account_id": "01JACCOUNT000000000000000001",
          "account": {"id": "01JACCOUNT000000000000000001", "nick": "TestUser"},
          "created_at": "2026-08-02T04:18:06.364061Z",
          "updated_at": "2026-08-02T04:18:06.364061Z",
          "deleted_at": null
        }
        """.trimIndent()
    }

    @Test
    fun `check-in success response parses fully`() {
        val result = gson.fromJson(checkInJson(), SnCheckInResult::class.java)
        assertNotNull(result)
        assertEquals(5, result.level)
        assertEquals("TestUser", result.account?.nick)
        val report = result.fortuneReport
        assertNotNull(report)
        assertEquals("长风破浪会有时", report?.poem)
        assertEquals("宜出行，忌拖延", report?.summary)
        assertEquals("红", report?.luckyColor)
        assertEquals(1, result.tips?.size)
        assertEquals("Lucky", result.tips?.get(0)?.title)
    }

    @Test
    fun `chat summary single object parses`() {
        val json = """
        {
          "unread_count": 3,
          "last_message": {"id": "msg1", "content": "hello", "chat_room_id": "room1"}
        }
        """.trimIndent()
        val summary = gson.fromJson(json, SnChatSummary::class.java)
        assertEquals(3, summary.unreadCount)
        assertEquals("room1", summary.lastMessage?.chatRoomId)
    }

    @Test
    fun `account status with int type parses`() {
        val json = """
        {"id": "st1", "type": 1, "label": "Busy", "icon": "🚌", "is_online": true}
        """.trimIndent()
        val status = gson.fromJson(json, SnAccountStatus::class.java)
        assertEquals(1, status.type)
        assertEquals("Busy", status.label)
    }

    @Test
    fun `room sync response parses`() {
        val json = """
        {
          "summaries": {
            "room1": {"unread_count": 2, "last_message": {"id": "m1", "chat_room_id": "room1"}}
          },
          "current_timestamp": 1700000000000,
          "total_count": 1
        }
        """.trimIndent()
        val sync = gson.fromJson(json, RoomSyncResponse::class.java)
        assertNotNull(sync.summaries)
        assertEquals(2, sync.summaries?.get("room1")?.unreadCount)
    }

    @Test
    fun `null primitive field falls back to default instead of crashing`() {
        // The backend may return "type": null. Gson skips writing null into a
        // primitive field, keeping the JVM default instead of throwing.
        val json = """{"id": "st1", "type": null, "label": "Busy"}""".trimIndent()
        val status = gson.fromJson(json, SnAccountStatus::class.java)
        assertEquals(AuthConstants.STATUS_TYPE_NORMAL, status.type)
    }

    @Test
    fun `null tip flag falls back to false instead of crashing`() {
        val json = """{"id": "i1", "is_positive": null, "title": "T", "content": "C"}""".trimIndent()
        val tip = gson.fromJson(json, SnFortuneTip::class.java)
        assertFalse(tip.isPositive)
        assertEquals("T", tip.title)
    }
}
