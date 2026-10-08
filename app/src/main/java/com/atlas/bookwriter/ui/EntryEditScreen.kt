@file:OptIn(ExperimentalMaterial3Api::class)

package com.atlas.bookwriter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.bookwriter.data.Storage
import com.atlas.bookwriter.model.*
import com.atlas.bookwriter.ui.components.*
import com.atlas.bookwriter.ui.theme.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditScreen(
    storage: Storage,
    bookId: String,
    entryId: String?,
    editorBackgroundPath: String = "",
    backgroundTheme: com.atlas.bookwriter.model.BackgroundTheme = com.atlas.bookwriter.model.BackgroundTheme.INK,
    onBack: () -> Unit
) {
    var allBooks by remember { mutableStateOf(storage.loadBooks()) }
    val book = remember(allBooks) { allBooks[bookId] }
    if (book == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val isNew = entryId == null
    val existingEntry = if (isNew) null else entryId?.let { book.findEntry(it) }
    if (!isNew && existingEntry == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Refresh data when screen is displayed
    LaunchedEffect(Unit) {
        allBooks = storage.loadBooks()
    }

    var name by remember { mutableStateOf(existingEntry?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(existingEntry?.category() ?: Category.CHARACTERS) }
    var tags by remember { mutableStateOf(existingEntry?.tags?.joinToString(", ") ?: "") }
    var summary by remember { mutableStateOf(existingEntry?.summary ?: "") }
    var description by remember { mutableStateOf(existingEntry?.description ?: "") }
    var fields by remember {
        mutableStateOf(
            existingEntry?.fields?.map { FieldData(it.key, it.value) } ?: emptyList()
        )
    }
    var linkedIds by remember {
        mutableStateOf(
            existingEntry?.links?.toMutableSet() ?: mutableSetOf()
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLinkPicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    val hazeState = remember { HazeState() }

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
            // Liquid Glass top bar
            GlassTopBar(hazeState = hazeState,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFE0E0E0))
                    }
                    Text(
                        if (isNew) "New entry" else "Edit entry",
                        color = Color(0xFFE0E0E0),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Liquid Glass form container
            GlassSurface(hazeState = hazeState,
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name field
                    GlassInputField(
                        hazeState = hazeState,
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        label = "Name",
                        isError = nameError,
                        errorMessage = "Name is required",
                        singleLine = true
                    )

                    // Category chips (horizontally scrollable so all 6 categories stay reachable)
                    Text("Category", style = MaterialTheme.typography.labelLarge, color = Color(0xFFE0E0E0))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Category.entries.forEach { cat ->
                            GlassChip(hazeState = hazeState,
                                selected = cat == selectedCategory,
                                onClick = { selectedCategory = cat }
                            ) {
                                Text(
                                    cat.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cat == selectedCategory) Color(0xFFC9A77C) else Color(0xFFE0E0E0)
                                )
                            }
                        }
                    }

                    // Tags
                    GlassInputField(
                        hazeState = hazeState,
                        value = tags,
                        onValueChange = { tags = it },
                        label = "Tags (comma separated)",
                        singleLine = true
                    )

                    // Summary
                    GlassInputField(
                        hazeState = hazeState,
                        value = summary,
                        onValueChange = { summary = it },
                        label = "Summary"
                    )

                    // Description
                    GlassInputField(
                        hazeState = hazeState,
                        value = description,
                        onValueChange = { description = it },
                        label = "Description",
                        minHeight = 100.dp
                    )

                    // Custom Fields
                    Text("Custom Fields", style = MaterialTheme.typography.labelLarge, color = Color(0xFFE0E0E0))
                    fields.forEachIndexed { index, field ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GlassInputField(
                                hazeState = hazeState,
                                value = field.key,
                                onValueChange = { newKey ->
                                    val currentVal = field.value
                                    fields = fields.toMutableList().apply {
                                        set(index, FieldData(newKey, currentVal))
                                    }
                                },
                                label = "Key",
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            GlassInputField(
                                hazeState = hazeState,
                                value = field.value,
                                onValueChange = { newValue ->
                                    val currentKey = field.key
                                    fields = fields.toMutableList().apply {
                                        set(index, FieldData(currentKey, newValue))
                                    }
                                },
                                label = "Value",
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                fields = fields.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFD98868))
                            }
                        }
                    }
                    TextButton(onClick = { fields = fields + FieldData("", "") }) {
                        Text("+ Add field", color = Color(0xFFC9A77C))
                    }

                    // Links
                    Text("Links", style = MaterialTheme.typography.labelLarge, color = Color(0xFFE0E0E0))
                    linkedIds.forEach { id ->
                        val other = book.findEntry(id) ?: return@forEach
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("${other.name} (${other.category().label})") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove link",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                linkedIds = linkedIds.toMutableSet().apply { remove(id) }
                                            }
                                    )
                                },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = Color(0xFFE0E0E0)
                            )
                            )
                        }
                    }
                    TextButton(onClick = { showLinkPicker = true }) {
                        Text("+ Link entries", color = Color(0xFFC9A77C))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isNew) {
                            GlassSurface(hazeState = hazeState,
                                modifier = Modifier.weight(1f).clickable { showDeleteDialog = true },
                                cornerRadius = 12.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Delete", color = Color(0xFFD98868), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        GlassSurface(hazeState = hazeState,
                            modifier = Modifier.weight(1f).clickable {
                                if (name.isBlank()) {
                                    nameError = true
                                    return@clickable
                                }
                                val entry = existingEntry ?: Entry().also { book.entries.add(it) }
                                entry.name = name.trim()
                                entry.categoryId = selectedCategory.id
                                entry.summary = summary.trim()
                                entry.description = description.trim()
                                entry.tags.clear()
                                entry.tags.addAll(tags.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                entry.fields.clear()
                                entry.fields.addAll(fields.filter { it.key.isNotBlank() || it.value.isNotBlank() }
                                    .map { Field(it.key, it.value) })

                                val oldLinks = entry.links.toSet()
                                entry.links.clear()
                                entry.links.addAll(linkedIds)
                                linkedIds.forEach { id ->
                                    book.findEntry(id)?.let { other ->
                                        if (entry.id !in other.links) other.links.add(entry.id)
                                    }
                                }
                                oldLinks.filter { it !in linkedIds }.forEach { id ->
                                    book.findEntry(id)?.links?.remove(entry.id)
                                }

                                val newBooks = LinkedHashMap(allBooks)
                                newBooks[book.id] = book
                                storage.saveBooks(newBooks)
                                onBack()
                            },
                            cornerRadius = 28.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFC9A77C), Color(0xFFC9A77C).copy(alpha = 0.8f))
                                        ),
                                        RoundedCornerShape(28.dp)
                                    )
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Save",
                                    color = Ink,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Cursive,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        SketchAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Delete \"${existingEntry?.name}\"?",
            text = {
                Text(
                    "This also removes it from any linked entries.",
                    color = Color(0xFFB0B0B8),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    existingEntry?.let { entry ->
                        book.entries.forEach { it.links.remove(entry.id) }
                        book.entries.remove(entry)
                        val newBooks = LinkedHashMap(allBooks)
                        newBooks[book.id] = book
                        storage.saveBooks(newBooks)
                    }
                    onBack()
                }) {
                    Text("Delete", color = Color(0xFFD98868))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0B0B8))
                }
            }
        )
    }

    // Link picker dialog
    if (showLinkPicker) {
        val candidates = book.entries.filter { it.id != entryId }
        if (candidates.isEmpty()) {
            SketchAlertDialog(
                onDismissRequest = { showLinkPicker = false },
                text = {
                    Text(
                        "No other entries in this book yet.",
                        color = Color(0xFFB0B0B8),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showLinkPicker = false }) {
                        Text("OK", color = Color(0xFFC9A77C))
                    }
                }
            )
        } else {
            val working = remember { mutableStateMapOf<String, Boolean>().apply {
                candidates.forEach { this[it.id] = it.id in linkedIds }
            } }

            SketchAlertDialog(
                onDismissRequest = { showLinkPicker = false },
                title = "Link to entries",
                text = {
                    Column {
                        candidates.forEach { candidate ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        working[candidate.id] = !(working[candidate.id] ?: false)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = working[candidate.id] ?: false,
                                    onCheckedChange = { working[candidate.id] = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFC9A77C),
                                        uncheckedColor = Line,
                                        checkmarkColor = Ink
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${candidate.name} (${candidate.category().label})",
                                    color = Color(0xFFE0E0E0)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        linkedIds = working.filter { it.value }.keys.toMutableSet()
                        showLinkPicker = false
                    }) {
                        Text("Done", color = Color(0xFFC9A77C))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLinkPicker = false }) {
                        Text("Cancel", color = Color(0xFFB0B0B8))
                    }
                }
            )
        }
    }
}

private data class FieldData(val key: String, val value: String)

@Composable
private fun GlassInputField(
    hazeState: HazeState,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = "",
    singleLine: Boolean = false,
    minHeight: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    GlassSurface(hazeState = hazeState,
        cornerRadius = 12.dp,
        modifier = modifier.then(
            if (minHeight != androidx.compose.ui.unit.Dp.Unspecified) {
                Modifier.heightIn(min = minHeight)
            } else Modifier
        )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            supportingText = if (isError && errorMessage.isNotEmpty()) {
                { Text(errorMessage, color = Color(0xFFD98868)) }
            } else null,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color(0xFFE0E0E0),
                unfocusedTextColor = Color(0xFFE0E0E0),
                cursorColor = Color(0xFFC9A77C),
                focusedLabelColor = Color(0xFFC9A77C),
                unfocusedLabelColor = Color(0xFFB0B0B8),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorBorderColor = Color(0xFFD98868),
                errorTextColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
