package com.heallens.android.utils

object AppStrings {

    private val strings = mapOf(
        // Bottom Navigation
        "nav_dashboard" to mapOf(
            AppLanguage.ENGLISH to "Dashboard",
            AppLanguage.HINDI to "डैशबोर्ड",
            AppLanguage.TAMIL to "டாஷ்போர்டு",
            AppLanguage.KANNADA to "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್"
        ),
        "nav_scanner" to mapOf(
            AppLanguage.ENGLISH to "Scanner",
            AppLanguage.HINDI to "स्कैनर",
            AppLanguage.TAMIL to "ஸ்கேனர்",
            AppLanguage.KANNADA to "ಸ್ಕ್ಯಾನರ್"
        ),
        "nav_report" to mapOf(
            AppLanguage.ENGLISH to "Report",
            AppLanguage.HINDI to "रिपोर्ट",
            AppLanguage.TAMIL to "அறிக்கை",
            AppLanguage.KANNADA to "ವರದಿ"
        ),
        "nav_chatbot" to mapOf(
            AppLanguage.ENGLISH to "Assistant",
            AppLanguage.HINDI to "सहायक",
            AppLanguage.TAMIL to "உதவியாளர்",
            AppLanguage.KANNADA to "ಸಹಾಯಕ"
        ),
        "nav_history" to mapOf(
            AppLanguage.ENGLISH to "History",
            AppLanguage.HINDI to "इतिहास",
            AppLanguage.TAMIL to "வரலாறு",
            AppLanguage.KANNADA to "ಇತಿಹಾಸ"
        ),
        "nav_ayurveda" to mapOf(
            AppLanguage.ENGLISH to "Ayurveda",
            AppLanguage.HINDI to "आयुर्वेद",
            AppLanguage.TAMIL to "ஆயுர்வேதம்",
            AppLanguage.KANNADA to "ಆಯುರ್ವೇದ"
        ),
        "nav_sos" to mapOf(
            AppLanguage.ENGLISH to "🚨 Emergency SOS",
            AppLanguage.HINDI to "🚨 आपातकालीन एसओएस",
            AppLanguage.TAMIL to "🚨 அவசர SOS",
            AppLanguage.KANNADA to "🚨 ತುರ್ತು SOS"
        ),
        "nav_profile" to mapOf(
            AppLanguage.ENGLISH to "Profile",
            AppLanguage.HINDI to "प्रोफ़ाइल",
            AppLanguage.TAMIL to "சுயவிவரம்",
            AppLanguage.KANNADA to "ಪ್ರೊಫೈಲ್"
        ),
        "nav_logout" to mapOf(
            AppLanguage.ENGLISH to "Logout",
            AppLanguage.HINDI to "लॉग आउट",
            AppLanguage.TAMIL to "வெளியேறு",
            AppLanguage.KANNADA to "ಲಾಗ್‌ಔಟ್"
        ),

        // Common Buttons & Actions
        "btn_save" to mapOf(
            AppLanguage.ENGLISH to "Save",
            AppLanguage.HINDI to "सहेजें",
            AppLanguage.TAMIL to "சேமி",
            AppLanguage.KANNADA to "ಉಳಿಸಿ"
        ),
        "btn_cancel" to mapOf(
            AppLanguage.ENGLISH to "Cancel",
            AppLanguage.HINDI to "रद्द करें",
            AppLanguage.TAMIL to "ரத்து செய்",
            AppLanguage.KANNADA to "ರದ್ದುಗೊಳಿಸಿ"
        ),
        "btn_delete" to mapOf(
            AppLanguage.ENGLISH to "Delete",
            AppLanguage.HINDI to "हटाएं",
            AppLanguage.TAMIL to "நீக்கு",
            AppLanguage.KANNADA to "அளಿಸಿ"
        ),
        "btn_close" to mapOf(
            AppLanguage.ENGLISH to "Close",
            AppLanguage.HINDI to "बंद करें",
            AppLanguage.TAMIL to "மூடு",
            AppLanguage.KANNADA to "ಮುಚ್ಚಿ"
        ),
        "btn_call" to mapOf(
            AppLanguage.ENGLISH to "Call",
            AppLanguage.HINDI to "कॉल करें",
            AppLanguage.TAMIL to "அழைக்கவும்",
            AppLanguage.KANNADA to "ಕರೆ ಮಾಡಿ"
        ),
        "btn_view_details" to mapOf(
            AppLanguage.ENGLISH to "View Details",
            AppLanguage.HINDI to "विवरण देखें",
            AppLanguage.TAMIL to "விவரங்களைக் காண்க",
            AppLanguage.KANNADA to "ವಿವರಗಳನ್ನು ವೀಕ್ಷಿಸಿ"
        ),
        "btn_loading" to mapOf(
            AppLanguage.ENGLISH to "Loading...",
            AppLanguage.HINDI to "लोड हो रहा है...",
            AppLanguage.TAMIL to "ஏற்றுகிறது...",
            AppLanguage.KANNADA to "ಲೋಡ್ ಆಗುತ್ತಿದೆ..."
        ),
        "btn_confirm" to mapOf(
            AppLanguage.ENGLISH to "Confirm",
            AppLanguage.HINDI to "पुष्टि करें",
            AppLanguage.TAMIL to "உறுதிப்படுத்து",
            AppLanguage.KANNADA to "ಖಚಿತಪಡಿಸಿ"
        ),

        // Engine Status Pill Text
        "engine_online" to mapOf(
            AppLanguage.ENGLISH to "AI Online",
            AppLanguage.HINDI to "एआई ऑनलाइन",
            AppLanguage.TAMIL to "AI ஆன்லைன்",
            AppLanguage.KANNADA to "ಎಐ ಆನ್‌ಲೈನ್"
        ),

        // Dashboard Welcome Card Translations Matching PDD i18n.js
        "dashboard_welcome" to mapOf(
            AppLanguage.ENGLISH to "Welcome to HealLens AI",
            AppLanguage.HINDI to "हीललेंस एआई में आपका स्वागत है",
            AppLanguage.TAMIL to "ஹீல் லென்ஸ் AI-க்கு நல்வரவு",
            AppLanguage.KANNADA to "ಹೀಲ್‌ಲೆನ್ಸ್ ಎಐಗೆ ಸುಸ್ವಾಗತ"
        ),
        "dashboard_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Intelligent Clinical Diagnostics & Ayurvedic Wellness Platform",
            AppLanguage.HINDI to "बुद्धिमान नैदानिक निदान और आयुर्वेदिक कल्याण मंच",
            AppLanguage.TAMIL to "செயற்கை நுண்ணறிவு மருத்துவ நோயறிதல் & ஆயுர்வேத நல்வாழ்வு தளம்",
            AppLanguage.KANNADA to "ಬುದ್ಧಿವಂತ ಕ್ಲಿನಿಕಲ್ ರೋಗನಿರ್ಣಯ ಮತ್ತು ಆಯುರ್ವೇದ ಆರೋಗ್ಯ ವೇದಿಕೆ"
        ),
        "dashboard_quick_scan" to mapOf(
            AppLanguage.ENGLISH to "Start Visual Scan",
            AppLanguage.HINDI to "विजुअल स्कैन शुरू करें",
            AppLanguage.TAMIL to "விஷுவல் ஸ்கேன் தொடங்கவும்",
            AppLanguage.KANNADA to "ದೃಶ್ಯ ಸ್ಕ್ಯಾನ್ ಪ್ರಾರಂಭಿಸಿ"
        ),
        "dashboard_quick_report" to mapOf(
            AppLanguage.ENGLISH to "Analyze Lab Report",
            AppLanguage.HINDI to "लैब रिपोर्ट विश्लेषण करें",
            AppLanguage.TAMIL to "ஆய்வக அறிக்கையை பகுப்பாய்வு செய்க",
            AppLanguage.KANNADA to "ಲ್ಯಾಬ್ ವರದಿ ವಿಶ್ಲೇಷಿಸಿ"
        ),

