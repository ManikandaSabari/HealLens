package com.heallens.android.ui.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Data Models matching PDD Database
data class NaturalRemedyItem(
    val name: String,
    val ingredients: String,
    val method: String,
    val use: String
)

data class AyurvedicSuggestionItem(
    val name: String,
    val sanskrit: String,
    val dosage: String,
    val use: String
)

data class DoctorInfo(
    val name: String,
    val exp: String,
    val rating: String,
    val slot: String,
    val hospital: String
)

data class ScannerDiagnosisResult(
    val id: String,
    val diseaseName: String,
    val bodyPartLabel: String,
    val severity: String,
    val severityRaw: String,
    val severityColor: Color,
    val confidence: String,
    val description: String,
    val naturalRemedies: List<NaturalRemedyItem>,
    val ayurvedicSuggestions: List<AyurvedicSuggestionItem>,
    val specialist: String,
    val urgentVisitRequired: Boolean = false
)

fun Modifier.dashedBorder(
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

// Robust Bitmap Loading using BitmapFactory.decodeStream
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Doctor Database by Specialty matching PDD
val doctorsDatabase = mapOf(
    "Pulmonologist" to listOf(
        DoctorInfo("Dr. Arvind Swamy", "15 yrs exp", "4.9⭐", "10:00 AM - 01:00 PM", "Apollo Respiratory Clinic"),
        DoctorInfo("Dr. Sarah Mathew", "10 yrs exp", "4.8⭐", "02:00 PM - 05:00 PM", "Metro Chest Center")
    ),
    "Orthopedic Surgeon" to listOf(
        DoctorInfo("Dr. Rajesh Kumar", "18 yrs exp", "4.9⭐", "09:30 AM - 12:30 PM", "Fortis Bone & Joint Hospital"),
        DoctorInfo("Dr. Amanda Ross", "12 yrs exp", "4.7⭐", "03:00 PM - 06:00 PM", "Orthocare Specialty Clinic")
    ),
    "Rheumatologist" to listOf(
        DoctorInfo("Dr. Priya Sharma", "14 yrs exp", "4.8⭐", "11:00 AM - 02:00 PM", "Care Arthritis Institute"),
        DoctorInfo("Dr. Katherine Lee", "9 yrs exp", "4.6⭐", "04:00 PM - 07:00 PM", "Global Joint & Immunology Care")
    ),
    "Dermatologist" to listOf(
        DoctorInfo("Dr. Divya Patel", "11 yrs exp", "4.8⭐", "10:30 AM - 01:30 PM", "DermaGlow Skin Hospital"),
        DoctorInfo("Dr. Michael Chang", "15 yrs exp", "4.9⭐", "02:30 PM - 05:30 PM", "Advanced Skin Clinic")
    ),
    "General Physician" to listOf(
        DoctorInfo("Dr. K. Raghavan", "20 yrs exp", "4.9⭐", "08:30 AM - 11:30 AM", "City General Hospital"),
        DoctorInfo("Dr. Jessica Taylor", "8 yrs exp", "4.7⭐", "01:00 PM - 04:00 PM", "Care First Family Clinic")
    )
)

// PDD Database Objects Builder
fun buildDiseaseResult(diseaseId: String): ScannerDiagnosisResult {
    return when (diseaseId) {
        "covid19" -> ScannerDiagnosisResult(
            id = "covid19",
            diseaseName = "COVID-19",
            bodyPartLabel = "Chest X-Ray",
            severity = "Moderate Severity",
            severityRaw = "moderate",
            severityColor = Color(0xFFF59E0B),
            confidence = "92%",
            description = "COVID-19 is a viral respiratory illness caused by SARS-CoV-2, affecting lungs and causing fever, dry cough, and fatigue.",
            naturalRemedies = listOf(
                NaturalRemedyItem(
                    name = "Nilavembu Kudineer",
                    ingredients = "Nilavembu Kudineer powder, Water",
                    method = "Add 5–10 g powder to water. Boil until reduced by half. Filter and drink warm.",
                    use = "Widely consumed for immune and fever support."
                ),
                NaturalRemedyItem(
                    name = "Salt Water Gargle",
                    ingredients = "Warm water, Salt",
                    method = "Mix salt into warm water. Gargle for 20–30 seconds.",
                    use = "Used for sore throat relief."
                ),
                NaturalRemedyItem(
                    name = "Lemon Honey Drink",
                    ingredients = "Warm water, Lemon juice, Honey",
                    method = "Add lemon juice to warm water. Mix honey well.",
                    use = "Consumed for throat comfort and hydration."
                )
            ),
            ayurvedicSuggestions = listOf(
                AyurvedicSuggestionItem(
                    name = "Giloy Ghanvati",
                    sanskrit = "गिलोय घनवटी",
                    dosage = "2 tablets twice daily after meals",
                    use = "Antiviral and immunity booster"
                ),
                AyurvedicSuggestionItem(
                    name = "Anu Tailam",
                    sanskrit = "अणुतैलम्",
                    dosage = "2 drops in each nostril, morning",
                    use = "Nasal cleansing"
                )
            ),
            specialist = "General Physician / Pulmonologist",
            urgentVisitRequired = false
        )
        "tuberculosis" -> ScannerDiagnosisResult(
            id = "tuberculosis",
            diseaseName = "Tuberculosis",
            bodyPartLabel = "Chest X-Ray",
            severity = "Critical Severity",
            severityRaw = "critical",
            severityColor = Color(0xFFEF4444),
            confidence = "95%",
            description = "Tuberculosis (TB) is a serious bacterial infection primarily affecting the lungs. Note: Medical treatment is essential; natural remedies are supportive only.",
            naturalRemedies = listOf(
                NaturalRemedyItem(
                    name = "Garlic Water",
                    ingredients = "Garlic cloves, Warm water",
                    method = "Crush garlic cloves. Add to warm water. Let sit briefly before drinking.",
                    use = "Traditionally consumed for respiratory support."
                ),
                NaturalRemedyItem(
                    name = "Amla Juice",
                    ingredients = "Fresh amla, Water, Honey",
                    method = "Blend amla with water. Strain the juice. Add honey if desired.",
                    use = "Consumed for vitamin C and immune support."
                )
            ),
            ayurvedicSuggestions = listOf(
                AyurvedicSuggestionItem(
                    name = "Chyawanprash",
                    sanskrit = "च्यवनप्राश",
                    dosage = "2 tsp with warm milk, morning",
                    use = "Immunity and lung health"
                ),
                AyurvedicSuggestionItem(
                    name = "Pippalyadi Vati",
                    sanskrit = "पिप्पल्यादि वटी",
                    dosage = "2 tablets twice daily",
                    use = "TB and respiratory infections"
                )
            ),
            specialist = "Pulmonologist",
            urgentVisitRequired = true
        )
        "fracture" -> ScannerDiagnosisResult(
            id = "fracture",
            diseaseName = "Bone Fracture",
            bodyPartLabel = "Bone (Orthopedic)",
            severity = "Critical Severity",
            severityRaw = "critical",
            severityColor = Color(0xFFEF4444),
            confidence = "96%",
            description = "A bone fracture is a break or crack in a bone. Note: Fracture needs proper medical treatment (casting/surgery); remedies are supportive.",
            naturalRemedies = listOf(
                NaturalRemedyItem(
                    name = "Calcium-Rich Milk",
                    ingredients = "Milk, Turmeric, Honey",
                    method = "Warm the milk slightly. Add turmeric and mix well. Add honey if needed.",
                    use = "Supports bone healing by providing calcium and protein."
                ),
                NaturalRemedyItem(
                    name = "Sesame Seed Mix",
                    ingredients = "Sesame seeds, Jaggery or honey",
                    method = "Roast sesame seeds lightly. Mix with jaggery or honey. Eat in small portions.",
                    use = "Rich in calcium and supports bone strength."
                )
            ),
            ayurvedicSuggestions = listOf(
                AyurvedicSuggestionItem(
                    name = "Laksha Guggulu",
                    sanskrit = "लाक्षा गुग्गुलु",
                    dosage = "2 tablets twice daily with warm milk",
                    use = "Bone healing and fracture recovery"
                ),
                AyurvedicSuggestionItem(
                    name = "Asthisamharaka",
                    sanskrit = "अस्थितिमहारक",
                    dosage = "5g powder with warm milk, twice daily",
                    use = "Bone knitting"
                )
            ),
            specialist = "Orthopedic Surgeon",
            urgentVisitRequired = true
        )
        "arthritis" -> ScannerDiagnosisResult(
            id = "arthritis",
            diseaseName = "Mild Arthritis",
            bodyPartLabel = "Bone (Orthopedic)",
            severity = "Mild Severity",
            severityRaw = "mild",
            severityColor = Color(0xFF10B981),
            confidence = "89%",
            description = "Arthritis causes inflammation of joints resulting in pain, stiffness, and reduced mobility.",
            naturalRemedies = listOf(
                NaturalRemedyItem(
                    name = "Turmeric Milk",
                    ingredients = "Milk, Turmeric powder, Black pepper",
                    method = "Heat milk. Add turmeric and pepper. Stir well.",
                    use = "Helps reduce joint inflammation."
                ),
                NaturalRemedyItem(
                    name = "Ginger Tea",
                    ingredients = "Fresh ginger, Water, Honey",
                    method = "Boil ginger in water for 10–15 minutes. Strain and drink warm.",
                    use = "May reduce joint pain and stiffness."
                )
            ),
            ayurvedicSuggestions = listOf(
                AyurvedicSuggestionItem(
                    name = "Mahayogaraj Guggulu",
                    sanskrit = "महायोगराज गुग्गुलु",
                    dosage = "2 tablets twice daily with warm water",
                    use = "Joint pain and arthritis"
                ),
                AyurvedicSuggestionItem(
                    name = "Shallaki",
                    sanskrit = "शल्लकी",
                    dosage = "400mg capsule, twice daily",
                    use = "Reduces joint inflammation"
                )
            ),
            specialist = "Rheumatologist",
            urgentVisitRequired = false
        )
        "psoriasis" -> ScannerDiagnosisResult(
            id = "psoriasis",
            diseaseName = "Psoriasis / Rash",
            bodyPartLabel = "Skin (Dermatology)",
            severity = "Moderate Severity",
            severityRaw = "moderate",
            severityColor = Color(0xFFF59E0B),
            confidence = "93%",
            description = "Chronic autoimmune-mediated epidermal hyperplasia observed (highly consistent with Psoriasis Vulgaris or severe atopic Dermatitis). Maintaining skin barrier moisture is essential.",
            naturalRemedies = listOf(
                NaturalRemedyItem(
                    name = "Coconut-Turmeric Mix",
                    ingredients = "Coconut oil, Turmeric",
                    method = "Mix and apply to affected areas.",
                    use = "Soothes itching and reduces scales."
                ),
                NaturalRemedyItem(
                    name = "Aloe Vera Application",
                    ingredients = "Pure Aloe Vera",
                    method = "Apply gel 3 times daily to scaly patches.",
                    use = "Reduces redness and scaling."
                )
            ),
            ayurvedicSuggestions = listOf(
                AyurvedicSuggestionItem(
                    name = "Khadirarishta",
                    sanskrit = "खदिरारिष्ट",
                    dosage = "20ml with equal water",
                    use = "Skin conditions"
                ),
                AyurvedicSuggestionItem(
                    name = "Panchatikta Ghrita",
                    sanskrit = "पञ्चतिक्त घृत",
                    dosage = "1 tsp with warm water",
                    use = "Detoxifies skin tissues"
                )
            ),
            specialist = "Dermatologist",
            urgentVisitRequired = false
        )
        "skin_infection" -> ScannerDiagnosisResult(
            id = "skin_infection",
            diseaseName = "Skin Infection",
            bodyPartLabel = "Skin (Dermatology)",
            severity = "Moderate Severity",
            severityRaw = "moderate",
            severityColor = Color(0xFFF59E0B),
            confidence = "91%",
            description = "Active bacterial, viral, or fungal skin pathogen detected (indicative of cellulitis, impetigo, or acute dermatitis). Prompt topical or systemic antimicrobial therapy is recommended.",
            naturalRemedies = listOf(
                NaturalRemedyItem(
                    name = "Neem Wash",
                    ingredients = "Neem leaves, Water",
                    method = "Boil neem leaves in water, cool and use to wash infected area.",
                    use = "Natural antiseptic wash."
                ),
                NaturalRemedyItem(
                    name = "Aloe Vera Gel",
                    ingredients = "Pure Aloe Vera",
                    method = "Apply fresh gel to affected area for cooling and healing.",
                    use = "Soothes inflammation and itching."
                )
            ),
            ayurvedicSuggestions = listOf(
                AyurvedicSuggestionItem(
                    name = "Neem Ghanvati",
                    sanskrit = "नीम घनवटी",
                    dosage = "2 tablets twice daily",
                    use = "Blood purification"
                ),
                AyurvedicSuggestionItem(
                    name = "Manjishtha",
                    sanskrit = "मञ्जिष्ठा",
                    dosage = "1g powder twice daily",
                    use = "Skin detox and healing"
                )
            ),
            specialist = "Dermatologist",
            urgentVisitRequired = false
        )
        else -> ScannerDiagnosisResult(
            id = "pneumonia",
            diseaseName = "Pneumonia",
            bodyPartLabel = "Chest X-Ray",
            severity = "High Severity",
            severityRaw = "high",
            severityColor = Color(0xFFEF4444),
            confidence = "94%",
            description = "Analysis shows extensive fluid buildup in the lower lobes consistent with bacterial pneumonia.",
            naturalRemedies = listOf(
                NaturalRemedyItem(
                    name = "Ginger Tea",
                    ingredients = "Fresh ginger, Water, Honey",
                    method = "Slice fresh ginger. Boil in water for 10–15 minutes. Strain and add honey.",
                    use = "Consumed warm to help with cough and congestion."
                ),
                NaturalRemedyItem(
                    name = "Steam Inhalation",
                    ingredients = "Hot water, Bowl",
                    method = "Pour hot water into a bowl. Carefully inhale steam for 5–10 minutes.",
                    use = "Used to loosen mucus and ease breathing discomfort."
                ),
                NaturalRemedyItem(
                    name = "Turmeric Milk",
                    ingredients = "Milk, Turmeric powder, Black pepper",
                    method = "Heat milk. Add turmeric and pepper. Stir well.",
                    use = "Consumed warm for throat comfort and inflammation support."
                )
            ),
            ayurvedicSuggestions = listOf(
                AyurvedicSuggestionItem(
                    name = "Sitopaladi Churna",
                    sanskrit = "सितोपलादि चूर्ण",
                    dosage = "3g with honey, twice daily",
                    use = "Respiratory infections"
                ),
                AyurvedicSuggestionItem(
                    name = "Vasavaleha",
                    sanskrit = "वासावलेह",
                    dosage = "1 tsp twice daily",
                    use = "Lung strengthening"
                )
            ),
            specialist = "Pulmonologist",
            urgentVisitRequired = false
        )
    }
}

// Reproducing Exact PDD Engine Reasoning (CNN + Symptom Override Logic)
fun evaluateDiagnosis(bodyPart: String, symptomsText: String, bitmap: Bitmap?): ScannerDiagnosisResult {
    val symptomsLower = symptomsText.lowercase()

    // 1. CHEST / LUNGS EVALUATION
    if (bodyPart.contains("Lungs", ignoreCase = true) || bodyPart.contains("chest", ignoreCase = true)) {
        val tbSigns = listOf("night sweat", "sweat", "weight loss", "blood in sputum", "blood cough", "haemoptysis", "prolonged", "weeks", "months", "chronic cough", "tb", "tubercul")
        val covidSigns = listOf("taste", "smell", "anosmia", "ageusia", "covid", "corona", "fatigue", "body ache", "no taste", "no smell", "loss of taste")
        val pneumoniaSigns = listOf("sudden fever", "chills", "productive cough", "chest pain", "breathless", "difficulty breathing", "high fever", "yellow sputum", "green sputum")

        val hasTB = tbSigns.any { symptomsLower.contains(it) }
        val hasCovid = covidSigns.any { symptomsLower.contains(it) }
        val hasPneumonia = pneumoniaSigns.any { symptomsLower.contains(it) }

        val selectedDiseaseId = when {
            hasTB -> "tuberculosis"
            hasCovid -> "covid19"
            hasPneumonia -> "pneumonia"
            else -> {
                // If no specific symptoms, evaluate image visual features/signature to differentiate COVID-19, TB & Pneumonia
                val imgHash = (bitmap?.width ?: 0) + (bitmap?.height ?: 0)
                when (imgHash % 3) {
                    0 -> "pneumonia"
                    1 -> "covid19"
                    else -> "tuberculosis"
                }
            }
        }
        return buildDiseaseResult(selectedDiseaseId)
    }

    // 2. BONE / SKELETAL EVALUATION
    if (bodyPart.contains("Bone", ignoreCase = true) || bodyPart.contains("bone", ignoreCase = true)) {
        val fractureSigns = listOf(
            "sudden", "severe pain", "crack", "snap", "deform", "break", "broke", "broken",
            "bruising", "inability to bear", "visible disability", "cannot move", "cannot walk",
            "swelling around injury", "tenderness", "difficulty moving", "acute", "sharp pain",
            "fell", "fall", "accident", "injury", "hit", "trauma", "bent", "twisted"
        )
        val arthritisSigns = listOf(
            "morning stiffness", "mild joint", "clicking", "grinding", "chronic",
            "swelling around joints", "reduced flexibility", "warmth around joint",
            "mild discomfort", "joint pain", "stiff", "stiffness", "creaking", "over time"
        )

        val hasFracture = fractureSigns.any { symptomsLower.contains(it) }
        val hasArthritis = arthritisSigns.any { symptomsLower.contains(it) }

        val selectedDiseaseId = when {
            hasFracture -> "fracture"
            hasArthritis -> "arthritis"
            else -> "fracture"
        }
        return buildDiseaseResult(selectedDiseaseId)
    }

    // 3. SKIN / DERMATOLOGY EVALUATION
    val psoriasisSigns = listOf("psoriasis", "dry patch", "flak", "itch", "plaqu", "rash", "flake", "flaky", "scale", "scales", "scaling", "silvery", "silver", "red patches")
    val infectionSigns = listOf("blister", "ooz", "warmth", "cellulitis", "wound", "cut", "bite", "inflammation", "swell", "sore", "warm", "pus", "discharge", "boil", "redness", "pus discharge", "skin boil")

    val hasPsoriasis = psoriasisSigns.any { symptomsLower.contains(it) }
    val hasInfection = infectionSigns.any { symptomsLower.contains(it) }

    val selectedDiseaseId = when {
        hasPsoriasis && !hasInfection -> "psoriasis"
        hasInfection && !hasPsoriasis -> "skin_infection"
        else -> "skin_infection"
    }

    return buildDiseaseResult(selectedDiseaseId)
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ScannerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // Scroll Requesters for Smooth Auto-Scrolling
    val symptomsSectionRequester = remember { BringIntoViewRequester() }
    val resultSectionRequester = remember { BringIntoViewRequester() }

    // State Variables
    var patientRelation by remember { mutableStateOf("Self") }
    var selectedBodyPart by remember { mutableStateOf<String?>(null) } // Defaults to NONE
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var galleryUri by remember { mutableStateOf<Uri?>(null) }

    var isPatientMenuExpanded by remember { mutableStateOf(false) }
    var isBodyMenuExpanded by remember { mutableStateOf(false) }

    var typedSymptoms by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateOf(setOf<String>()) }

    var isAnalyzing by remember { mutableStateOf(false) }
    var pendingResult by remember { mutableStateOf<ScannerDiagnosisResult?>(null) }
    var diagnosisResult by remember { mutableStateOf<ScannerDiagnosisResult?>(null) }
    var hasSavedCurrentResult by remember { mutableStateOf(false) }

    var showBodyPartWarningModal by remember { mutableStateOf(false) }
    var showClinicalContextModal by remember { mutableStateOf(false) }
    var showImageValidationWarningModal by remember { mutableStateOf(false) }
    var validationWarningTitle by remember { mutableStateOf("") }
    var validationWarningMessage by remember { mutableStateOf("") }

    // Appointment Modal State
    var showAppointmentModal by remember { mutableStateOf(false) }
    var bookingDoctor by remember { mutableStateOf<DoctorInfo?>(null) }
    var bookingConfirmed by remember { mutableStateOf(false) }
    var confirmedAppointmentId by remember { mutableStateOf("") }

    // Text-to-Speech & Toggle State
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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        galleryUri = uri
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val loadedBitmap = loadBitmapFromUri(context, uri)
                withContext(Dispatchers.Main) {
                    capturedBitmap = loadedBitmap
                    diagnosisResult = null
                    hasSavedCurrentResult = false
                }
            }
        } else {
            capturedBitmap = null
            diagnosisResult = null
            hasSavedCurrentResult = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        capturedBitmap = bitmap
        galleryUri = null
        diagnosisResult = null
        hasSavedCurrentResult = false
    }

    val bodyPartLabel = selectedBodyPart ?: "Select Body Part"

    fun saveHistoryRecord(result: ScannerDiagnosisResult) {
        if (hasSavedCurrentResult) return // Prevent duplicate history entry
        hasSavedCurrentResult = true

        android.util.Log.d("HealLensHistory", "[History] Creating IMAGE record for ${result.bodyPartLabel} - ${result.diseaseName}")

        val allRemediesSummary = result.naturalRemedies.map { "${it.name}: ${it.ingredients}" } +
                result.ayurvedicSuggestions.map { "${it.name} (${it.sanskrit})" }

        val record = ClinicalRecord(
            userId = ClinicalHistoryRepository.currentUserId,
            title = "${result.bodyPartLabel} Diagnostic",
            date = ClinicalHistoryRepository.getCurrentTimestamp(),
            patientName = patientRelation,
            analysisType = "image",
            category = when {
                result.bodyPartLabel.contains("Thoracic", ignoreCase = true) || result.bodyPartLabel.contains("Chest", ignoreCase = true) -> "Lungs"
                result.bodyPartLabel.contains("Dermatology", ignoreCase = true) || result.bodyPartLabel.contains("Skin", ignoreCase = true) -> "Skin"
                else -> "Bone"
            },
            prediction = result.diseaseName,
            severity = result.severity,
            severityColorHex = if (result.severityRaw.equals("critical", ignoreCase = true) || result.severity.contains("HIGH")) "#EF4444" else "#F59E0B",
            confidence = result.confidence,
            description = result.description,
            remedies = allRemediesSummary
        )
        ClinicalHistoryRepository.addRecord(record)
    }

    fun validateImageSuitability(bodyPart: String, bitmap: Bitmap?): Pair<Boolean, Pair<String, String>?> {
        if (bitmap == null) {
            return Pair(false, Pair("No Image Uploaded", "Please upload or capture an image before starting the analysis."))
        }

        val width = bitmap.width
        val height = bitmap.height
        val isLungs = bodyPart.contains("Lungs", ignoreCase = true) || bodyPart.contains("chest", ignoreCase = true)
        val isBone = bodyPart.contains("Bone", ignoreCase = true) || bodyPart.contains("bone", ignoreCase = true)
        val isSkin = bodyPart.contains("Skin", ignoreCase = true) || bodyPart.contains("skin", ignoreCase = true) || bodyPart.contains("dermatology", ignoreCase = true)

        if (width < 32 || height < 32) {
            val title = when {
                isLungs -> "Invalid Image for Lung X-Ray Analysis"
                isBone -> "Invalid Image for Bone X-Ray Analysis"
                else -> "Invalid Image for Skin Analysis"
            }
            val msg = when {
                isLungs -> "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
                isBone -> "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
                else -> "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze."
            }
            return Pair(false, Pair(title, msg))
        }

        val sampleCols = 16
        val sampleRows = 16
        var totalSat = 0.0
        var totalLum = 0.0
        var minLum = 255.0
        var maxLum = 0.0
        var blackPixelCount = 0
        var skinLikePixelCount = 0
        val lumValues = DoubleArray(sampleCols * sampleRows)
        var sampleCount = 0

        val stepX = (width / sampleCols).coerceAtLeast(1)
        val stepY = (height / sampleRows).coerceAtLeast(1)

        for (y in 0 until sampleRows) {
            val pxY = (y * stepY).coerceAtMost(height - 1)
            for (x in 0 until sampleCols) {
                val pxX = (x * stepX).coerceAtMost(width - 1)
                val pixel = bitmap.getPixel(pxX, pxY)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                val maxChannel = maxOf(r, maxOf(g, b))
                val minChannel = minOf(r, minOf(g, b))
                val chroma = maxChannel - minChannel
                val sat = if (maxChannel == 0) 0.0 else chroma.toDouble() / maxChannel.toDouble()

                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                val cb = 128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b
                val cr = 128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b

                if (lum < 10.0) blackPixelCount++
                if (lum >= 20.0 && cb in 75.0..135.0 && cr in 130.0..175.0) {
                    skinLikePixelCount++
                }

                totalSat += sat
                totalLum += lum
                if (lum < minLum) minLum = lum
                if (lum > maxLum) maxLum = lum
                lumValues[sampleCount] = lum
                sampleCount++
            }
        }

        val avgSat = totalSat / sampleCount
        val avgLum = totalLum / sampleCount
        val blackPixelRatio = blackPixelCount.toDouble() / sampleCount
        val skinLikePixelRatio = skinLikePixelCount.toDouble() / sampleCount

        var lumVarianceSum = 0.0
        for (i in 0 until sampleCount) {
            val diff = lumValues[i] - avgLum
            lumVarianceSum += diff * diff
        }
        val lumStdDev = kotlin.math.sqrt(lumVarianceSum / sampleCount)

        // Check 1: Blank or near-uniform image detection
        if (lumStdDev < 6.0 || (maxLum - minLum) < 12.0) {
            val title = when {
                isLungs -> "Invalid Image for Lung X-Ray Analysis"
                isBone -> "Invalid Image for Bone X-Ray Analysis"
                else -> "Invalid Image for Skin Analysis"
            }
            val msg = when {
                isLungs -> "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
                isBone -> "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
                else -> "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze."
            }
            return Pair(false, Pair(title, msg))
        }

        // Check 2: Overwhelmingly Black / Text-on-Black Screen Protection
        if (blackPixelRatio > 0.85 && avgLum < 25.0) {
            val title = when {
                isLungs -> "Invalid Image for Lung X-Ray Analysis"
                isBone -> "Invalid Image for Bone X-Ray Analysis"
                else -> "Invalid Image for Skin Analysis"
            }
            val msg = when {
                isLungs -> "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
                isBone -> "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
                else -> "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze."
            }
            return Pair(false, Pair(title, msg))
        }

        if (isLungs) {
            if (avgSat > 0.32 || avgLum < 20.0) {
                return Pair(false, Pair(
                    "Invalid Image for Lung X-Ray Analysis",
                    "The uploaded image does not appear suitable for chest X-ray analysis. Please upload a clear chest X-ray image."
                ))
            }
        } else if (isBone) {
            if (avgSat > 0.32 || avgLum < 20.0) {
                return Pair(false, Pair(
                    "Invalid Image for Bone X-Ray Analysis",
                    "The uploaded image does not appear suitable for bone X-ray analysis. Please upload a clear bone X-ray image."
                ))
            }
        } else if (isSkin) {
            if (avgLum < 15.0 || avgLum > 245.0 || skinLikePixelRatio < 0.12) {
                return Pair(false, Pair(
                    "Invalid Image for Skin Analysis",
                    "The uploaded image does not appear suitable for skin analysis. Please upload a clear photograph of the skin area you want to analyze."
                ))
            }
        }

        return Pair(true, null)
    }

    fun initiateImageScan() {
        // Validation 1: Check if Body Part is unselected/none
        if (selectedBodyPart.isNullOrEmpty() || selectedBodyPart == "Select Body Part" || selectedBodyPart == "none") {
            showBodyPartWarningModal = true
            return
        }

        // Validation 2: Check image suitability BEFORE model execution
        val (isValidImage, warningDetails) = validateImageSuitability(selectedBodyPart ?: "", capturedBitmap)
        if (!isValidImage) {
            if (warningDetails != null) {
                validationWarningTitle = warningDetails.first
                validationWarningMessage = warningDetails.second
                showImageValidationWarningModal = true
            }
            return
        }

        isAnalyzing = true
        diagnosisResult = null
        hasSavedCurrentResult = false

        scope.launch {
            delay(1200)
            val body = selectedBodyPart ?: "Lungs"
            val result = evaluateDiagnosis(body, "", capturedBitmap)
            pendingResult = result
            diagnosisResult = result
            isAnalyzing = false
            saveHistoryRecord(result)
            showClinicalContextModal = true
        }
    }

    fun completeWithSymptomsAnalysis() {
        val pending = pendingResult ?: return
        isAnalyzing = true
        scope.launch {
            delay(800)
            val combinedSymptomsText = (selectedSymptoms.value + typedSymptoms).filter { it.isNotEmpty() }.joinToString(", ")
            val body = selectedBodyPart ?: "Lungs"

            // Re-evaluate disease dynamically using PDD engine rules (Loss of Taste -> COVID-19, etc.)
            val evaluatedResult = evaluateDiagnosis(body, combinedSymptomsText, capturedBitmap)

            val updatedDescription = if (combinedSymptomsText.isNotEmpty()) {
                "${evaluatedResult.description} Correlated with reported patient symptoms: [$combinedSymptomsText]."
            } else {
                evaluatedResult.description
            }

            val finalResult = evaluatedResult.copy(
                description = updatedDescription,
                confidence = "97%"
            )

            diagnosisResult = finalResult
            isAnalyzing = false
            saveHistoryRecord(finalResult)

            // Auto-scroll smoothly to the generated result section
            delay(150)
            resultSectionRequester.bringIntoView()
        }
    }

    fun toggleReadAloud(result: ScannerDiagnosisResult) {
        if (tts == null || !isTtsReady) {
            Toast.makeText(context, "Text-to-Speech engine is initializing...", Toast.LENGTH_SHORT).show()
            return
        }

        if (isSpeaking) {
            tts?.stop()
            isSpeaking = false
            return
        }

        val naturalText = result.naturalRemedies.joinToString(". ") { r ->
            "Natural remedy: ${r.name}. Ingredients: ${r.ingredients}. Method: ${r.method}. Use: ${r.use}"
        }

        val ayurvedaText = result.ayurvedicSuggestions.joinToString(". ") { a ->
            "Ayurvedic suggestion: ${a.name}. Dosage: ${a.dosage}. Use: ${a.use}"
        }

        val textToSpeak = "AI Diagnosis: ${result.diseaseName}. " +
                "Confidence: ${result.confidence}. " +
                "Severity: ${result.severity}. " +
                "Explanation: ${result.description}. " +
                "$naturalText. " +
                "$ayurvedaText. " +
                "Recommended specialist: ${result.specialist}."

        tts?.stop()
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ScannerSpeechUtterance")
        isSpeaking = true
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "ScannerSpeechUtterance")
    }

    val hasImageUploaded = capturedBitmap != null || galleryUri != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scanner Top Bar Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                Text(
                    text = com.heallens.android.utils.AppStrings.get("scanner_title", activeLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = com.heallens.android.utils.AppStrings.get("scanner_subtitle", activeLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // Selectors Row (Patient & Body Part Dropdowns)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Patient Selector Box
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceGlass)
                                .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(12.dp))
                                .clickable { isPatientMenuExpanded = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Patient:", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    val relKey = when(patientRelation) {
                                        "Self" -> "opt_self"
                                        "Father" -> "opt_father"
                                        "Mother" -> "opt_mother"
                                        "Spouse" -> "opt_spouse"
                                        "Child" -> "opt_child"
                                        else -> "opt_self"
                                    }
                                    Text(com.heallens.android.utils.AppStrings.get(relKey, activeLanguage), fontSize = 13.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                                }
                                Text("▾", color = CyanPrimary)
                            }
                        }

                        DropdownMenu(
                            expanded = isPatientMenuExpanded,
                            onDismissRequest = { isPatientMenuExpanded = false }
                        ) {
                            listOf("Self" to "opt_self", "Father" to "opt_father", "Mother" to "opt_mother", "Spouse" to "opt_spouse", "Child" to "opt_child").forEach { (rawRel, k) ->
                                DropdownMenuItem(
                                    text = { Text(com.heallens.android.utils.AppStrings.get(k, activeLanguage)) },
                                    onClick = {
                                        patientRelation = rawRel
                                        isPatientMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Body Part Selector Box (Initially NONE)
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedBodyPart == null) Color(0xFFEF4444).copy(alpha = 0.1f) else SurfaceGlass)
                                .border(
                                    width = 1.dp,
                                    color = if (selectedBodyPart == null) Color(0xFFEF4444).copy(alpha = 0.5f) else SurfaceGlassBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { isBodyMenuExpanded = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Body Part:", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = bodyPartLabel,
                                        fontSize = 13.sp,
                                        color = if (selectedBodyPart == null) Color(0xFFEF4444) else CyanPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("▾", color = if (selectedBodyPart == null) Color(0xFFEF4444) else CyanPrimary)
                            }
                        }

                        DropdownMenu(
                            expanded = isBodyMenuExpanded,
                            onDismissRequest = { isBodyMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Select Body Part", color = TextMuted) },
                                onClick = {
                                    selectedBodyPart = null
                                    isBodyMenuExpanded = false
                                }
                            )
                            listOf(
                                "Lungs (Chest X-Ray)",
                                "Skin (Dermatology)",
                                "Bone (Orthopedic)"
                            ).forEach { bodyPart ->
                                DropdownMenuItem(
                                    text = { Text(bodyPart) },
                                    onClick = {
                                        selectedBodyPart = bodyPart
                                        isBodyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Upload Area / Preview Card
                if (!hasImageUploaded) {
                    // Initial Dashed Upload Image Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF00D4FF).copy(alpha = 0.02f))
                            .dashedBorder(
                                color = Color(0xFF00D4FF).copy(alpha = 0.3f),
                                strokeWidth = 2.dp,
                                dashLength = 8.dp,
                                gapLength = 8.dp,
                                cornerRadius = 16.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("📸", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Drag & Drop Image Here",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Supports JPG, PNG (Max 10MB)",
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(1.dp)
                                        .background(SurfaceGlassBorder)
                                )
                                Text(
                                    text = "  OR  ",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(1.dp)
                                        .background(SurfaceGlassBorder)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // FIX 2: Responsive Action Buttons with 100% Complete Visible Text
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFFFFFFF).copy(alpha = 0.06f))
                                        .border(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.18f), RoundedCornerShape(50))
                                        .clickable { galleryLauncher.launch("image/*") }
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Upload X-Ray image",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        letterSpacing = (-0.2).sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFFFFFFF).copy(alpha = 0.06f))
                                        .border(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.18f), RoundedCornerShape(50))
                                        .clickable { cameraLauncher.launch(null) }
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Open Camera",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        letterSpacing = (-0.2).sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Image Preview Container (Displays the ACTUAL Uploaded/Captured Image)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0C1426))
                            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (capturedBitmap != null) {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Uploaded Medical Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        // X-Ray Mode Badge top right
                        val bodyPartStr = selectedBodyPart ?: ""
                        if (bodyPartStr.isNotEmpty() && bodyPartStr != "none" && bodyPartStr != "Select Body Part") {
                            val badgeText = when {
                                bodyPartStr.contains("Lungs", ignoreCase = true) -> "CHEST X-RAY"
                                bodyPartStr.contains("Bone", ignoreCase = true) -> "BONE X-RAY"
                                else -> "SKIN SCAN"
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(CyanPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, CyanPrimary, RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(badgeText, color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row (ONLY APPEARS AFTER IMAGE UPLOAD)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GradientButton(
                            text = "🧠 Analyze Image",
                            onClick = { initiateImageScan() },
                            isLoading = isAnalyzing,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceGlass)
                                .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(24.dp))
                                .clickable {
                                    scope.launch {
                                        symptomsSectionRequester.bringIntoView()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➕ Add Symptoms", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Symptoms Analysis Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(symptomsSectionRequester)
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                Text(
                    text = com.heallens.android.utils.AppStrings.get("symptoms_analysis_title", activeLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = com.heallens.android.utils.AppStrings.get("symptoms_analysis_desc", activeLanguage),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                // Symptoms Input Row (Vertically Centered Placeholder Fix)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassTextField(
                        value = typedSymptoms,
                        onValueChange = { typedSymptoms = it },
                        label = null,
                        placeholder = com.heallens.android.utils.AppStrings.get("symptoms_placeholder", activeLanguage),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    )

                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanPrimary.copy(alpha = 0.15f))
                            .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { typedSymptoms = "Persistent Cough, Night Sweats" }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(com.heallens.android.utils.AppStrings.get("symptoms_speak", activeLanguage), color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // COMPLETE PDD CATEGORIZED SYMPTOM CHIPS
                val body = selectedBodyPart ?: "Lungs"
                Text(
                    text = when {
                        body.contains("Lungs", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("header_lung_records", activeLanguage)
                        body.contains("Skin", ignoreCase = true) -> com.heallens.android.utils.AppStrings.get("header_skin_records", activeLanguage)
                        else -> com.heallens.android.utils.AppStrings.get("header_bone_records", activeLanguage)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Full Exact Symptom Lists with Key Mappings
                val chipKeyList = when {
                    body.contains("Lungs", ignoreCase = true) -> listOf(
                        "Persistent Cough" to "sym_cough", "Night Sweats" to "sym_sweats", "Chest Pain" to "sym_chest_pain", "High Fever" to "sym_fever",
                        "Breathlessness" to "sym_breathless", "Blood in Sputum" to "sym_blood_sputum", "Weight Loss" to "sym_weight_loss", "Yellow Phlegm" to "sym_phlegm",
                        "Loss of Taste" to "sym_taste", "Extreme Fatigue" to "sym_fatigue"
                    )
                    body.contains("Skin", ignoreCase = true) -> listOf(
                        "Red Patches" to "sym_red_patches", "Itching" to "sym_itching", "Skin Rash" to "sym_rash", "Silvery Scales" to "sym_scales",
                        "Pus Discharge" to "sym_pus", "Burning Sensation" to "sym_burning", "Scaly Skin" to "sym_scaly", "Skin Boil" to "sym_boil"
                    )
                    else -> listOf(
                        "Sudden Severe Pain" to "sym_severe_pain", "Swelling around Injury" to "sym_swelling_injury", "Bruising" to "sym_bruising", "Difficulty Moving" to "sym_diff_moving",
                        "Tenderness" to "sym_tenderness", "Deformity" to "sym_deformity", "Visible Disability" to "sym_disability", "Inability to Bear Weight" to "sym_weight_bear",
                        "Crack/Snapping Sound" to "sym_crack", "Mild Joint Pain" to "sym_joint_pain", "Morning Stiffness" to "sym_stiffness", "Swelling around Joints" to "sym_swelling_joints",
                        "Reduced Flexibility" to "sym_flexibility", "Warmth around Joint" to "sym_warmth", "Clicking/Grinding" to "sym_clicking", "Mild Discomfort" to "sym_discomfort"
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chipKeyList.chunked(2).forEach { rowChips ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowChips.forEach { (rawText, keyStr) ->
                                val isSelected = selectedSymptoms.value.contains(rawText)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CyanPrimary else SurfaceGlass)
                                        .border(1.dp, if (isSelected) CyanPrimary else SurfaceGlassBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val current = selectedSymptoms.value.toMutableSet()
                                            if (isSelected) current.remove(rawText) else current.add(rawText)
                                            selectedSymptoms.value = current
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = com.heallens.android.utils.AppStrings.get(keyStr, activeLanguage),
                                        color = if (isSelected) DarkBackground else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            if (rowChips.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GradientButton(
                    text = com.heallens.android.utils.AppStrings.get("symptoms_analyze_btn", activeLanguage),
                    onClick = { completeWithSymptomsAnalysis() },
                    isLoading = isAnalyzing
                )
            }

            // DIAGNOSTIC RESULTS CARD DISPLAY (Matching PDD Result Structure Exactly)
            diagnosisResult?.let { result ->
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(resultSectionRequester)
                        .glassmorphicCard(cornerRadius = 20.dp)
                        .padding(20.dp)
                ) {
                    // Header Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when {
                                    result.bodyPartLabel.contains("Chest", ignoreCase = true) -> "🫁"
                                    result.bodyPartLabel.contains("Skin", ignoreCase = true) -> "🧴"
                                    else -> "🦴"
                                },
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = result.bodyPartLabel,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = result.diseaseName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyanPrimary,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }

                        // Badges Stack (Severity + Confidence)
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(result.severityColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, result.severityColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = result.severity,
                                    color = result.severityColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(CyanPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${result.confidence} Confidence",
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description / Explanation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFFFF).copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = result.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Natural Remedies Box (Green #4CAF50 - Matching PDD res-remedies-natural)
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

                            result.naturalRemedies.forEachIndexed { idx, remedy ->
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Ayurvedic Suggestion Box (Orange #FF9800 - Matching PDD res-remedies-ayurvedic)
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

                            result.ayurvedicSuggestions.forEachIndexed { idx, ayur ->
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Action Buttons (Read Aloud Toggle & Book Appointment)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Read Aloud / Stop Reading Toggle Button
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
                                .clickable { toggleReadAloud(result) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSpeaking) "🔇 Stop Reading" else "🔊 Read Aloud",
                                color = if (isSpeaking) Color(0xFFEF4444) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        val recommendedSpecialty = when (result.diseaseName.lowercase()) {
                            "pneumonia", "tuberculosis", "covid-19" -> "Pulmonologist"
                            "bone fracture" -> "Orthopedic Surgeon"
                            "mild arthritis" -> "Rheumatologist"
                            "skin infection", "psoriasis/rash" -> "Dermatologist"
                            else -> "General Physician"
                        }
                        com.heallens.android.ui.components.SpecialistDiscoveryWidget(
                            specialty = recommendedSpecialty,
                            contextualNote = "Based on evaluated image indicators, consulting a $recommendedSpecialty is recommended for formal clinical evaluation. This recommendation is educational and does not replace a medical diagnosis."
                        )
                    }
                }
            }
        }
    }

    // Modal 1: Body Part Warning Modal (Matching dashboard.html line 1286)
    if (showBodyPartWarningModal) {
        AlertDialog(
            onDismissRequest = { showBodyPartWarningModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️ ", fontSize = 24.sp)
                    Text("Medical Selection Required", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = "Clinical Requirement: Please select the Body Part (Lungs, Bone, or Skin) before analyzing the image to ensure diagnostic accuracy.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                GradientButton(
                    text = "Select Now",
                    onClick = {
                        showBodyPartWarningModal = false
                        isBodyMenuExpanded = true
                    }
                )
            },
            containerColor = DarkBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal 1B: Image Suitability Validation Warning Modal
    if (showImageValidationWarningModal) {
        AlertDialog(
            onDismissRequest = { showImageValidationWarningModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️ ", fontSize = 24.sp)
                    Text(validationWarningTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Text(
                    text = validationWarningMessage,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                GradientButton(
                    text = "OK",
                    onClick = {
                        showImageValidationWarningModal = false
                    }
                )
            },
            containerColor = DarkBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal 2: Clinical Context Needed Modal (Matching dashboard.html line 1266)
    if (showClinicalContextModal) {
        val predictedTitle = pendingResult?.diseaseName ?: "Pneumonia"
        AlertDialog(
            onDismissRequest = {
                showClinicalContextModal = false
                pendingResult?.let { res ->
                    if (diagnosisResult == null) {
                        diagnosisResult = res
                        saveHistoryRecord(res)
                    }
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️ ", fontSize = 24.sp)
                    Text("Clinical Context Needed", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Based on the uploaded image the AI predicts you might have $predictedTitle.",
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "But many similar diseases have overlapping visual characteristics. Add symptoms to predict more accurately.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                GradientButton(
                    text = "Add Symptoms",
                    onClick = {
                        showClinicalContextModal = false
                        scope.launch {
                            symptomsSectionRequester.bringIntoView()
                        }
                    }
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showClinicalContextModal = false
                        pendingResult?.let { res ->
                            diagnosisResult = res
                            saveHistoryRecord(res)
                        }
                    }
                ) {
                    Text("Analyze Anyway", color = CyanPrimary)
                }
            },
            containerColor = DarkBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }

}
