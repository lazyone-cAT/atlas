@file:OptIn(ExperimentalMaterial3Api::class)

package com.atlas.bookwriter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.bookwriter.data.Storage
import com.atlas.bookwriter.model.Book
import com.atlas.bookwriter.model.Category
import com.atlas.bookwriter.model.Chapter
import com.atlas.bookwriter.model.Entry
import com.atlas.bookwriter.ui.components.*
import com.atlas.bookwriter.ui.theme.*
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    storage: Storage,
    bookId: String,
    editorBackgroundPath: String = "",
    backgroundTheme: com.atlas.bookwriter.model.BackgroundTheme = com.atlas.bookwriter.model.BackgroundTheme.INK,
    onBack: () -> Unit,
    onEditEntry: (String, String?) -> Unit
) {
    var allBooks by remember { mutableStateOf(storage.loadBooks()) }
    var book by remember { mutableStateOf(allBooks[bookId]) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeCategories by remember { mutableStateOf(setOf<String>()) }
    var searchTerm by remember { mutableStateOf("") }
    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        allBooks = storage.loadBooks()
        book = allBooks[bookId]
    }

    val currentBook = book
    if (currentBook == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenBackground(
            hazeState = hazeState,
            editorBackgroundPath = editorBackgroundPath,
            storage = storage,
            base = backgroundTheme.primary,
            glowA = backgroundTheme.tertiary,
            glowB = backgroundTheme.surface,
            glowC = backgroundTheme.secondary,
            glowD = backgroundTheme.tertiary
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Liquid Glass Top Bar ──
            GlassTopBar(
                hazeState = hazeState,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFE0E0E0)
                        )
                    }
                    Text(
                        currentBook.name,
                        color = Color(0xFFE0E0E0),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Cursive,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Liquid Glass Tab Bar ──
            GlassSurface(
                hazeState = hazeState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 12.dp,
                tint = GlassTokens.FrostPrimary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GlassTab(
                        hazeState = hazeState,
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "Wiki",
                                color = if (selectedTab == 0) Color(0xFFC9A77C) else Color(0xFFB0B0B8),
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                    GlassTab(
                        hazeState = hazeState,
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "Story",
                                color = if (selectedTab == 1) Color(0xFFC9A77C) else Color(0xFFB0B0B8),
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FAB for Wiki tab
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> WikiTab(
                        hazeState = hazeState,
                        book = currentBook,
                        activeCategories = activeCategories,
                        onToggleCategory = { catId ->
                            activeCategories = if (catId in activeCategories) activeCategories - catId
                            else activeCategories + catId
                        },
                        searchTerm = searchTerm,
                        onSearchChange = { searchTerm = it },
                        onEntryClick = { entry -> onEditEntry(currentBook.id, entry.id) }
                    )
                    1 -> StoryTab(
                        hazeState = hazeState,
                        book = currentBook,
                        onBookUpdated = { updated ->
                            val newBooks = LinkedHashMap(allBooks)
                            newBooks[updated.id] = updated
                            storage.saveBooks(newBooks)
                            allBooks = newBooks
                            book = updated
                        }
                    )
                }

                // Liquid Glass FAB
                if (selectedTab == 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .clickable { onEditEntry(currentBook.id, null) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (LocalSketchEnabled.current) {
                            SketchRing(
                                modifier = Modifier.size(74.dp),
                                color = Color(0xFFC9A77C),
                                strokeWidth = 1.8.dp
                            )
                        }
                        GlassSurface(
                            hazeState = hazeState,
                            cornerRadius = 28.dp,
                            tint = GlassTokens.FrostPrimary,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFC9A77C),
                                                Color(0xFFC9A77C).copy(alpha = 0.85f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add entry", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WikiTab(
    hazeState: HazeState,
    book: Book,
    activeCategories: Set<String>,
    onToggleCategory: (String) -> Unit,
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    onEntryClick: (Entry) -> Unit
) {
    val filtered = book.entries.filter { entry ->
        (activeCategories.isEmpty() || entry.categoryId in activeCategories) &&
                (searchTerm.isEmpty() || listOf(entry.name, entry.summary, entry.description)
                    .joinToString(" ").contains(searchTerm, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Liquid Glass search field
        GlassSurface(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            cornerRadius = 12.dp,
            tint = GlassTokens.FrostSecondary
        ) {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        "Search entries...",
                        color = Color(0xFFB0B0B8).copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color(0xFFE0E0E0),
                    unfocusedTextColor = Color(0xFFE0E0E0),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedPlaceholderColor = Color(0xFFB0B0B8).copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = Color(0xFFB0B0B8).copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Liquid Glass category chips (horizontally scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Category.entries.forEach { cat ->
                GlassChip(
                    hazeState = hazeState,
                    selected = cat.id in activeCategories,
                    onClick = { onToggleCategory(cat.id) }
                ) {
                    Text(
                        cat.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (cat.id in activeCategories) Color(0xFFC9A77C) else Color(0xFFE0E0E0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Liquid Glass entry grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { entry ->
                LiquidGlassEntryCard(hazeState = hazeState, entry = entry, onClick = { onEntryClick(entry) })
            }
        }
    }
}

@Composable
private fun LiquidGlassEntryCard(hazeState: HazeState, entry: Entry, onClick: () -> Unit) {
    val cat = entry.category()

    GlassSurface(
        hazeState = hazeState,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        cornerRadius = 14.dp,
        tint = GlassTokens.FrostSecondary,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category badge with liquid glass tint
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(cat.color)
                        .border(1.dp, cat.color.copy(alpha = 0.5f), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = cat.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = cat.color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entry name
            Text(
                text = entry.name.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFFE0E0E0)
            )

            // Summary
            if (entry.summary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFB0B0B8)
                )
            }

            // Tags
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    entry.tags.take(3).forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB0B0B8).copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                    if (entry.tags.size > 3) {
                        Text(
                            text = "+${entry.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB0B0B8).copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryTab(
    hazeState: HazeState,
    book: Book,
    onBookUpdated: (Book) -> Unit
) {
    var chapters by remember { mutableStateOf(book.chapters.sortedBy { it.order }.toMutableList()) }
    var selectedChapterId by remember { mutableStateOf<String?>(chapters.firstOrNull()?.id) }
    var showNewChapterDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Chapter?>(null) }
    var deleteTarget by remember { mutableStateOf<Chapter?>(null) }
    val scope = rememberCoroutineScope()

    val selectedChapter = remember(selectedChapterId, chapters) {
        chapters.find { it.id == selectedChapterId }
    }

    LaunchedEffect(chapters.size) {
        if (chapters.isEmpty()) {
            val ch = Chapter(title = "Chapter 1", order = 0)
            chapters = mutableListOf(ch)
            selectedChapterId = ch.id
            book.chapters.clear()
            book.chapters.add(ch)
            onBookUpdated(book)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Liquid Glass Chapter Bar ──
        GlassSurface(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            cornerRadius = 14.dp,
            tint = GlassTokens.FrostPrimary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                chapters.forEach { chapter ->
                    val isSelected = chapter.id == selectedChapterId
                    GlassSurface(
                        hazeState = hazeState,
                        cornerRadius = 10.dp,
                        tint = if (isSelected) GlassTokens.FrostPrimary else GlassTokens.FrostSecondary,
                        modifier = Modifier.clickable { selectedChapterId = chapter.id }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = chapter.title,
                                color = if (isSelected) Color(0xFFC9A77C) else Color(0xFFB0B0B8),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = Color(0xFFC9A77C).copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { renameTarget = chapter }
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFD98868).copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { deleteTarget = chapter }
                                )
                            }
                        }
                    }
                }

                // Add chapter button
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 10.dp,
                    tint = GlassTokens.FrostSecondary,
                    modifier = Modifier.clickable { showNewChapterDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add chapter",
                            tint = Color(0xFFC9A77C),
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Add", color = Color(0xFFC9A77C), fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Liquid Glass Editor ──
        selectedChapter?.let { chapter ->
            ChapterEditor(
                hazeState = hazeState,
                chapter = chapter,
                onContentChanged = { newContent ->
                    chapter.content = newContent
                    chapter.updatedAt = System.currentTimeMillis()
                },
                onWordSaved = { onBookUpdated(book) }
            )
        }
    }

    // ── Liquid Glass Dialogs ──
    if (showNewChapterDialog) {
        var newTitle by remember { mutableStateOf("Chapter ${chapters.size + 1}") }
        LiquidGlassDialog(
            title = "New Chapter",
            onDismiss = { showNewChapterDialog = false },
            onConfirm = {
                if (newTitle.isNotBlank()) {
                    val ch = Chapter(
                        title = newTitle.trim(),
                        order = book.nextChapterOrder()
                    )
                    chapters = (chapters + ch).toMutableList()
                    book.chapters.add(ch)
                    selectedChapterId = ch.id
                    onBookUpdated(book)
                }
                showNewChapterDialog = false
            },
            confirmText = "Create"
        ) {
            LiquidGlassTextField(
                hazeState = hazeState,
                value = newTitle,
                onValueChange = { newTitle = it },
                label = "Chapter title"
            )
        }
    }

    renameTarget?.let { chapter ->
        var renameText by remember { mutableStateOf(chapter.title) }
        LiquidGlassDialog(
            title = "Rename Chapter",
            onDismiss = { renameTarget = null },
            onConfirm = {
                if (renameText.isNotBlank()) {
                    chapter.title = renameText.trim()
                    chapters = chapters.toMutableList()
                    onBookUpdated(book)
                }
                renameTarget = null
            },
            confirmText = "Rename"
        ) {
            LiquidGlassTextField(
                hazeState = hazeState,
                value = renameText,
                onValueChange = { renameText = it },
                label = "Chapter title"
            )
        }
    }

    deleteTarget?.let { chapter ->
        LiquidGlassDialog(
            title = "Delete \"${chapter.title}\"?",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                book.chapters.removeAll { it.id == chapter.id }
                chapters = chapters.filter { it.id != chapter.id }.toMutableList()
                if (selectedChapterId == chapter.id) {
                    selectedChapterId = chapters.firstOrNull()?.id
                }
                onBookUpdated(book)
                deleteTarget = null
            },
            confirmText = "Delete",
            confirmColor = Color(0xFFD98868)
        ) {
            Text(
                "This permanently removes the chapter and its content.",
                color = Color(0xFFB0B0B8),
                fontSize = 14.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// LIQUID GLASS EDITOR
// ═══════════════════════════════════════════════════════

@Composable
private fun ChapterEditor(
    hazeState: HazeState,
    chapter: Chapter,
    onContentChanged: (String) -> Unit,
    onWordSaved: () -> Unit
) {
    var contentText by remember(chapter.id) { mutableStateOf(chapter.content) }
    val scope = rememberCoroutineScope()
    var pendingSaveJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun wordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).size
    }

    val count = wordCount(contentText)

    Column(modifier = Modifier.fillMaxSize()) {
        // Liquid Glass editor container
        GlassSurface(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            cornerRadius = 18.dp,
            tint = GlassTokens.FrostSecondary,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // The actual text field — transparent container inside the glass
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { newText ->
                        contentText = newText
                        onContentChanged(newText)
                        pendingSaveJob?.cancel()
                        pendingSaveJob = scope.launch {
                            delay(500)
                            onWordSaved()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFFE0E0E0),
                        unfocusedTextColor = Color(0xFFE0E0E0),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                // Liquid Glass word count bar
                GlassSurface(
                    hazeState = hazeState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp),
                    cornerRadius = 0.dp,
                    tint = GlassTokens.FrostSecondary
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${chapter.title}  \u00B7  $count ${if (count == 1) "word" else "words"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB0B0B8),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pendingSaveJob?.cancel()
            onContentChanged(contentText)
            onWordSaved()
        }
    }
}

// ═══════════════════════════════════════════════════════
// LIQUID GLASS DIALOG + TEXT FIELD
// ═══════════════════════════════════════════════════════

@Composable
private fun LiquidGlassDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "OK",
    confirmColor: Color = Color(0xFFC9A77C),
    content: @Composable () -> Unit
) {
    SketchAlertDialog(
        onDismissRequest = onDismiss,
        title = title,
        text = { content() },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFB0B0B8))
            }
        }
    )
}

@Composable
private fun LiquidGlassTextField(
    hazeState: HazeState,
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GlassTokens.FrostSecondary)
            .then(
                if (LocalSketchEnabled.current) Modifier.drawWithContent {
                    drawContent()
                    drawSketchStroke(10.dp, seed = 9L)
                } else Modifier.border(1.dp, GlassTokens.BorderColor, RoundedCornerShape(10.dp))
            )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color(0xFFE0E0E0),
                unfocusedTextColor = Color(0xFFE0E0E0),
                cursorColor = Color(0xFFC9A77C),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color(0xFFB0B0B8),
                unfocusedLabelColor = Color(0xFFB0B0B8)
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
