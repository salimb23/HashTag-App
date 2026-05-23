package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.ParsedHashtagsResponse
import com.example.data.db.SavedHashtagSet
import com.example.data.repository.HashtagRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GenerationUiState {
    object Initial : GenerationUiState
    object Loading : GenerationUiState
    data class Success(val data: ParsedHashtagsResponse) : GenerationUiState
    data class Error(val errorMessage: String) : GenerationUiState
}

class HashtagViewModel(private val repository: HashtagRepository) : ViewModel() {

    // Input States
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _selectedPlatform = MutableStateFlow("Instagram")
    val selectedPlatform: StateFlow<String> = _selectedPlatform.asStateFlow()

    // Generation States
    private val _generationUiState = MutableStateFlow<GenerationUiState>(GenerationUiState.Initial)
    val generationUiState: StateFlow<GenerationUiState> = _generationUiState.asStateFlow()

    // Saved Hashtags List observed reactively from DB
    val savedHashtags: StateFlow<List<SavedHashtagSet>> = repository.allSavedHashtags
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI Events (e.g. for Toast messages)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun updateDescription(value: String) {
        _description.value = value
    }

    fun updatePlatform(value: String) {
        _selectedPlatform.value = value
    }

    /**
     * Triggers AI / Local hashtag generation
     */
    fun generateTags() {
        val desc = _description.value.trim()
        if (desc.isEmpty()) {
            viewModelScope.launch {
                _toastEvent.emit("الرجاء إدخال فكرة أو وصف أولاً!")
            }
            return
        }

        _generationUiState.value = GenerationUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.generateHashtags(desc, _selectedPlatform.value)
                _generationUiState.value = GenerationUiState.Success(result)
            } catch (e: Exception) {
                _generationUiState.value = GenerationUiState.Error(e.localizedMessage ?: "حدث خطأ غير متوقع")
            }
        }
    }

    /**
     * Saves currently generated hashtag set to Room
     */
    fun saveCurrentSet(title: String, data: ParsedHashtagsResponse) {
        viewModelScope.launch {
            try {
                val hashtagString = data.hashtags.joinToString(" ")
                repository.saveHashtagSet(
                    title = title,
                    platform = data.platform,
                    hashtags = hashtagString,
                    category = data.category,
                    reachScore = data.reachScore,
                    tips = data.tips
                )
                _toastEvent.emit("تم حفظ الهاشتاجات بنجاح في المفضلة ⭐")
            } catch (e: Exception) {
                _toastEvent.emit("فشل الحفظ: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Deletes a saved hashtag set
     */
    fun deleteSavedSet(set: SavedHashtagSet) {
        viewModelScope.launch {
            try {
                repository.deleteHashtagSet(set)
                _toastEvent.emit("تم حذف الهاشتاجات المحفوظة 🗑️")
            } catch (e: Exception) {
                _toastEvent.emit("فشل الحذف: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Selects a quick suggest search category of hashtags
     */
    fun selectQuickCategory(topic: String) {
        _description.value = topic
        generateTags()
    }

    fun resetState() {
        _generationUiState.value = GenerationUiState.Initial
    }
}

class HashtagViewModelFactory(private val repository: HashtagRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HashtagViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HashtagViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
