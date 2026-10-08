package com.atlas.bookwriter.model

import org.json.JSONObject
import java.util.UUID

data class Chapter(
    val id: String = UUID.randomUUID().toString().substring(0, 8),
    var title: String = "Untitled Chapter",
    var content: String = "",
    var order: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("content", content)
        put("order", order)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    companion object {
        fun fromJson(o: JSONObject): Chapter = Chapter(
            id = o.optString("id", UUID.randomUUID().toString().substring(0, 8)),
            title = o.optString("title", "Untitled Chapter"),
            content = o.optString("content", ""),
            order = o.optInt("order", 0),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
        )
    }
}
