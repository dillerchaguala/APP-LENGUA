package com.example.lengua.screen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lengua.data.model.CreateMediaRequest
import com.example.lengua.data.model.MediaItem
import com.example.lengua.data.repository.GalleryRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(private val galleryRepository: GalleryRepository) : ViewModel() {

    // State for the list of gallery items
    private val _galleryState = MutableStateFlow<Result<List<MediaItem>>>(Result.Loading)
    val galleryState: StateFlow<Result<List<MediaItem>>> = _galleryState.asStateFlow()

    // State for the creation of a new item
    private val _createMediaState = MutableStateFlow<Result<MediaItem>?>(null)
    val createMediaState: StateFlow<Result<MediaItem>?> = _createMediaState.asStateFlow()

    init {
        loadGallery()
    }

    fun loadGallery() {
        viewModelScope.launch {
            _galleryState.value = Result.Loading
            _galleryState.value = galleryRepository.getGalleryItems()
        }
    }

    fun createMediaItem(
        type: String,
        title: String,
        description: String,
        category: String,
        author: String,
        url: String? = null,
        thumbnail: String? = null
    ) {
        viewModelScope.launch {
            _createMediaState.value = Result.Loading
            val request = CreateMediaRequest(
                type = type,
                title = title,
                description = description,
                category = category,
                author = author,
                url = url,
                thumbnail = thumbnail
            )
            val result = galleryRepository.createMediaItem(request)
            _createMediaState.value = result

            if (result is Result.Success) {
                loadGallery()
            }
        }
    }

    fun createMediaItemWithFile(
        type: String, title: String, description: String, author: String, category: String,
        fileUri: Uri, thumbnailUri: Uri? = null
    ) {
        viewModelScope.launch {
            _createMediaState.value = Result.Loading
            val result = galleryRepository.createMediaItemWithFile(
                type, title, description, author, category, fileUri, thumbnailUri
            )
            _createMediaState.value = result

            if (result is Result.Success) {
                loadGallery()
            }
        }
    }

    fun resetCreateMediaState() {
        _createMediaState.value = null
    }
}

// --- ViewModel Factory ---
class GalleryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            val apiService = RetrofitInstance.api
            val sessionManager = SessionManager(context.applicationContext)
            // ✅ CONSTRUCTOR CORREGIDO
            val galleryRepository = GalleryRepository(apiService, sessionManager, context.applicationContext)
            return GalleryViewModel(galleryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
