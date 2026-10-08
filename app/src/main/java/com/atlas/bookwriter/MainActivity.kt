package com.atlas.bookwriter

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.atlas.bookwriter.data.Storage
import com.atlas.bookwriter.model.AppSettings
import com.atlas.bookwriter.ui.components.LocalSketchEnabled
import com.atlas.bookwriter.ui.navigation.AtlasNavGraph
import com.atlas.bookwriter.ui.theme.AtlasTheme

class MainActivity : ComponentActivity() {

    private var storage: Storage? = null

    private var onPfpSaved: ((String) -> Unit)? = null
    private var onEditorBgSaved: ((String) -> Unit)? = null
    private var onCoverSaved: ((String) -> Unit)? = null

    private val pfpPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val currentStorage = storage ?: return@registerForActivityResult
            val path = currentStorage.saveImage(uri, applicationContext, prefix = "pfp")
            if (path.isNotEmpty()) {
                onPfpSaved?.invoke(path)
            }
        }
    }

    private val editorBgPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val currentStorage = storage ?: return@registerForActivityResult
            val path = currentStorage.saveImage(uri, applicationContext, prefix = "editor_bg")
            if (path.isNotEmpty()) {
                onEditorBgSaved?.invoke(path)
            }
        }
    }

    private val coverPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val currentStorage = storage ?: return@registerForActivityResult
            val path = currentStorage.saveImage(uri, applicationContext, prefix = "cover")
            if (path.isNotEmpty()) {
                onCoverSaved?.invoke(path)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        storage = Storage(this)

        setContent {
            val currentStorage = storage ?: return@setContent
            var settings by remember { mutableStateOf(currentStorage.loadSettings()) }

            AtlasTheme(settings = settings) {
                CompositionLocalProvider(LocalSketchEnabled provides settings.sketchBorders) {
                    AtlasNavGraph(
                        storage = currentStorage,
                        settings = settings,
                        onSettingsChanged = { newSettings ->
                            settings = newSettings
                        },
                        onPickPfp = { onSaved ->
                            onPfpSaved = onSaved
                            pfpPickerLauncher.launch(arrayOf("image/*"))
                        },
                        onPickEditorBg = { onSaved ->
                            onEditorBgSaved = onSaved
                            editorBgPickerLauncher.launch(arrayOf("image/*"))
                        },
                        onPickCover = { bookId, onSaved ->
                            onCoverSaved = onSaved
                            coverPickerLauncher.launch(arrayOf("image/*"))
                        }
                    )
                }
            }
        }
    }
}
