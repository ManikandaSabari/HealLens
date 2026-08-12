package com.heallens.android.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.data.repository.ClinicalHistoryRepository
import com.heallens.android.model.ClinicalRecord
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard

@Composable
fun ClinicalHistoryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ClinicalHistoryRepository.init(context)
    }

    var selectedSubtab by remember { mutableStateOf("image") }
    var selectedRecordForDetails by remember { mutableStateOf<ClinicalRecord?>(null) }
    var selectedRecordForDelete by remember { mutableStateOf<ClinicalRecord?>(null) }

    val records by ClinicalHistoryRepository.recordsFlow.collectAsState()
    val scrollState = rememberScrollState()
    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()

    val filteredRecords = remember(records, selectedSubtab) {
        records.filter { it.analysisType.lowercase() == selectedSubtab.lowercase() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = "History",
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = com.heallens.android.utils.AppStrings.get("history_title", activeLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = com.heallens.android.utils.AppStrings.get("history_subtitle", activeLanguage),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subtabs: Image Analysis vs Report Analysis
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceGlass, RoundedCornerShape(12.dp))
                        .padding(3.dp)
                ) {
                    // Image Analysis Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedSubtab == "image") CyanPrimary else SurfaceGlass)
                            .clickable { selectedSubtab = "image" }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = if (selectedSubtab == "image") DarkBackground else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = com.heallens.android.utils.AppStrings.get("history_filter_image", activeLanguage),
                                color = if (selectedSubtab == "image") DarkBackground else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Report Analysis Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedSubtab == "report") PurpleAccent else SurfaceGlass)
                            .clickable { selectedSubtab = "report" }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = if (selectedSubtab == "report") DarkBackground else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = com.heallens.android.utils.AppStrings.get("history_filter_report", activeLanguage),
                                color = if (selectedSubtab == "report") DarkBackground else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // History Record Cards
                if (filteredRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = com.heallens.android.utils.AppStrings.get("history_no_records", activeLanguage),
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    filteredRecords.forEach { item ->
                        HistoryRecordCard(
                            record = item,
                            onViewDetails = { selectedRecordForDetails = it },
                            onDelete = { selectedRecordForDelete = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Details Dialog
        selectedRecordForDetails?.let { record ->
            RecordDetailsDialog(
                record = record,
                onDismiss = { selectedRecordForDetails = null }
            )
        }

        // Delete Dialog
        selectedRecordForDelete?.let { record ->
            RecordDeleteDialog(
                record = record,
                onConfirmDelete = {
                    ClinicalHistoryRepository.deleteRecord(record.id)
                    selectedRecordForDelete = null
                },
                onDismiss = { selectedRecordForDelete = null }
            )
        }
    }
}

private fun localizeClinicalText(text: String, language: com.heallens.android.utils.AppLanguage): String {
    if (language == com.heallens.android.utils.AppLanguage.ENGLISH || text.isBlank()) return text
    return when {
        text.contains("Healthy Biomarker Profile", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_healthy_profile", language)
        text.contains("Diabetic Tendency", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_diabetic_tendency", language)
        text.contains("Cardiovascular Risk", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_cardio_risk", language)
        text.contains("Pneumonia", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_pneumonia", language)
        text.contains("Tuberculosis", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_tuberculosis", language)
        text.contains("Covid", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_covid", language)
        text.contains("Fracture", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_fracture", language)
        text.contains("Arthritis", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_arthritis", language)
        text.contains("Skin Infection", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_skin_infection", language)
        text.contains("Psoriasis", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("diag_psoriasis", language)
        text.contains("Blood Biomarker Report", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("report_biomarkers", language)
        text.contains("Blood Report", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("history_filter_report", language)
        text.contains("Critical", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("severe_critical", language)
        text.contains("Moderate", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("severe_moderate", language)
        text.contains("Mild", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("severe_mild", language)
        text.contains("Healthy", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("statusNormal", language)
        else -> text
    }
}

@Composable
private fun HistoryRecordCard(
    record: ClinicalRecord,
    onViewDetails: (ClinicalRecord) -> Unit,
    onDelete: (ClinicalRecord) -> Unit
) {
    val parseColor = try {
        Color(android.graphics.Color.parseColor(record.severityColorHex))
    } catch (e: Exception) {
        CyanPrimary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceGlass, RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
                Text(
                    text = localizeClinicalText(record.title, activeLanguage),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${com.heallens.android.utils.AppStrings.get("lbl_patient", activeLanguage)} ${record.patientName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = " • ",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = localizeClinicalText(record.category, activeLanguage),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
                Box(
                    modifier = Modifier
                        .background(parseColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, parseColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = localizeClinicalText(record.severity, activeLanguage),
                        color = parseColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.date,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Prediction & Confidence
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
            Text(
                text = localizeClinicalText(record.prediction, activeLanguage),
                color = parseColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            if (record.confidence.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(SurfaceGlass, RoundedCornerShape(4.dp))
                        .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = record.confidence,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (record.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = record.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Divider Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceGlassBorder)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row inside Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // View Details Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyanPrimary.copy(alpha = 0.12f))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .clickable { onViewDetails(record) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View Details",
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("btn_view_details", activeLanguage),
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Delete Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .clickable { onDelete(record) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("btn_delete", activeLanguage),
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordDetailsDialog(
    record: ClinicalRecord,
    onDismiss: () -> Unit
) {
    val parseColor = try {
        Color(android.graphics.Color.parseColor(record.severityColorHex))
    } catch (e: Exception) {
        CyanPrimary
    }

    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeClinicalText(record.title, activeLanguage),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${localizeClinicalText(record.category, activeLanguage)} • ${com.heallens.android.utils.AppStrings.get("lbl_patient", activeLanguage)} ${record.patientName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(parseColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, parseColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = localizeClinicalText(record.severity, activeLanguage),
                        color = parseColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "${com.heallens.android.utils.AppStrings.get("lbl_timestamp", activeLanguage)} ${record.date}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = com.heallens.android.utils.AppStrings.get("lbl_prediction", activeLanguage),
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${localizeClinicalText(record.prediction, activeLanguage)} (${record.confidence})",
                    color = parseColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                if (record.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("lbl_description", activeLanguage),
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                if (record.remedies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("lbl_remedies", activeLanguage),
                        color = PurpleAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    record.remedies.forEach { remedy ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(text = "• ", color = PurpleAccent, fontSize = 12.sp)
                            Text(text = remedy, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = CyanPrimary)
            ) {
                Text(text = com.heallens.android.utils.AppStrings.get("btn_close", activeLanguage), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF10192D),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun RecordDeleteDialog(
    record: ClinicalRecord,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = com.heallens.android.utils.AppStrings.get("history_delete_title", activeLanguage),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Text(
                text = com.heallens.android.utils.AppStrings.get("history_delete_confirm", activeLanguage),
                color = TextSecondary,
                fontSize = 13.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text(text = com.heallens.android.utils.AppStrings.get("btn_delete", activeLanguage), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
            ) {
                Text(text = com.heallens.android.utils.AppStrings.get("btn_cancel", activeLanguage))
            }
        },
        containerColor = Color(0xFF10192D),
        shape = RoundedCornerShape(20.dp)
    )
}
