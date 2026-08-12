package com.heallens.android.ui.chatbot

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard
import com.heallens.android.utils.AppLanguage
import com.heallens.android.utils.AppStrings
import com.heallens.android.utils.LanguageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
    val sender: String, // "USER" or "ASSISTANT"
    val content: String,
    val isRedFlag: Boolean = false,
    val timestamp: String = "Just now"
)

enum class HealthTopic {
    RED_FLAG,
    HEADACHE,
    COUGH,
    FEVER,
    COLD_FLU,
    SORE_THROAT,
    STOMACH_DIGESTIVE,
    FATIGUE_WEAKNESS,
    SLEEP,
    STRESS_ANXIETY,
    LOW_GLUCOSE,
    HIGH_GLUCOSE,
    CHOLESTEROL,
    HEMOGLOBIN_ANEMIA,
    THYROID,
    SKIN,
    JOINT_MUSCLE,
    GREETING,
    UNKNOWN
}

class HealthConversationState {
    var activeTopic: HealthTopic? = null
    var primarySymptom: String? = null
    var duration: String? = null
    var severityOrValue: String? = null
    val associatedSymptoms = mutableSetOf<String>()
    val userAnswers = mutableMapOf<String, String>()
    val askedQuestions = mutableSetOf<String>()
    var isFeverNegative: Boolean = false
    var isCoughNegative: Boolean = false

    fun updateTopic(newTopic: HealthTopic) {
        if (newTopic != HealthTopic.UNKNOWN && newTopic != HealthTopic.GREETING && newTopic != HealthTopic.RED_FLAG) {
            if (activeTopic == null || activeTopic == HealthTopic.UNKNOWN || activeTopic == HealthTopic.GREETING) {
                activeTopic = newTopic
            }
        }
    }

    fun extractAndAccumulate(userMessage: String) {
        val lower = userMessage.lowercase(Locale.ROOT)

        // Multilingual Duration Extraction
        val extractedDur = when {
            lower.contains("last night") || lower.contains("कल रात") || lower.contains("நேற்று இரவு") || lower.contains("ನಿನ್ನೆ ರಾತ್ರಿ") -> "since last night"
            lower.contains("yesterday") || lower.contains("कल से") || lower.contains("நேற்றிலிருந்து") || lower.contains("ನಿನ್ನೆಯಿಂದ") -> "since yesterday"
            lower.contains("2 days") || lower.contains("two days") || lower.contains("दो दिन") || lower.contains("இரண்டு நாட்கள்") || lower.contains("ಎರಡು ದಿನ") -> "for 2 days"
            lower.contains("3 days") || lower.contains("तीन दिन") || lower.contains("மூன்று நாட்கள்") || lower.contains("ಮೂರು ದಿನ") -> "for 3 days"
            lower.contains("week") || lower.contains("हफ्ता") || lower.contains("வாரம்") || lower.contains("ವಾರ") -> "for about a week"
            lower.contains("this morning") || lower.contains("आज सुबह") || lower.contains("இன்று காலை") || lower.contains("ಇಂದು ಬೆಳಿಗ್ಗೆ") -> "since this morning"
            else -> null
        }
        if (extractedDur != null) {
            duration = extractedDur
            userAnswers["duration"] = extractedDur
            askedQuestions.add("duration")
        }

        // Temperature / Value extraction
        val tempMatch = Regex("""(10[0-5](\.\d)?|99(\.\d)?)""").find(userMessage)
        if (tempMatch != null) {
            val tempVal = "${tempMatch.value}°F"
            severityOrValue = tempVal
            userAnswers["temperature"] = tempVal
            askedQuestions.add("temperature")
        }

        // Sleep hours
        val sleepMatch = Regex("""(\d+)\s*(hours|hrs|hour)""").find(lower)
        if (sleepMatch != null) {
            val hrs = sleepMatch.groupValues[1] + " hours"
            severityOrValue = hrs
            userAnswers["sleep_hours"] = hrs
            askedQuestions.add("sleep_hours")
        }

        if (lower.contains("no fever") || lower.contains("बुखार नहीं") || lower.contains("காய்ச்சல் இல்லை") || lower.contains("ಜ್ವರವಿಲ್ಲ")) {
            isFeverNegative = true
            userAnswers["fever"] = "no"
            askedQuestions.add("fever")
        }

        if (lower.contains("dizzy") || lower.contains("चक्कर") || lower.contains("மயக்கம்") || lower.contains("ತಲೆಸುತ್ತು")) associatedSymptoms.add("dizziness")
        if (lower.contains("tired") || lower.contains("fatigue") || lower.contains("थकान") || lower.contains("சோர்வு") || lower.contains("ಆಯಾಸ")) associatedSymptoms.add("fatigue")
        if (lower.contains("sleep") || lower.contains("नींद") || lower.contains("தூக்கம்") || lower.contains("ನಿದ್ರೆ")) associatedSymptoms.add("poor sleep")
    }
}

