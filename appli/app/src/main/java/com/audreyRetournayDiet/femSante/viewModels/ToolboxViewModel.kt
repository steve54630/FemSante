package com.audreyRetournayDiet.femSante.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.ToolboxNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ToolboxViewModel : ViewModel() {

    private val navigationSharedFlow = MutableSharedFlow<ToolboxNavigationEvent>()
    val navigationEvent: SharedFlow<ToolboxNavigationEvent> = navigationSharedFlow.asSharedFlow()

    fun onToolClicked(toolId: Int) {
        val fileName = when (toolId) {
            1 -> "automassage_ventre.pdf"
            2 -> "bouillote.pdf"
            3 -> "douleurs_abdominales.pdf"
            4 -> "emotional_tempest.pdf"
            5 -> "emotional_tempest_oil.pdf"
            6 -> "infusion_digestion.pdf"
            7 -> "infusions_menstruations.pdf"
            else -> ""
        }

        if (fileName.isNotEmpty()) {
            viewModelScope.launch {
                navigationSharedFlow.emit(ToolboxNavigationEvent.NavigateToPdf(fileName))
            }
        }
    }
}