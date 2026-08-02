package dev.solsynth.solian.data.ws

import org.json.JSONObject

import dev.solsynth.solian.data.model.SnChatMessage

sealed class ChatWsEvent {
    data class NewMessage(val message: SnChatMessage) : ChatWsEvent()
    data class UpdateMessage(val messageId: String, val roomId: String?, val content: String?) : ChatWsEvent()
    data class DeleteMessage(val messageId: String, val roomId: String?) : ChatWsEvent()
    data class Delivered(val message: SnChatMessage) : ChatWsEvent()
    data class Typing(val roomId: String, val type: String = "typing") : ChatWsEvent()
    data class ReactionUpdated(
        val messageId: String,
        val symbol: String?,
        val reactionsCount: Map<String, Int>?,
    ) : ChatWsEvent()
}
