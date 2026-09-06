package com.example.data.model

data class Channel(
    val id: String,
    val name: String,
    val group: String = "General",
    val logoUrl: String? = null,
    val streamUrl: String,
    val tvgId: String? = null,
    val isFavorite: Boolean = false
) {
    val displayName: String
        get() = name.trim().ifEmpty { "Channel $id" }

    val displayGroup: String
        get() = group.trim().ifEmpty { "General" }
}