object HealthResponseEngine {

    fun detectTopic(text: String, state: HealthConversationState): HealthTopic {
        val lower = text.lowercase(Locale.ROOT)

        // 1. Multilingual Red Flag Detection
        if (lower.contains("chest pain") || lower.contains("shortness of breath") || lower.contains("can't breathe") ||
            lower.contains("difficulty breathing") || lower.contains("coughing blood") || lower.contains("unconscious") ||
            lower.contains("सीने में दर्द") || lower.contains("सांस लेने में तकलीफ") || lower.contains("खून की उल्टी") || lower.contains("बेहोश") ||
            lower.contains("நெஞ்சு வலி") || lower.contains("மூச்சுத்திணறல்") || lower.contains("ரத்தம் கக்குதல்") || lower.contains("மயக்கம்") ||
            lower.contains("ಎದೆ ನೋವು") || lower.contains("ಉಸಿರಾಟದ ತೊಂದರೆ") || lower.contains("ರಕ್ತ ವಾಂತಿ") || lower.contains("ಮೂರ್ಛೆ")
        ) {
            return HealthTopic.RED_FLAG
        }

        // 2. Multilingual Topic Matching
        if (lower.contains("headache") || lower.contains("head pain") || lower.contains("migraine") || lower.contains("head hurts") ||
            lower.contains("सिरदर्द") || lower.contains("सर दर्द") || lower.contains("सिर में दर्द") ||
            lower.contains("தலைவலி") || lower.contains("தலையில் வலி") || lower.contains("தலை வலி") ||
            lower.contains("ತಲೆನೋವು") || lower.contains("ತಲೆ ನೋವು")
        ) {
            return HealthTopic.HEADACHE
        }

        if (lower.contains("cough") || lower.contains("phlegm") || lower.contains("mucus") ||
            lower.contains("खांसी") || lower.contains("कफ") || lower.contains("बलगम") ||
            lower.contains("இருமல்") || lower.contains("சளி") ||
            lower.contains("ಕೆಮ್ಮು") || lower.contains("ಕಫ")
        ) {
            return HealthTopic.COUGH
        }

        if (lower.contains("fever") || lower.contains("temperature") || lower.contains("chills") ||
            lower.contains("बुखार") || lower.contains("तापमान") ||
            lower.contains("காய்ச்சல்") || lower.contains("வெப்பநிலை") ||
            lower.contains("ಜ್ವರ") || lower.contains("ತಾಪಮಾನ")
        ) {
            return HealthTopic.FEVER
        }

        if (lower.contains("cold") || lower.contains("runny nose") || lower.contains("flu") ||
            lower.contains("जुकाम") || lower.contains("सर्दी") ||
            lower.contains("மூக்கடைப்பு") || lower.contains("ஜலதோஷம்") ||
            lower.contains("ನೆಗಡಿ") || lower.contains("ಶೀತ")
        ) {
            return HealthTopic.COLD_FLU
        }

        if (lower.contains("sore throat") || lower.contains("throat pain") ||
            lower.contains("गले में खराश") || lower.contains("गला दर्द") ||
            lower.contains("தொண்டை வலி") || lower.contains("தொண்டை எரிச்சல்") ||
            lower.contains("ಗಂಟಲು ನೋವು") || lower.contains("ಗಂಟಲು ಉರಿ")
        ) {
            return HealthTopic.SORE_THROAT
        }

        if (lower.contains("stomach") || lower.contains("acidity") || lower.contains("gas") || lower.contains("vomiting") || lower.contains("diarrhea") ||
            lower.contains("पेट दर्द") || lower.contains("एसिडिटी") || lower.contains("उल्टी") || lower.contains("दस्त") ||
            lower.contains("வயிற்று வலி") || lower.contains("அசிடிட்டி") || lower.contains("வாந்தி") ||
            lower.contains("ಹೊಟ್ಟೆ ನೋವು") || lower.contains("ಆಸಿಡಿಟಿ") || lower.contains("ವಾಂತಿ")
        ) {
            return HealthTopic.STOMACH_DIGESTIVE
        }

        if (lower.contains("tired") || lower.contains("fatigue") || lower.contains("weakness") ||
            lower.contains("थकान") || lower.contains("कमजोरी") ||
            lower.contains("சோர்வு") || lower.contains("பலவீனம்") ||
            lower.contains("ದಣಿವು") || lower.contains("ಆಯಾಸ")
        ) {
            return HealthTopic.FATIGUE_WEAKNESS
        }

        if (lower.contains("sleep") || lower.contains("insomnia") ||
            lower.contains("नींद") || lower.contains("अनिद्रा") ||
            lower.contains("தூக்கம்") || lower.contains("தூக்கமின்மை") ||
            lower.contains("ನಿದ್ರೆ") || lower.contains("ನಿದ್ರೆಯಿಲ್ಲ")
        ) {
            return HealthTopic.SLEEP
        }

        if (lower.contains("stress") || lower.contains("anxiety") ||
            lower.contains("तनाव") || lower.contains("चिंता") ||
            lower.contains("மன அழுத்தம்") || lower.contains("பயம்") ||
            lower.contains("ಒತ್ತಡ") || lower.contains("ಆತಂಕ")
        ) {
            return HealthTopic.STRESS_ANXIETY
        }

        if (lower.contains("low sugar") || lower.contains("hypoglycemia") || lower.contains("कम शुगर") || lower.contains("குறைந்த சர்க்கரை") || lower.contains("ಕಡಿಮೆ ಸಕ್ಕರೆ")) {
            return HealthTopic.LOW_GLUCOSE
        }

        if (lower.contains("high sugar") || lower.contains("diabetes") || lower.contains("हाई शुगर") || lower.contains("डायबिटीज") || lower.contains("அதிக சர்க்கரை") || lower.contains("நீரிழிவு") || lower.contains("ಹೆಚ್ಚಿನ ಸಕ್ಕರೆ") || lower.contains("ಮಧುಮೇಹ")) {
            return HealthTopic.HIGH_GLUCOSE
        }

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("namaste") || lower.contains("नमस्ते") || lower.contains("வணக்கம்") || lower.contains("நமஸ்காரம்") || lower.contains("ನಮಸ್ಕಾರ")) {
            return HealthTopic.GREETING
        }

