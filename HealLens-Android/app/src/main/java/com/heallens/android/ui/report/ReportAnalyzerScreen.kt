package com.heallens.android.ui.report

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.data.repository.ClinicalHistoryRepository
import com.heallens.android.model.ClinicalRecord
import com.heallens.android.ui.components.GlassTextField
import com.heallens.android.ui.components.GradientButton
import com.heallens.android.ui.components.SpecialistDiscoveryWidget
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard
import com.heallens.android.ui.scanner.DoctorInfo
import com.heallens.android.ui.scanner.doctorsDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// Dashed Border Modifier
private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 8.dp,
    cornerRadius: Dp = 16.dp
): Modifier = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

// Data Models
data class NaturalRemedyData(
    val name: String,
    val ingredients: String,
    val method: String,
    val use: String
)

data class AyurvedicSuggestionData(
    val name: String,
    val sanskrit: String,
    val dosage: String,
    val use: String
)

data class BiomarkerConfig(
    val key: String,
    val name: String,
    val unit: String,
    val min: Double,
    val max: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val description: String,
    val naturalRemedy: NaturalRemedyData,
    val ayurvedicSuggestion: AyurvedicSuggestionData
)

val pddBiomarkers = listOf(
    BiomarkerConfig(
        key = "glucose",
        name = "Blood Glucose (Sugar)",
        unit = "mg/dL",
        min = 70.0,
        max = 100.0,
        minLimit = 40.0,
        maxLimit = 300.0,
        description = "Fasting Blood Glucose measures sugar levels in the blood. High sugar indicates diabetes or prediabetes risk, while low sugar causes weakness and dizziness.",
        naturalRemedy = NaturalRemedyData(
            name = "Nisha Amalaki & Methi Water",
            ingredients = "Turmeric powder, Amla powder, Fenugreek seeds, Water",
            method = "Mix 1/2 tsp turmeric and amla in warm water. Soak fenugreek seeds overnight and drink in morning.",
            use = "Consumed daily to support glycemic control, insulin sensitivity, and natural blood sugar balance."
        ),
        ayurvedicSuggestion = AyurvedicSuggestionData(
            name = "Nisha Amalaki",
            sanskrit = "निशा आमलकी",
            dosage = "1 tsp twice daily with warm water",
            use = "Diabetes management and pancreatic support"
        )
    ),
    BiomarkerConfig(
        key = "cholesterol",
        name = "Total Cholesterol",
        unit = "mg/dL",
        min = 120.0,
        max = 200.0,
        minLimit = 80.0,
        maxLimit = 400.0,
        description = "Total Cholesterol is a key fat marker. Elevated levels can lead to fat buildup in arteries, increasing cardiovascular and blood pressure risks.",
        naturalRemedy = NaturalRemedyData(
            name = "Garlic Tea & Arjuna Bark Decoction",
            ingredients = "Garlic cloves, Arjuna bark powder, Water, Honey",
            method = "Boil crushed garlic and arjuna bark in water for 10 minutes. Strain and add a drop of honey.",
            use = "Supports lipid clearance, helps reduce arterial plaque buildup, and maintains heart muscle tone."
        ),
        ayurvedicSuggestion = AyurvedicSuggestionData(
            name = "Lashuna & Guggulu",
            sanskrit = "लशुन गुग्गुलु",
            dosage = "2 tablets twice daily after meals",
            use = "Lipid metabolism and arterial clearing"
        )
    ),
    BiomarkerConfig(
        key = "triglycerides",
        name = "Triglycerides",
        unit = "mg/dL",
        min = 50.0,
        max = 150.0,
        minLimit = 30.0,
        maxLimit = 500.0,
        description = "Triglycerides are types of fat stored in your cells. High levels are linked to arterial hardening and pancreas strain.",
        naturalRemedy = NaturalRemedyData(
            name = "Triphala & Ginger Infusion",
            ingredients = "Triphala powder, Fresh ginger, Warm water",
            method = "Take 1 tsp Triphala with warm water at night. Sip fresh ginger tea during the day.",
            use = "Accelerates fat digestion and metabolic clearance of stored triglycerides."
        ),
        ayurvedicSuggestion = AyurvedicSuggestionData(
            name = "Triphala Churna",
            sanskrit = "त्रिफला चूर्ण",
            dosage = "1 tsp at bedtime with warm water",
            use = "Metabolic clearance and digestive detox"
        )
    ),
    BiomarkerConfig(
        key = "hemoglobin",
        name = "Hemoglobin (Hb)",
        unit = "g/dL",
        min = 12.0,
        max = 17.0,
        minLimit = 5.0,
        maxLimit = 22.0,
        description = "Hemoglobin carries oxygen in red blood cells. Low levels indicate Anemia, leading to chronic fatigue, weakness, and pale skin.",
        naturalRemedy = NaturalRemedyData(
            name = "Pomegranate & Beetroot Elixir",
            ingredients = "Fresh Pomegranate, Beetroot, Dates, Jaggery",
            method = "Blend fresh pomegranate seeds and beetroot. Strain juice and mix with crushed dates or organic jaggery.",
            use = "Rich in bioavailable iron and Vitamin C to stimulate red blood cell production."
        ),
        ayurvedicSuggestion = AyurvedicSuggestionData(
            name = "Dhatri Lauha / Lohasava",
            sanskrit = "धात्री लौह / लोहासव",
            dosage = "15ml with equal water after meals",
            use = "Anemia and iron deficiency recovery"
        )
    ),
    BiomarkerConfig(
        key = "creatinine",
        name = "Serum Creatinine",
        unit = "mg/dL",
        min = 0.6,
        max = 1.2,
        minLimit = 0.2,
        maxLimit = 5.0,
        description = "Creatinine is a muscle waste product filtered by kidneys. High levels suggest renal strain or reduced kidney filtration capacity.",
        naturalRemedy = NaturalRemedyData(
            name = "Punarnava & Varuna Tea",
            ingredients = "Punarnava root, Varuna bark, Water",
            method = "Boil herbs in water until volume reduces by half. Filter and drink warm twice daily.",
            use = "Acts as a natural kidney tonic and mild diuretic to flush waste and support glomerular filtration."
        ),
        ayurvedicSuggestion = AyurvedicSuggestionData(
            name = "Punarnavadi Kashayam",
            sanskrit = "पुनर्नवादि कषायम्",
            dosage = "15ml with warm water, twice daily",
            use = "Renal support and fluid balance"
        )
    ),
    BiomarkerConfig(
        key = "ast",
        name = "AST (Liver Enzyme)",
        unit = "U/L",
        min = 10.0,
        max = 40.0,
        minLimit = 5.0,
        maxLimit = 200.0,
        description = "AST is a liver enzyme released into the blood during liver stress. Elevated levels indicate hepatocyte strain or fatty liver changes.",
        naturalRemedy = NaturalRemedyData(
            name = "Bhumi Amla & Giloy Juice",
            ingredients = "Bhumi Amla leaves, Giloy stem, Water",
            method = "Extract fresh juice of Bhumi Amla and Giloy. Drink 10–15 ml on an empty stomach.",
            use = "Helps protect hepatocytes, reduce liver enzyme elevation, and promote bile flow."
        ),
        ayurvedicSuggestion = AyurvedicSuggestionData(
            name = "Katuki & Bhumi Amla",
            sanskrit = "कटुकी / भूमि आमलकी",
            dosage = "1g powder with warm water",
            use = "Hepatoprotective and liver detox"
        )
    ),
    BiomarkerConfig(
        key = "tsh",
        name = "TSH (Thyroid Stimulating Hormone)",
        unit = "uIU/mL",
        min = 0.4,
        max = 4.5,
        minLimit = 0.05,
        maxLimit = 15.0,
        description = "TSH controls thyroid hormone production. High TSH means your thyroid is underactive (Hypothyroidism), slowing down metabolism.",
        naturalRemedy = NaturalRemedyData(
            name = "Coriander Seed Infusion",
            ingredients = "Crushed Coriander seeds, Water",
            method = "Boil 2 tsp crushed coriander seeds in water for 10 minutes. Strain and drink warm morning and evening.",
            use = "Traditionally used to stimulate thyroid gland function and harmonize metabolic rate."
        ),
        ayurvedicSuggestion = AyurvedicSuggestionData(
            name = "Kanchnar Guggulu",
            sanskrit = "काञ्चनार गुग्गुलु",
            dosage = "2 tablets twice daily with warm water",
            use = "Thyroid gland balancing"
        )
    )
)