        // Dashboard Redesign Translations (Section 2, 3, 4)
        "dashboard_about_title" to mapOf(
            AppLanguage.ENGLISH to "Your Health, Smarter with HealLens AI",
            AppLanguage.HINDI to "आपका स्वास्थ्य, हीललेंस एआई के साथ अधिक बुद्धिमान",
            AppLanguage.TAMIL to "உங்கள் ஆரோக்கியம், ஹீல் லென்ஸ் AI உடன் சுலபமாக",
            AppLanguage.KANNADA to "ನಿಮ್ಮ ಆರೋಗ್ಯ, ಹೀಲ್‌ಲೆನ್ಸ್ ಎಐಯೊಂದಿಗೆ ಸ್ಮಾರ್ಟ್"
        ),
        "dashboard_about_desc" to mapOf(
            AppLanguage.ENGLISH to "HealLens AI helps you understand your health information through AI-assisted image analysis, laboratory report insights, and personalized health guidance.",
            AppLanguage.HINDI to "हीललेंस एआई आपको एआई-सहायता प्राप्त छवि विश्लेषण, प्रयोगशाला रिपोर्ट अंतर्दृष्टि और व्यक्तिगत स्वास्थ्य मार्गदर्शन के माध्यम से अपनी स्वास्थ्य जानकारी को समझने में मदद करता है।",
            AppLanguage.TAMIL to "ஹீல் லென்ஸ் AI உங்களுக்கு AI-உதவியுடன் கூடிய பட பகுப்பாய்வு, ஆய்வக அறிக்கை விவரங்கள் மற்றும் தனிப்பயனாக்கப்பட்ட சுகாதார வழிகாட்டுதல் மூலம் உங்கள் சுகாதாரத் தகவலைப் புரிந்துகொள்ள உதவுகிறது.",
            AppLanguage.KANNADA to "ಹೀಲ್‌ಲೆನ್ಸ್ ಎಐ ನಿಮಗೆ ಎಐ-ಸಹಾಯದ ಚಿತ್ರ ವಿಶ್ಲೇಷಣೆ, ಪ್ರಯೋಗಾಲಯ ವರದಿ ಒಳನೋಟಗಳು ಮತ್ತು ವೈಯಕ್ತಿಕಗೊಳಿಸಿದ ಆರೋಗ್ಯ ಮಾರ್ಗದರ್ಶನದ ಮೂಲಕ ನಿಮ್ಮ ಆರೋಗ್ಯ ಮಾಹಿತಿಯನ್ನು ಅರ್ಥಮಾಡಿಕೊಳ್ಳಲು ಸಹಾಯ ಮಾಡುತ್ತದೆ."
        ),
        "dashboard_capabilities_title" to mapOf(
            AppLanguage.ENGLISH to "HealLens Capabilities",
            AppLanguage.HINDI to "हीललेंस की क्षमताएं",
            AppLanguage.TAMIL to "ஹீல் லென்ஸ் திறன்கள்",
            AppLanguage.KANNADA to "ಹೀಲ್‌ಲೆನ್ಸ್ ಸಾಮರ್ಥ್ಯಗಳು"
        ),
        "cap_visual_title" to mapOf(
            AppLanguage.ENGLISH to "AI Visual Analysis",
            AppLanguage.HINDI to "एआई विजुअल विश्लेषण",
            AppLanguage.TAMIL to "AI விஷுவல் பகுப்பாய்வு",
            AppLanguage.KANNADA to "ಎಐ ದೃಶ್ಯ ವಿಶ್ಲೇಷಣೆ"
        ),
        "cap_visual_desc" to mapOf(
            AppLanguage.ENGLISH to "Analyze supported medical images with AI assistance",
            AppLanguage.HINDI to "एआई सहायता से समर्थित चिकित्सा छवियों का विश्लेषण करें",
            AppLanguage.TAMIL to "AI உதவியுடன் மருத்துவப் படங்களை பகுப்பாய்வு செய்க",
            AppLanguage.KANNADA to "ಎಐ ಸಹಾಯದೊಂದಿಗೆ ವೈದ್ಯಕೀಯ ಚಿತ್ರಗಳನ್ನು ವಿಶ್ಲೇಷಿಸಿ"
        ),
        "cap_report_title" to mapOf(
            AppLanguage.ENGLISH to "Lab Report Insights",
            AppLanguage.HINDI to "लैब रिपोर्ट अंतर्दृष्टि",
            AppLanguage.TAMIL to "ஆய்வக அறிக்கை விவரங்கள்",
            AppLanguage.KANNADA to "ಲ್ಯಾಬ್ ವರದಿ ಒಳನೋಟಗಳು"
        ),
        "cap_report_desc" to mapOf(
            AppLanguage.ENGLISH to "Analyze laboratory reports and understand key findings",
            AppLanguage.HINDI to "प्रयोगशाला रिपोर्ट का विश्लेषण करें और प्रमुख निष्कर्षों को समझें",
            AppLanguage.TAMIL to "ஆய்வக அறிக்கைகளை ஆராய்ந்து முக்கிய விவரங்களை அறியவும்",
            AppLanguage.KANNADA to "ಪ್ರಯೋಗಾಲಯ ವರದಿಗಳನ್ನು ವಿಶ್ಲೇಷಿಸಿ ಪ್ರಮುಖ ಒಳನೋಟಗಳನ್ನು ತಿಳಿಯಿರಿ"
        ),
        "cap_assistant_title" to mapOf(
            AppLanguage.ENGLISH to "AI Health Assistant",
            AppLanguage.HINDI to "एआई स्वास्थ्य सहायक",
            AppLanguage.TAMIL to "AI சுகாதார உதவியாளர்",
            AppLanguage.KANNADA to "ಎಐ ಆರೋಗ್ಯ ಸಹಾಯಕ"
        ),
        "cap_assistant_desc" to mapOf(
            AppLanguage.ENGLISH to "Ask health questions and receive AI-guided advice",
            AppLanguage.HINDI to "स्वास्थ्य प्रश्न पूछें और एआई-निर्देशित सलाह प्राप्त करें",
            AppLanguage.TAMIL to "சுகாதாரக் கேள்விகளைக் கேட்டு AI வழிகாட்டுதலைப் பெறுங்கள்",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ ಪ್ರಶ್ನೆಗಳನ್ನು ಕೇಳಿ ಎಐ ಮಾರ್ಗದರ್ಶನ ಪಡೆಯಿರಿ"
        ),
        "cap_history_title" to mapOf(
            AppLanguage.ENGLISH to "Health History",
            AppLanguage.HINDI to "स्वास्थ्य इतिहास",
            AppLanguage.TAMIL to "சுகாதார வரலாறு",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ ಇತಿಹಾಸ"
        ),
        "cap_history_desc" to mapOf(
            AppLanguage.ENGLISH to "Access and review your previous clinical analyses",
            AppLanguage.HINDI to "अपने पिछले नैदानिक विश्लेषणों तक पहुंचें और समीक्षा करें",
            AppLanguage.TAMIL to "முந்தைய மருத்துவ பகுப்பாய்வுகளைப் பார்வையிடவும்",
            AppLanguage.KANNADA to "ನಿಮ್ಮ ಹಿಂದಿನ ಕ್ಲಿನಿಕಲ್ ವಿಶ್ಲೇಷಣೆಗಳನ್ನು ವೀಕ್ಷಿಸಿ"
        ),
        "dashboard_quick_actions_title" to mapOf(
            AppLanguage.ENGLISH to "Quick Actions",
            AppLanguage.HINDI to "त्वरित कार्रवाई",
            AppLanguage.TAMIL to "விரைவு நடவடிக்கைகள்",
            AppLanguage.KANNADA to "ತ್ವರಿತ ಕ್ರಿಯೆಗಳು"
        ),
        "quick_action_scanner_title" to mapOf(
            AppLanguage.ENGLISH to "Visual Scanner",
            AppLanguage.HINDI to "विजुअल स्कैनर",
            AppLanguage.TAMIL to "விஷுவல் ஸ்கேனர்",
            AppLanguage.KANNADA to "ದೃಶ್ಯ ಸ್ಕ್ಯಾನರ್"
        ),
        "quick_action_scanner_desc" to mapOf(
            AppLanguage.ENGLISH to "Analyze supported medical images with AI",
            AppLanguage.HINDI to "एआई के साथ समर्थित चिकित्सा छवियों का विश्लेषण करें",
            AppLanguage.TAMIL to "AI மூலம் மருத்துவப் படங்களை பகுப்பாய்வு செய்க",
            AppLanguage.KANNADA to "ಎಐ ಜೊತೆಗೆ ವೈದ್ಯಕೀಯ ಚಿತ್ರಗಳನ್ನು ವಿಶ್ಲೇಷಿಸಿ"
        ),
        "quick_action_report_title" to mapOf(
            AppLanguage.ENGLISH to "Report Analyzer",
            AppLanguage.HINDI to "रिपोर्ट विश्लेषक",
            AppLanguage.TAMIL to "அறிக்கை பகுப்பாய்வி",
            AppLanguage.KANNADA to "ವರದಿ ವಿಶ್ಲೇಷಕ"
        ),
        "quick_action_report_desc" to mapOf(
            AppLanguage.ENGLISH to "Extract and interpret blood biomarker lab values",
            AppLanguage.HINDI to "रक्त बायोमार्कर लैब मूल्यों को निकालें और व्याख्या करें",
            AppLanguage.TAMIL to "இரத்த பயோமார்க்கர் ஆய்வக மதிப்புகளை பிரித்தெடுத்து பகுப்பாய்வு செய்க",
            AppLanguage.KANNADA to "ರಕ್ತದ ಬಯೋಮಾರ್ಕರ್ ಲ್ಯಾಬ್ ಮೌಲ್ಯಗಳನ್ನು ಹೊರತೆಗೆದು ವಿಶ್ಲೇಷಿಸಿ"
        ),
        "quick_action_assistant_title" to mapOf(
            AppLanguage.ENGLISH to "AI Health Assistant",
            AppLanguage.HINDI to "एआई स्वास्थ्य सहायक",
            AppLanguage.TAMIL to "AI சுகாதார உதவியாளர்",
            AppLanguage.KANNADA to "எಐ ಆರೋಗ್ಯ ಸಹಾಯಕ"
        ),
        "quick_action_assistant_desc" to mapOf(
            AppLanguage.ENGLISH to "Interactive health query & symptom guidance bot",
            AppLanguage.HINDI to "इंटरएक्टिव स्वास्थ्य प्रश्न और लक्षण मार्गदर्शन बोट",
            AppLanguage.TAMIL to "ஊடாடும் சுகாதார கேள்வி & அறிகுறி வழிகாட்டும் பாட்",
            AppLanguage.KANNADA to "ಸಂವಾದಾತ್ಮಕ ಆರೋಗ್ಯ ಪ್ರಶ್ನೆ ಮತ್ತು ರೋಗಲಕ್ಷಣ ಮಾರ್ಗದರ್ಶನ ಬೋಟ್"
        ),
        "quick_action_history_title" to mapOf(
            AppLanguage.ENGLISH to "Health History",
            AppLanguage.HINDI to "स्वास्थ्य इतिहास",
            AppLanguage.TAMIL to "சுகாதார வரலாறு",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ ಇತಿಹಾಸ"
        ),
        "quick_action_history_desc" to mapOf(
            AppLanguage.ENGLISH to "View all saved medical records & diagnostic results",
            AppLanguage.HINDI to "सभी सहेजे गए मेडिकल रिकॉर्ड और नैदानिक परिणाम देखें",
            AppLanguage.TAMIL to "சேமிக்கப்பட்ட அனைத்து மருத்துவ பதிவுகளையும் மருத்துவ முடிவுகளையும் காண்க",
            AppLanguage.KANNADA to "ಉಳಿಸಿದ ಎಲ್ಲಾ ವೈದ್ಯಕೀಯ ದಾಖಲೆಗಳು ಮತ್ತು ರೋಗನಿರ್ಣಯದ ಫಲಿತಾಂಶಗಳನ್ನು ವೀಕ್ಷಿಸಿ"
        ),
        "dashboard_scanner_title" to mapOf(
            AppLanguage.ENGLISH to "Visual Scanner",
            AppLanguage.HINDI to "विजुअल स्कैनर",
            AppLanguage.TAMIL to "விஷுவல் ஸ்கேனர்",
            AppLanguage.KANNADA to "ದೃಶ್ಯ ಸ್ಕ್ಯಾನರ್"
        ),
        "dashboard_scanner_desc" to mapOf(
            AppLanguage.ENGLISH to "Analyze supported medical images with AI",
            AppLanguage.HINDI to "एआई के साथ समर्थित चिकित्सा छवियों का विश्लेषण करें",
            AppLanguage.TAMIL to "AI மூலம் மருத்துவப் படங்களை பகுப்பாய்வு செய்க",
            AppLanguage.KANNADA to "ಎಐ ಜೊತೆಗೆ ವೈದ್ಯಕೀಯ ಚಿತ್ರಗಳನ್ನು ವಿಶ್ಲೇಷಿಸಿ"
        ),
        "dashboard_report_title" to mapOf(
            AppLanguage.ENGLISH to "Report Analyzer",
            AppLanguage.HINDI to "रिपोर्ट विश्लेषक",
            AppLanguage.TAMIL to "அறிக்கை பகுப்பாய்வி",
            AppLanguage.KANNADA to "ವರದಿ ವಿಶ್ಲೇಷಕ"
        ),
        "dashboard_report_desc" to mapOf(
            AppLanguage.ENGLISH to "Extract and interpret blood biomarker lab values",
            AppLanguage.HINDI to "रक्त बायोमार्कर लैब मूल्यों को निकालें और व्याख्या करें",
            AppLanguage.TAMIL to "இரத்த பயோமார்க்கர் ஆய்வக மதிப்புகளை பிரித்தெடுத்து பகுப்பாய்வு செய்க",
            AppLanguage.KANNADA to "ರಕ್ತದ ಬಯೋಮಾರ್ಕರ್ ಲ್ಯಾಬ್ ಮೌಲ್ಯಗಳನ್ನು ಹೊರತೆಗೆದು ವಿಶ್ಲೇಷಿಸಿ"
        ),
        "dashboard_chatbot_title" to mapOf(
            AppLanguage.ENGLISH to "AI Health Assistant",
            AppLanguage.HINDI to "एआई स्वास्थ्य सहायक",
            AppLanguage.TAMIL to "AI சுகாதார உதவியாளர்",
            AppLanguage.KANNADA to "ಎಐ ಆರೋಗ್ಯ ಸಹಾಯಕ"
        ),
        "dashboard_chatbot_desc" to mapOf(
            AppLanguage.ENGLISH to "Interactive health query & symptom guidance bot",
            AppLanguage.HINDI to "इंटरएक्टिव स्वास्थ्य प्रश्न और लक्षण मार्गदर्शन बोट",
            AppLanguage.TAMIL to "ஊடாடும் சுகாதார கேள்வி & அறிகுறி வழிகாட்டும் பாட்",
            AppLanguage.KANNADA to "ಸಂವಾದாತ್ಮಕ ಆರೋಗ್ಯ ಪ್ರಶ್ನೆ ಮತ್ತು ರೋಗಲಕ್ಷಣ ಮಾರ್ಗದರ್ಶನ ಬೋಟ್"
        ),
        "dashboard_history_title" to mapOf(
            AppLanguage.ENGLISH to "Health History",
            AppLanguage.HINDI to "स्वास्थ्य इतिहास",
            AppLanguage.TAMIL to "சுகாதார வரலாறு",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ ಇತಿಹಾಸ"
        ),
        "dashboard_history_desc" to mapOf(
            AppLanguage.ENGLISH to "View all saved medical records & diagnostic results",
            AppLanguage.HINDI to "सभी सहेजे गए मेडिकल रिकॉर्ड और नैदानिक परिणाम देखें",
            AppLanguage.TAMIL to "சேமிக்கப்பட்ட அனைத்து மருத்துவ பதிவுகளையும் மருத்துவ முடிவுகளையும் காண்க",
            AppLanguage.KANNADA to "ಉಳಿಸಿದ ಎಲ್ಲಾ ವೈದ್ಯಕೀಯ ದಾಖಲೆಗಳು ಮತ್ತು ರೋಗನಿರ್ಣಯದ ಫಲಿತಾಂಶಗಳನ್ನು ವೀಕ್ಷಿಸಿ"
        ),

