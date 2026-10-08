package com.atlas.bookwriter.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Book(
    val id: String = UUID.randomUUID().toString().substring(0, 8),
    var name: String = "Untitled World",
    var worldName: String = "Untitled World",
    var coverImagePath: String = "",
    var story: String = "",
    val entries: MutableList<Entry> = mutableListOf(),
    val chapters: MutableList<Chapter> = mutableListOf()
) {
    fun findEntry(entryId: String): Entry? = entries.find { it.id == entryId }

    fun findChapter(chapterId: String): Chapter? = chapters.find { it.id == chapterId }

    fun nextChapterOrder(): Int = if (chapters.isEmpty()) 0 else chapters.maxOf { it.order } + 1

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("worldName", worldName)
        put("coverImagePath", coverImagePath)
        put("story", story)
        put("entries", JSONArray().apply {
            entries.forEach { put(it.toJson()) }
        })
        put("chapters", JSONArray().apply {
            chapters.sortedBy { it.order }.forEach { put(it.toJson()) }
        })
    }

    companion object {
        fun fromJson(o: JSONObject): Book = Book(
            id = o.optString("id", UUID.randomUUID().toString().substring(0, 8)),
            name = o.optString("name", "Untitled World"),
            worldName = o.optString("worldName", "Untitled World"),
            coverImagePath = o.optString("coverImagePath", ""),
            story = o.optString("story", "")
        ).apply {
            o.optJSONArray("entries")?.let { arr ->
                for (i in 0 until arr.length()) {
                    entries.add(Entry.fromJson(arr.getJSONObject(i)))
                }
            }
            o.optJSONArray("chapters")?.let { arr ->
                for (i in 0 until arr.length()) {
                    chapters.add(Chapter.fromJson(arr.getJSONObject(i)))
                }
            }
        }
    }
}
