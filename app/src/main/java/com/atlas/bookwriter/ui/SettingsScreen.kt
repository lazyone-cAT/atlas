@file:OptIn(ExperimentalMaterial3Api::class)

package com.atlas.bookwriter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.bookwriter.data.Storage
import com.atlas.bookwriter.model.AppFont
import com.atlas.bookwriter.model.AppSettings
import com.atlas.bookwriter.model.BackgroundTheme
import com.atlas.bookwriter.ui.components.*
import com.atlas.bookwriter.ui.theme.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    storage: Storage,
    settings: AppSettings,
    editorBackgroundPath: String = "",
    onSettingsChanged: (AppSettings) -> Unit,
    onBack: () -> Unit,
    onPickPfp: ((String) -> Unit) -> Unit,
    onPickEditorBg: ((String) -> Unit) -> Unit
) {
    var currentSettings by remember { mutableStateOf(settings) }
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        val theme = currentSettings.backgroundTheme
        ScreenBackground(
            hazeState = hazeState,
            editorBackgroundPath = editorBackgroundPath,
            storage = storage,
            base = theme.primary,
            glowA = theme.tertiary,
            glowB = theme.surface,
            glowC = theme.secondary,
            glowD = theme.tertiary
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            GlassTopBar(
                hazeState = hazeState,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFE0E0E0))
                    }
                    Text(
                        "Settings",
                        color = Color(0xFFE0E0E0),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Settings content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Appearance Section (one-tap Light / Dark)
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Appearance",
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Switch between paper light and ink dark",
                            color = Color(0xFFB0B0B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isLight = currentSettings.backgroundTheme == BackgroundTheme.PARCHMENT
                            listOf("Light" to true, "Dark" to false).forEach { (label, light) ->
                                val selected = isLight == light
                                GlassSurface(
                                    hazeState = hazeState,
                                    cornerRadius = 12.dp,
                                    modifier = Modifier.weight(1f).clickable {
                                        currentSettings = currentSettings.copy(
                                            backgroundTheme = if (light) BackgroundTheme.PARCHMENT else BackgroundTheme.INK
                                        )
                                        onSettingsChanged(currentSettings)
                                        storage.saveSettings(currentSettings)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            color = if (selected) Color(0xFFC9A77C) else Color(0xFFE0E0E0),
                                            fontSize = 14.sp,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Profile Picture Section ──
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Profile Picture",
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your avatar shown on the bookshelf",
                            color = Color(0xFFB0B0B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Current PFP preview
                            val pfpFile = remember(currentSettings.profilePicturePath) {
                                storage.loadImage(currentSettings.profilePicturePath)
                            }
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (pfpFile != null) {
                                    coil.compose.AsyncImage(
                                        model = pfpFile,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFB0B0B8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlassSurface(
                                    hazeState = hazeState,
                                    cornerRadius = 10.dp,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        onPickPfp { path ->
                                            val updated = currentSettings.copy(profilePicturePath = path)
                                            currentSettings = updated
                                            storage.saveSettings(updated)
                                            onSettingsChanged(updated)
                                        }
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Choose Photo",
                                            color = Color(0xFFC9A77C),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (currentSettings.profilePicturePath.isNotEmpty()) {
                                    GlassSurface(
                                        hazeState = hazeState,
                                        cornerRadius = 10.dp,
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val updated = currentSettings.copy(profilePicturePath = "")
                                            currentSettings = updated
                                            storage.saveSettings(updated)
                                            onSettingsChanged(updated)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "Remove",
                                                color = Color(0xFFD98868),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Custom Background Photo Section ──
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Background Photo",
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Use your own photo behind the glass on every screen",
                            color = Color(0xFFB0B0B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview of the current background
                        val bgFile = remember(currentSettings.editorBackgroundPath) {
                            storage.loadImage(currentSettings.editorBackgroundPath)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x0DFFFFFF))
                                .border(0.5.dp, GlassTokens.BorderColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bgFile != null) {
                                coil.compose.AsyncImage(
                                    model = bgFile,
                                    contentDescription = "Background photo preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    alpha = 0.8f
                                )
                                // Label overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GlassTokens.DarkBase.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Current Photo",
                                        color = Color(0xFFE0E0E0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Text(
                                    "No photo set",
                                    color = Color(0xFFB0B0B8),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassSurface(
                                hazeState = hazeState,
                                cornerRadius = 10.dp,
                                modifier = Modifier.weight(1f).clickable {
                                    onPickEditorBg { path ->
                                        val updated = currentSettings.copy(editorBackgroundPath = path)
                                        currentSettings = updated
                                        storage.saveSettings(updated)
                                        onSettingsChanged(updated)
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Choose Photo",
                                        color = Color(0xFFC9A77C),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (currentSettings.editorBackgroundPath.isNotEmpty()) {
                                GlassSurface(
                                    hazeState = hazeState,
                                    cornerRadius = 10.dp,
                                    modifier = Modifier.weight(1f).clickable {
                                        val updated = currentSettings.copy(editorBackgroundPath = "")
                                        currentSettings = updated
                                        storage.saveSettings(updated)
                                        onSettingsChanged(updated)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Remove",
                                            color = Color(0xFFD98868),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Background Theme Section
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Background Theme",
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Choose the look of your app",
                            color = Color(0xFFB0B0B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Theme grid
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            BackgroundTheme.entries.chunked(4).forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    row.forEach { theme ->
                                        ThemeColorButton(
                                            theme = theme,
                                            isSelected = currentSettings.backgroundTheme == theme,
                                            onClick = {
                                                currentSettings = currentSettings.copy(backgroundTheme = theme)
                                                onSettingsChanged(currentSettings)
                                                storage.saveSettings(currentSettings)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Fill remaining space if row is incomplete
                                    repeat(4 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Sketch Style Section (hand-drawn chrome)
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                "Hand-drawn Style",
                                color = Color(0xFFE0E0E0),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Wobbly pencil borders, paper texture and doodles",
                                color = Color(0xFFB0B0B8),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = currentSettings.sketchBorders,
                            onCheckedChange = { enabled ->
                                currentSettings = currentSettings.copy(sketchBorders = enabled)
                                onSettingsChanged(currentSettings)
                                storage.saveSettings(currentSettings)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFC9A77C),
                                checkedTrackColor = Color(0xFFC9A77C).copy(alpha = 0.35f),
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = Color(0xFFB0B0B8),
                                uncheckedTrackColor = Color(0x33FFFFFF),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                // Font Section
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Reading & Writing Font",
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Select the font for your content",
                            color = Color(0xFFB0B0B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFont.entries.forEach { font ->
                                FontOption(
                                    font = font,
                                    isSelected = currentSettings.appFont == font,
                                    hazeState = hazeState,
                                    onClick = {
                                        currentSettings = currentSettings.copy(appFont = font)
                                        onSettingsChanged(currentSettings)
                                        storage.saveSettings(currentSettings)
                                    }
                                )
                            }
                        }
                    }
                }

                // Font Size Section
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Font Size",
                                color = Color(0xFFE0E0E0),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${currentSettings.fontSize}sp",
                                color = Color(0xFFC9A77C),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Preview text
                        Text(
                            "The quick brown fox jumps over the lazy dog",
                            color = Color(0xFFE0E0E0),
                            fontSize = currentSettings.fontSize.sp,
                            fontFamily = currentSettings.appFont.fontFamily,
                            lineHeight = (currentSettings.fontSize * 1.5).sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Slider
                        Slider(
                            value = currentSettings.fontSize.toFloat(),
                            onValueChange = { newSize ->
                                currentSettings = currentSettings.copy(fontSize = newSize.toInt())
                            },
                            onValueChangeFinished = {
                                onSettingsChanged(currentSettings)
                                storage.saveSettings(currentSettings)
                            },
                            valueRange = 12f..28f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFC9A77C),
                                activeTrackColor = Color(0xFFC9A77C),
                                inactiveTrackColor = Line
                            )
                        )

                        // Size labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("A", color = Color(0xFFB0B0B8), fontSize = 10.sp)
                            Text("A", color = Color(0xFFB0B0B8), fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ThemeColorButton(
    theme: BackgroundTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(theme.primary, theme.secondary, theme.tertiary)
                    )
                )
                .then(
                    when {
                        LocalSketchEnabled.current && isSelected -> Modifier.drawWithContent {
                            drawContent()
                            drawSketchStroke(12.dp, seed = 21L)
                        }
                        isSelected -> Modifier.border(2.dp, Color(0xFFC9A77C), RoundedCornerShape(12.dp))
                        else -> Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFFC9A77C),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            theme.label.split(" ").last(),
            color = if (isSelected) Color(0xFFC9A77C) else Color(0xFFB0B0B8),
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun FontOption(
    font: AppFont,
    isSelected: Boolean,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    GlassSurface(
        hazeState = hazeState,
        cornerRadius = 12.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Font preview
            Text(
                "Aa",
                color = if (isSelected) Color(0xFFC9A77C) else Color(0xFFE0E0E0),
                fontSize = 22.sp,
                fontFamily = font.fontFamily,
                fontWeight = FontWeight.Bold
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    font.label,
                    color = if (isSelected) Color(0xFFC9A77C) else Color(0xFFE0E0E0),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
                Text(
                    "The quick brown fox",
                    color = Color(0xFFB0B0B8),
                    fontSize = 12.sp,
                    fontFamily = font.fontFamily
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC9A77C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Ink,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