        // Medical AI Disclaimer Localization Entries
        "disclaimer_title" to mapOf(
            AppLanguage.ENGLISH to "Medical AI Disclaimer",
            AppLanguage.HINDI to "चिकित्सा एआई अस्वीकरण",
            AppLanguage.TAMIL to "மருத்துவ AI மறுப்புரை",
            AppLanguage.KANNADA to "ವೈದ್ಯಕೀಯ ಎಐ ಹಕ್ಕುತ್ಯಾಗ"
        ),
        "disclaimer_body" to mapOf(
            AppLanguage.ENGLISH to "HealLens AI provides preliminary health analysis for informational purposes only.\n\nThe results generated by this application are based on Artificial Intelligence and must not be considered a medical diagnosis.\n\nHealLens does not replace qualified doctors, hospitals, laboratory tests, or professional medical advice.\n\nAlways consult a licensed healthcare professional before making any medical decisions or starting any treatment.",
            AppLanguage.HINDI to "हीललेंस एआई केवल सूचनात्मक उद्देश्यों के लिए प्रारंभिक स्वास्थ्य विश्लेषण प्रदान करता है।\n\nइस एप्लिकेशन द्वारा उत्पन्न परिणाम आर्टिफिशियल इंटेलिजेंस पर आधारित हैं और इन्हें चिकित्सा निदान नहीं माना जाना चाहिए।\n\nहीललेंस योग्य डॉक्टरों, अस्पतालों, प्रयोगशाला परीक्षणों या पेशेवर चिकित्सा सलाह का विकल्प नहीं है।\n\nकोई भी चिकित्सा निर्णय लेने या इलाज शुरू करने से पहले हमेशा लाइसेंस प्राप्त स्वास्थ्य देखभाल पेशेवर से परामर्श लें।",
            AppLanguage.TAMIL to "ஹீல் லென்ஸ் AI தகவல் நோக்கங்களுக்காக மட்டுமே முதற்கட்ட சுகாதார பகுப்பாய்வை வழங்குகிறது.\n\nஇந்த செயலினால் உருவாக்கப்படும் முடிவுகள் செயற்கை நுண்ணறிவை அடிப்படையாகக் கொண்டவை மற்றும் மருத்துவ நோயறிதலாகக் கருதப்படக்கூடாது.\n\nஹீல் லென்ஸ் தகுதியான மருத்துவர்கள், மருத்துவமனைகள், ஆய்வக பரிசோதனைகள் அல்லது தொழில்முறை மருத்துவ ஆலோசனைகளுக்கு மாற்றாகாது.\n\nஎந்தவொரு மருத்துவ முடிவுகளையும் எடுப்பதற்கு முன் அல்லது சிகிச்சையைத் தொடங்குவதற்கு முன் எப்போதும் உரிமம் பெற்ற சுகாதார நிபுணரை அணுகவும்.",
            AppLanguage.KANNADA to "ಹೀಲ್‌ಲೆನ್ಸ್ ಎಐ ಕೇವಲ ಮಾಹಿತಿಯ ಉದ್ದೇಶಗಳಿಗಾಗಿ ಪ್ರಾಥಮಿಕ ಆರೋಗ್ಯ ವಿಶ್ಲೇಷಣೆಯನ್ನು ಒದಗಿಸುತ್ತದೆ.\n\nಈ ಅಪ್ಲಿಕೇಶನ್‌ನಿಂದ ರಚಿಸಲಾದ ಫಲಿತಾಂಶಗಳು ಕೃತಕ ಬುದ್ಧಿಮತ್ತೆಯನ್ನು ಆಧರಿಸಿವೆ ಮತ್ತು ಇದನ್ನು ವೈದ್ಯಕೀಯ ರೋಗನಿರ್ಣಯವೆಂದು ಪರಿಗಣಿಸಬಾರದು.\n\nಹೀಲ್‌ಲೆನ್ಸ್ ಅರ್ಹ ವೈದ್ಯರು, ಆಸ್ಪತ್ರೆಗಳು, ಪ್ರಯೋಗಾಲಯ ಪರೀಕ್ಷೆಗಳು ಅಥವಾ ವೃತ್ತಿಪರ ವೈದ್ಯಕೀಯ ಸಲಹೆಗೆ ಪರ್ಯಾಯವಲ್ಲ.\n\nಯಾವುದೇ ವೈದ್ಯಕೀಯ ನಿರ್ಧಾರಗಳನ್ನು ತೆಗೆದುಕೊಳ್ಳುವ ಮೊದಲು ಅಥವಾ ಚಿಕಿತ್ಸೆಯನ್ನು ಪ್ರಾರಂಭಿಸುವ ಮೊದಲು ಯಾವಾಗಲೂ ಪರವಾನಗಿ ಪಡೆದ ಆರೋಗ್ಯ ವೃತ್ತಿಪರರನ್ನು ಸಂಪರ್ಕಿಸಿ."
        ),
        "disclaimer_emergency" to mapOf(
            AppLanguage.ENGLISH to "🚨 If you are experiencing severe symptoms or a medical emergency, seek immediate medical attention.",
            AppLanguage.HINDI to "🚨 यदि आप गंभीर लक्षणों या चिकित्सा आपातकाल का अनुभव कर रहे हैं, तो तुरंत चिकित्सा सहायता लें।",
            AppLanguage.TAMIL to "🚨 நீங்கள் கடுமையான அறிகுறிகள் அல்லது அவசர மருத்துவ நிலையை எதிர்கொண்டால், உடனடியாக மருத்துவ உதவியை நாடுங்கள்.",
            AppLanguage.KANNADA to "🚨 ನೀವು ತೀವ್ರವಾದ ರೋಗಲಕ್ಷಣಗಳು ಅಥವಾ ವೈದ್ಯಕೀಯ ತುರ್ತು ಪರಿಸ್ಥಿತಿಯನ್ನು ಅನುಭವಿಸುತ್ತಿದ್ದರೆ, ತಕ್ಷಣ ವೈದ್ಯಕೀಯ ನೆರವು ಪಡೆಯಿರಿ."
        ),
        "disclaimer_btn_agree" to mapOf(
            AppLanguage.ENGLISH to "I Understand",
            AppLanguage.HINDI to "मैं समझता/समझती हूँ",
            AppLanguage.TAMIL to "நான் புரிந்து கொண்டேன்",
            AppLanguage.KANNADA to "ನನಗೆ ಅರ್ಥವಾಯಿತು"
        ),

        // Scanner Symptom Chips Localization Mappings (34 keys)
        // Lungs Symptoms (10 keys)
        "sym_cough" to mapOf(
            AppLanguage.ENGLISH to "Persistent Cough",
            AppLanguage.HINDI to "लगातार खांसी",
            AppLanguage.TAMIL to "தொடர் இருமல்",
            AppLanguage.KANNADA to "ನಿರಂತರ ಕೆಮ್ಮು"
        ),
        "sym_sweats" to mapOf(
            AppLanguage.ENGLISH to "Night Sweats",
            AppLanguage.HINDI to "रात में पसीना आना",
            AppLanguage.TAMIL to "இரவு வேர்வை",
            AppLanguage.KANNADA to "ರಾತ್ರಿ ಬೆವರುವುದು"
        ),
        "sym_chest_pain" to mapOf(
            AppLanguage.ENGLISH to "Chest Pain",
            AppLanguage.HINDI to "सीने में दर्द",
            AppLanguage.TAMIL to "நெஞ்சு வலி",
            AppLanguage.KANNADA to "ಎದೆ ನೋவு"
        ),
        "sym_fever" to mapOf(
            AppLanguage.ENGLISH to "High Fever",
            AppLanguage.HINDI to "तेज बुखार",
            AppLanguage.TAMIL to "அதிக காய்ச்சல்",
            AppLanguage.KANNADA to "ಹೆಚ್ಚಿನ ಜ್ವರ"
        ),
        "sym_breathless" to mapOf(
            AppLanguage.ENGLISH to "Breathlessness",
            AppLanguage.HINDI to "सांस फूलना",
            AppLanguage.TAMIL to "மூச்சுத்திணறல்",
            AppLanguage.KANNADA to "ಉಸಿರಾಟದ ತೊಂದರೆ"
        ),
        "sym_blood_sputum" to mapOf(
            AppLanguage.ENGLISH to "Blood in Sputum",
            AppLanguage.HINDI to "बलगम में खून",
            AppLanguage.TAMIL to "சளியில் ரத்தம்",
            AppLanguage.KANNADA to "ಕಫದಲ್ಲಿ ರಕ್ತ"
        ),
        "sym_weight_loss" to mapOf(
            AppLanguage.ENGLISH to "Weight Loss",
            AppLanguage.HINDI to "वजन कम होना",
            AppLanguage.TAMIL to "எடை இழப்பு",
            AppLanguage.KANNADA to "ತೂಕ ನಷ್ಟ"
        ),
        "sym_phlegm" to mapOf(
            AppLanguage.ENGLISH to "Yellow Phlegm",
            AppLanguage.HINDI to "पीला बलगम",
            AppLanguage.TAMIL to "மஞ்சள் சளி",
            AppLanguage.KANNADA to "ಹಳದಿ ಕಫ"
        ),
        "sym_taste" to mapOf(
            AppLanguage.ENGLISH to "Loss of Taste",
            AppLanguage.HINDI to "स्वाद का न आना",
            AppLanguage.TAMIL to "சுவை இழப்பு",
            AppLanguage.KANNADA to "ருಚಿ ನಷ್ಟ"
        ),
        "sym_fatigue" to mapOf(
            AppLanguage.ENGLISH to "Extreme Fatigue",
            AppLanguage.HINDI to "अत्यधिक थकान",
            AppLanguage.TAMIL to "கடுமையான சோர்வு",
            AppLanguage.KANNADA to "ಅತಿಯಾದ ಆಯಾಸ"
        ),

        // Skin Symptoms (8 keys)
        "sym_red_patches" to mapOf(
            AppLanguage.ENGLISH to "Red Patches",
            AppLanguage.HINDI to "लाल धब्बे",
            AppLanguage.TAMIL to "சிவப்பு திட்டுகள்",
            AppLanguage.KANNADA to "ಕೆಂಪು ಕಲೆಗಳು"
        ),
        "sym_itching" to mapOf(
            AppLanguage.ENGLISH to "Itching",
            AppLanguage.HINDI to "खुजली",
            AppLanguage.TAMIL to "அரிப்பு",
            AppLanguage.KANNADA to "ತುರಿಕೆ"
        ),
        "sym_rash" to mapOf(
            AppLanguage.ENGLISH to "Skin Rash",
            AppLanguage.HINDI to "त्वचा पर चकत्ते",
            AppLanguage.TAMIL to "தோல் தடிப்பு",
            AppLanguage.KANNADA to "ಚರ್ಮದ ದದ್ದು"
        ),
        "sym_scales" to mapOf(
            AppLanguage.ENGLISH to "Silvery Scales",
            AppLanguage.HINDI to "चांदी जैसे पपड़ी",
            AppLanguage.TAMIL to "வெள்ளி நிற செதில்கள்",
            AppLanguage.KANNADA to "ಬೆಳ್ಳಿಯ ಹೊಟ್ಟು"
        ),
        "sym_pus" to mapOf(
            AppLanguage.ENGLISH to "Pus Discharge",
            AppLanguage.HINDI to "पस का निकलना",
            AppLanguage.TAMIL to "சீழ் வடிதல்",
            AppLanguage.KANNADA to "ಸೀಳು ಸ್ರವಿಸುವಿಕೆ"
        ),
        "sym_burning" to mapOf(
            AppLanguage.ENGLISH to "Burning Sensation",
            AppLanguage.HINDI to "जलन की अनुभूति",
            AppLanguage.TAMIL to "எரிச்சல் உணர்வு",
            AppLanguage.KANNADA to "ಉರಿಯುವ ಸಂವೇದನೆ"
        ),
        "sym_scaly" to mapOf(
            AppLanguage.ENGLISH to "Scaly Skin",
            AppLanguage.HINDI to "पपड़ीदार त्वचा",
            AppLanguage.TAMIL to "செதில் தோல்",
            AppLanguage.KANNADA to "ಹೊಟ್ಟು ಚರ್ಮ"
        ),
        "sym_boil" to mapOf(
            AppLanguage.ENGLISH to "Skin Boil",
            AppLanguage.HINDI to "त्वचा का फोड़ा",
            AppLanguage.TAMIL to "தோல் கட்டி",
            AppLanguage.KANNADA to "ಚರ್ಮದ ಗಡ್ಡೆ"
        ),

