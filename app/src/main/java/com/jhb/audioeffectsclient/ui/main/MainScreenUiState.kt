package com.jhb.audioeffectsclient.ui.main

data class MainScreenUiState(
    val testWord: String? = "Hello, World",
    val record: Boolean = false,
    val level: Double = 0.0,
    val maxLevel: Double= 0.0,
    val addresses: List<String> = listOf(),
)
