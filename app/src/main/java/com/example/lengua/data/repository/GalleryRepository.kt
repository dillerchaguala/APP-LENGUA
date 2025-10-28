package com.example.lengua.data.repository

import android.content.Context
import android.net.Uri
import com.example.lengua.data.model.CreateMediaRequest
import com.example.lengua.data.model.MediaItem
import com.example.lengua.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class GalleryRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val context: Context
) {

    suspend fun getGalleryItems(): Result<List<MediaItem>> {
        return try {
            val response = apiService.getGalleryItems()
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.success) {
                    Result.Success(response.body()!!.items)
                } else {
                    Result.Error("Error del backend al obtener la galería")
                }
            } else {
                Result.Error("Error de red al obtener la galería: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión al obtener la galería: ${e.message}")
        }
    }

    suspend fun createMediaItem(request: CreateMediaRequest): Result<MediaItem> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay sesión iniciada para crear un ítem")
        return try {
            val response = apiService.createMediaItem("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error de red al crear el ítem: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión al crear el ítem: ${e.message}")
        }
    }

    suspend fun createMediaItemWithFile(
        type: String, title: String, description: String, author: String, category: String,
        fileUri: Uri, thumbnailUri: Uri? = null
    ): Result<MediaItem> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay sesión iniciada para crear un ítem")

        return try {
            // Create RequestBody for all text fields
            val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val authorBody = author.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

            // Create MultipartBody.Part for the main file
            val filePart = context.contentResolver.openInputStream(fileUri)?.use {
                val fileBytes = it.readBytes()
                val requestFile = fileBytes.toRequestBody(context.contentResolver.getType(fileUri)?.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", "file", requestFile)
            } ?: return Result.Error("No se pudo leer el archivo seleccionado")

            // Create MultipartBody.Part for the optional thumbnail
            val thumbnailPart = thumbnailUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use {
                    val thumbBytes = it.readBytes()
                    val requestThumb = thumbBytes.toRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("thumbnail", "thumbnail", requestThumb)
                }
            }
            
            val response = apiService.createMediaItemWithFile(
                token = "Bearer $token",
                type = typeBody,
                title = titleBody,
                description = descriptionBody,
                author = authorBody,
                category = categoryBody,
                file = filePart,
                thumbnail = thumbnailPart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                 Result.Error("Error de red al subir el archivo: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión al subir el archivo: ${e.message}")
        }
    }
}