        // Bone / Joint Symptoms (16 keys)
        "sym_severe_pain" to mapOf(
            AppLanguage.ENGLISH to "Sudden Severe Pain",
            AppLanguage.HINDI to "अचानक तेज दर्द",
            AppLanguage.TAMIL to "திடீர் கடுமையான வலி",
            AppLanguage.KANNADA to "ದಿಢೀರ್ ತೀವ್ರ ನೋವು"
        ),
        "sym_swelling_injury" to mapOf(
            AppLanguage.ENGLISH to "Swelling around Injury",
            AppLanguage.HINDI to "चोट के आसपास सूजन",
            AppLanguage.TAMIL to "காயத்தைச் சுற்றி வீக்கம்",
            AppLanguage.KANNADA to "ಗಾಯದ ಸುತ್ತಲೂ ಊತ"
        ),
        "sym_bruising" to mapOf(
            AppLanguage.ENGLISH to "Bruising",
            AppLanguage.HINDI to "खरोंच/नील पड़ना",
            AppLanguage.TAMIL to "காயம்/இரத்தக் கட்டு",
            AppLanguage.KANNADA to "ಗಾಯದ ಕಲೆ"
        ),
        "sym_diff_moving" to mapOf(
            AppLanguage.ENGLISH to "Difficulty Moving",
            AppLanguage.HINDI to "हिलने-डुलने में कठिनाई",
            AppLanguage.TAMIL to "நடைப்பயிற்சி சிரமம்",
            AppLanguage.KANNADA to "ಚಲಿಸಲು ಕಷ್ಟ"
        ),
        "sym_tenderness" to mapOf(
            AppLanguage.ENGLISH to "Tenderness",
            AppLanguage.HINDI to "कोमलता/छूने पर दर्द",
            AppLanguage.TAMIL to "தொட்டால் வலி",
            AppLanguage.KANNADA to "ಮುಟ್ಟಿದರೆ ನೋವು"
        ),
        "sym_deformity" to mapOf(
            AppLanguage.ENGLISH to "Deformity",
            AppLanguage.HINDI to "विकृति",
            AppLanguage.TAMIL to "வடிவக் கேடு",
            AppLanguage.KANNADA to "ವಿಕಾರತೆ"
        ),
        "sym_disability" to mapOf(
            AppLanguage.ENGLISH to "Visible Disability",
            AppLanguage.HINDI to "दृश्यमान विकलांगता",
            AppLanguage.TAMIL to "தெளிவான இயலாமை",
            AppLanguage.KANNADA to "ಕಾಣಿಸುವ ಅಸಾಮರ್ಥ್ಯ"
        ),
        "sym_weight_bear" to mapOf(
            AppLanguage.ENGLISH to "Inability to Bear Weight",
            AppLanguage.HINDI to "वजन सहने में असमर्थता",
            AppLanguage.TAMIL to "எடை தாங்க முடியாமை",
            AppLanguage.KANNADA to "ತೂಕ ಹೊರಲು ಸಾಧ್ಯವಾಗದಿರುವುದು"
        ),
        "sym_crack" to mapOf(
            AppLanguage.ENGLISH to "Crack/Snapping Sound",
            AppLanguage.HINDI to "चटकने की आवाज",
            AppLanguage.TAMIL to "சொடுக்கு சத்தம்",
            AppLanguage.KANNADA to "ಬಿರುಕು ಧ್ವನಿ"
        ),
        "sym_joint_pain" to mapOf(
            AppLanguage.ENGLISH to "Mild Joint Pain",
            AppLanguage.HINDI to "हल्का जोड़ों का दर्द",
            AppLanguage.TAMIL to "லேசான மூட்டு வலி",
            AppLanguage.KANNADA to "ಸಣ್ಣ ಕೀಲು ನೋವು"
        ),
        "sym_stiffness" to mapOf(
            AppLanguage.ENGLISH to "Morning Stiffness",
            AppLanguage.HINDI to "सुबह की जकड़न",
            AppLanguage.TAMIL to "காலை விறைப்பு",
            AppLanguage.KANNADA to "ಬೆಳಗಿನ ಬಿಗಿತ"
        ),
        "sym_swelling_joints" to mapOf(
            AppLanguage.ENGLISH to "Swelling around Joints",
            AppLanguage.HINDI to "जोड़ों के आसपास सूजन",
            AppLanguage.TAMIL to "மூட்டுகளைச் சுற்றி வீக்கம்",
            AppLanguage.KANNADA to "ಕೀಲುಗಳ ಸುತ್ತಲೂ ಊತ"
        ),
        "sym_flexibility" to mapOf(
            AppLanguage.ENGLISH to "Reduced Flexibility",
            AppLanguage.HINDI to "कम लचीलापन",
            AppLanguage.TAMIL to "குறைந்த நெகிழ்வுத்தன்மை",
            AppLanguage.KANNADA to "ಕಡಿಮೆಯಾದ ನಮ್ಯತೆ"
        ),
        "sym_warmth" to mapOf(
            AppLanguage.ENGLISH to "Warmth around Joint",
            AppLanguage.HINDI to "जोड़ के आसपास गर्मी",
            AppLanguage.TAMIL to "மூட்டைச் சுற்றி வெப்பம்",
            AppLanguage.KANNADA to "ಕೀಲಿನ ಸುತ್ತಲೂ ಕಾವು"
        ),
        "sym_clicking" to mapOf(
            AppLanguage.ENGLISH to "Clicking/Grinding",
            AppLanguage.HINDI to "क्लिक/रगड़ की आवाज",
            AppLanguage.TAMIL to "கிளிக் சத்தம்",
            AppLanguage.KANNADA to "ಕ್ಲಿಕ್ಕಿಸುವ ಶಬ್ದ"
        ),
        "sym_discomfort" to mapOf(
            AppLanguage.ENGLISH to "Mild Discomfort",
            AppLanguage.HINDI to "हल्की असुविधा",
            AppLanguage.TAMIL to "லேசான அசௌகரியம்",
            AppLanguage.KANNADA to "ಸಣ್ಣ ಅಸ್ವಸ್ಥತೆ"
        ),

        // Issue 1 Fix: Ayurveda Map Card Content Mappings
        "ayurveda_tridosha_title" to mapOf(
            AppLanguage.ENGLISH to "Tridosha Balance Profiles",
            AppLanguage.HINDI to "त्रिदोष संतुलन प्रोफाइल",
            AppLanguage.TAMIL to "த்ரிதோஷ சமநிலை சுயவிவரங்கள்",
            AppLanguage.KANNADA to "ತ್ರಿದೋಷ ಸಮತೋಲನ ಪ್ರೊಫೈಲ್‌ಗಳು"
        ),
        "ayurveda_botanical_title" to mapOf(
            AppLanguage.ENGLISH to "Botanical Directory & Clinical Indications",
            AppLanguage.HINDI to "वनस्पति निर्देशिका और नैदानिक संकेत",
            AppLanguage.TAMIL to "தாவரவியல் அடைவு & மருத்துவக் குறிப்புகள்",
            AppLanguage.KANNADA to "ಸಸ್ಯಶಾಸ್ತ್ರದ ಡೈರೆಕ್ಟರಿ ಮತ್ತು ಕ್ಲಿನಿಕಲ್ ಸೂಚನೆಗಳು"
        ),
        "ayurveda_usage_label" to mapOf(
            AppLanguage.ENGLISH to "Suggested Usage:",
            AppLanguage.HINDI to "सुझाया गया उपयोग:",
            AppLanguage.TAMIL to "பரிந்துரைக்கப்பட்ட பயன்பாடு:",
            AppLanguage.KANNADA to "ಸೂಚಿಸಿದ ಬಳಕೆ:"
        ),

        // Dosha Cards Content
        "vata_title" to mapOf(AppLanguage.ENGLISH to "Vata 🌀", AppLanguage.HINDI to "वात 🌀", AppLanguage.TAMIL to "வாதம் 🌀", AppLanguage.KANNADA to "ವಾತ 🌀"),
        "vata_element" to mapOf(AppLanguage.ENGLISH to "Air & Ether", AppLanguage.HINDI to "वायु और आकाश", AppLanguage.TAMIL to "காற்று & ஆகாயம்", AppLanguage.KANNADA to "ಗಾಳಿ & ಆಕಾಶ"),
        "vata_note" to mapOf(
            AppLanguage.ENGLISH to "Calmed by Warm Tulsi & Sesame Oil",
            AppLanguage.HINDI to "गर्म तुलसी और तिल के तेल से शांत",
            AppLanguage.TAMIL to "வெதுவெதுப்பான துளசி & எள் எண்ணெயால் தணியும்",
            AppLanguage.KANNADA to "ಬಿಸಿ ತುಳಸಿ & ಎಳ್ಳಿನ ಎಣ್ಣೆಯಿಂದ ಶಮನ"
        ),
        "pitta_title" to mapOf(AppLanguage.ENGLISH to "Pitta 🔥", AppLanguage.HINDI to "पित्त 🔥", AppLanguage.TAMIL to "பித்தம் 🔥", AppLanguage.KANNADA to "ಪಿತ್ತ 🔥"),
        "pitta_element" to mapOf(AppLanguage.ENGLISH to "Fire & Water", AppLanguage.HINDI to "अग्नि और जल", AppLanguage.TAMIL to "தீ & நீர்", AppLanguage.KANNADA to "ಅಗ್ನಿ & ನೀರು"),
        "pitta_note" to mapOf(
            AppLanguage.ENGLISH to "Balanced by Neem & Cooling Ghee",
            AppLanguage.HINDI to "नीम और ठंडे घी से संतुलित",
            AppLanguage.TAMIL to "வேம்பு & குளிர்ச்சியான நெய்யால் சமநிலை",
            AppLanguage.KANNADA to "ಬೇವು & ತಂಪಾದ ತುಪ್ಪದಿಂದ ಸಮತೋಲನ"
        ),
        "kapha_title" to mapOf(AppLanguage.ENGLISH to "Kapha 🌊", AppLanguage.HINDI to "कफ 🌊", AppLanguage.TAMIL to "கபம் 🌊", AppLanguage.KANNADA to "ಕಫ 🌊"),
        "kapha_element" to mapOf(AppLanguage.ENGLISH to "Earth & Water", AppLanguage.HINDI to "पृथ्वी और जल", AppLanguage.TAMIL to "நிலம் & நீர்", AppLanguage.KANNADA to "ಭೂಮಿ & ನೀರು"),
        "kapha_note" to mapOf(
            AppLanguage.ENGLISH to "Stimulated by Ginger & Black Pepper",
            AppLanguage.HINDI to "अदरक और काली मिर्च से उत्तेजित",
            AppLanguage.TAMIL to "இஞ்சி & மிளகால் தூண்டப்படும்",
            AppLanguage.KANNADA to "ಶುಂಠಿ & ಮೆಣಸಿನಿಂದ ಉತ್ತೇಜನ"
        ),

        // Herb Cards Content
        "herb_tulsi_name" to mapOf(
            AppLanguage.ENGLISH to "Tulsi (Holy Basil) 🍃",
            AppLanguage.HINDI to "तुलसी (पवित्र तुलसी) 🍃",
            AppLanguage.TAMIL to "துளசி (புனித துளசி) 🍃",
            AppLanguage.KANNADA to "ತುಳಸಿ (ಪವಿತ್ರ ತುಳಸಿ) 🍃"
        ),
        "herb_tulsi_benefits" to mapOf(
            AppLanguage.ENGLISH to "Immunomodulatory, anti-inflammatory, respiratory disinfectant. Indicated for cough, mild chest congestion, and elevated stress response.",
            AppLanguage.HINDI to "प्रतिरक्षा बढ़ाने वाला, सूजनरोधी, श्वसन कीटाणुनाशक। खांसी और सीने में जकड़न के लिए उपयोगी।",
            AppLanguage.TAMIL to "நோய் எதிர்ப்புச் சக்தி, வீக்கத்தைக் குறைக்கும், சுவாசத் தொற்று நீக்கி. இருமல், நெஞ்சுச் சளி மற்றும் மன அழுத்தத்திற்கு உகந்தது.",
            AppLanguage.KANNADA to "ರೋಗನಿರೋಧಕ ಶಕ್ತಿ ಹೆಚ್ಚಿಸುವ, ಉರಿಯೂತ ಶಮನಗೊಳಿಸುವ, ಉಸಿರಾಟದ ಸೋಂಕು ನಿವಾರಕ. ಕೆಮ್ಮು ಮತ್ತು ಎದೆ ನಿಕಟತೆಗೆ ಸೂಕ್ತ."
        ),
        "herb_tulsi_dose" to mapOf(
            AppLanguage.ENGLISH to "500mg extract twice daily with warm water.",
            AppLanguage.HINDI to "500mg अर्क दिन में दो बार गुनगुने पानी के साथ।",
            AppLanguage.TAMIL to "500mg சாறு தினமும் இருவேளை வெதுவெதுப்பான நீரில்.",
            AppLanguage.KANNADA to "500mg ಸಾರವನ್ನು ದಿನಕ್ಕೆ ಎರಡು ಬಾರಿ ಉಗುರುಬೆಚ್ಚಗಿನ ನೀರಿನೊಂದಿಗೆ."
        ),

        "herb_ashwa_name" to mapOf(
            AppLanguage.ENGLISH to "Ashwagandha (Indian Ginseng) 🌿",
            AppLanguage.HINDI to "अश्वगंधा (भारतीय जिनसेंग) 🌿",
            AppLanguage.TAMIL to "அஸ்வகந்தா (அமுக்கரா) 🌿",
            AppLanguage.KANNADA to "ಅಶ್ವಗಂಧ (ಭಾರತೀಯ ಜಿನ್‌ಸೆಂಗ್) 🌿"
        ),
        "herb_ashwa_benefits" to mapOf(
            AppLanguage.ENGLISH to "Adaptogen, nerve tonic, lipid balancer. Helps modulate cortisol levels and restore vital energy during metabolic stress.",
            AppLanguage.HINDI to "एडैप्टोजेन, तंत्रिका टॉनिक, लिपिड संतुलनकर्ता। तनाव को कम करने और ऊर्जा बहाल करने में मदद करता है।",
            AppLanguage.TAMIL to "நரம்பு தளர்ச்சி நீக்கி, ஆற்றல் பெருக்கி, கொழுப்பு சமநிலைப்படுத்தி. மன அழுத்தம் குறைத்து ஆற்றலை மீட்டெடுக்கிறது.",
            AppLanguage.KANNADA to "ನರಗಳ ಟಾನಿಕ್, ಶಕ್ತಿ ವರ್ಧಕ, ಲಿಪಿಡ್ ಸಮತೋಲನಕಾರ. ಒತ್ತಡವನ್ನು ಕಡಿಮೆ ಮಾಡಿ ಪ್ರಮುಖ ಶಕ್ತಿಯನ್ನು ಮರುಸ್ಥಾಪಿಸುತ್ತದೆ."
        ),
        "herb_ashwa_dose" to mapOf(
            AppLanguage.ENGLISH to "1-2 capsules (300mg) at bedtime with warm milk.",
            AppLanguage.HINDI to "1-2 कैप्सूल (300mg) सोते समय गर्म दूध के साथ।",
            AppLanguage.TAMIL to "1-2 காப்ஸ்யூல்கள் (300mg) இரவில் வெதுவெதுப்பான பாலுடன்.",
            AppLanguage.KANNADA to "1-2 ಕ್ಯಾಪ್ಸುಲ್‌ಗಳು (300mg) ರಾತ್ರಿ ಮಲಗುವಾಗ ಬಿಸಿ ಹಾಲಿನೊಂದಿಗೆ."
        ),

