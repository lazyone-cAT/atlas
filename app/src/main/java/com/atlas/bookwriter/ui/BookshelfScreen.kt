@file:OptIn(ExperimentalMaterial3Api::class)

package com.atlas.bookwriter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.bookwriter.data.Storage
import com.atlas.bookwriter.model.Book
import com.atlas.bookwriter.ui.components.*
import com.atlas.bookwriter.ui.theme.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookshelfScreen(
    storage: Storage,
    settings: com.atlas.bookwriter.model.AppSettings,
    editorBackgroundPath: String = "",
    onSettingsChanged: (com.atlas.bookwriter.model.AppSettings) -> Unit,
    onOpenBook: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onPickPfp: ((String) -> Unit) -> Unit,
    onPickCover: (String, (String) -> Unit) -> Unit
) {
    var currentSettings by remember { mutableStateOf(settings) }
    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        currentSettings = storage.loadSettings()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val theme = settings.backgroundTheme
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
            // ── Top Bar with PFP ──
            GlassTopBar(
                hazeState = hazeState,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val pfpFile = remember(currentSettings.profilePicturePath) {
                            storage.loadImage(currentSettings.profilePicturePath)
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    onPickPfp { path ->
                                        val updated = currentSettings.copy(profilePicturePath = path)
                                        currentSettings = updated
                                        storage.saveSettings(updated)
                                        onSettingsChanged(updated)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (pfpFile != null) {
                                androidx.compose.foundation.Image(
                                    painter = coil.compose.rememberAsyncImagePainter(model = pfpFile),
                                    contentDescription = "Profile picture",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Set profile picture",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "Atlas",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Cursive
                            )
                            if (LocalSketchEnabled.current) {
                                SquiggleUnderline(
                                    width = 58.dp,
                                    height = 8.dp,
                                    color = Color(0xFFC9A77C),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }

                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp).clickable(onClick = onOpenSettings)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Worlds Grid ──
            WriteTab(hazeState = hazeState, storage = storage, onOpenBook = onOpenBook, onPickCover = onPickCover)
        }
    }
}

// ═══════════════════════════════════════════════════════
// WRITE TAB (now the only tab)
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WriteTab(
    hazeState: HazeState,
    storage: Storage,
    onOpenBook: (String) -> Unit,
    onPickCover: (String, (String) -> Unit) -> Unit
) {
    var books by remember { mutableStateOf(loadWriteBooks(storage)) }
    var showNewBookDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Book?>(null) }
    var deleteTarget by remember { mutableStateOf<Book?>(null) }
    var menuTarget by remember { mutableStateOf<Book?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (books.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlassSurface(
                    hazeState = hazeState,
                    cornerRadius = 24.dp,
                    tint = GlassTokens.FrostSecondary
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            tint = Color(0xFFC9A77C),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (LocalSketchEnabled.current) {
                        SketchStar(modifier = Modifier.size(18.dp), color = Color(0xFFC9A77C), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("No worlds yet", color = Color(0xFFE0E0E0), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (LocalSketchEnabled.current) {
                        SketchStar(modifier = Modifier.size(12.dp), color = Color(0xFFB0B0B8), strokeWidth = 1.6.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Tap + to create a new world", color = Color(0xFFB0B0B8), fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(books.values.toList(), key = { it.id }) { book ->
                    WriteBookCard(
                        hazeState = hazeState,
                        storage = storage,
                        book = book,
                        onOpen = { onOpenBook(book.id) },
                        onMenu = { menuTarget = book }
                    )
                }
            }
        }

        // Glass FAB with hand-drawn ring
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
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
                cornerRadius = 16.dp,
                tint = GlassTokens.FrostPrimary.copy(alpha = 0.6f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC9A77C))
                        .clickable { showNewBookDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New world", tint = GlassTokens.DarkBase)
                }
            }
        }
    }

    if (showNewBookDialog) {
        GlassTextInputDialog(
            title = "New world",
            hint = "World name",
            onConfirm = { name ->
                val book = Book(
                    name = name.ifEmpty { "Untitled World" },
                    worldName = name.ifEmpty { "Untitled World" }
                )
                val updated = LinkedHashMap(books)
                updated[book.id] = book
                storage.saveBooks(updated)
                books = updated
                showNewBookDialog = false
            },
            onDismiss = { showNewBookDialog = false }
        )
    }

    renameTarget?.let { book ->
        GlassTextInputDialog(
            title = "Rename world",
            initialText = book.name,
            onConfirm = { name ->
                if (name.isNotEmpty()) {
                    val updatedBook = book.copy(
                        name = name,
                        worldName = name,
                        entries = ArrayList(book.entries),
                        chapters = ArrayList(book.chapters),
                        story = book.story
                    )
                    val updated = LinkedHashMap(books)
                    updated[book.id] = updatedBook
                    storage.saveBooks(updated)
                    books = updated
                }
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { book ->
        SketchAlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = "Delete \"${book.name}\"?",
            text = {
                Text(
                    "This permanently deletes the world and all its entries.",
                    color = Color(0xFFB0B0B8),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val updated = LinkedHashMap(books)
                    updated.remove(book.id)
                    storage.saveBooks(updated)
                    books = updated
                    deleteTarget = null
                }) {
                    Text("Delete", color = Color(0xFFD98868))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = Color(0xFFB0B0B8))
                }
            }
        )
    }

    menuTarget?.let { book ->
        BookOptionsDialog(
            book = book,
            hasCover = book.coverImagePath.isNotEmpty(),
            onOpen = {
                menuTarget = null
                onOpenBook(book.id)
            },
            onChangeCover = {
                menuTarget = null
                onPickCover(book.id) { path ->
                    book.coverImagePath = path
                    storage.saveBooks(storage.loadBooks().also { it[book.id] = book })
                    books = loadWriteBooks(storage)
                }
            },
            onRename = {
                menuTarget = null
                renameTarget = book
            },
            onDelete = {
                menuTarget = null
                deleteTarget = book
            },
            onDismiss = { menuTarget = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WriteBookCard(
    hazeState: HazeState,
    storage: Storage,
    book: Book,
    onOpen: () -> Unit,
    onMenu: () -> Unit
) {
    val entryCount = book.entries.size
    val charCount = book.entries.count { it.categoryId == "characters" }
    val coverFile = remember(book.coverImagePath) { storage.loadImage(book.coverImagePath) }

    Box(modifier = Modifier.fillMaxWidth()) {
        GlassSurface(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = onMenu
                ),
            cornerRadius = 16.dp,
            tint = GlassTokens.FrostPrimary
        ) {
        // Full-bleed cover with text written over it
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
        ) {
            if (coverFile != null) {
                coil.compose.AsyncImage(
                    model = coverFile,
                    contentDescription = "Book cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                // Glass placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        tint = Color(0xFFB0B0B8).copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            // Bottom scrim so the letters read over any cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GlassTokens.DarkBase.copy(alpha = 0.55f),
                                GlassTokens.DarkBase.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            // Letters written over the cover
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = book.name.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFFFFFFF)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$entryCount ${if (entryCount == 1) "entry" else "entries"} \u00B7 $charCount characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD8D8DE)
                )
            }
        }
        }

        // Scrapbook tape corners, overhanging the card edges
        if (LocalSketchEnabled.current) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-5).dp, y = 12.dp)
                    .width(50.dp)
                    .height(15.dp)
                    .rotate(-20f)
                    .background(GlassTokens.TapeColor)
                    .border(0.5.dp, Color(0x22000000), RoundedCornerShape(1.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = 12.dp)
                    .width(50.dp)
                    .height(15.dp)
                    .rotate(18f)
                    .background(GlassTokens.TapeColor)
                    .border(0.5.dp, Color(0x22000000), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
private fun BookOptionsDialog(
    book: Book,
    hasCover: Boolean,
    onOpen: () -> Unit,
    onChangeCover: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    SketchAlertDialog(
        onDismissRequest = onDismiss,
        title = book.name.ifEmpty { "Untitled" },
        text = {
            Column {
                BookOptionRow(
                    icon = Icons.Default.OpenInNew,
                    label = "Open book",
                    onClick = onOpen
                )
                BookOptionRow(
                    icon = Icons.Default.Image,
                    label = if (hasCover) "Change cover" else "Add cover",
                    onClick = onChangeCover
                )
                BookOptionRow(
                    icon = Icons.Default.Edit,
                    label = "Rename",
                    onClick = onRename
                )
                BookOptionRow(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    labelColor = Color(0xFFD98868),
                    onClick = onDelete
                )
            }
        }
    )
}

@Composable
private fun BookOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    labelColor: Color = Color(0xFFE0E0E0),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFC9A77C), modifier = Modifier.size(22.dp))
        Text(label, color = labelColor, fontSize = 15.sp)
    }
}

// ═══════════════════════════════════════════════════════
// SHARED
// ═══════════════════════════════════════════════════════

@Composable
private fun GlassTextInputDialog(
    title: String,
    hint: String = "",
    initialText: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    SketchAlertDialog(
        onDismissRequest = onDismiss,
        title = title,
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(hint) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC9A77C),
                    unfocusedBorderColor = GlassTokens.BorderColor,
                    focusedTextColor = Color(0xFFE0E0E0),
                    unfocusedTextColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFFC9A77C),
                    focusedPlaceholderColor = Color(0xFFB0B0B8),
                    unfocusedPlaceholderColor = Color(0xFFB0B0B8),
                    focusedContainerColor = GlassTokens.FrostPrimary,
                    unfocusedContainerColor = GlassTokens.FrostSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Create", color = Color(0xFFC9A77C))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFB0B0B8))
            }
        }
    )
}

private fun loadWriteBooks(storage: Storage): LinkedHashMap<String, Book> {
    val books = storage.loadBooks()
    if (books.isEmpty()) {
        val sample = storage.buildSampleBook()
        books[sample.id] = sample
        storage.saveBooks(books)
        storage.setCurrentBookId(sample.id)
    }
    return books
}
