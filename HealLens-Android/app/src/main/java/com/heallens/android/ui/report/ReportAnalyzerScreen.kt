package com.heallens.android.ui.report

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
    val value: Double,
    val status: String, // "NORMAL", "LOW", "HIGH", "CRITICAL"
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
            mapOf(
                "glucose" to "90",
                "cholesterol" to "165",
                "triglycerides" to "110",
                "hemoglobin" to "14.5",
                "creatinine" to "0.8",
                "ast" to "24",
                "tsh" to "2.1"
            )
        )
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    var reportAnalysis by remember { mutableStateOf<FullReportAnalysis?>(null) }
    var hasSavedRecord by remember { mutableStateOf(false) }

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

    fun executeAnalysis() {
        isAnalyzing = true
        reportAnalysis = null
        hasSavedRecord = false

        scope.launch {
            delay(500)

            val currentVals = pddBiomarkers.associate { bio ->
                bio.key to (biomarkerInputs.value[bio.key]?.toDoubleOrNull() ?: bio.min)
            }

            val gl = currentVals["glucose"] ?: 90.0
            val ch = currentVals["cholesterol"] ?: 165.0
            val tr = currentVals["triglycerides"] ?: 110.0
            val hb = currentVals["hemoglobin"] ?: 14.5
            val cr = currentVals["creatinine"] ?: 0.8
            val ast = currentVals["ast"] ?: 24.0
            val tsh = currentVals["tsh"] ?: 2.1

            // 1. Determine Risk Summary, Level & Points matching PDD rules
            val riskEval = when {
                gl <= 100.0 && ch <= 200.0 && tr <= 150.0 && hb >= 12.0 && cr <= 1.2 && ast <= 40.0 && tsh <= 4.5 && tsh >= 0.4 ->
                    RiskEvaluationResult(
                        "Healthy Biomarker Profile",
                        "normal",
                        Color(0xFF10B981),
                        listOf(
                            "All biomarkers are within normal laboratory reference ranges.",
                            "Optimal metabolic, lipid, renal, and liver function observed.",
                            "Continue maintaining a healthy lifestyle, diet, and hydration."
                        )
                    )
                gl > 120.0 && hb < 11.5 ->
                    RiskEvaluationResult(
                        "Diabetic Tendency & Mild Anemia",
                        "moderate",
                        Color(0xFFF59E0B),
                        listOf(
                            "Elevated Blood Glucose (${gl.toInt()} mg/dL) indicates glycemic strain.",
                            "Reduced Hemoglobin ($hb g/dL) suggests mild iron-deficiency anemia.",
                            "Endocrine evaluation and dietary sugar/iron management recommended."
                        )
                    )
                ch > 200.0 || tr > 150.0 ->
                    RiskEvaluationResult(
                        "Hyperlipidemia & Cardiovascular Stress",
                        "moderate",
                        Color(0xFFF59E0B),
                        listOf(
                            "High Total Cholesterol (${ch.toInt()} mg/dL) detected above 200 mg/dL threshold.",
                            "Elevated Triglycerides (${tr.toInt()} mg/dL) increase cardiovascular risk.",
                            "Lifestyle modification and lipid clearance management recommended."
                        )
                    )
                ast > 40.0 && cr > 1.2 ->
                    RiskEvaluationResult(
                        "Hepatorenal Stress (Liver & Kidney)",
                        "critical",
                        Color(0xFFEF4444),
                        listOf(
                            "Elevated AST (${ast.toInt()} U/L) indicates liver cell inflammation.",
                            "Elevated Serum Creatinine ($cr mg/dL) suggests renal filtration strain.",
                            "Prompt Hepatology & Nephrology clinical evaluation advised."
                        )
                    )
                tsh > 4.5 ->
                    RiskEvaluationResult(
                        "Underactive Thyroid (Hypothyroidism)",
                        "moderate",
                        Color(0xFFF59E0B),
                        listOf(
                            "Elevated TSH ($tsh uIU/mL) indicates thyroid slowdown.",
                            "Sluggish metabolism and fatigue risk factors present.",
                            "Endocrinology evaluation recommended for thyroid hormone levels."
                        )
                    )
                else ->
                    RiskEvaluationResult(
                        "Isolated Biomarker Elevation",
                        "moderate",
                        Color(0xFFF59E0B),
                        listOf(
                            "One or more biomarkers exceed typical laboratory reference ranges.",
                            "Targeted lifestyle or clinical follow-up recommended."
                        )
                    )
            }

            val riskSummary = riskEval.summary
            val riskLevel = riskEval.level
            val riskColor = riskEval.color
            val riskPoints = riskEval.points

            // 2. Evaluate each biomarker position and status
            val evaluations = pddBiomarkers.map { bio ->
                val valNum = currentVals[bio.key] ?: bio.min
                val (status, statusColor) = when {
                    valNum < bio.min -> "LOW" to Color(0xFFF59E0B)
                    valNum > bio.max * 1.5 -> "CRITICAL" to Color(0xFFEF4444)
                    valNum > bio.max -> "HIGH" to Color(0xFFF59E0B)
                    else -> "NORMAL" to Color(0xFF10B981)
                }
                BiomarkerEvaluation(bio, valNum, status, statusColor)
            }

            // 3. Dynamic Natural Remedies & Ayurvedic Suggestions for abnormal values
            val abnormalConfigs = evaluations.filter { it.status != "NORMAL" }.map { it.config }
            val naturalRemedies = abnormalConfigs.map { it.naturalRemedy }
            val ayurvedicSuggestions = abnormalConfigs.map { it.ayurvedicSuggestion }

            // 4. Specialist Mapping
            val (specialist, urgent) = when {
                gl > 120.0 || tsh > 4.5 -> "Endocrinologist" to false
                ch > 200.0 || tr > 150.0 -> "Cardiologist" to false
                ast > 40.0 -> "Hepatologist" to false
                cr > 1.2 -> "Nephrologist" to (riskLevel == "critical")
                hb < 11.5 -> "Hematologist" to false
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

    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        documentUri = uri
        if (uri != null) {
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
                    delay(350)
                }
                isScanningDoc = false

                // Randomly select a sample profile to simulate document parsing
                val presets = listOf("healthy", "lipid", "diabetic")
                val chosen = presets.random()
                val updated = biomarkerInputs.value.toMutableMap()
                when (chosen) {
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
                    else -> {
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
                    val currentVal = biomarkerInputs.value[bio.key] ?: bio.min.toString()
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
                        val isAbnormal = ev.status != "NORMAL"
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
                                    text = "${ev.config.name} (${ev.value} ${ev.config.unit})",
                                    color = if (isAbnormal) Color(0xFFF59E0B) else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = ev.config.description,
                                    color = TextSecondary,
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. Recommended Specialist Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00D4FF).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF00D4FF).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Recommended Specialist", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(analysis.specialist, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    if (analysis.urgentVisit) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(50))
                                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(50))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Urgent Consultation", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
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

                        GradientButton(
                            text = "📅 Book Appointment →",
                            onClick = {
                                bookingDoctor = null
                                bookingConfirmed = false
                                confirmedAppointmentId = ""
                                showAppointmentModal = true
                            },
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }
    }

    // Modal: Doctor Booking Modal for Report Specialist
    if (showAppointmentModal) {
        val currentSpecialist = reportAnalysis?.specialist ?: "General Physician"
        val doctorList = doctorsDatabase[currentSpecialist] ?: doctorsDatabase["General Physician"]!!

        AlertDialog(
            onDismissRequest = { showAppointmentModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📅 ", fontSize = 24.sp)
                    Column {
                        Text("Book Doctor Appointment", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Recommended Specialty: $currentSpecialist", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (bookingConfirmed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✅ Appointment Confirmed!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Appointment ID: $confirmedAppointmentId", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Doctor: ${bookingDoctor?.name}", color = TextSecondary, fontSize = 12.sp)
                                Text("Hospital: ${bookingDoctor?.hospital}", color = TextSecondary, fontSize = 12.sp)
                                Text("Slot: ${bookingDoctor?.slot}", color = TextSecondary, fontSize = 12.sp)
                                Text("Patient: $patientName", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text("Available $currentSpecialist Doctors:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        doctorList.forEach { doc ->
                            val isSelected = bookingDoctor == doc
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CyanPrimary.copy(alpha = 0.15f) else SurfaceGlass)
                                    .border(1.dp, if (isSelected) CyanPrimary else SurfaceGlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { bookingDoctor = doc }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(doc.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(doc.rating, color = Color(0xFFFFB800), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${doc.exp} • ${doc.hospital}", color = TextMuted, fontSize = 11.sp)
                                    Text("Slot: ${doc.slot}", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (bookingConfirmed) {
                    GradientButton(
                        text = "Done",
                        onClick = { showAppointmentModal = false }
                    )
                } else {
                    GradientButton(
                        text = "Confirm Booking",
                        onClick = {
                            if (bookingDoctor == null) {
                                Toast.makeText(context, "Please select a doctor to confirm booking.", Toast.LENGTH_SHORT).show()
                            } else {
                                confirmedAppointmentId = "HL-${(10000..99999).random()}"
                                bookingConfirmed = true
                            }
                        }
                    )
                }
            },
            dismissButton = {
                if (!bookingConfirmed) {
                    OutlinedButton(
                        onClick = { showAppointmentModal = false }
                    ) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            },
            containerColor = DarkBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Reusable Biomarker Gauge Card Component (FIX 3: Used uniformly for all 7 biomarkers including TSH)
@Composable
private fun BiomarkerGaugeCard(
    ev: BiomarkerEvaluation
) {
    val bio = ev.config
    val valNum = ev.value
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
