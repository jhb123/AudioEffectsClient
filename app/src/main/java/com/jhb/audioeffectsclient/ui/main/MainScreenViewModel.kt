package com.jhb.audioeffectsclient.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



const val TAG = "MainScreenViewModel"

class MainScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState : StateFlow<MainScreenUiState> = _uiState

    // This is space for UI to do with the entire App.
}