        if (state.activeTopic != null && state.activeTopic != HealthTopic.UNKNOWN && state.activeTopic != HealthTopic.GREETING) {
            return state.activeTopic!!
        }

        return HealthTopic.UNKNOWN
    }

    fun generateReply(userMessage: String, state: HealthConversationState, language: AppLanguage): String {
        state.extractAndAccumulate(userMessage)

        val topic = detectTopic(userMessage, state)
        state.updateTopic(topic)
        val lower = userMessage.lowercase(Locale.ROOT)

        // 1. RED FLAG EMERGENCY RESPONSE IN SELECTED LANGUAGE
        if (topic == HealthTopic.RED_FLAG) {
            return when (language) {
                AppLanguage.TAMIL ->
                    "🚨 அவசர மருத்துவ எச்சரிக்கை 🚨\n\nஇந்த அறிகுறிக்கு உடனடியாக மருத்துவ கவனிப்பு தேவைப்படலாம். உடனடியாக அவசர மருத்துவ சேவையை அணுகவும் (எ.கா. 108).\n\nஅவசர நோயறிதலுக்கு சாட்பாட்டைச் சார்ந்திருக்க வேண்டாம்.\n\nஉடனடி நடவடிக்கைகள்:\n• அமைதியாக பாதுகாப்பான நிலையில் ஓய்வெடுக்கவும்.\n• அருகிலுள்ள எவரிடமாவது உடனடி உதவி கேட்கவும்.\n• அருகிலுள்ள அவசர சிகிச்சைப் பிரிவை அணுகவும்."
                AppLanguage.HINDI ->
                    "🚨 आपातकालीन चिकित्सा चेतावनी 🚨\n\nइस लक्षण के लिए तत्काल चिकित्सा सहायता की आवश्यकता हो सकती है। कृपया तुरंत आपातकालीन चिकित्सा सेवा (जैसे 108 / ईआर) से संपर्क करें।\n\nआपातकालीन निदान के लिए चैटबॉट पर निर्भर न रहें।\n\nतत्काल कदम:\n• शांत रहें और सुरक्षित स्थिति में आराम करें।\n• पास के किसी व्यक्ति से तुरंत मदद मांगें।\n• निकटतम आपातकालीन कक्ष में जाएं।"
                AppLanguage.KANNADA ->
                    "🚨 ತುರ್ತು ವೈದ್ಯಕೀಯ ಎಚ್ಚರಿಕೆ 🚨\n\nಈ ರೋಗಲಕ್ಷಣಕ್ಕೆ ತಕ್ಷಣದ ವೈದ್ಯಕೀಯ ಗಮನ ಅಗತ್ಯವಾಗಬಹುದು. ದಯವಿಟ್ಟು ತಕ್ಷಣ ತುರ್ತು ವೈದ್ಯಕೀಯ ಸೇವೆಯನ್ನು ಸಂಪರ್ಕಿಸಿ (ಉದಾ. 108).\n\nತುರ್ತು ರೋಗನಿರ್ಣಯಕ್ಕಾಗಿ ಚಾಟ್‌ಬಾಟ್ ಅನ್ನು ಅವಲಂಬಿಸಬೇಡಿ.\n\nತಕ್ಷಣದ ಕ್ರಮಗಳು:\n• ಶಾಂತರಾಗಿರಿ ಮತ್ತು ಸುರಕ್ಷಿತ ಸ್ಥಳದಲ್ಲಿ ವಿಶ್ರಾಂತಿ ಪಡೆಯಿರಿ.\n• ಹತ್ತಿರದ ಯಾರಾದರೂ ತಕ್ಷಣದ ಸಹಾಯವನ್ನು ಕೇಳಿ.\n• ಹತ್ತಿರದ ತುರ್ತು ವಿಭಾಗವನ್ನು ಸಂಪರ್ಕಿಸಿ."
                AppLanguage.ENGLISH ->
                    "🚨 URGENT MEDICAL WARNING 🚨\n\nThis symptom can require immediate medical attention. Please seek emergency medical care immediately or contact your local emergency service (e.g. 108 / ER).\n\nDo NOT rely on the chatbot for emergency diagnosis or treatment.\n\nImmediate Steps:\n• Stay calm and rest in a safe position.\n• Ask someone nearby for immediate assistance.\n• Seek nearest Emergency Room evaluation."
            }
        }

        // 2. CONSUMPTION QUERY
        val isConsumptionQuery = lower.contains("consume") || lower.contains("eat") || lower.contains("drink") || lower.contains("food") ||
                lower.contains("क्या खाएं") || lower.contains("क्या पीएं") || lower.contains("क्या खाएं") ||
                lower.contains("என்ன சாப்பிடலாம்") || lower.contains("என்ன குடிக்கலாம்") ||
                lower.contains("ಏನು ತಿನ್ನಬೇಕು") || lower.contains("ಏನು ಕುಡಿಯಬೇಕು")

        if (isConsumptionQuery && state.activeTopic != null) {
            return when (language) {
                AppLanguage.TAMIL -> when (state.activeTopic) {
                    HealthTopic.HEADACHE -> "🍵 தலைவலி நிவாரணத்திற்கான உணவுகள்:\n• நீர்ச்சத்து: உடனே 2 டம்ளர் தண்ணீர் குடிக்கவும். நீர்ச்சத்து குறைபாடே தலைவலிக்கு முக்கிய காரணம்.\n• நீர்ச்சத்து உணவுகள்: தர்பூசணி, வெள்ளரிக்காய், இளநீர்.\n• மிதமான உணவு: காரமில்லாத லேசான உணவை உட்கொள்ளவும்.\n\n*தவிர்க்கவும்:* அதிக காபி, டீ, குளிர்ந்த பானங்கள்."
                    HealthTopic.COUGH -> "🍵 இருமலுக்கான நிவாரண உணவுகள்:\n• வெதுவெதுப்பான நீர், இஞ்சி-துளசி டீ, தேன் அருந்தவும்.\n• இரவில் வெதுவெதுப்பான மஞ்சள் பால் குடிக்கவும்."
                    else -> "🍵 பொதுவான உணவு வழிகாட்டுதல்:\nதினமும் 2.5 லிட்டர் தண்ணீர் குடிக்கவும், லேசான சூடான உணவை உண்ணவும், அதிக காரம் மற்றும் பொரித்த உணவுகளைத் தவிர்க்கவும்."
                }
                AppLanguage.HINDI -> when (state.activeTopic) {
                    HealthTopic.HEADACHE -> "🍵 सिरदर्द राहत के लिए क्या खाएं/पीएं:\n• सबसे पहले पानी पीएं: तुरंत 2 गिलास पानी पीएं। डिहाइड्रेशन सिरदर्द का मुख्य कारण है।\n• ताजे फल: तरबूज, खीरा, या नारियल पानी पीएं।\n• हल्का भोजन: हल्का और सुपाच्य भोजन लें।"
                    else -> "🍵 सामान्य आहार मार्गदर्शन:\nरोजाना 2.5 लीटर पानी पीएं, हल्का और ताजा भोजन करें, तथा अत्यधिक मसालेदार भोजन से बचें।"
                }
                AppLanguage.KANNADA -> when (state.activeTopic) {
                    HealthTopic.HEADACHE -> "🍵 ತಲೆನೋವು ಶಮನಕ್ಕೆ ಆಹಾರ ಮಾರ್ಗದರ್ಶನ:\n• ನೀರಿನಂಶ: ತಕ್ಷಣ 2 ಗ್ಲಾಸ್ ನೀರು ಕುಡಿಯಿರಿ. ನಿರ್ಜಲೀಕರಣವು ತಲೆನೋವಿಗೆ ಪ್ರಮುಖ ಕಾರಣವಾಗಿದೆ.\n• ಎಳೆನೀರು, ಕಲ್ಲಂಗಡಿ, ಸೌತೆಕಾಯಿ ಸೇವಿಸಿ.\n• ಹಗುರವಾದ ಆಹಾರ ಸೇವಿಸಿ."
                    else -> "🍵 ಸಾಮಾನ್ಯ ಆಹಾರ ಮಾರ್ಗದರ್ಶನ:\nದಿನಕ್ಕೆ 2.5 ಲೀಟರ್ ನೀರು ಕುಡಿಯಿರಿ, ಹಗುರವಾದ ಬಿಸಿ ಆಹಾರ ಸೇವಿಸಿ."
                }
                AppLanguage.ENGLISH -> when (state.activeTopic) {
                    HealthTopic.HEADACHE -> "🍵 What to Consume for Headache Relief:\n• Hydration First: Sip fresh water regularly (drink 2 glasses immediately).\n• Hydrating Foods: Watermelon, cucumber, oranges, or fresh coconut water.\n• Light Meals: Have a light, non-greasy meal if you skipped food.\n\n*Avoid:* Excessive caffeine, sugary drinks, or heavy fried meals."
                    else -> "🍵 General Hydration & Nutrition Guidance:\nDrink adequate fresh water (2.5L daily), eat warm, light meals, and avoid excessive caffeine or processed foods."
                }
            }
        }

        // 3. TOPIC RESPONSES
        return when (topic) {
            HealthTopic.HEADACHE -> {
                val dur = state.duration
                when (language) {
                    AppLanguage.TAMIL -> {
                        if (dur != null) {
                            "🤖 $dur முதல் தலைவலி நீடிப்பதால், இது தூக்கமின்மை, மன அழுத்தம், திரையைப் பார்ப்பது அல்லது நீர்ச்சத்து குறைபாட்டுடன் தொடர்புடையதாக இருக்கலாம்.\n\nபோதிய ஓய்வு எடுத்து, போதுமான தண்ணீர் குடித்து, திரையைப் பார்ப்பதைக் குறைக்கவும்."
                        } else {
                            "🤖 தலைவலி பல காரணங்களால் ஏற்படலாம். தூக்கமின்மை, உடலில் நீர்ச்சத்து குறைவு, மன அழுத்தம், அதிக நேரம் திரையைப் பார்ப்பது போன்றவை காரணமாக இருக்கலாம்.\n\nஇது எப்போது தொடங்கியது?\nஉங்களுக்கு காய்ச்சல், வாந்தி அல்லது பார்வை மங்குதல் போன்ற வேறு அறிகுறிகள் உள்ளதா?"
                        }
                    }
                    AppLanguage.HINDI -> {
                        if (dur != null) {
                            "🤖 $dur से सिरदर्द होने के कारण यह नींद की कमी, तनाव, या डिहाइड्रेशन से जुड़ा हो सकता है।\n\nपर्याप्त आराम करें, पानी पीएं और स्क्रीन टाइम कम करें।"
                        } else {
                            "🤖 सिरदर्द कई कारणों से हो सकता है जैसे नींद की कमी, तनाव, डिहाइड्रेशन या स्क्रीन टाइम।\n\nयह कब से शुरू हुआ?\nक्या आपको बुखार या उल्टी जैसी समस्या भी है?"
                        }
                    }
                    AppLanguage.KANNADA -> {
                        if (dur != null) {
                            "🤖 $dur ರಿಂದ ತಲೆನೋವು ಇರುವುದರಿಂದ, ಇದು ನಿದ್ರೆಯ ಕೊರತೆ, ಒತ್ತಡ ಅಥವಾ ನಿರ್ಜಲೀಕರಣದಿಂದ ಆಗಿರಬಹುದು.\n\nಸಾಕಷ್ಟು ವಿಶ್ರಾಂತಿ ಪಡೆಯಿರಿ, ನೀರು ಕುಡಿಯಿರಿ ಮತ್ತು ಸ್ಕ್ರೀನ್ ಸಮಯವನ್ನು ಕಡಿಮೆ ಮಾಡಿ."
                        } else {
                            "🤖 ತಲೆನೋವು ನಿದ್ರೆಯ ಕೊರತೆ, ನಿರ್ಜಲೀಕರಣ, ಒತ್ತಡ ಅಥವಾ ಸ್ಕ್ರೀನ್ ಸಮಯದಿಂದ ಉಂಟಾಗಬಹುದು.\n\nಇದು ಯಾವಾಗ ಪ್ರಾರಂಭವಾಯಿತು?\nನಿಮಗೆ ಜ್ವರ ಅಥವಾ ವಾಂತಿಯ ಲಕ್ಷಣಗಳಿವೆಯೇ?"
                        }
                    }
                    AppLanguage.ENGLISH -> {
                        if (dur != null) {
                            "🤖 Since $dur, your headache may be related to poor sleep, dehydration, stress, or prolonged screen time.\n\nTry getting proper rest, sipping water, and reducing screen exposure."
                        } else {
                            "🤖 Sorry you're dealing with a headache. A headache can be related to lack of sleep, dehydration, stress, or screen time.\n\nHow long have you had it?\nAre you experiencing fever, vomiting, or vision changes?"
                        }
                    }
                }
            }

            HealthTopic.COUGH -> {
                when (language) {
                    AppLanguage.TAMIL -> "🤖 இருமல் தொண்டை எரிச்சல் அல்லது சளியினால் ஏற்படலாம். வெதுவெதுப்பான இஞ்சி-துளசி டீ மற்றும் ஆவி பிடிப்பது நிவாரணம் தரும்.\n\nஉங்களுக்கு எத்தனை நாட்களாக இருமல் உள்ளது?"
                    AppLanguage.HINDI -> "🤖 खांसी गले में खराश या सर्दी के कारण हो सकती है। गुनगुना पानी और भाप लेना फायदेमंद होगा।\n\nआपको कितने दिनों से खांसी है?"
                    AppLanguage.KANNADA -> "🤖 ಕೆಮ್ಮು ಗಂಟಲು ಕಿರಿಕಿರಿಯಿಂದ ಉಂಟಾಗಬಹುದು. ಬಿಸಿ ನೀರು ಮತ್ತು ಹಾವಿ ತೆಗೆದುಕೊಳ್ಳುವುದು ಶಮನ ನೀಡುತ್ತದೆ.\n\nನಿಮಗೆ ಎಷ್ಟು ದಿನಗಳಿಂದ ಕೆಮ್ಮು ಇದೆ?"
                    AppLanguage.ENGLISH -> "🤖 Cough is typically caused by throat irritation or a common viral cold. Warm fluids and steam inhalation can help.\n\nHow long have you had this cough?"
                }
            }

            HealthTopic.FEVER -> {
                when (language) {
                    AppLanguage.TAMIL -> "🤖 காய்ச்சல் இருக்கும் போது போதுமான ஓய்வு மற்றும் நீர்ச்சத்து மிக முக்கியம். வெதுவெதுப்பான நீர் அருந்தி காய்ச்சலின் அளவைக் கண்காணிக்கவும்."
                    AppLanguage.HINDI -> "🤖 बुखार होने पर पर्याप्त आराम और पानी पीना आवश्यक है। थर्मामीटर से तापमान चेक करते रहें।"
                    AppLanguage.KANNADA -> "🤖 ಜ್ವರವಿದ್ದಾಗ ಸಾಕಷ್ಟು ವಿಶ್ರಾಂತಿ ಮತ್ತು ನೀರು ಕುಡಿಯುವುದು ಮುಖ್ಯ. ತಾಪಮಾನವನ್ನು ಪರೀಕ್ಷಿಸುತ್ತಿರಿ."
                    AppLanguage.ENGLISH -> "🤖 Fever indicates your immune system is active. Rest well, drink plenty of fluids, and monitor your temperature."
                }
            }

            HealthTopic.GREETING -> {
                when (language) {
                    AppLanguage.TAMIL -> "🤖 வணக்கம்! நான் உங்கள் ஹீல்ாலென்ஸ் ஏஐ சுகாதார உதவியாளர்.\n\nதலைவலி, இருமல், காய்ச்சல், வயிற்று வலி, சோர்வு போன்ற அறிகுறிகள் பற்றி என்னிடம் கேட்கலாம். இன்று உங்களுக்கு எவ்வாறு உதவட்டும்?"
                    AppLanguage.HINDI -> "🤖 नमस्ते! मैं आपका हीललेंस एआई स्वास्थ्य सहायक हूँ।\n\nआप मुझसे सिरदर्द, खांसी, बुखार, पेट दर्द, या थकान जैसे लक्षणों के बारे में पूछ सकते हैं। आज मैं आपकी क्या मदद करूँ?"
                    AppLanguage.KANNADA -> "🤖 ನಮಸ್ಕಾರ! ನಾನು ನಿಮ್ಮ ಹೀಲ್‌ಲೆನ್ಸ್ ಎಐ ಆರೋಗ್ಯ ಸಹಾಯಕ.\n\nತಲೆನೋವು, ಕೆಮ್ಮು, ಜ್ವರ, ಹೊಟ್ಟೆ ನೋವು, ಆಯಾಸದ ಬಗ್ಗೆ ಕೇಳಬಹುದು. ಇಂದು ನಾನು ನಿಮಗೆ ಹೇಗೆ ಸಹಾಯ ಮಾಡಲಿ?"
                    AppLanguage.ENGLISH -> "🤖 Namaste! I am your HealLens AI Health Assistant.\n\nAsk me about any symptoms (headache, cough, fever, stomach pain, fatigue), home remedies, or lab reports. How can I help you today?"
                }
            }

            else -> {
                when (language) {
                    AppLanguage.TAMIL -> "🤖 உங்கள் உடல்நிலையைக் கவனிக்க உதவுகிறேன். உங்களுக்கு தலைவலி, காய்ச்சல், இருமல், வயிற்று வலி அல்லது வேறு ஏதேனும் அசௌகரியம் உள்ளதா?"
                    AppLanguage.HINDI -> "🤖 स्वास्थ्य मार्गदर्शन के लिए मैं यहाँ हूँ। क्या आपको सिरदर्द, बुखार, खांसी या पेट दर्द की समस्या है?"
                    AppLanguage.KANNADA -> "🤖 ನಿಮ್ಮ ಆರೋಗ್ಯ ಮಾರ್ಗದರ್ಶನಕ್ಕಾಗಿ ನಾನಿದ್ದೇನೆ. ನಿಮಗೆ ತಲೆನೋವು, ಜ್ವರ, ಕೆಮ್ಮು ಅಥವಾ ಹೊಟ್ಟೆ ನೋವು ಇದೆಯೇ?"
                    AppLanguage.ENGLISH -> "🤖 I am here to help with your health questions. Are you experiencing headache, fever, cough, stomach pain, or fatigue?"
                }
            }
        }
    }
}

