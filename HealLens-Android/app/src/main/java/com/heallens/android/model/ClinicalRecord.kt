package com.heallens.android.model

data class ClinicalRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String,
    val date: String,
    val patientName: String = "Self",
    val patientAge: Int? = null,
    val patientGender: String? = null,
    val analysisType: String, // "image" or "report"
    val category: String, // "Lungs", "Skin", "Bone", "Blood Report"
    val prediction: String,
    val severity: String, // "NORMAL", "MODERATE RISK", "CRITICAL RISK", "HIGH SEVERITY"
    val severityColorHex: String = "#00D4FF",
    val confidence: String = "94%",
    val description: String = "",
    val remedies: List<String> = emptyList()
)