        "herb_turmeric_name" to mapOf(
            AppLanguage.ENGLISH to "Turmeric (Curcumin) 💛",
            AppLanguage.HINDI to "हल्दी (करक्यूमिन) 💛",
            AppLanguage.TAMIL to "மஞ்சள் (குர்குமின்) 💛",
            AppLanguage.KANNADA to "ಅರಿಶಿನ (ಕುರ್ಕುಮಿನ್) 💛"
        ),
        "herb_turmeric_benefits" to mapOf(
            AppLanguage.ENGLISH to "Potent antioxidant, hepatoprotective, lipid peroxide reducer. Clinical synergy with piperine for cellular regeneration.",
            AppLanguage.HINDI to "शक्तिशाली एंटीऑक्सीडेंट, लिवर रक्षक, कोशिका पुनर्जनन में सहायक।",
            AppLanguage.TAMIL to "சக்திவாய்ந்த ஆக்ஸிஜனேற்ற தடுப்பி, கல்லீரல் பாதுகாப்பு, செல் மீளுருவாக்கத்திற்கு உகந்தது.",
            AppLanguage.KANNADA to "ಪ್ರಬಲವಾದ ಉತ್ಕರ್ಷಣ ನಿರೋಧಕ, ಯಕೃತ್ತಿನ ರಕ್ಷಕ, ಜೀವಕೋಶದ ಪುನರುಜ್ಜೀವನಕ್ಕೆ ಸಹಕಾರಿ."
        ),
        "herb_turmeric_dose" to mapOf(
            AppLanguage.ENGLISH to "500mg standardized curcumin with black pepper twice daily.",
            AppLanguage.HINDI to "500mg मानकीकृत करक्यूमिन काली मिर्च के साथ दिन में दो बार।",
            AppLanguage.TAMIL to "500mg மஞ்சள் தூள் மிளகுடன் தினமும் இருவேளை.",
            AppLanguage.KANNADA to "500mg ಅರಿಶಿನವನ್ನು ಮೆಣಸಿನೊಂದಿಗೆ ದಿನಕ್ಕೆ ಎರಡು ಬಾರಿ."
        ),

        "herb_triphala_name" to mapOf(
            AppLanguage.ENGLISH to "Triphala (Three Fruits) 🍇",
            AppLanguage.HINDI to "त्रिफला (तीन फल) 🍇",
            AppLanguage.TAMIL to "திரிபலா (மூன்று கனிகள்) 🍇",
            AppLanguage.KANNADA to "ತ್ರಿಫಲ (ಮೂರು ಹಣ್ಣುಗಳು) 🍇"
        ),
        "herb_triphala_benefits" to mapOf(
            AppLanguage.ENGLISH to "Gastrointestinal cleanser, antioxidant, glycemic response normalizer. Promotes healthy intestinal microbiome balance.",
            AppLanguage.HINDI to "पाचन तंत्र की सफाई, एंटीऑक्सीडेंट, आंतों के स्वास्थ्य को बढ़ावा देता है।",
            AppLanguage.TAMIL to "செரிமானப் பாதை சுத்திகரிப்பு, ஆக்ஸிஜனேற்ற தடுப்பி, குடல் ஆரோக்கியத்தை மேம்படுத்துகிறது.",
            AppLanguage.KANNADA to "ಜೀರ್ಣಾಂಗ ವ್ಯವಸ್ಥೆಯ ಶುಚಿಗೊಳಿಸುವಿಕೆ, ಕರುಳಿನ ಆರೋಗ್ಯವನ್ನು ಉತ್ತೇಜಿಸುತ್ತದೆ."
        ),
        "herb_triphala_dose" to mapOf(
            AppLanguage.ENGLISH to "1 teaspoon powder with warm water before sleep.",
            AppLanguage.HINDI to "1 चम्मच पाउडर सोने से पहले गुनगुने पानी के साथ।",
            AppLanguage.TAMIL to "1 தேக்கரண்டி பொடி தூங்கும் முன் வெதுவெதுப்பான நீரில்.",
            AppLanguage.KANNADA to "1 ಟೀಸ್ಪೂನ್ ಪುಡಿಯನ್ನು ಮಲಗುವ ಮುನ್ನ ಉಗುರುಬೆಚ್ಚಗಿನ ನೀರಿನೊಂದಿಗೆ."
        ),

        // Issue 2 Fix: Display-Layer Clinical Predictions & Diagnosis Mappings
        "diag_healthy_profile" to mapOf(
            AppLanguage.ENGLISH to "Healthy Biomarker Profile",
            AppLanguage.HINDI to "स्वस्थ बायोमार्कर प्रोफाइल",
            AppLanguage.TAMIL to "ஆரோக்கியமான பயோமார்க்கர் சுயவிவரம்",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯಕರ ಬಯೋಮಾರ್ಕರ್ ಪ್ರೊಫೈಲ್"
        ),
        "diag_diabetic_tendency" to mapOf(
            AppLanguage.ENGLISH to "Diabetic Tendency & Mild Anemia",
            AppLanguage.HINDI to "मधुमेह की प्रवृत्ति और हल्का एनीमिया",
            AppLanguage.TAMIL to "நீரிழிவு போக்கு & லேசான ரத்த சோகை",
            AppLanguage.KANNADA to "ಮಧುಮೇಹ ಪ್ರವೃತ್ತಿ ಮತ್ತು ಸೌಮ್ಯ ರಕ್ತಹೀನತೆ"
        ),
        "diag_cardio_risk" to mapOf(
            AppLanguage.ENGLISH to "Elevated Lipid & Cardiovascular Risk",
            AppLanguage.HINDI to "उच्च लिपिड और हृदय जोखिम",
            AppLanguage.TAMIL to "அதிக கொழுப்பு & இதய ஆபத்து",
            AppLanguage.KANNADA to "ಹೆಚ್ಚಿನ ಲಿಪಿಡ್ ಮತ್ತು ಹೃದಯದ ಅಪಾಯ"
        ),
        "diag_pneumonia" to mapOf(
            AppLanguage.ENGLISH to "Pneumonia (Lung Infection)",
            AppLanguage.HINDI to "न्यूमोनिया (फेफड़ों का संक्रमण)",
            AppLanguage.TAMIL to "நியூமோனியா (நுரையீரல் தொற்று)",
            AppLanguage.KANNADA to "ನ್ಯುಮೋನಿಯಾ (ಶ್ವಾಸಕೋಶದ ಸೋಂಕು)"
        ),
        "diag_tuberculosis" to mapOf(
            AppLanguage.ENGLISH to "Tuberculosis (TB)",
            AppLanguage.HINDI to "टीबी (तपेदिक)",
            AppLanguage.TAMIL to "காசநோய் (TB)",
            AppLanguage.KANNADA to "ಕ್ಷಯರೋಗ (TB)"
        ),
        "diag_covid" to mapOf(
            AppLanguage.ENGLISH to "COVID-19 Infection",
            AppLanguage.HINDI to "कोविड-19 संक्रमण",
            AppLanguage.TAMIL to "கோவிட்-19 தொற்று",
            AppLanguage.KANNADA to "ಕೋವಿಡ್-19 ಸೋಂಕು"
        ),
        "diag_fracture" to mapOf(
            AppLanguage.ENGLISH to "Bone Fracture",
            AppLanguage.HINDI to "हड्डी का फ्रैक्चर",
            AppLanguage.TAMIL to "எலும்பு முறிவு",
            AppLanguage.KANNADA to "ಮೂಳೆ ಮುರಿತ"
        ),
        "diag_arthritis" to mapOf(
            AppLanguage.ENGLISH to "Mild Arthritis",
            AppLanguage.HINDI to "हल्का गठिया",
            AppLanguage.TAMIL to "லேசான மூட்டுவலி",
            AppLanguage.KANNADA to "ಸೌಮ್ಯ ಕೀಲುನೋವು"
        ),
        "diag_skin_infection" to mapOf(
            AppLanguage.ENGLISH to "Skin Infection",
            AppLanguage.HINDI to "त्वचा संक्रमण",
            AppLanguage.TAMIL to "தோல் தொற்று",
            AppLanguage.KANNADA to "ಚರ್ಮದ ಸೋಂಕು"
        ),
        "diag_psoriasis" to mapOf(
            AppLanguage.ENGLISH to "Psoriasis / Eczema Rash",
            AppLanguage.HINDI to "सोरायसिस / एक्जिमा चकत्ते",
            AppLanguage.TAMIL to "சொரியாசிஸ் / எக்சிமா சொறி",
            AppLanguage.KANNADA to "ಸೊರಿಯಾಸಿಸ್ / ಎಕ್ಸಿಮಾ ದದ್ದು"
        ),