@Composable
fun HealthAssistantScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentLang by LanguageManager.currentLanguageFlow.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val conversationState = remember { HealthConversationState() }

    val initialWelcomeMsg = AppStrings.get("chatbot_welcome_msg", currentLang)

    val messages = remember(currentLang) {
        mutableStateListOf(
            ChatMessage(
                sender = "ASSISTANT",
                content = initialWelcomeMsg
            )
        )
    }

    fun sendMessage(queryText: String) {
        if (queryText.trim().isEmpty() || isThinking) return

        val userQuery = queryText.trim()
        val userMsg = ChatMessage(sender = "USER", content = userQuery)
        messages.add(userMsg)
        inputText = ""
        isThinking = true

        scope.launch {
            delay(100)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        scope.launch {
            delay(700)

            val detected = HealthResponseEngine.detectTopic(userQuery, conversationState)
            val reply = HealthResponseEngine.generateReply(userQuery, conversationState, currentLang)
            val isRedFlag = detected == HealthTopic.RED_FLAG

            messages.add(ChatMessage(sender = "ASSISTANT", content = reply, isRedFlag = isRedFlag))
            isThinking = false

            delay(100)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 16.dp)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.15f))
                            .border(1.dp, CyanPrimary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Assistant",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppStrings.get("chatbot_title", currentLang),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = AppStrings.get("chatbot_subtitle", currentLang),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    text = AppStrings.get("chatbot_quick_pneumonia", currentLang),
                    onClick = { sendMessage("Tell me about Pneumonia symptoms and care") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = AppStrings.get("chatbot_quick_cough", currentLang),
                    onClick = { sendMessage("What remedies can I try for cough?") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = AppStrings.get("chatbot_quick_glucose", currentLang),
                    onClick = { sendMessage("How to manage high glucose levels?") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }

                if (isThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CyanPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Assistant is thinking...",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = AppStrings.get("chatbot_placeholder", currentLang),
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = SurfaceGlassBorder,
                        focusedContainerColor = SurfaceGlass,
                        unfocusedContainerColor = SurfaceGlass,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary)
                        .clickable { sendMessage(inputText) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = DarkBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "USER"
    val isRedFlag = message.isRedFlag

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isRedFlag) Color(0xFFEF4444).copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRedFlag) Icons.Default.Warning else Icons.Default.Android,
                    contentDescription = null,
                    tint = if (isRedFlag) Color(0xFFEF4444) else CyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    when {
                        isRedFlag -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        isUser -> CyanPrimary.copy(alpha = 0.2f)
                        else -> SurfaceGlass
                    }
                )
                .border(
                    width = 1.dp,
                    color = when {
                        isRedFlag -> Color(0xFFEF4444).copy(alpha = 0.4f)
                        isUser -> CyanPrimary.copy(alpha = 0.4f)
                        else -> SurfaceGlassBorder
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = when {
                    isRedFlag -> Color(0xFFEF4444)
                    isUser -> TextPrimary
                    else -> TextPrimary
                },
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = if (isRedFlag) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PurpleAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PurpleAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceGlass)
            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = CyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