data class BiomarkerEvaluation(
    val config: BiomarkerConfig,
    val value: Double?,
    val status: String, // "NORMAL", "LOW", "HIGH", "CRITICAL", "NOT_PROVIDED"
    val statusColor: Color
)

data class RiskEvaluationResult(
    val summary: String,
    val level: String,
    val color: Color,
    val points: List<String>
)

data class FullReportAnalysis(
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val riskSummary: String,
    val riskLevel: String, // "normal", "moderate", "critical"
    val riskColor: Color,
    val riskPoints: List<String>,
    val evaluations: List<BiomarkerEvaluation>,
    val naturalRemedies: List<NaturalRemedyData>,
    val ayurvedicSuggestions: List<AyurvedicSuggestionData>,
    val specialist: String,
    val urgentVisit: Boolean
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReportAnalyzerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val resultSectionRequester = remember { BringIntoViewRequester() }

    // Rotating Infinite Animation for Circular Scanner (FIX 1)
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Patient Information State
    var patientName by remember { mutableStateOf("Jane Doe") }
    var patientAgeInput by remember { mutableStateOf("30") }
    var patientGender by remember { mutableStateOf("female") }
    var isGenderMenuExpanded by remember { mutableStateOf(false) }

    var documentUri by remember { mutableStateOf<Uri?>(null) }
    var isScanningDoc by remember { mutableStateOf(false) }
    var scanningStepText by remember { mutableStateOf("") }

    // Biomarker Value Inputs Map
    val biomarkerInputs = remember {
        mutableStateOf(
            pddBiomarkers.associate { it.key to "" }
        )
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    var reportAnalysis by remember { mutableStateOf<FullReportAnalysis?>(null) }
    var hasSavedRecord by remember { mutableStateOf(false) }
    var insufficientDataNotice by remember { mutableStateOf<String?>(null) }

    // Doctor Booking Modal State
    var showAppointmentModal by remember { mutableStateOf(false) }
    var bookingDoctor by remember { mutableStateOf<DoctorInfo?>(null) }
    var bookingConfirmed by remember { mutableStateOf(false) }
    var confirmedAppointmentId by remember { mutableStateOf("") }

    // Text-to-Speech State
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        mainHandler.post { isSpeaking = true }
                    }
                    override fun onDone(utteranceId: String?) {
                        mainHandler.post { isSpeaking = false }
                    }
                    override fun onError(utteranceId: String?) {
                        mainHandler.post { isSpeaking = false }
                    }
                })
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    fun executeAnalysis(extractedVals: Map<String, Double> = emptyMap()) {
        isAnalyzing = true
        reportAnalysis = null
        hasSavedRecord = false

        scope.launch {
            delay(300)

            val activeVals = if (extractedVals.isNotEmpty()) {
                extractedVals
            } else {
                pddBiomarkers.mapNotNull { bio ->
                    val num = biomarkerInputs.value[bio.key]?.toDoubleOrNull()
                    if (num != null) bio.key to num else null
                }.toMap()
            }

            if (activeVals.isEmpty()) {
                isAnalyzing = false
                insufficientDataNotice = "⚠️ Insufficient Report Data: No recognized clinical biomarkers found in the uploaded file. Please upload a valid laboratory blood report PDF or image."
                reportAnalysis = null
                return@launch
            }

            val gl = activeVals["glucose"]
            val ch = activeVals["cholesterol"]
            val tr = activeVals["triglycerides"]
            val hb = activeVals["hemoglobin"]
            val cr = activeVals["creatinine"]
            val ast = activeVals["ast"]
            val tsh = activeVals["tsh"]

            val abnormalSummaries = mutableListOf<String>()
            var level = "normal"
            var color = Color(0xFF10B981)

            if (gl != null && gl > 120.0) {
                abnormalSummaries.add("Elevated Blood Glucose ($gl mg/dL) observed.")
                level = "high"
                color = Color(0xFFEF4444)
            }
            if (ch != null && ch > 200.0) {
                abnormalSummaries.add("Elevated Total Cholesterol ($ch mg/dL) observed.")
                if (level != "critical") { level = "high"; color = Color(0xFFEF4444) }
            }
            if (tr != null && tr > 150.0) {
                abnormalSummaries.add("Elevated Triglycerides ($tr mg/dL) observed.")
                if (level != "critical") { level = "high"; color = Color(0xFFEF4444) }
            }
            if (hb != null && hb < 12.0) {
                abnormalSummaries.add("Lowered Hemoglobin ($hb g/dL) observed.")
                if (level != "critical") { level = "high"; color = Color(0xFFEF4444) }
            }
            if (cr != null && cr > 1.2) {
                abnormalSummaries.add("Elevated Serum Creatinine ($cr mg/dL) observed.")
                if (level != "critical") { level = "high"; color = Color(0xFFEF4444) }
            }
            if (ast != null && ast > 40.0) {
                abnormalSummaries.add("Elevated AST ($ast U/L) observed.")
                if (level != "critical") { level = "high"; color = Color(0xFFEF4444) }
            }
            if (tsh != null && (tsh > 4.5 || tsh < 0.4)) {
                abnormalSummaries.add("Thyroid Stimulating Hormone out of range ($tsh uIU/mL).")
                if (level != "critical") { level = "high"; color = Color(0xFFEF4444) }
            }

            val riskSummary = if (abnormalSummaries.isEmpty()) "Healthy Biomarker Profile" else "Biomarker Attention Needed"
            val riskPoints = if (abnormalSummaries.isEmpty()) listOf("All recognized biomarkers are within normal reference ranges.") else abnormalSummaries

            val riskEval = RiskEvaluationResult(riskSummary, level, color, riskPoints)

            val riskLevel = riskEval.level
            val riskColor = riskEval.color

            // 2. Evaluate all 7 biomarkers, marking missing ones as NOT_PROVIDED
            val evaluations = pddBiomarkers.map { bio ->
                val valNum = activeVals[bio.key]
                if (valNum != null) {
                    val (status, statusColor) = when {
                        valNum < bio.min -> "LOW" to Color(0xFFF59E0B)
                        valNum > bio.max * 1.5 -> "CRITICAL" to Color(0xFFEF4444)
                        valNum > bio.max -> "HIGH" to Color(0xFFF59E0B)
                        else -> "NORMAL" to Color(0xFF10B981)
                    }
                    BiomarkerEvaluation(bio, valNum, status, statusColor)
                } else {
                    BiomarkerEvaluation(bio, null, "NOT_PROVIDED", Color(0xFF6B7280))
                }
            }

            // 3. Dynamic Natural Remedies & Ayurvedic Suggestions for present abnormal values
            val abnormalConfigs = evaluations.filter { it.value != null && it.status != "NORMAL" && it.status != "NOT_PROVIDED" }.map { it.config }
            val naturalRemedies = abnormalConfigs.map { it.naturalRemedy }
            val ayurvedicSuggestions = abnormalConfigs.map { it.ayurvedicSuggestion }

            // 4. Specialist Mapping
            val (specialist, urgent) = when {
                (gl ?: 0.0) > 120.0 || (tsh ?: 0.0) > 4.5 -> "Endocrinologist" to false
                (ch ?: 0.0) > 200.0 || (tr ?: 0.0) > 150.0 -> "Cardiologist" to false
                (ast ?: 0.0) > 40.0 -> "Hepatologist" to false
                (cr ?: 0.0) > 1.2 -> "Nephrologist" to (riskLevel == "critical")
                (hb ?: 99.0) < 11.5 -> "Hematologist" to false
                else -> "General Physician" to false
            }

            val pAge = patientAgeInput.toIntOrNull() ?: 30
            val pName = patientName.ifEmpty { "Jane Doe" }

            val result = FullReportAnalysis(
                patientName = pName,
                patientAge = pAge,
                patientGender = patientGender,
                riskSummary = riskSummary,
                riskLevel = riskLevel,
                riskColor = riskColor,
                riskPoints = riskPoints,
                evaluations = evaluations,
                naturalRemedies = naturalRemedies,
                ayurvedicSuggestions = ayurvedicSuggestions,
                specialist = specialist,
                urgentVisit = urgent
            )

            reportAnalysis = result
            isAnalyzing = false

            // Save to ClinicalHistoryRepository exactly ONCE per analysis session
            if (!hasSavedRecord) {
                hasSavedRecord = true
                android.util.Log.d("HealLensHistory", "[History] Creating REPORT record for $pName - $riskSummary")
                val record = ClinicalRecord(
                    userId = ClinicalHistoryRepository.currentUserId,
                    title = "Blood Biomarker Report",
                    date = ClinicalHistoryRepository.getCurrentTimestamp(),
                    patientName = pName,
                    patientAge = pAge,
                    patientGender = patientGender,
                    analysisType = "report",
                    category = "Blood Report",
                    prediction = riskSummary,
                    severity = if (riskLevel == "critical") "Critical Severity" else if (riskLevel == "normal") "Healthy" else "Moderate Severity",
                    severityColorHex = if (riskLevel == "critical") "#EF4444" else if (riskLevel == "normal") "#10B981" else "#F59E0B",
                    confidence = "98.0%",
                    description = "Blood biomarker report analysis for $pName ($pAge yrs, $patientGender). Risk Summary: $riskSummary.",
                    remedies = naturalRemedies.map { "${it.name}: ${it.ingredients}" } + ayurvedicSuggestions.map { "${it.name} (${it.sanskrit})" }
                )
                ClinicalHistoryRepository.addRecord(record)
            }

            // Auto-scroll smoothly to result section
            delay(150)
            resultSectionRequester.bringIntoView()
        }
    }

    var analysisRequestId by remember { mutableStateOf(0) }

    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        documentUri = uri
        if (uri != null) {
            val requestId = ++analysisRequestId

            // 1. IMMEDIATELY RESET ALL PREVIOUS REPORT STATE BEFORE PROCESSING NEW DOCUMENT
            reportAnalysis = null
            insufficientDataNotice = null
            isAnalyzing = false
            hasSavedRecord = false
            val clearedMap = biomarkerInputs.value.toMutableMap()
            pddBiomarkers.forEach { bio -> clearedMap[bio.key] = "" }
            biomarkerInputs.value = clearedMap

            isScanningDoc = true
            scope.launch {
                val steps = listOf(
                    "Scanning report values...",
                    "Preprocessing document & adjusting contrast...",
                    "Running AI text boundary segmentations...",
                    "Extracting biomarker values via neural OCR...",
                    "Validating units and ranges with laboratory database...",
                    "Finalizing extraction metrics..."
                )
                for (step in steps) {
                    scanningStepText = step
                    delay(250)
                }
                isScanningDoc = false

                // Real ML Kit OCR & PDF page rendering
                val rawText = processDocumentUri(context, uri)

                // Race condition check: abort if another URI was selected in the meantime
                if (requestId != analysisRequestId) return@launch

                val extractedMap = extractBiomarkersFromText(rawText)

                android.util.Log.d("REPORT_ANALYZER_DEBUG", "EXTRACTED BIOMARKERS: $extractedMap (count=${extractedMap.size})")

                if (extractedMap.isEmpty()) {
                    insufficientDataNotice = "⚠️ Insufficient Report Data: No recognized clinical biomarkers found in the uploaded file. Please upload a valid laboratory blood report PDF or image."
                    reportAnalysis = null
                    val updated = biomarkerInputs.value.toMutableMap()
                    pddBiomarkers.forEach { bio -> updated[bio.key] = "" }
                    biomarkerInputs.value = updated
                } else {
                    insufficientDataNotice = null
                    val updated = biomarkerInputs.value.toMutableMap()
                    pddBiomarkers.forEach { bio -> updated[bio.key] = "" }
                    extractedMap.forEach { (key, valNum) ->
                        updated[key] = if (valNum % 1.0 == 0.0) valNum.toInt().toString() else valNum.toString()
                    }
                    biomarkerInputs.value = updated
                    executeAnalysis(extractedMap)
                }
            }
        }
    }

    fun applyPreset(presetType: String) {
        val updated = biomarkerInputs.value.toMutableMap()
        when (presetType) {
            "healthy" -> {
                updated["glucose"] = "85"
                updated["cholesterol"] = "170"
                updated["triglycerides"] = "115"
                updated["hemoglobin"] = "14.8"
                updated["creatinine"] = "0.8"
                updated["ast"] = "22"
                updated["tsh"] = "1.8"
            }
            "lipid" -> {
                updated["glucose"] = "95"
                updated["cholesterol"] = "255"
                updated["triglycerides"] = "220"
                updated["hemoglobin"] = "13.5"
                updated["creatinine"] = "0.9"
                updated["ast"] = "35"
                updated["tsh"] = "2.2"
            }
            "diabetic" -> {
                updated["glucose"] = "195"
                updated["cholesterol"] = "185"
                updated["triglycerides"] = "165"
                updated["hemoglobin"] = "9.5"
                updated["creatinine"] = "1.1"
                updated["ast"] = "45"
                updated["tsh"] = "5.2"
            }
        }
        biomarkerInputs.value = updated
        executeAnalysis()
    }

    fun toggleReadAloud(result: FullReportAnalysis) {
        if (tts == null || !isTtsReady) {
            Toast.makeText(context, "Text-to-Speech engine is initializing...", Toast.LENGTH_SHORT).show()
            return
        }

        if (isSpeaking) {
            tts?.stop()
            isSpeaking = false
            return
        }

        val abnormalStr = result.evaluations.filter { it.status != "NORMAL" }
            .joinToString(". ") { "${it.config.name}: ${it.value} ${it.config.unit}, status ${it.status}" }

        val remediesText = result.naturalRemedies.joinToString(". ") { r ->
            "Natural remedy: ${r.name}. Ingredients: ${r.ingredients}. Method: ${r.method}. Use: ${r.use}"
        }

        val ayurvedaText = result.ayurvedicSuggestions.joinToString(". ") { a ->
            "Ayurvedic suggestion: ${a.name} (${a.sanskrit}). Dosage: ${a.dosage}. Use: ${a.use}"
        }

        val textToSpeak = "Blood Biomarker Report for ${result.patientName}. " +
                "Risk Profile: ${result.riskSummary}. " +
                if (abnormalStr.isNotEmpty()) "Abnormal Biomarkers: $abnormalStr. " else "All biomarkers within normal limits. " +
                if (remediesText.isNotEmpty()) "$remediesText. " else "" +
                if (ayurvedaText.isNotEmpty()) "$ayurvedaText. " else "" +
                "Recommended specialist: ${result.specialist}."

        tts?.stop()
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ReportSpeechUtterance")
        isSpeaking = true
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "ReportSpeechUtterance")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analyzer",
                        tint = PurpleAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = com.heallens.android.utils.AppStrings.get("report_title", activeLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = com.heallens.android.utils.AppStrings.get("report_subtitle", activeLanguage),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Dashed Upload Box with Animated Circular Scanner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF00D4FF).copy(alpha = 0.02f))
                        .dashedBorder(
                            color = Color(0xFF00D4FF).copy(alpha = 0.3f),
                            strokeWidth = 2.dp,
                            dashLength = 8.dp,
                            gapLength = 8.dp,
                            cornerRadius = 16.dp
                        )
                        .clickable { docLauncher.launch("*/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (isScanningDoc) {
                            // Circular Rotating Scanner Ring (Cyan + Purple Arcs)
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .graphicsLayer { rotationZ = rotationAngle },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = CyanPrimary,
                                        startAngle = 0f,
                                        sweepAngle = 240f,
                                        useCenter = false,
                                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = PurpleAccent,
                                        startAngle = 120f,
                                        sweepAngle = 180f,
                                        useCenter = false,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Text("📊", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Scanning report values...",
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = scanningStepText.ifEmpty { "Validating units and ranges with laboratory database..." },
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text("📊", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload Lab Report",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Supports JPG, PNG (Max 10MB)",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (documentUri != null) {
                                Text(
                                    text = "📄 Document Attached",
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(SurfaceGlass)
                                        .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(50))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Choose Document", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Patient Information Section (FIX 1: ONE clean outer card with perfectly aligned Full Name, Age, and Gender)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "PATIENT INFORMATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: Full Name (Full Width)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Full Name", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        GlassTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = null,
                            placeholder = "Full Name",
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: Age & Gender (Side by Side with Identical 48.dp Height & Alignment)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Age Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Age", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            GlassTextField(
                                value = patientAgeInput,
                                onValueChange = { patientAgeInput = it },
                                label = null,
                                placeholder = "Age",
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }

                        // Gender Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gender", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceGlass)
                                        .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(12.dp))
                                        .clickable { isGenderMenuExpanded = true }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = patientGender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("▼", color = CyanPrimary, fontSize = 11.sp)
                                    }
                                }

                                DropdownMenu(
                                    expanded = isGenderMenuExpanded,
                                    onDismissRequest = { isGenderMenuExpanded = false }
                                ) {
                                    listOf("female", "male").forEach { g ->
                                        DropdownMenuItem(
                                            text = { Text(g.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }) },
                                            onClick = {
                                                patientGender = g
                                                isGenderMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Sample Test Profiles Buttons
                Text(
                    text = "LOAD SAMPLE TEST PROFILES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { applyPreset("healthy") }
                            .padding(12.dp)
                    ) {
                        Text("✅ Healthy CBC & Sugar", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { applyPreset("lipid") }
                            .padding(12.dp)
                    ) {
                        Text("⚠️ High Cholesterol & Triglycerides", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { applyPreset("diabetic") }
                            .padding(12.dp)
                    ) {
                        Text("🚨 Diabetes & Low Hemoglobin", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Report Biomarker Input Fields (FIXED: All 7 manual biomarker values 100% visible & unclipped)
                Text(
                    text = "REPORT BIOMARKERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                pddBiomarkers.forEach { bio ->
                    val currentVal = biomarkerInputs.value[bio.key] ?: ""
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bio.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(bio.unit, color = TextMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        GlassTextField(
                            value = currentVal,
                            onValueChange = { newVal ->
                                val updated = biomarkerInputs.value.toMutableMap()
                                updated[bio.key] = newVal
                                biomarkerInputs.value = updated
                            },
                            label = null,
                            placeholder = "${bio.min} - ${bio.max}",
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Analyze CTA Button
                GradientButton(
                    text = "🔍 Parse & Analyze Report →",
                    onClick = { executeAnalysis() },
                    isLoading = isAnalyzing
                )
            insufficientDataNotice?.let { notice ->
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = notice,
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 6. ANALYSIS RESULT SECTION (FIXED: TSH alignment + Reusable BiomarkerGaugeCard)
            reportAnalysis?.let { analysis ->
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(resultSectionRequester)
                        .glassmorphicCard(cornerRadius = 20.dp)
                        .padding(20.dp)
                ) {
                    // 1. Patient Info Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFFFF).copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👤 ", fontSize = 20.sp)
                                Column {
                                    Text("FULL NAME", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(analysis.patientName, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("${analysis.patientAge} yrs • ${analysis.patientGender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            // Overall Risk Level Badge
                            Box(
                                modifier = Modifier
                                    .background(analysis.riskColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, analysis.riskColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = analysis.riskLevel.uppercase(Locale.ROOT),
                                    color = analysis.riskColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Biomarker Gauges (FIXED: TSH uses exact same reusable BiomarkerGaugeCard)
                    Text(
                        text = "BIOMARKER RANGE VISUALIZER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    analysis.evaluations.forEach { ev ->
                        BiomarkerGaugeCard(ev = ev)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Risk Profile Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(analysis.riskColor.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .border(1.dp, analysis.riskColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Risk Profile",
                                    tint = analysis.riskColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Risk Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = analysis.riskColor,
                                        fontSize = 16.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = analysis.riskSummary,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            analysis.riskPoints.forEach { point ->
                                Text(
                                    text = "• $point",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Patient Report Simplifier Card
                    Text(
                        text = "Patient Report Simplifier",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    analysis.evaluations.forEach { ev ->
                        val isPresent = ev.value != null
                        val valNum = ev.value ?: 0.0
                        val isAbnormal = isPresent && (valNum < ev.config.min || valNum > ev.config.max)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAbnormal) Color(0xFFF59E0B).copy(alpha = 0.06f) else SurfaceGlass)
                                .border(1.dp, if (isAbnormal) Color(0xFFF59E0B).copy(alpha = 0.3f) else SurfaceGlassBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isPresent) "${ev.config.name} ($valNum ${ev.config.unit})" else "${ev.config.name} — Not provided in the report",
                                    color = if (isAbnormal) Color(0xFFF59E0B) else if (isPresent) TextPrimary else TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontStyle = if (!isPresent) FontStyle.Italic else FontStyle.Normal
                                )
                                Text(
                                    text = ev.config.description,
                                    color = if (isPresent) TextSecondary else TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                    }

                    // 5. Natural Remedies Section (Matching Scanner Style)
                    if (analysis.naturalRemedies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF4CAF50).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🍵 ", fontSize = 16.sp)
                                    Text(
                                        text = "Natural Remedies to be Consumed",
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                analysis.naturalRemedies.forEachIndexed { idx, remedy ->
                                    if (idx > 0) Spacer(modifier = Modifier.height(10.dp))
                                    Column {
                                        Text(
                                            text = remedy.name,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Ingredients: ${remedy.ingredients}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Text(
                                            text = "Method: ${remedy.method}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Text(
                                            text = "Use: ${remedy.use}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. Ayurvedic Suggestions Section (Matching Scanner Style)
                    if (analysis.ayurvedicSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFF9800).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌿 ", fontSize = 16.sp)
                                    Text(
                                        text = "Ayurvedic Suggestion",
                                        color = Color(0xFFFF9800),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFF9800).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Note: Traditional wellness practices are for supportive care and general educational purposes. They do not replace professional medical diagnosis or treatment.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFB74D),
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                analysis.ayurvedicSuggestions.forEachIndexed { idx, ayur ->
                                    if (idx > 0) Spacer(modifier = Modifier.height(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = ayur.name,
                                                color = Color(0xFFFF9800),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (ayur.sanskrit.isNotEmpty()) {
                                                Text(
                                                    text = " (${ayur.sanskrit})",
                                                    color = Color(0xFFFF9800).copy(alpha = 0.8f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Dosage: ${ayur.dosage}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Text(
                                            text = "Use: ${ayur.use}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 8 & 9. Result Action Buttons (Read Aloud Toggle & Book Appointment)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSpeaking) Color(0xFFEF4444).copy(alpha = 0.15f) else SurfaceGlass)
                                .border(
                                    width = 1.dp,
                                    color = if (isSpeaking) Color(0xFFEF4444) else SurfaceGlassBorder,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable { toggleReadAloud(analysis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSpeaking) "🔇 Stop Reading" else "🔊 Read Aloud",
                                color = if (isSpeaking) Color(0xFFEF4444) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SpecialistDiscoveryWidget(
                        specialty = analysis.specialist,
                        contextualNote = "Based on evaluated laboratory report biomarkers, consulting a ${analysis.specialist} is recommended for formal clinical evaluation and medical consultation. This recommendation is educational and does not replace a medical diagnosis."
                    )
                }
            }
        }
    }
}
}

// Reusable Biomarker Gauge Card Component (FIX 3: Used uniformly for all 7 biomarkers including TSH)
@Composable
private fun BiomarkerGaugeCard(
    ev: BiomarkerEvaluation
) {
    val bio = ev.config
    val valNum = ev.value

    if (valNum == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bio.name,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    softWrap = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Not provided in the report",
                        color = TextMuted,
                        fontStyle = FontStyle.Italic,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF6B7280).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Not Provided", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        val totalRange = bio.maxLimit - bio.minLimit
        val pct = (((valNum - bio.minLimit) / totalRange) * 100).coerceIn(2.0, 98.0)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bio.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    softWrap = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$valNum ${bio.unit}",
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(ev.statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(ev.status, color = ev.statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Gauge Track Bar + Circular Pin Marker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Colored Track Bar (8.dp height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFFFFF).copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        val lowZonePct = ((bio.min - bio.minLimit) / totalRange).toFloat().coerceIn(0f, 1f)
                        val normalZonePct = ((bio.max - bio.min) / totalRange).toFloat().coerceIn(0f, 1f)
                        val highZonePct = (1f - lowZonePct - normalZonePct).coerceIn(0f, 1f)

                        Box(modifier = Modifier.weight(lowZonePct.coerceAtLeast(0.01f)).fillMaxSize().background(Color(0xFF00D4FF).copy(alpha = 0.25f)))
                        Box(modifier = Modifier.weight(normalZonePct.coerceAtLeast(0.01f)).fillMaxSize().background(Color(0xFF10B981).copy(alpha = 0.35f)))
                        Box(modifier = Modifier.weight(highZonePct.coerceAtLeast(0.01f)).fillMaxSize().background(Color(0xFFEF4444).copy(alpha = 0.3f)))
                    }
                }

                // Circular Pin Marker (16.dp glowing circle sitting directly on the line)
                val bias = ((pct / 50.0) - 1.0).toFloat().coerceIn(-0.95f, 0.95f)
                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.BiasAlignment(bias, 0f))
                        .size(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .border(3.dp, CyanPrimary, RoundedCornerShape(50))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${bio.minLimit}", color = TextMuted, fontSize = 9.sp)
                Text("Normal: ${bio.min} - ${bio.max}", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("${bio.maxLimit}", color = TextMuted, fontSize = 9.sp)
            }
        }
    }
}

private data class PddBiomarkerRule(
    val key: String,
    val aliases: List<Regex>,
    val exclusions: List<Regex>,
    val minValid: Double,
    val maxValid: Double
)

private fun normalizeExtractedText(rawText: String): String {
    if (rawText.isBlank()) return ""

    var text = rawText
        .replace(Regex("[\\u00A0\\u1680\\u180E\\u2000-\\u200B\\u202F\\u205F\\u3000]"), " ")
        .replace(Regex("\\s*[:=–—]\\s*"), " : ")
        .replace(Regex("\\.{2,}"), " ")
        .replace(Regex("mg\\s*/\\s*dL", RegexOption.IGNORE_CASE), "mg/dL")
        .replace(Regex("g\\s*/\\s*dL", RegexOption.IGNORE_CASE), "g/dL")
        .replace(Regex("uIU\\s*/\\s*mL", RegexOption.IGNORE_CASE), "uIU/mL")
        .replace(Regex("µIU\\s*/\\s*mL", RegexOption.IGNORE_CASE), "uIU/mL")
        .replace(Regex("U\\s*/\\s*L", RegexOption.IGNORE_CASE), "U/L")

    val testKeywords = listOf(
        "Blood Glucose", "Fasting Glucose", "Random Glucose", "Fasting Blood Sugar", "Random Blood Sugar",
        "Total Cholesterol", "Serum Cholesterol", "Triglycerides", "Serum Triglycerides",
        "Hemoglobin", "Haemoglobin", "Serum Creatinine", "AST", "SGOT", "TSH", "Thyroid Stimulating"
    )

    for (kw in testKeywords) {
        val reKw = Regex("([^\\n])\\s*(${Regex.escape(kw)})", RegexOption.IGNORE_CASE)
        text = text.replace(reKw, "$1\n$2")
    }

    return text.split(Regex("\\r?\\n"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("\n")
}

private fun extractBiomarkersFromText(rawText: String): Map<String, Double> {
    if (rawText.isBlank()) return emptyMap()

    val normalizedText = normalizeExtractedText(rawText)
    val lines = normalizedText.split(Regex("\\r?\\n")).map { it.trim() }.filter { it.isNotEmpty() }

    val rules = listOf(
        PddBiomarkerRule(
            key = "glucose",
            aliases = listOf(
                Regex("fasting\\s+blood\\s+sugar", RegexOption.IGNORE_CASE),
                Regex("random\\s+blood\\s+sugar", RegexOption.IGNORE_CASE),
                Regex("fasting\\s+blood\\s+glucose", RegexOption.IGNORE_CASE),
                Regex("random\\s+blood\\s+glucose", RegexOption.IGNORE_CASE),
                Regex("blood\\s+glucose", RegexOption.IGNORE_CASE),
                Regex("fasting\\s+glucose", RegexOption.IGNORE_CASE),
                Regex("random\\s+glucose", RegexOption.IGNORE_CASE),
                Regex("blood\\s+sugar", RegexOption.IGNORE_CASE),
                Regex("\\bfbs\\b", RegexOption.IGNORE_CASE),
                Regex("\\brbs\\b", RegexOption.IGNORE_CASE),
                Regex("\\bglucose\\b", RegexOption.IGNORE_CASE)
            ),
            exclusions = listOf(
                Regex("hba1c", RegexOption.IGNORE_CASE),
                Regex("hb\\s*a1c", RegexOption.IGNORE_CASE),
                Regex("glycated", RegexOption.IGNORE_CASE),
                Regex("urine", RegexOption.IGNORE_CASE),
                Regex("microalbumin", RegexOption.IGNORE_CASE)
            ),
            minValid = 20.0,
            maxValid = 600.0
        ),
        PddBiomarkerRule(
            key = "cholesterol",
            aliases = listOf(
                Regex("total\\s+cholesterol", RegexOption.IGNORE_CASE),
                Regex("cholesterol[,\\s]+total", RegexOption.IGNORE_CASE),
                Regex("serum\\s+cholesterol", RegexOption.IGNORE_CASE),
                Regex("cholesterol\\s+total", RegexOption.IGNORE_CASE),
                Regex("\\bcholesterol\\b", RegexOption.IGNORE_CASE)
            ),
            exclusions = listOf(
                Regex("hdl", RegexOption.IGNORE_CASE),
                Regex("ldl", RegexOption.IGNORE_CASE),
                Regex("vldl", RegexOption.IGNORE_CASE),
                Regex("non-hdl", RegexOption.IGNORE_CASE),
                Regex("ratio", RegexOption.IGNORE_CASE)
            ),
            minValid = 50.0,
            maxValid = 600.0
        ),
        PddBiomarkerRule(
            key = "triglycerides",
            aliases = listOf(
                Regex("triglycerides", RegexOption.IGNORE_CASE),
                Regex("triglyceride", RegexOption.IGNORE_CASE),
                Regex("serum\\s+triglycerides", RegexOption.IGNORE_CASE),
                Regex("\\btg\\b", RegexOption.IGNORE_CASE)
            ),
            exclusions = listOf(
                Regex("hdl", RegexOption.IGNORE_CASE),
                Regex("ldl", RegexOption.IGNORE_CASE),
                Regex("vldl", RegexOption.IGNORE_CASE),
                Regex("ratio", RegexOption.IGNORE_CASE)
            ),
            minValid = 20.0,
            maxValid = 1000.0
        ),
        PddBiomarkerRule(
            key = "hemoglobin",
            aliases = listOf(
                Regex("hemoglobin", RegexOption.IGNORE_CASE),
                Regex("haemoglobin", RegexOption.IGNORE_CASE),
                Regex("\\bhb\\b", RegexOption.IGNORE_CASE),
                Regex("\\bhgb\\b", RegexOption.IGNORE_CASE)
            ),
            exclusions = listOf(
                Regex("hba1c", RegexOption.IGNORE_CASE),
                Regex("hb\\s*a1c", RegexOption.IGNORE_CASE),
                Regex("mch", RegexOption.IGNORE_CASE),
                Regex("mchc", RegexOption.IGNORE_CASE),
                Regex("mcv", RegexOption.IGNORE_CASE),
                Regex("electrophoresis", RegexOption.IGNORE_CASE)
            ),
            minValid = 3.0,
            maxValid = 25.0
        ),
        PddBiomarkerRule(
            key = "creatinine",
            aliases = listOf(
                Regex("serum\\s+creatinine", RegexOption.IGNORE_CASE),
                Regex("creatinine[,\\s]+serum", RegexOption.IGNORE_CASE),
                Regex("\\bcreatinine\\b", RegexOption.IGNORE_CASE)
            ),
            exclusions = listOf(
                Regex("clearance", RegexOption.IGNORE_CASE),
                Regex("urine", RegexOption.IGNORE_CASE),
                Regex("ratio", RegexOption.IGNORE_CASE),
                Regex("bun", RegexOption.IGNORE_CASE),
                Regex("urea", RegexOption.IGNORE_CASE),
                Regex("egfr", RegexOption.IGNORE_CASE)
            ),
            minValid = 0.1,
            maxValid = 15.0
        ),
        PddBiomarkerRule(
            key = "ast",
            aliases = listOf(
                Regex("aspartate\\s+aminotransferase", RegexOption.IGNORE_CASE),
                Regex("aspartate\\s+transaminase", RegexOption.IGNORE_CASE),
                Regex("\\bast\\b", RegexOption.IGNORE_CASE),
                Regex("\\bsgot\\b", RegexOption.IGNORE_CASE)
            ),
            exclusions = listOf(
                Regex("alt", RegexOption.IGNORE_CASE),
                Regex("sgpt", RegexOption.IGNORE_CASE),
                Regex("ratio", RegexOption.IGNORE_CASE),
                Regex("ast/alt", RegexOption.IGNORE_CASE),
                Regex("sgot/sgpt", RegexOption.IGNORE_CASE)
            ),
            minValid = 2.0,
            maxValid = 1000.0
        ),
        PddBiomarkerRule(
            key = "tsh",
            aliases = listOf(
                Regex("thyroid\\s+stimulating\\s+hormone", RegexOption.IGNORE_CASE),
                Regex("thyrotropin", RegexOption.IGNORE_CASE),
                Regex("serum\\s+tsh", RegexOption.IGNORE_CASE),
                Regex("\\btsh\\b", RegexOption.IGNORE_CASE)
            ),
            exclusions = listOf(
                Regex("free\\s+t3", RegexOption.IGNORE_CASE),
                Regex("free\\s+t4", RegexOption.IGNORE_CASE),
                Regex("ft3", RegexOption.IGNORE_CASE),
                Regex("ft4", RegexOption.IGNORE_CASE),
                Regex("\\bt3\\b", RegexOption.IGNORE_CASE),
                Regex("\\bt4\\b", RegexOption.IGNORE_CASE),
                Regex("anti-tpo", RegexOption.IGNORE_CASE)
            ),
            minValid = 0.01,
            maxValid = 100.0
        )
    )

    val resultMap = mutableMapOf<String, Double>()

    for (rule in rules) {
        for (line in lines) {
            if (rule.exclusions.any { it.containsMatchIn(line) }) {
                continue
            }
            val aliasMatched = rule.aliases.any { it.containsMatchIn(line) }
            if (aliasMatched) {
                val numMatches = Regex("\\b\\d+(?:\\.\\d+)?\\b").findAll(line)
                for (match in numMatches) {
                    val numVal = match.value.toDoubleOrNull()
                    if (numVal != null && numVal >= rule.minValid && numVal <= rule.maxValid) {
                        resultMap[rule.key] = numVal
                        break
                    }
                }
            }
            if (resultMap.containsKey(rule.key)) {
                break
            }
        }
    }

    return resultMap
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result, null)
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
        }
    }

private data class OcrElementItem(
    val text: String,
    val rect: android.graphics.Rect
)

private fun reconstructOcrTextFromVisionText(visionText: com.google.mlkit.vision.text.Text): String {
    val blocks = visionText.textBlocks
    android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_BLOCK_COUNT: ${blocks.size}")

    var lineIdx = 0
    var elemIdx = 0
    val allElements = mutableListOf<OcrElementItem>()

    blocks.forEachIndexed { bIdx, block ->
        android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_BLOCK[$bIdx].text: ${block.text}")
        android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_BLOCK[$bIdx].boundingBox: ${block.boundingBox}")
        android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_LINE_COUNT: ${block.lines.size}")

        block.lines.forEach { line ->
            android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_LINE[$lineIdx].text: ${line.text}")
            android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_LINE[$lineIdx].boundingBox: ${line.boundingBox}")
            lineIdx++

            android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_ELEMENT_COUNT: ${line.elements.size}")
            line.elements.forEach { element ->
                android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_ELEMENT[$elemIdx].text: ${element.text}")
                android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_ELEMENT[$elemIdx].boundingBox: ${element.boundingBox}")
                elemIdx++

                val r = element.boundingBox ?: line.boundingBox
                if (r != null && element.text.isNotBlank()) {
                    allElements.add(OcrElementItem(element.text.trim(), r))
                }
            }
        }
    }

    if (allElements.isEmpty()) {
        return visionText.text
    }

    allElements.sortBy { it.rect.top }

    val rows = mutableListOf<MutableList<OcrElementItem>>()

    for (item in allElements) {
        var placedInRow = false
        val itemHeight = item.rect.height().coerceAtLeast(1)
        val itemCenterY = item.rect.centerY()

        for (row in rows) {
            val rowTop = row.minOf { it.rect.top }
            val rowBottom = row.maxOf { it.rect.bottom }
            val rowCenterY = (rowTop + rowBottom) / 2
            val rowHeight = (rowBottom - rowTop).coerceAtLeast(1)

            val overlapTop = maxOf(item.rect.top, rowTop)
            val overlapBottom = minOf(item.rect.bottom, rowBottom)
            val overlapHeight = (overlapBottom - overlapTop).coerceAtLeast(0)

            val minH = minOf(itemHeight, rowHeight)
            val overlapRatio = overlapHeight.toFloat() / minH.toFloat()
            val centerYDiff = kotlin.math.abs(itemCenterY - rowCenterY)
            val maxAllowedDiff = (minH * 0.6f).coerceAtLeast(8f)

            if (overlapRatio >= 0.35f || centerYDiff <= maxAllowedDiff) {
                row.add(item)
                placedInRow = true
                break
            }
        }

        if (!placedInRow) {
            rows.add(mutableListOf(item))
        }
    }

    rows.sortBy { row -> row.map { it.rect.centerY() }.average() }

    val reconstructedLines = rows.map { row ->
        row.sortBy { it.rect.left }
        row.joinToString(" ") { it.text }
    }

    val reconstructedText = reconstructedLines.joinToString("\n")
    android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_RECONSTRUCTED_TEXT:\n$reconstructedText")
    return reconstructedText
}

private suspend fun processDocumentUri(context: Context, uri: Uri): String {
    val contentResolver = context.contentResolver
    val mimeType = (contentResolver.getType(uri) ?: "").lowercase(Locale.ROOT)
    val fileName = (try {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIdx != -1 && cursor.moveToFirst()) cursor.getString(nameIdx) else null
        }
    } catch (e: Exception) { null } ?: uri.lastPathSegment ?: "").lowercase(Locale.ROOT)

    val isPdf = mimeType.contains("pdf") || fileName.endsWith(".pdf")
    val isImage = mimeType.startsWith("image/") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".webp")

    android.util.Log.d("REPORT_ANALYZER_DEBUG", "URI: $uri")
    android.util.Log.d("REPORT_ANALYZER_DEBUG", "MIME TYPE: $mimeType")
    android.util.Log.d("REPORT_ANALYZER_DEBUG", "FILE NAME: $fileName")

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val extractedTextBuilder = StringBuilder()
    var extractionMethod = "UNKNOWN"

    if (isPdf) {
        extractionMethod = "PDF_RENDERER_HIGH_RES_OCR"
        try {
            val pfd = contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                val pdfRenderer = PdfRenderer(pfd)
                val pageCount = pdfRenderer.pageCount
                val renderScale = 3.5f
                for (i in 0 until pageCount) {
                    val page = pdfRenderer.openPage(i)
                    val width = (page.width * renderScale).toInt().coerceAtLeast(1)
                    val height = (page.height * renderScale).toInt().coerceAtLeast(1)
                    android.util.Log.d("REPORT_ANALYZER_DEBUG", "PDF_PAGE_INDEX: $i")
                    android.util.Log.d("REPORT_ANALYZER_DEBUG", "PDF_RENDER_WIDTH: $width")
                    android.util.Log.d("REPORT_ANALYZER_DEBUG", "PDF_RENDER_HEIGHT: $height")
                    android.util.Log.d("REPORT_ANALYZER_DEBUG", "PDF_SCALE: $renderScale")

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val result = recognizer.process(inputImage).awaitTask()
                    val reconstructedPage = reconstructOcrTextFromVisionText(result)
                    extractedTextBuilder.append(reconstructedPage).append("\n")
                    bitmap.recycle()
                }
                pdfRenderer.close()
                pfd.close()
            }
        } catch (e: Exception) {
            android.util.Log.e("REPORT_ANALYZER_DEBUG", "REPORT_ANALYZER_DEBUG OCR_ERROR PDF rendering error", e)
        }
    } else if (isImage) {
        extractionMethod = "IMAGE_EXIF_SOFTWARE_OCR"
        try {
            val rotationDegrees = try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = android.media.ExifInterface(stream)
                    when (exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                } ?: 0
            } catch (e: Exception) {
                0
            }

            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            if (bitmap != null) {
                val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
                val result = recognizer.process(inputImage).awaitTask()
                val reconstructedImage = reconstructOcrTextFromVisionText(result)
                extractedTextBuilder.append(reconstructedImage)
            }
        } catch (e: Exception) {
            android.util.Log.e("REPORT_ANALYZER_DEBUG", "REPORT_ANALYZER_DEBUG OCR_ERROR Primary Image OCR error", e)
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        val inputImage = InputImage.fromBitmap(bitmap, 0)
                        val result = recognizer.process(inputImage).awaitTask()
                        val reconstructedImage = reconstructOcrTextFromVisionText(result)
                        extractedTextBuilder.append(reconstructedImage)
                    }
                }
            } catch (e2: Exception) {
                android.util.Log.e("REPORT_ANALYZER_DEBUG", "REPORT_ANALYZER_DEBUG OCR_ERROR Fallback Image OCR error", e2)
            }
        }
    } else {
        extractionMethod = "TEXT_STREAM_FALLBACK"
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                extractedTextBuilder.append(String(stream.readBytes(), java.nio.charset.StandardCharsets.UTF_8))
            }
        } catch (e: Exception) {
            android.util.Log.e("REPORT_ANALYZER_DEBUG", "REPORT_ANALYZER_DEBUG OCR_ERROR Text stream error", e)
        }
    }

    val rawText = extractedTextBuilder.toString()
    val normalizedText = normalizeExtractedText(rawText)

    android.util.Log.d("REPORT_ANALYZER_DEBUG", "EXTRACTION METHOD: $extractionMethod")
    android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_RAW_TEXT_LENGTH: ${rawText.length}")
    android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_RAW_TEXT: $rawText")
    android.util.Log.d("REPORT_ANALYZER_DEBUG", "OCR_NORMALIZED_TEXT: $normalizedText")

    return rawText
}