        // Problem 1 Symptoms Analysis Section
        "symptoms_analysis_title" to mapOf(
            AppLanguage.ENGLISH to "Symptoms Analysis",
            AppLanguage.HINDI to "लक्षण विश्लेषण",
            AppLanguage.TAMIL to "அறிகுறிகள் பகுப்பாய்வு",
            AppLanguage.KANNADA to "ರೋಗಲಕ್ಷಣಗಳ ವಿಶ್ಲೇಷಣೆ"
        ),
        "symptoms_analysis_desc" to mapOf(
            AppLanguage.ENGLISH to "Correlate visual image features with reported patient symptoms",
            AppLanguage.HINDI to "रिपोर्ट किए गए लक्षणों के साथ दृश्य छवि का मिलान करें",
            AppLanguage.TAMIL to "காட்சிப் பட அம்சங்களை நோயாளி அறிக்கையிட்ட அறிகுறிகளுடன் தொடர்புபடுத்துங்கள்",
            AppLanguage.KANNADA to "ವರದಿಯಾದ ರೋಗಲಕ್ಷಣಗಳೊಂದಿಗೆ ದೃಶ್ಯ ಚಿತ್ರದ ವೈಶಿಷ್ಟ್ಯಗಳನ್ನು ಸಂಯೋಜಿಸಿ"
        ),
        "symptoms_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Type symptoms (e.g. cough, fever, pain)",
            AppLanguage.HINDI to "लक्षण टाइप करें (जैसे खांसी, बुखार, दर्द)",
            AppLanguage.TAMIL to "அறிகுறிகளைத் தட்டச்சு செய்யவும் (எ.கா. இருமல், காய்ச்சல்)",
            AppLanguage.KANNADA to "ರೋಗಲಕ್ಷಣಗಳನ್ನು ಟೈಪ್ ಮಾಡಿ (ಉದಾ. ಕೆಮ್ಮು, ಜ್ವರ)"
        ),
        "symptoms_speak" to mapOf(
            AppLanguage.ENGLISH to "🎙️ Speak",
            AppLanguage.HINDI to "🎙️ बोलें",
            AppLanguage.TAMIL to "🎙️ பேசுங்கள்",
            AppLanguage.KANNADA to "🎙️ ಮಾತನಾಡಿ"
        ),
        "symptoms_analyze_btn" to mapOf(
            AppLanguage.ENGLISH to "🔍 Analyze Symptoms →",
            AppLanguage.HINDI to "🔍 लक्षणों का विश्लेषण करें →",
            AppLanguage.TAMIL to "🔍 அறிகுறிகளை ஆய்வு செய் →",
            AppLanguage.KANNADA to "🔍 ರೋಗಲಕ್ಷಣಗಳನ್ನು ವಿಶ್ಲೇಷಿಸಿ →"
        ),
        "symptoms_add_btn" to mapOf(
            AppLanguage.ENGLISH to "➕ Add Symptoms",
            AppLanguage.HINDI to "➕ लक्षण जोड़ें",
            AppLanguage.TAMIL to "➕ அறிகுறிகளைச் சேர்",
            AppLanguage.KANNADA to "➕ ರೋಗಲಕ್ಷಣಗಳನ್ನು ಸೇರಿಸಿ"
        ),
        "header_lung_records" to mapOf(
            AppLanguage.ENGLISH to "🫁 LUNG DIAGNOSTIC RECORDS",
            AppLanguage.HINDI to "🫁 फेफड़ों के निदान रिकॉर्ड",
            AppLanguage.TAMIL to "🫁 நுரையீரல் நோயறிதல் பதிவுகள்",
            AppLanguage.KANNADA to "🫁 ಶ್ವಾಸಕೋಶದ ರೋಗನಿರ್ಣಯ ದಾಖಲೆಗಳು"
        ),
        "header_skin_records" to mapOf(
            AppLanguage.ENGLISH to "🧴 SKIN & DERMATOLOGY RECORDS",
            AppLanguage.HINDI to "🧴 त्वचा और त्वचा विज्ञान रिकॉर्ड",
            AppLanguage.TAMIL to "🧴 தோல் & தோல் மருத்துவ பதிவுகள்",
            AppLanguage.KANNADA to "🧴 ಚರ್ಮ ಮತ್ತು ಚರ್ಮರೋಗ ದಾಖಲೆಗಳು"
        ),
        "header_bone_records" to mapOf(
            AppLanguage.ENGLISH to "🦴 BONE & JOINT RECORDS",
            AppLanguage.HINDI to "🦴 हड्डी और जोड़ रिकॉर्ड",
            AppLanguage.TAMIL to "🦴 எலும்பு & மூட்டு பதிவுகள்",
            AppLanguage.KANNADA to "🦴 ಮೂಳೆ ಮತ್ತು ಕೀಲು ದಾಖಲೆಗಳು"
        ),

        // Problem 2 History Cards Static UI Labels
        "lbl_patient" to mapOf(AppLanguage.ENGLISH to "Patient:", AppLanguage.HINDI to "रोगी:", AppLanguage.TAMIL to "நோயாளி:", AppLanguage.KANNADA to "ರೋಗಿ:"),
        "lbl_category" to mapOf(AppLanguage.ENGLISH to "Category:", AppLanguage.HINDI to "श्रेणी:", AppLanguage.TAMIL to "வகை:", AppLanguage.KANNADA to "ವರ್ಗ:"),
        "lbl_timestamp" to mapOf(AppLanguage.ENGLISH to "Timestamp:", AppLanguage.HINDI to "समय:", AppLanguage.TAMIL to "நேர முத்திரை:", AppLanguage.KANNADA to "ಸಮಯದ ಮುದ್ರೆ:"),
        "lbl_prediction" to mapOf(AppLanguage.ENGLISH to "DIAGNOSIS / PREDICTION", AppLanguage.HINDI to "निदान / भविष्यवाणी", AppLanguage.TAMIL to "நோயறிதல் / கணிப்பு", AppLanguage.KANNADA to "ರೋಗನಿರ್ಣಯ / ಮುನ್ಸೂಚನೆ"),
        "lbl_description" to mapOf(AppLanguage.ENGLISH to "CLINICAL DESCRIPTION", AppLanguage.HINDI to "नैदानिक विवरण", AppLanguage.TAMIL to "மருத்துவ விளக்கம்", AppLanguage.KANNADA to "ಕ್ಲಿನಿಕಲ್ ವಿವರಣೆ"),
        "lbl_remedies" to mapOf(AppLanguage.ENGLISH to "RECOMMENDED REMEDIES & SUGGESTIONS", AppLanguage.HINDI to "अनुशंसित उपचार और सुझाव", AppLanguage.TAMIL to "பரிந்துரைக்கப்பட்ட வைத்தியம் & ஆலோசனைகள்", AppLanguage.KANNADA to "ಶಿಫಾರಸು ಮಾಡಿದ ಉಪಚಾರಗಳು ಮತ್ತು ಸಲಹೆಗಳು"),

        // Severity Labels
        "severe_mild" to mapOf(AppLanguage.ENGLISH to "Mild", AppLanguage.HINDI to "हल्का", AppLanguage.TAMIL to "மிதமான", AppLanguage.KANNADA to "ಸೌಮ್ಯ"),
        "severe_moderate" to mapOf(AppLanguage.ENGLISH to "Moderate", AppLanguage.HINDI to "मध्यम", AppLanguage.TAMIL to "நடுத்தர", AppLanguage.KANNADA to "ಮಧ್ಯಮ"),
        "severe_critical" to mapOf(AppLanguage.ENGLISH to "Critical", AppLanguage.HINDI to "गंभीर", AppLanguage.TAMIL to "கடுமையான", AppLanguage.KANNADA to "ತೀವ್ರ"),
        "statusNormal" to mapOf(AppLanguage.ENGLISH to "Healthy", AppLanguage.HINDI to "सामान्य", AppLanguage.TAMIL to "சாதாரணமானது", AppLanguage.KANNADA to "ಆರೋಗ್ಯಕರ"),

        // Scanner Screen
        "scanner_title" to mapOf(
            AppLanguage.ENGLISH to "AI Visual Diagnostic Scanner",
            AppLanguage.HINDI to "एआई विजुअल डायग्नोस्टिक स्कैनर",
            AppLanguage.TAMIL to "ஏஐ விஷுவல் நோயறிதல் ஸ்கேனர்",
            AppLanguage.KANNADA to "ಎಐ ದೃಶ್ಯ ರೋಗನಿರ್ணಯ ಸ್ಕ್ಯಾನರ್"
        ),
        "scanner_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Upload or capture medical images for intelligent clinical analysis",
            AppLanguage.HINDI to "चिकित्सकीय विश्लेषण के लिए मेडिकल इमेज अपलोड करें या फोटो लें",
            AppLanguage.TAMIL to "புத்திசாலித்தனமான மருத்துவப் பகுப்பாய்விற்கு மருத்துவப் படங்களைப் பதிவேற்றவும் அல்லது படம் எடுக்கவும்",
            AppLanguage.KANNADA to "ಬುದ್ಧಿವಂತ ಕ್ಲಿನಿಕಲ್ ವಿಶ್ಲೇಷಣೆಗಾಗಿ ವೈದ್ಯಕೀಯ ಚಿತ್ರಗಳನ್ನು ಅಪ್‌ಲೋಡ್ ಮಾಡಿ ಅಥವಾ ತೆಗೆಯಿರಿ"
        ),
        "scanner_upload_xray" to mapOf(
            AppLanguage.ENGLISH to "Upload X-Ray / Clinical Photo",
            AppLanguage.HINDI to "एक्स-रे / क्लिनिकल फोटो अपलोड करें",
            AppLanguage.TAMIL to "எக்ஸ்-ரே / மருத்துவப் படத்தைப் பதிவேற்றவும்",
            AppLanguage.KANNADA to "ಎಕ್ಸ್-ರೇ / ಕ್ಲಿನಿಕಲ್ ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ"
        ),
        "scanner_open_camera" to mapOf(
            AppLanguage.ENGLISH to "Open Camera",
            AppLanguage.HINDI to "कैमरा खोलें",
            AppLanguage.TAMIL to "கேமராவைத் திறக்கவும்",
            AppLanguage.KANNADA to "ಕ್ಯಾಮೆರಾ ತೆರೆಯಿರಿ"
        ),
        "scanner_select_body_part" to mapOf(
            AppLanguage.ENGLISH to "Select Body Part / Category",
            AppLanguage.HINDI to "शरीर का अंग / श्रेणी चुनें",
            AppLanguage.TAMIL to "உடல் பகுதி / வகையைத் தேர்ந்தெடுக்கவும்",
            AppLanguage.KANNADA to "ದೇಹದ ಭಾಗ / ವರ್ಗವನ್ನು ಆಯ್ಕೆಮಾಡಿ"
        ),
        "scanner_analyze_btn" to mapOf(
            AppLanguage.ENGLISH to "Initiate AI Diagnosis Scan →",
            AppLanguage.HINDI to "एआई डायग्नोसिस स्कैन शुरू करें →",
            AppLanguage.TAMIL to "ஏஐ நோயறிதல் ஸ்கேன் தொடங்கவும் →",
            AppLanguage.KANNADA to "ಎಐ ರೋಗನಿರ್ணಯ ಸ್ಕ್ಯಾನ್ ಪ್ರಾರಂಭಿಸಿ →"
        ),
        "scanner_prediction" to mapOf(
            AppLanguage.ENGLISH to "Predicted Clinical Diagnosis",
            AppLanguage.HINDI to "अनुमानित चिकित्सीय निदान",
            AppLanguage.TAMIL to "கணிக்கப்பட்ட மருத்துவ நோயறிதல்",
            AppLanguage.KANNADA to "ಅಂದಾಜು ಕ್ಲಿನಿಕಲ್ ರೋಗನಿರ್ணಯ"
        ),
        "scanner_confidence" to mapOf(
            AppLanguage.ENGLISH to "AI Confidence Score",
            AppLanguage.HINDI to "एआई विश्वास स्कोर",
            AppLanguage.TAMIL to "ஏஐ நம்பிக்கை மதிப்பெண்",
            AppLanguage.KANNADA to "ಎಐ ವಿಶ್ವಾಸಾರ್ಹತೆ ಅಂಕ"
        ),
        "scanner_severity" to mapOf(
            AppLanguage.ENGLISH to "Clinical Severity",
            AppLanguage.HINDI to "चिकित्सकीय गंभीरता",
            AppLanguage.TAMIL to "மருத்துவத் தீவிரம்",
            AppLanguage.KANNADA to "ಕ್ಲಿನಿಕಲ್ ತೀವ್ರತೆ"
        ),
        "scanner_description" to mapOf(
            AppLanguage.ENGLISH to "Diagnostic Findings & Clinical Summary",
            AppLanguage.HINDI to "निदान निष्कर्ष और नैदानिक सारांश",
            AppLanguage.TAMIL to "நோயறிதல் கண்டுபிடிப்புகள் & மருத்துவச் சுருக்கம்",
            AppLanguage.KANNADA to "ರೋಗನಿರ್ಣಯದ ಸಂಶೋಧನೆಗಳು ಮತ್ತು ಕ್ಲಿನಿಕಲ್ ಸಾರಾಂಶ"
        ),
        "scanner_remedies" to mapOf(
            AppLanguage.ENGLISH to "Natural & Home Remedies",
            AppLanguage.HINDI to "प्राकृतिक और घरेलू उपचार",
            AppLanguage.TAMIL to "இயற்கை மற்றும் வீட்டு வைத்தியம்",
            AppLanguage.KANNADA to "ನೈಸರ್ಗಿಕ ಮತ್ತು ಗೃಹ ಉಪಚಾರಗಳು"
        ),
        "scanner_ayurveda" to mapOf(
            AppLanguage.ENGLISH to "Ayurvedic Wellness Guidance",
            AppLanguage.HINDI to "आयुर्वेदिक कल्याण मार्गदर्शन",
            AppLanguage.TAMIL to "ஆயுர்வேத ஆரோக்கிய வழிகாட்டுதல்",
            AppLanguage.KANNADA to "ಆಯುರ್ವೇದ ಆರೋಗ್ಯ ಮಾರ್ಗದರ್ಶನ"
        ),
        "scanner_specialist" to mapOf(
            AppLanguage.ENGLISH to "Recommended Specialist Consultant",
            AppLanguage.HINDI to "अनुशंसित विशेषज्ञ चिकित्सक",
            AppLanguage.TAMIL to "பரிந்துரைக்கப்பட்ட சிறப்பு மருத்துவர்",
            AppLanguage.KANNADA to "ಶಿಫಾರಸು ಮಾಡಿದ ತಜ್ಞ ವೈದ್ಯರು"
        ),
        "scanner_read_aloud" to mapOf(
            AppLanguage.ENGLISH to "🔊 Read Aloud Summary",
            AppLanguage.HINDI to "🔊 सारांश पढ़कर सुनाएं",
            AppLanguage.TAMIL to "🔊 சுருக்கத்தைச் உரக்கப் படிக்கவும்",
            AppLanguage.KANNADA to "🔊 ಸಾರಾಂಶವನ್ನು ಗಟ್ಟಿಯಾಗಿ ಓದಿ"
        ),
        "scanner_book_appointment" to mapOf(
            AppLanguage.ENGLISH to "📅 Book Specialist Appointment",
            AppLanguage.HINDI to "📅 विशेषज्ञ नियुक्ति बुक करें",
            AppLanguage.TAMIL to "📅 சிறப்பு மருத்துவ சந்திப்பை முன்பதிவு செய்யவும",
            AppLanguage.KANNADA to "📅 ತಜ್ಞ ವೈದ್ಯರ ಭೇಟಿ ಕಾಯ್ದಿರಿಸಿ"
        ),

        // Report Analyzer Screen
        "report_title" to mapOf(
            AppLanguage.ENGLISH to "Clinical Report Analyzer",
            AppLanguage.HINDI to "क्लीनिकल रिपोर्ट विश्लेषक",
            AppLanguage.TAMIL to "மருத்துவ அறிக்கை பகுப்பாய்வாளர்",
            AppLanguage.KANNADA to "ಕ್ಲಿನಿಕಲ್ ವರದಿ ವಿಶ್ಲೇಷಕ"
        ),
        "report_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Parse blood biomarkers and lab parameters instantly",
            AppLanguage.HINDI to "रक्त बायोमार्कर और लैब मापदंडों का तुरंत विश्लेषण करें",
            AppLanguage.TAMIL to "இரத்த பயோமார்க்ஸ் மற்றும் ஆய்வக அளவுருக்களை உடனடியாகப் பகுப்பாய்வு செய்யுங்கள்",
            AppLanguage.KANNADA to "ರಕ್ತದ ಬಯೋಮಾರ್ಕರ್‌ಗಳು ಮತ್ತು ಲ್ಯಾಬ್ ನಿಯಮಾವಳಿಗಳನ್ನು ತಕ್ಷಣ ವಿಶ್ಲೇಷಿಸಿ"
        ),
        "report_upload_doc" to mapOf(
            AppLanguage.ENGLISH to "Upload Lab Report Document / Photo",
            AppLanguage.HINDI to "लैब रिपोर्ट दस्तावेज़ / फोटो अपलोड करें",
            AppLanguage.TAMIL to "ஆய்வக அறிக்கை ஆவணம் / புகைப்படத்தைப் பதிவேற்றவும்",
            AppLanguage.KANNADA to "ಲ್ಯಾಬ್ ವರದಿ ದಾಖಲೆ / ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ"
        ),
        "report_biomarkers" to mapOf(
            AppLanguage.ENGLISH to "Blood Biomarker Values",
            AppLanguage.HINDI to "रक्त बायोमार्कर मान",
            AppLanguage.TAMIL to "இரத்த பயோமார்க்கர் மதிப்புகள்",
            AppLanguage.KANNADA to "ರಕ್ತದ ಬಯೋಮಾರ್ಕರ್ ಮೌಲ್ಯಗಳು"
        ),
        "report_risk_profile" to mapOf(
            AppLanguage.ENGLISH to "Health Risk Profile",
            AppLanguage.HINDI to "स्वास्थ्य जोखिम प्रोफ़ाइल",
            AppLanguage.TAMIL to "சுகாதார ஆபத்து சுயவிவரம்",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ ಅಪಾಯದ ಪ್ರೊಫೈಲ್"
        ),
        "report_simplifier" to mapOf(
            AppLanguage.ENGLISH to "Patient Report Simplifier",
            AppLanguage.HINDI to "रोगी रिपोर्ट सरलीकरण",
            AppLanguage.TAMIL to "நோயாளி அறிக்கை எளிமையாக்கம்",
            AppLanguage.KANNADA to "ರೋಗಿಯ ವರದಿ ಸರಳೀಕರಣ"
        ),

        // History Screen
        "history_title" to mapOf(
            AppLanguage.ENGLISH to "Clinical History",
            AppLanguage.HINDI to "चिकित्सकीय इतिहास",
            AppLanguage.TAMIL to "மருத்துவ வரலாறு",
            AppLanguage.KANNADA to "ಕ್ಲಿನಿಕಲ್ ಇತಿಹಾಸ"
        ),
        "history_subtitle" to mapOf(
            AppLanguage.ENGLISH to "View and manage your saved diagnostic scans and report analyses",
            AppLanguage.HINDI to "अपने सहेजे गए स्कैन और रिपोर्ट विश्लेषण देखें और प्रबंधित करें",
            AppLanguage.TAMIL to "உங்கள் சேமிக்கப்பட்ட ஸ்கேன்கள் மற்றும் அறிக்கை பகுப்பாய்வுகளைப் பார்த்து நிர்வகிக்கவும்",
            AppLanguage.KANNADA to "ನಿಮ್ಮ ಉಳಿಸಿದ ಸ್ಕ್ಯಾನ್‌ಗಳು ಮತ್ತು ವರದಿ ವಿಶ್ಲೇಷಣೆಗಳನ್ನು ವೀಕ್ಷಿಸಿ ಮತ್ತು ನಿರ್ವಹಿಸಿ"
        ),
        "history_filter_all" to mapOf(
            AppLanguage.ENGLISH to "All Records",
            AppLanguage.HINDI to "सभी रिकॉर्ड",
            AppLanguage.TAMIL to "அனைத்து பதிவுகள்",
            AppLanguage.KANNADA to "ಎಲ್ಲಾ ದಾಖಲೆಗಳು"
        ),
        "history_filter_image" to mapOf(
            AppLanguage.ENGLISH to "Image Scans 📷",
            AppLanguage.HINDI to "इमेज स्कैन 📷",
            AppLanguage.TAMIL to "பட ஸ்கேன்கள் 📷",
            AppLanguage.KANNADA to "ಚಿತ್ರ ಸ್ಕ್ಯಾನ್‌ಗಳು 📷"
        ),
        "history_filter_report" to mapOf(
            AppLanguage.ENGLISH to "Lab Reports 📄",
            AppLanguage.HINDI to "लैब रिपोर्ट 📄",
            AppLanguage.TAMIL to "ஆய்வக அறிக்கைகள் 📄",
            AppLanguage.KANNADA to "ಲ್ಯಾಬ್ ವರದಿಗಳು 📄"
        ),
        "history_no_records" to mapOf(
            AppLanguage.ENGLISH to "No clinical history records found for this account.",
            AppLanguage.HINDI to "इस खाते के लिए कोई चिकित्सीय इतिहास नहीं मिला।",
            AppLanguage.TAMIL to "இந்தக் கணக்கிற்கு மருத்துவ வரலாறு எதுவும் இல்லை.",
            AppLanguage.KANNADA to "ಈ ಖಾತೆಗೆ ಯಾವುದೇ ಕ್ಲಿನಿಕಲ್ ಇತಿಹಾಸದ ದಾಖಲೆಗಳು ಕಂಡುಬಂದಿಲ್ಲ."
        ),
        "history_delete_title" to mapOf(
            AppLanguage.ENGLISH to "Delete Clinical Record?",
            AppLanguage.HINDI to "क्या क्लिनिकल रिकॉर्ड हटाएं?",
            AppLanguage.TAMIL to "மருத்துவப் பதிவை நீக்கவா?",
            AppLanguage.KANNADA to "ಕ್ಲಿನಿಕಲ್ ದಾಖಲೆಯನ್ನು ಅಳಿಸಬೇಕೆ?"
        ),
        "history_delete_confirm" to mapOf(
            AppLanguage.ENGLISH to "Are you sure you want to delete this clinical record from your history?",
            AppLanguage.HINDI to "क्या आप निश्चित रूप से इस क्लिनिकल रिकॉर्ड को अपने इतिहास से हटाना चाहते हैं?",
            AppLanguage.TAMIL to "உங்கள் வரலாற்றிலிருந்து இந்த மருத்துவப் பதிவை நிச்சயமாக நீக்க விரும்புகிறீர்களா?",
            AppLanguage.KANNADA to "ನಿಮ್ಮ ಇತಿಹಾಸದಿಂದ ಈ ಕ್ಲಿನಿಕಲ್ ದಾಖಲೆಯನ್ನು ಅಳಿಸಲು ನೀವು ಖಚಿತವಾಗಿ ಬಯಸುವಿರಾ?"
        ),

        // Emergency SOS Screen
        "sos_title" to mapOf(
            AppLanguage.ENGLISH to "Medical Emergency SOS",
            AppLanguage.HINDI to "मेडिकल इमरजेंसी एसओएस",
            AppLanguage.TAMIL to "மருத்துவ அவசர SOS",
            AppLanguage.KANNADA to "ವೈದ್ಯಕೀಯ ತುರ್ತು SOS"
        ),
        "sos_banner_title" to mapOf(
            AppLanguage.ENGLISH to "🚨 Medical Emergency SOS",
            AppLanguage.HINDI to "🚨 मेडिकल इमरजेंसी एसओएस",
            AppLanguage.TAMIL to "🚨 மருத்துவ அவசர SOS",
            AppLanguage.KANNADA to "🚨 ವೈದ್ಯಕೀಯ ತುರ್ತು SOS"
        ),
        "sos_banner_desc" to mapOf(
            AppLanguage.ENGLISH to "If you or someone around you is experiencing life-threatening symptoms, chest pain, or severe trauma, initiate immediate emergency dispatch.",
            AppLanguage.HINDI to "यदि आप या आपके आसपास कोई जीवन के लिए खतरनाक लक्षणों का सामना कर रहा है, तो तुरंत आपातकालीन सहायता लें।",
            AppLanguage.TAMIL to "நீங்களோ அல்லது உங்களைச் சுற்றியுள்ள யாராவது உயிருக்கு ஆபத்தான அறிகுறிகளை எதிர்கொண்டால், உடனடியாக அவசர உதவியை நாடுங்கள்.",
            AppLanguage.KANNADA to "ನೀವು ಅಥವಾ ನಿಮ್ಮ ಸುತ್ತಮುತ್ತಲಿನ ಯಾರಾದರೂ ಜೀವಕ್ಕೆ ಅಪಾಯಕಾರಿ ರೋಗಲಕ್ಷಣಗಳನ್ನು ಅನುಭವಿಸುತ್ತಿದ್ದರೆ, ತಕ್ಷಣ ತುರ್ತು ನೆರವು ಪಡೆಯಿರಿ."
        ),
        "sos_call_108" to mapOf(
            AppLanguage.ENGLISH to "CALL NATIONAL EMERGENCY 108 NOW",
            AppLanguage.HINDI to "राष्ट्रीय आपातकालीन 108 पर अभी कॉल करें",
            AppLanguage.TAMIL to "தேசிய அவசரநிலை 108 ஐ இப்போது அழைக்கவும்",
            AppLanguage.KANNADA to "ರಾಷ್ಟ್ರೀಯ ತುರ್ತು 108 ಕ್ಕೆ ಈಗಲೇ ಕರೆ ಮಾಡಿ"
        ),
        "sos_contact_title" to mapOf(
            AppLanguage.ENGLISH to "Emergency Contacts",
            AppLanguage.HINDI to "आपातकालीन संपर्क",
            AppLanguage.TAMIL to "அவசர தொடர்புகள்",
            AppLanguage.KANNADA to "ತುರ್ತು ಸಂಪರ್ಕಗಳು"
        ),
        "sos_contact_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Add trusted contacts who can be notified during an emergency.",
            AppLanguage.HINDI to "आपात स्थिति में संपर्क करने योग्य विश्वसनीय व्यक्तियों को जोड़ें।",
            AppLanguage.TAMIL to "அவசர காலத்தில் தொடர்பு கொள்ளக்கூடிய நம்பகமான நபர்களைச் சேர்க்கவும்.",
            AppLanguage.KANNADA to "ತುರ್ತು ಪರಿಸ್ಥಿತಿಯಲ್ಲಿ ಸಂಪರ್ಕಿಸಬಹುದಾದ ವಿಶ್ವಾಸಾರ್ಹ ವ್ಯಕ್ತಿಗಳನ್ನು ಸೇರಿಸಿ."
        ),
        "sos_contact_name" to mapOf(
            AppLanguage.ENGLISH to "Contact Name",
            AppLanguage.HINDI to "संपर्क का नाम",
            AppLanguage.TAMIL to "தொடர்பு பெயர்",
            AppLanguage.KANNADA to "ಸಂಪರ್ಕ ಹೆಸರು"
        ),
        "sos_contact_phone" to mapOf(
            AppLanguage.ENGLISH to "Phone Number",
            AppLanguage.HINDI to "फोन नंबर",
            AppLanguage.TAMIL to "தொலைபேசி எண்",
            AppLanguage.KANNADA to "ಫೋನ್ ಸಂಖ್ಯೆ"
        ),
        "sos_contact_relationship" to mapOf(
            AppLanguage.ENGLISH to "Relationship",
            AppLanguage.HINDI to "रिश्ता",
            AppLanguage.TAMIL to "உறவுமுறை",
            AppLanguage.KANNADA to "ಸಂಬಂಧ"
        ),
        "sos_add_contact_btn" to mapOf(
            AppLanguage.ENGLISH to "+ Add Emergency Contact",
            AppLanguage.HINDI to "+ आपातकालीन संपर्क जोड़ें",
            AppLanguage.TAMIL to "+ அவசர தொடர்பைச் சேர்",
            AppLanguage.KANNADA to "+ ತುರ್ತು ಸಂಪರ್ಕವನ್ನು ಸೇರಿಸಿ"
        ),
        "sos_no_contacts" to mapOf(
            AppLanguage.ENGLISH to "No emergency contacts added yet.",
            AppLanguage.HINDI to "अभी तक कोई आपातकालीन संपर्क नहीं जोड़ा गया है।",
            AppLanguage.TAMIL to "இன்னும் அவசர தொடர்புகள் எதுவும் சேர்க்கப்படவில்லை.",
            AppLanguage.KANNADA to "ಇನ್ನೂ ಯಾವುದೇ ತುರ್ತು ಸಂಪರ್ಕಗಳನ್ನು ಸೇರಿಸಲಾಗಿಲ್ಲ."
        ),
        "sos_delete_contact_title" to mapOf(
            AppLanguage.ENGLISH to "Delete Emergency Contact?",
            AppLanguage.HINDI to "आपातकालीन संपर्क हटाएं?",
            AppLanguage.TAMIL to "அவசர தொடர்பை நீக்கவா?",
            AppLanguage.KANNADA to "ತುರ್ತು ಸಂಪರ್ಕವನ್ನು ಅಳಿಸಬೇಕೆ?"
        ),

        // Relationships List
        "rel_father" to mapOf(AppLanguage.ENGLISH to "Father", AppLanguage.HINDI to "पिता", AppLanguage.TAMIL to "தந்தை", AppLanguage.KANNADA to "ತಂದೆ"),
        "rel_mother" to mapOf(AppLanguage.ENGLISH to "Mother", AppLanguage.HINDI to "माता", AppLanguage.TAMIL to "தாய்", AppLanguage.KANNADA to "ತಾಯಿ"),
        "rel_son" to mapOf(AppLanguage.ENGLISH to "Son", AppLanguage.HINDI to "पुत्र", AppLanguage.TAMIL to "மகன்", AppLanguage.KANNADA to "ಮಗ"),
        "rel_daughter" to mapOf(AppLanguage.ENGLISH to "Daughter", AppLanguage.HINDI to "पुत्री", AppLanguage.TAMIL to "மகள்", AppLanguage.KANNADA to "ಮಗಳು"),
        "rel_brother" to mapOf(AppLanguage.ENGLISH to "Brother", AppLanguage.HINDI to "भाई", AppLanguage.TAMIL to "சகோதரன்", AppLanguage.KANNADA to "ಸಹೋದರ"),
        "rel_sister" to mapOf(AppLanguage.ENGLISH to "Sister", AppLanguage.HINDI to "बहन", AppLanguage.TAMIL to "சகோதரி", AppLanguage.KANNADA to "ಸಹೋದರಿ"),
        "rel_husband" to mapOf(AppLanguage.ENGLISH to "Husband", AppLanguage.HINDI to "पति", AppLanguage.TAMIL to "கணவர்", AppLanguage.KANNADA to "ಗಂಡ"),
        "rel_wife" to mapOf(AppLanguage.ENGLISH to "Wife", AppLanguage.HINDI to "पत्नी", AppLanguage.TAMIL to "மனைவி", AppLanguage.KANNADA to "ಹೆಂಡತಿ"),
        "rel_friend" to mapOf(AppLanguage.ENGLISH to "Friend", AppLanguage.HINDI to "मित्र", AppLanguage.TAMIL to "நண்பர்", AppLanguage.KANNADA to "ಸ್ನೇಹಿತ"),
        "rel_guardian" to mapOf(AppLanguage.ENGLISH to "Guardian", AppLanguage.HINDI to "अभिभावक", AppLanguage.TAMIL to "பாதுகாவலர்", AppLanguage.KANNADA to "ಪೋಷக"),
        "rel_other" to mapOf(AppLanguage.ENGLISH to "Other", AppLanguage.HINDI to "अन्य", AppLanguage.TAMIL to "மற்றவை", AppLanguage.KANNADA to "இතර"),

        // Profile Screen
        "profile_language_title" to mapOf(
            AppLanguage.ENGLISH to "Preferred Language",
            AppLanguage.HINDI to "पसंदीदा भाषा",
            AppLanguage.TAMIL to "விருப்பமான மொழி",
            AppLanguage.KANNADA to "ಆದ್ಯತೆಯ ಭಾಷೆ"
        ),
        "profile_language_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Select active language for all screens and AI Assistant",
            AppLanguage.HINDI to "सभी स्क्रीन और एआई सहायक के लिए सक्रिय भाषा चुनें",
            AppLanguage.TAMIL to "அனைத்து திரைகள் மற்றும் ஏஐ உதவியாளருக்கு செயலில் உள்ள மொழியைத் தேர்ந்தெடுக்கவும்",
            AppLanguage.KANNADA to "ಎಲ್ಲಾ ಪರದೆಗಳು ಮತ್ತು ಎಐ ಸಹಾಯಕಕ್ಕಾಗಿ ಸಕ್ರಿಯ ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ"
        ),
        "profile_patient_title" to mapOf(
            AppLanguage.ENGLISH to "Patient Medical Profile",
            AppLanguage.HINDI to "रोगी चिकित्सा प्रोफ़ाइल",
            AppLanguage.TAMIL to "நோயாளி மருத்துவ சுயவிவரம்",
            AppLanguage.KANNADA to "ರೋಗಿಯ ವೈದ್ಯಕೀಯ ಪ್ರೊಫೈಲ್"
        ),
        "profile_save_btn" to mapOf(
            AppLanguage.ENGLISH to "Save Patient Profile →",
            AppLanguage.HINDI to "रोगी प्रोफ़ाइल सहेजें →",
            AppLanguage.TAMIL to "நோயாளி சுயவிவரத்தை சேமி →",
            AppLanguage.KANNADA to "ರೋಗಿಯ ಪ್ರೊಫೈಲ್ ಉಳಿಸಿ →"
        ),
        "profile_security_title" to mapOf(
            AppLanguage.ENGLISH to "Security & Change Password",
            AppLanguage.HINDI to "सुरक्षा और पासवर्ड बदलें",
            AppLanguage.TAMIL to "பாதுகாப்பு & கடவுச்சொல் மாற்றம்",
            AppLanguage.KANNADA to "ಭದ್ರತೆ மற்றும் பாಸ್‌வர்ட் ಬದಲಾಯಿಸಿ"
        ),
        "profile_update_password_btn" to mapOf(
            AppLanguage.ENGLISH to "Update Security Password →",
            AppLanguage.HINDI to "सुरक्षा पासवर्ड अपडेट करें →",
            AppLanguage.TAMIL to "கடவுச்சொல்லைப் புதுப்பிக்கவும் →",
            AppLanguage.KANNADA to "ಪಾಸ್‌ವರ್ಡ್ ನವೀಕರಿಸಿ →"
        ),

        // Chatbot Screen
        "chatbot_title" to mapOf(
            AppLanguage.ENGLISH to "AI Health Assistant",
            AppLanguage.HINDI to "एआई स्वास्थ्य सहायक",
            AppLanguage.TAMIL to "ஏஐ சுகாதார உதவியாளர்",
            AppLanguage.KANNADA to "ಎಐ ಆರೋಗ್ಯ ಸಹಾಯಕ"
        ),
        "chatbot_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Interactive clinical & Ayurvedic health guidance",
            AppLanguage.HINDI to "इंटरएक्टिव क्लिनिकल और आयुर्वेदिक स्वास्थ्य मार्गदर्शन",
            AppLanguage.TAMIL to "ஊடாடும் மருத்துவ & ஆயுர்வேத சுகாதார வழிகாட்டுதல்",
            AppLanguage.KANNADA to "ಸಂವಾದಾತ್ಮಕ ಕ್ಲಿನಿಕಲ್ ಮತ್ತು ಆಯುರ್ವೇದ ಆರೋಗ್ಯ ಮಾರ್ಗದರ್ಶನ"
        ),
        "chatbot_welcome_msg" to mapOf(
            AppLanguage.ENGLISH to "Namaste! I am your HealLens AI Health Assistant. Ask me about any symptoms (headache, cough, fever, stomach pain, fatigue), home remedies, or lab report values. How can I help you today?",
            AppLanguage.HINDI to "नमस्ते! मैं आपका हीललेंस एआई स्वास्थ्य सहायक हूँ। मुझसे किसी भी लक्षण (सिरदर्द, खांसी, बुखार, पेट दर्द, थकान), घरेलू उपचार या लैब रिपोर्ट के बारे में पूछें। आज मैं आपकी क्या मदद कर सकता हूँ?",
            AppLanguage.TAMIL to "வணக்கம்! நான் உங்கள் ஹீல்ாலென்ஸ் ஏஐ சுகாதார உதவியாளர். ஏதேனும் அறிகுறிகள் (தலைவலி, இருமல், காய்ச்சல், வயிற்று வலி, சோர்வு), வீட்டு வைத்தியம் அல்லது ஆய்வக அறிக்கைகள் பற்றி என்னிடம் கேளுங்கள். இன்று உங்களுக்கு நான் எவ்வாறு உதவ முடியும்?",
            AppLanguage.KANNADA to "ನಮಸ್ಕಾರ! ನಾನು ನಿಮ್ಮ ಹೀಲ್‌ಲೆನ್ಸ್ ಎಐ ಆರೋಗ್ಯ ಸಹಾಯಕ. ರೋಗಲಕ್ಷಣಗಳು (ತಲೆನೋವು, ಕೆಮ್ಮು, ಜ್ವರ, ಹೊಟ್ಟೆ ನೋವು, ಆಯಾಸ), ಗೃಹ ಉಪಚಾರಗಳು ಅಥವಾ ಲ್ಯಾಬ್ ವರದಿ ಮೌಲ್ಯಗಳ ಬಗ್ಗೆ ನನ್ನನ್ನು ಕೇಳಿ. ಇಂದು ನಾನು ನಿಮಗೆ ಹೇಗೆ ಸಹಾಯ ಮಾಡಲಿ?"
        ),
        "chatbot_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Describe symptoms or health questions...",
            AppLanguage.HINDI to "लक्षण या स्वास्थ्य प्रश्न लिखें...",
            AppLanguage.TAMIL to "அறிகுறிகள் அல்லது சுகாதார கேள்விகளை விவரிக்கவும்...",
            AppLanguage.KANNADA to "ರೋಗಲಕ್ಷಣಗಳು ಅಥವಾ ಆರೋಗ್ಯ ಪ್ರಶ್ನೆಗಳನ್ನು ವಿವರಿಸಿ..."
        ),
        "chatbot_quick_pneumonia" to mapOf(
            AppLanguage.ENGLISH to "Pneumonia 🫁",
            AppLanguage.HINDI to "न्यूमोनिया 🫁",
            AppLanguage.TAMIL to "நியூமோனியா 🫁",
            AppLanguage.KANNADA to "ನ್ಯುಮೋನಿಯಾ 🫁"
        ),
        "chatbot_quick_cough" to mapOf(
            AppLanguage.ENGLISH to "Cough Remedies 🌿",
            AppLanguage.HINDI to "खांसी के उपाय 🌿",
            AppLanguage.TAMIL to "இருமல் வைத்தியம் 🌿",
            AppLanguage.KANNADA to "ಕೆಮ್ಮಿನ ಉಪಚಾರ 🌿"
        ),
        "chatbot_quick_glucose" to mapOf(
            AppLanguage.ENGLISH to "Lower Glucose 🩸",
            AppLanguage.HINDI to "शुगर कम करें 🩸",
            AppLanguage.TAMIL to "சர்க்கரையைக் குறைக்கவும் 🩸",
            AppLanguage.KANNADA to "ಗ್ಲುಕೋಸ್ ಕಡಿಮೆ ಮಾಡಿ 🩸"
        ),

        // Patient Gender / Relation Options
        "opt_self" to mapOf(AppLanguage.ENGLISH to "Self", AppLanguage.HINDI to "स्वयं", AppLanguage.TAMIL to "சுய", AppLanguage.KANNADA to "ಸ್ವಂತ"),
        "opt_father" to mapOf(AppLanguage.ENGLISH to "Father", AppLanguage.HINDI to "पिता", AppLanguage.TAMIL to "தந்தை", AppLanguage.KANNADA to "ತಂದೆ"),
        "opt_mother" to mapOf(AppLanguage.ENGLISH to "Mother", AppLanguage.HINDI to "माता", AppLanguage.TAMIL to "தாய்", AppLanguage.KANNADA to "ತಾಯಿ"),
        "opt_spouse" to mapOf(AppLanguage.ENGLISH to "Spouse", AppLanguage.HINDI to "जीवनसाथी", AppLanguage.TAMIL to "துணைவர்", AppLanguage.KANNADA to "ಪತಿ/ಪತ್ನಿ"),
        "opt_child" to mapOf(AppLanguage.ENGLISH to "Child", AppLanguage.HINDI to "बच्चा", AppLanguage.TAMIL to "குழந்தை", AppLanguage.KANNADA to "ಮಗು")
    )

    fun get(key: String, language: AppLanguage = LanguageManager.currentLanguage): String {
        return strings[key]?.get(language)
            ?: strings[key]?.get(AppLanguage.ENGLISH)
            ?: key
    }
}
