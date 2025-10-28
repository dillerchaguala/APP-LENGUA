package com.example.lengua.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val id: Int,
    val type: String,  // "video" o "image"
    val title: String,
    val description: String,
    val url: String? = null,
    val file: String? = null,
    val thumbnail: String? = null,
    val author: String,
    val category: String,  // "Videos", "Infografías", "Fotos"
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("is_active")
    val isActive: Boolean = true
) {
    /**
     * Obtener URL del contenido (prioriza file sobre url)
     */
    fun getContentUrl(): String? {
        return file ?: url
    }

    /**
     * Obtener URL del thumbnail
     */
    fun getThumbnailUrl(): String? {
        // Si hay thumbnail explícito, usarlo
        if (!thumbnail.isNullOrBlank()) return thumbnail

        // Si es una imagen, usar la URL del contenido
        if (type == "image") {
            return getContentUrl()
        }

        // Si es un video de YouTube, generar thumbnail
        if (type == "video" && url != null && (url.contains("youtube") || url.contains("youtu.be"))) {
            val videoId = extractYouTubeId(url)
            if (videoId != null) {
                return "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
            }
        }

        return null
    }

    private fun extractYouTubeId(url: String): String? {
        val patterns = listOf(
            """(?:youtube\.com/watch\?v=|youtu\.be/)([^&\s]+)""".toRegex(),
            """youtube\.com/embed/([^&\s]+)""".toRegex()
        )

        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        return null
    }
}

@Serializable
data class GalleryResponse(
    val success: Boolean,
    val total: Int,
    val items: List<MediaItem>
)

@Serializable
data class CreateMediaRequest(
    val type: String,
    val title: String,
    val description: String,
    val url: String? = null,
    val thumbnail: String? = null,
    val author: String,
    val category: String
)
