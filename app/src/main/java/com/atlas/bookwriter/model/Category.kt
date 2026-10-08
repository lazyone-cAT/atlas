package com.atlas.bookwriter.model

import androidx.compose.ui.graphics.Color

enum class Category(val id: String, val label: String, val color: Color) {
    CHARACTERS("characters", "Characters", Color(0xFFC9A24D)),
    LOCATIONS("locations", "Locations", Color(0xFF4F8A7B)),
    FACTIONS("factions", "Factions", Color(0xFFB1543A)),
    ITEMS("items", "Items", Color(0xFF8D7BB0)),
    EVENTS("events", "Events", Color(0xFFD98F4E)),
    CONCEPTS("concepts", "Concepts", Color(0xFF6F93B8));

    companion object {
        fun fromId(id: String): Category = entries.find { it.id == id } ?: CHARACTERS
    }
}
