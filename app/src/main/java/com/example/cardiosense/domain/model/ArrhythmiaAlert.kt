package com.example.cardiosense.domain.model

data class ArrhythmiaAlert(
    val type: String,
    val severity: Int,
    val timestamp: Long,
    val details: String
)
