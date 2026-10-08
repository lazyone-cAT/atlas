package com.atlas.bookwriter.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.atlas.bookwriter.model.AppSettings
import com.atlas.bookwriter.model.BackgroundTheme
import com.atlas.bookwriter.model.AppFont
import com.atlas.bookwriter.model.Book
import com.atlas.bookwriter.model.Entry
import com.atlas.bookwriter.model.Field
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class Storage(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val imagesDir = File(appContext.filesDir, "images").also { it.mkdirs() }

    // ── Write Books (world-building) ──

    fun loadBooks(): LinkedHashMap<String, Book> {
        val books = LinkedHashMap<String, Book>()
        val raw = prefs.getString(KEY_BOOKS, null) ?: return books
        try {
            val root = JSONObject(raw)
            val keys = root.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                books[id] = Book.fromJson(root.getJSONObject(id))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load books", e)
        }
        return books
    }

    fun saveBooks(books: Map<String, Book>) {
        try {
            val root = JSONObject()
            for ((id, book) in books) {
                root.put(id, book.toJson())
            }
            prefs.edit().putString(KEY_BOOKS, root.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save books", e)
        }
    }

    fun getCurrentBookId(): String? = prefs.getString(KEY_CURRENT_BOOK, null)

    fun setCurrentBookId(id: String) {
        prefs.edit().putString(KEY_CURRENT_BOOK, id).apply()
    }

    fun buildSampleBook(): Book {
        val b = Book(name = "My First Story", worldName = "Untitled World").apply {
            story = "Vesna Cael had not set foot in Ambergate in three years, " +
                    "and the harbor still glowed the same faint amber it always had."
        }

        val vesna = Entry(name = "Vesna Cael", categoryId = "characters").apply {
            tags.addAll(listOf("exiled", "cartographer"))
            summary = "The exiled royal cartographer who redrew the map to hide her home."
            description = "Once the crown cartographer of the Salt Court, Vesna was cast out " +
                    "for refusing to map the road that led invaders to her village."
            fields.addAll(listOf(Field("Age", "34"), Field("Home", "Ambergate")))
        }

        val ambergate = Entry(name = "Ambergate", categoryId = "locations").apply {
            tags.addAll(listOf("port", "free city"))
            summary = "A lawless port city built on smuggled amber and older secrets."
            description = "Ambergate answers to no crown. Its harbor glows faintly at " +
                    "night from amber deposits in the cliffs."
            fields.add(Field("Population", "~40,000"))
            links.add(vesna.id)
        }

        vesna.links.add(ambergate.id)
        b.entries.addAll(listOf(vesna, ambergate))
        return b
    }

    // ── App Settings ──

    fun loadSettings(): AppSettings {
        return AppSettings(
            backgroundTheme = try {
                BackgroundTheme.valueOf(prefs.getString(KEY_BG_THEME, null) ?: "INK")
            } catch (_: Exception) { BackgroundTheme.INK },
            appFont = try {
                AppFont.valueOf(prefs.getString(KEY_FONT, null) ?: "CURSIVE")
            } catch (_: Exception) { AppFont.CURSIVE },
            fontSize = prefs.getInt(KEY_FONT_SIZE, 16),
            profilePicturePath = prefs.getString(KEY_PFP_PATH, "") ?: "",
            editorBackgroundPath = prefs.getString(KEY_EDITOR_BG_PATH, "") ?: "",
            sketchBorders = prefs.getBoolean(KEY_SKETCH, true)
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_BG_THEME, settings.backgroundTheme.name)
            .putString(KEY_FONT, settings.appFont.name)
            .putInt(KEY_FONT_SIZE, settings.fontSize)
            .putString(KEY_PFP_PATH, settings.profilePicturePath)
            .putString(KEY_EDITOR_BG_PATH, settings.editorBackgroundPath)
            .putBoolean(KEY_SKETCH, settings.sketchBorders)
            .apply()
    }

    /** Copy an image from a content URI into internal storage, return the saved path. */
    fun saveImage(uri: android.net.Uri, context: Context, prefix: String = "img"): String {
        val sanitizedName = "${prefix}_${System.currentTimeMillis()}.jpg"
        val dest = File(imagesDir, sanitizedName)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image: $sanitizedName", e)
            dest.delete()
            return ""
        }
        return dest.absolutePath
    }

    fun loadImage(path: String): java.io.File? {
        if (path.isEmpty()) return null
        val f = File(path)
        return if (f.exists()) f else null
    }

    companion object {
        private const val TAG = "Storage"
        private const val PREFS_NAME = "atlas_prefs"
        private const val KEY_BOOKS = "atlas_books_json"
        private const val KEY_CURRENT_BOOK = "atlas_current_book_id"
        private const val KEY_BG_THEME = "atlas_bg_theme"
        private const val KEY_FONT = "atlas_font"
        private const val KEY_FONT_SIZE = "atlas_font_size"
        private const val KEY_PFP_PATH = "atlas_pfp_path"
        private const val KEY_EDITOR_BG_PATH = "atlas_editor_bg_path"
        private const val KEY_SKETCH = "atlas_sketch_borders"
    }
}
