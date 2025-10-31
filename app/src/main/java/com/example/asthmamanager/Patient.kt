package com.example.asthmamanager

data class Patient(
    val name: String,
    val zone: String,
    val latestPEFR: Int,
    val symptomSeverity: String
)
