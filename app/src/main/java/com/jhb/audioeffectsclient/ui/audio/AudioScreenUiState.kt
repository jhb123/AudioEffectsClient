package com.jhb.audioeffectsclient.ui.audio

data class AudioScreenUiState(
    val record: Boolean = false,
    val level: Double = 0.0,
    val maxLevel: Double= 0.0,
    val addresses: MutableList<String> = mutableListOf(),
    val isScanning: Boolean = false,
    val audioWaveForm: List<Float> = listOf(0f,0f),
)
