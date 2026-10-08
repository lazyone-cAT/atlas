package com.atlas.bookwriter.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Entry(
    val id: String = UUID.randomUUID().toString().substring(0, 8),
    var name: String = "",
    var categoryId: String = "characters",
    val tags: MutableList<String> = mutableListOf(),
    var summary: String = "",
    var description: String = "",
    val fields: MutableList<Field> = mutableListOf(),
    val links: MutableList<String> = mutableListOf()
) {
    fun category(): Category = Category.fromId(categoryId)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", categoryId)
        put("summary", summary)
        put("description", description)
        put("tags", JSONArray(tags))
        put("fields", JSONArray().apply {
            fields.forEach { f ->
                put(JSONObject().apply {
                    put("k", f.key)
                    put("v", f.value)
                })
            }
        })
        put("links", JSONArray(links))
    }

    companion object {
        fun fromJson(o: JSONObject): Entry = Entry(
            id = o.optString("id", UUID.randomUUID().toString().substring(0, 8)),
            name = o.optString("name", ""),
            categoryId = o.optString("category", "characters"),
            summary = o.optString("summary", ""),
            description = o.optString("description", "")
        ).apply {
            o.optJSONArray("tags")?.let { arr ->
                for (i in 0 until arr.length()) tags.add(arr.getString(i))
            }
            o.optJSONArray("fields")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val fo = arr.getJSONObject(i)
                    fields.add(Field(fo.optString("k", ""), fo.optString("v", "")))
                }
            }
            o.optJSONArray("links")?.let { arr ->
                for (i in 0 until arr.length()) links.add(arr.getString(i))
            }
        }
    }
}
