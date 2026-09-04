package com.audreyRetournayDiet.femSante.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxAdvice
import com.audreyRetournayDiet.femSante.repository.local.ToolboxFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ToolboxFileDetailViewModel @Inject constructor(
    repository: ToolboxFileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _fileState = MutableStateFlow<ToolboxAdvice?>(null)
    val fileState: StateFlow<ToolboxAdvice?> = _fileState.asStateFlow()

    init {
        val fileId: String? = savedStateHandle["EXTRA_FILE_ID"]
        if (fileId != null) {
            _fileState.value = repository.getAll().firstOrNull { it.id == fileId }
        }
    }
}