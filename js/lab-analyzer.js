// lab-analyzer.js - Core logic for HealLens Lab Report Analyzer

class LabAnalyzer {
  constructor() {
    this.patientName   = 'Jane Doe';
    this.patientAge    = 30;
    this.patientGender = 'female'; // 'male' | 'female'

    this.currentValues = {
      glucose: 90,
      cholesterol: 165,
      triglycerides: 110,
      hemoglobin: 14.5,
      creatinine: 0.8,
      ast: 24,
      tsh: 2.1
    };

    // Base biomarker reference ranges
    this.biomarkerBase = {
      glucose:       { min: 70,   max: 100,  unit: 'mg/dL',   step: 1,   minLimit: 40,   maxLimit: 300  },
      cholesterol:   { min: 120,  max: 200,  unit: 'mg/dL',   step: 5,   minLimit: 80,   maxLimit: 400  },
      triglycerides: { min: 50,   max: 150,  unit: 'mg/dL',   step: 5,   minLimit: 30,   maxLimit: 500  },
      hemoglobin:    { min: 12.0, max: 17.0, unit: 'g/dL',    step: 0.1, minLimit: 5.0,  maxLimit: 22.0 },
      creatinine:    { min: 0.6,  max: 1.2,  unit: 'mg/dL',   step: 0.1, minLimit: 0.2,  maxLimit: 5.0  },
      ast:           { min: 10,   max: 40,   unit: 'U/L',     step: 1,   minLimit: 5,    maxLimit: 200  },
      tsh:           { min: 0.4,  max: 4.5,  unit: 'uIU/mL',  step: 0.1, minLimit: 0.05, maxLimit: 15.0 }
    };

    // Preset data for simulation templates
    this.samples = {
      healthy: {
        glucose: 85,
        cholesterol: 170,
        triglycerides: 115,
        hemoglobin: 14.8,
        creatinine: 0.8,
        ast: 22,
        tsh: 1.8
      },
      lipid: {
        glucose: 95,
        cholesterol: 255,
        triglycerides: 220,
        hemoglobin: 13.5,
        creatinine: 0.9,
        ast: 35,
        tsh: 2.2
      },
      diabetic: {
        glucose: 195,
        cholesterol: 185,
        triglycerides: 165,
        hemoglobin: 9.5,
        creatinine: 1.1,
        ast: 45,
        tsh: 5.2
      }
    };

    this.localizedDb = {
      en: {
        name_glucose: "Blood Glucose (Sugar)",
        desc_glucose: "Fasting Blood Glucose measures sugar levels in the blood. High sugar indicates diabetes or prediabetes risk, while low sugar causes weakness and dizziness.",
        ayur_glucose: "Nisha Amalaki (Turmeric + Amla) and Fenugreek (Methi) seeds daily to balance sugar and support insulin production.",
        
        name_cholesterol: "Total Cholesterol",
        desc_cholesterol: "Total Cholesterol is a key fat marker. Elevated levels can lead to fat buildup in arteries, increasing cardiovascular and blood pressure risks.",
        ayur_cholesterol: "Lashuna (Garlic) extract, Arjuna bark powder, and Guggulu to clear fat blockages and support heart muscle health.",
        
        name_triglycerides: "Triglycerides",
        desc_triglycerides: "Triglycerides are types of fat stored in your cells. High levels are linked to arterial hardening and pancreas strain.",
        ayur_triglycerides: "Triphala powder taken with warm water at night, along with ginger tea to accelerate fat digestion and metabolic clearance.",
        
        name_hemoglobin: "Hemoglobin (Hb)",
        desc_hemoglobin: "Hemoglobin carries oxygen in red blood cells. Low levels indicate Anemia, leading to chronic fatigue, weakness, and pale skin.",
        ayur_hemoglobin: "Dhatri Lauha, Lohasava (Ayurvedic iron tonic), and regular intake of pomegranate (Dadima) juice to boost blood count.",
        
        name_creatinine: "Serum Creatinine",
        desc_creatinine: "Creatinine is a muscle waste product filtered by kidneys. High levels suggest renal strain or reduced kidney filtration capacity.",
        ayur_creatinine: "Punarnava (Hogweed) decoction and Varuna tea as natural diuretics to flush waste and optimize glomerular filtration.",
        
        name_ast: "AST (Liver Enzyme)",
        desc_ast: "AST is a liver enzyme released into the blood during liver stress. Elevated levels indicate hepatocyte strain or fatty liver changes.",
        ayur_ast: "Bhumi Amla juice, Giloy stem extract, and Katuki root to protect liver cells, detoxify tissue, and regularize bile flow.",
        
        name_tsh: "TSH (Thyroid Stimulating Hormone)",
        desc_tsh: "TSH controls thyroid hormone production. High TSH means your thyroid is underactive (Hypothyroidism), slowing down metabolism.",
        ayur_tsh: "Kanchnar Guggulu, coriander seed water (Dhania water), and regular neck massage with warm sesame oil to balance thyroid gland.",

        // Risk profiles
        risk_normal: "✅ All Biomarkers within Healthy Ranges. Maintain a balanced diet, stay hydrated, and continue regular exercise.",
        risk_diab_anemia: "⚠️ Risk Profile: Diabetic Tendency & Mild Anemia. High glucose requires sugar control, while low hemoglobin suggests iron deficiency anemia.",
        risk_lipid: "⚠️ Risk Profile: Hyperlipidemia & Cardiovascular Stress. High total cholesterol and triglycerides increase the risk of arterial plaque buildup.",
        risk_liver_kidney: "⚠️ Risk Profile: Hepatorenal Stress. Slightly high liver enzymes and creatinine indicate systemic detoxification strain. Rest, hydrate, and avoid processed foods.",
        risk_thyroid: "⚠️ Risk Profile: Underactive Thyroid (Hypothyroidism). Elevated TSH indicates metabolic slowdown. Consult a specialist for thyroid hormone levels.",
        risk_generic_abnormal: "⚠️ Risk Profile: Isolated Biomarker Elevation. One or more values exceed typical clinical ranges. Monitor and review diet.",
        
        remedy_title_lbl: "Ingredient: ",
        remedy_dose_lbl: "Dosage: "
      },
      hi: {
        name_glucose: "ब्लड ग्लूकोज (शुगर)",
        desc_glucose: "फास्टिंग ब्लड ग्लूकोज रक्त में शर्करा के स्तर को मापता है। उच्च शर्करा मधुमेह या प्रीडायबिटीज के जोखिम को दर्शाती है, जबकि कम शर्करा कमजोरी का कारण बनती है।",
        ayur_glucose: "शुगर को संतुलित करने और इंसुलिन उत्पादन में सहायता के लिए रोजाना निशा आमलकी (हल्दी + आंवला) और मेथी दाने का सेवन करें।",
        
        name_cholesterol: "कुल कोलेस्ट्रॉल",
        desc_cholesterol: "कुल कोलेस्ट्रॉल एक प्रमुख वसा मार्कर है। ऊंचा स्तर धमनियों में वसा जमा कर सकता है, जिससे हृदय और रक्तचाप के जोखिम बढ़ जाते हैं।",
        ayur_cholesterol: "वसा रुकावटों को साफ करने और हृदय की मांसपेशियों के स्वास्थ्य का समर्थन करने के लिए लशुन (लहसुन) का अर्क, अर्जुन की छाल का पाउडर और गुग्गुलु।",
        
        name_triglycerides: "ट्राइग्लिसराइड्स",
        desc_triglycerides: "ट्राइग्लिसराइड्स कोशिकाओं में संग्रहीत वसा के प्रकार हैं। उच्च स्तर धमनियों के सख्त होने और अग्न्याशय के खिंचाव से जुड़े हैं।",
        ayur_triglycerides: "वसा पाचन और चयापचय निकासी में तेजी लाने के लिए रात में गर्म पानी के साथ त्रिफला चूर्ण, और अदरक की चाय लें।",
        
        name_hemoglobin: "हीमोग्लोबिन (Hb)",
        desc_hemoglobin: "हीमोग्लोबिन लाल रक्त कोशिकाओं में ऑक्सीजन ले जाता है। कम स्तर एनीमिया का संकेत देता है, जिससे थकान, कमजोरी और पीली त्वचा होती है।",
        ayur_hemoglobin: "रक्त कोशिकाओं को बढ़ावा देने के लिए धात्री लौह, लोहासाव (आयुर्वेदिक आयरन टॉनिक) और अनार (दाड़िम) के रस का नियमित सेवन करें।",
        
        name_creatinine: "सीरम क्रिएटिनिन",
        desc_creatinine: "क्रिएटिनिन गुर्दे द्वारा फ़िल्टर किया गया मांसपेशियों का अपशिष्ट उत्पाद है। उच्च स्तर गुर्दे के तनाव या कम निस्पंदन क्षमता का सुझाव देता है।",
        ayur_creatinine: "अपशिष्ट को बाहर निकालने और गुर्दे की कार्यप्रणाली को अनुकूलित करने के लिए प्राकृतिक मूत्रवर्धक के रूप में पुनर्नवा का काढ़ा और वरुणा की चाय।",
        
        name_ast: "एएसटी (लिवर एंजाइम)",
        desc_ast: "एएसटी एक लिवर एंजाइम है जो लिवर के तनाव के दौरान रक्त में जारी होता है। बढ़ा हुआ स्तर लिवर कोशिकाओं के तनाव या फैटी लिवर के बदलाव को दर्शाता है।",
        ayur_ast: "लिवर कोशिकाओं की रक्षा करने, ऊतकों को डिटॉक्स करने और पित्त प्रवाह को नियमित करने के लिए भूमि आंवला का रस, गिलोय का अर्क और कुटकी जड़।",
        
        name_tsh: "टीएसएच (थायरॉयड उत्तेजक हार्मोन)",
        desc_tsh: "टीएसएच थायराइड हार्मोन उत्पादन को नियंत्रित करता है। उच्च टीएसएच का मतलब है कि आपका थायराइड कम सक्रिय (हाइपोथायरायडिज्म) है, जिससे चयापचय धीमा हो जाता है।",
        ayur_tsh: "थायरॉयड ग्रंथि को संतुलित करने के लिए कांचनार गुग्गुलु, धनिया बीज का पानी और गर्म तिल के तेल से गले की नियमित मालिश करें।",

        // Risk profiles
        risk_normal: "✅ सभी बायोमार्कर स्वस्थ सीमाओं के भीतर हैं। संतुलित आहार बनाए रखें, हाइड्रेटेड रहें और नियमित व्यायाम जारी रखें।",
        risk_diab_anemia: "⚠️ जोखिम प्रोफाइल: मधुमेह की प्रवृत्ति और हल्का एनीमिया। उच्च ग्लूकोज के लिए शुगर नियंत्रण की आवश्यकता होती है, जबकि कम हीमोग्लोबिन आयरन की कमी वाले एनीमिया का सुझाव देता है।",
        risk_lipid: "⚠️ जोखिम प्रोफाइल: हाइपरलिपिडेमिया और कार्डियोवैस्कुलर तनाव। उच्च कुल कोलेस्ट्रॉल और ट्राइग्लिसराइड्स धमनियों में प्लाक के निर्माण के जोखिम को बढ़ाते हैं।",
        risk_liver_kidney: "⚠️ जोखिम प्रोफाइल: हेपेटोरेनल तनाव। थोड़ा बढ़ा हुआ लिवर एंजाइम और क्रिएटिनिन प्रणालीगत विषहरण तनाव का संकेत देते हैं। आराम करें, पानी पिएं और प्रसंस्कृत खाद्य पदार्थों से बचें।",
        risk_thyroid: "⚠️ जोखिम प्रोफाइल: निष्क्रिय थायरॉयड (हाइपोथायरायडिज्म)। बढ़ा हुआ टीएसएच चयापचय मंदी का संकेत देता है। हार्मोन के स्तर के लिए विशेषज्ञ से सलाह लें।",
        risk_generic_abnormal: "⚠️ जोखिम प्रोफाइल: असामान्य बायोमार्कर स्तर। एक या अधिक मान सामान्य नैदानिक सीमाओं से अधिक हैं। आहार की निगरानी और समीक्षा करें।",
        
        remedy_title_lbl: "सामग्री: ",
        remedy_dose_lbl: "खुराक: "
      },
      ta: {
        name_glucose: "இரத்த குளுக்கோஸ் (சர்க்கரை)",
        desc_glucose: "வெறும் வயிற்றில் இரத்த குளுக்கோஸ் அளவு சர்க்கரையின் அளவை அளவிடுகிறது. அதிக சர்க்கரை நீரிழிவு ஆபத்தை குறிக்கிறது, குறைந்த சர்க்கரை பலவீனத்தை ஏற்படுத்துகிறது.",
        ayur_glucose: "சர்க்கரையை சமநிலைப்படுத்தவும் இன்சுலின் உற்பத்தியை அதிகரிக்கவும் தினமும் நிஷா ஆமலகி (மஞ்சள் + நெல்லிக்காய்) மற்றும் வெந்தயம் உட்கொள்ளவும்.",
        
        name_cholesterol: "மொத்த கொலஸ்ட்ரால்",
        desc_cholesterol: "மொத்த கொலஸ்ட்ரால் என்பது ஒரு முக்கிய கொழுப்பு காரணியாகும். அதிக அளவு கொழுப்பு இரத்த நாளங்களில் படிந்து, இதய நோய்களை அதிகரிக்கும்.",
        ayur_cholesterol: "கொழுப்பு அடைப்புகளை நீக்கி, இதய தசைகளின் ஆரோக்கியத்தை ஆதரிக்க லசுனா (பூண்டு) சாறு, அர்ஜுனா பட்டை பொடி மற்றும் குக்குலு.",
        
        name_triglycerides: "ட்ரைகிளிசரைடுகள்",
        desc_triglycerides: "ட்ரைகிளிசரைடுகள் செல்களில் சேமிக்கப்படும் கொழுப்பு வகையாகும். அதிக அளவு தமனிகளின் கடினத்தன்மை மற்றும் கணைய அழுத்தத்துடன் தொடர்புடையது.",
        ayur_triglycerides: "கொழுப்பு செரிமானம் மற்றும் வளர்சிதை மாற்றத்தை விரைவுபடுத்த இரவில் வெதுவெதுப்பான நீருடன் திரிபலா பொடி, மற்றும் இஞ்சி தேநீர் உட்கொள்ளவும்.",
        
        name_hemoglobin: "ஹீமோகுளோபின் (Hb)",
        desc_hemoglobin: "ஹீமோகுளோபின் இரத்த அணுக்களில் ஆக்ஸிஜனைக் கொண்டு செல்கிறது. குறைந்த அளவு இரத்த சோகையை (Anemia) குறிக்கிறது, இது சோர்வு மற்றும் பலவீனத்தை ஏற்படுத்தும்.",
        ayur_hemoglobin: "இரத்த அணுக்களை அதிகரிக்க தாத்ரி லௌஹா, லோஹாசவா (ஆயுர்வேத இரும்பு டானிக்) மற்றும் மாதுளை சாறு தொடர்ந்து பருகவும்.",
        
        name_creatinine: "சீரம் கிரியேட்டினின்",
        desc_creatinine: "கிரியேட்டினின் என்பது சிறுநீரகங்களால் வடிகட்டப்படும் தசை கழிவுப்பொருள். அதிக அளவு சிறுநீரக அழுத்தம் அல்லது வடிகட்டுதல் திறன் குறைவதைக் குறிக்கிறது.",
        ayur_creatinine: "கழிவுகளை வெளியேற்றவும் சிறுநீரக செயல்பாட்டை மேம்படுத்தவும் இயற்கை சிறுநீரிறக்கியாக புனர்னவா கஷாயம் மற்றும் வருணா தேநீர் குடிக்கவும்.",
        
        name_ast: "AST (கல்லீரல் என்சைம்)",
        desc_ast: "AST என்பது கல்லீரல் அழுத்தத்தின் போது இரத்தத்தில் வெளியிடப்படும் ஒரு என்சைம் ஆகும். அதிக அளவு கல்லீரல் செல்கள் அழுத்தத்தை அல்லது கொழுப்பு கல்லீரல் மாற்றங்களைக் குறிக்கிறது.",
        ayur_ast: "கல்லீரல் செல்களைப் பாதுகாக்கவும், பித்த ஓட்டத்தை சீராக்கவும் பூமி நெல்லி சாறு, சீந்தில் தண்டு சாறு மற்றும் கடுகு ரோகிணி வேர்.",
        
        name_tsh: "TSH (தைராய்டு தூண்டுதல் ஹார்மோன்)",
        desc_tsh: "TSH தைராய்டு ஹார்மோன் உற்பத்தியைக் கட்டுப்படுத்துகிறது. அதிக TSH என்பது தைராய்டு சுரப்பி குறைவாக வேலை செய்கிறது (Hypothyroidism) என்பதாகும், இது வளர்சிதை மாற்றத்தை குறைக்கும்.",
        ayur_tsh: "தைராய்டு சுரப்பியை சமநிலைப்படுத்த காஞ்சனார குக்குலு, கொத்தமல்லி விதை தண்ணீர் மற்றும் வெதுவெதுப்பான நல்லெண்ணெய் கொண்டு கழுத்தில் மசாஜ் செய்யவும்.",

        // Risk profiles
        risk_normal: "✅ அனைத்து பயோமார்க்கர்களும் ஆரோக்கியமான வரம்புகளுக்குள் உள்ளன. சமச்சீர் உணவைப் பேணுங்கள், போதுமான அளவு தண்ணீர் குடியுங்கள் மற்றும் உடற்பயிற்சி செய்யுங்கள்.",
        risk_diab_anemia: "⚠️ சுகாதார அபாயம்: நீரிழிவு போக்கு & லேசான இரத்த சோகை. அதிக குளுக்கோஸுக்கு சர்க்கரை கட்டுப்பாடு தேவைப்படுகிறது, குறைந்த ஹீமோகுளோபின் இரும்புச்சத்து குறைபாடு இரத்த சோகையை குறிக்கிறது.",
        risk_lipid: "⚠️ சுகாதார அபாயம்: ஹைப்பர்லிபிடெமியா & இருதய அழுத்தம். அதிக மொத்த கொலஸ்ட்ரால் மற்றும் ட்ரைகிளிசரைடுகள் இரத்த நாள அடைப்பு அபாயத்தை அதிகரிக்கும்.",
        risk_liver_kidney: "⚠️ சுகாதார அபாயம்: கல்லீரல் & சிறுநீரக அழுத்தம். சற்று உயர்ந்த கல்லீரல் என்சைம்கள் மற்றும் கிரியேட்டினின் கழிவு வெளியேற்ற அழுத்தத்தைக் குறிக்கின்றன. ஓய்வெடுக்கவும், தண்ணீர் பருகவும்.",
        risk_thyroid: "⚠️ சுகாதார அபாயம்: தைராய்டு குறைபாடு (Hypothyroidism). உயர்ந்த TSH வளர்சிதை மாற்ற மந்தநிலையைக் குறிக்கிறது. ஹார்மோன் அளவுகளுக்கு மருத்துவரை அணுகவும்.",
        risk_generic_abnormal: "⚠️ சுகாதார அபாயம்: அசாதாரண பயோமார்க்கர் அளவுகள். ஒன்று அல்லது அதற்கு மேற்பட்ட அளவுகள் சாதாரண வரம்பை விட அதிகமாக உள்ளன. உணவை கண்காணித்து மாற்றியமைக்கவும்.",
        
        remedy_title_lbl: "தேவையானவை: ",
        remedy_dose_lbl: "அளவு: "
      },
      kn: {
        name_glucose: "ರಕ್ತದ ಗ್ಲುಕೋಸ್ (ಸಕ್ಕರೆ)",
        desc_glucose: "ಖಾಲಿ ಹೊಟ್ಟೆಯಲ್ಲಿ ರಕ್ತದ ಗ್ಲುಕೋಸ್ ಪ್ರಮಾಣ ಸಕ್ಕರೆಯ ಮಟ್ಟವನ್ನು ಅಳೆಯುತ್ತದೆ. ಹೆಚ್ಚಿನ ಸಕ್ಕರೆ ಮಧುಮೇಹದ ಅಪಾಯವನ್ನು ಸೂಚಿಸುತ್ತದೆ, ಕಡಿಮೆ ಸಕ್ಕರೆ ದೌರ್ಬಲ್ಯವನ್ನು ಉಂಟುಮಾಡುತ್ತದೆ.",
        ayur_glucose: "ಸಕ್ಕರೆಯನ್ನು ಸಮತೋಲನಗೊಳಿಸಲು ಮತ್ತು ಇನ್ಸುಲಿನ್ ಉತ್ಪಾದನೆಯನ್ನು ಬೆಂಬಲಿಸಲು ಪ್ರತಿದಿನ ನಿಶಾ ಆಮಲಕಿ (ಅರಿಶಿನ + ನೆಲ್ಲಿಕಾಯಿ) ಮತ್ತು ಮೆಂತೆ ಬೀಜಗಳನ್ನು ಸೇವಿಸಿ.",
        
        name_cholesterol: "ಒಟ್ಟು ಕೊಲೆಸ್ಟ್ರಾಲ್",
        desc_cholesterol: "ಒಟ್ಟು ಕೊಲೆಸ್ಟ್ರಾಲ್ ಪ್ರಮುಖ ಕೊಬ್ಬಿನ ಸೂಚಕವಾಗಿದೆ. ಹೆಚ್ಚಿನ ಮಟ್ಟವು ಅಪಧಮನಿಗಳಲ್ಲಿ ಕೊಬ್ಬು ಶೇಖರಣೆಗೆ ಕಾರಣವಾಗಿ, ಹೃದಯ ಕಾಯಿಲೆಯ ಅಪಾಯವನ್ನು ಹೆಚ್ಚಿಸುತ್ತದೆ.",
        ayur_cholesterol: "ಕೊಬ್ಬಿನ ಅಡೆತಡೆಗಳನ್ನು ನಿವಾರಿಸಲು ಮತ್ತು ಹೃದಯ ಸ್ನಾಯುಗಳ ಆರೋಗ್ಯವನ್ನು ಬೆಂಬಲಿಸಲು ಲಶುನ (ಬೆಳ್ಳುಳ್ಳಿ) ಸಾರ, ಅರ್ಜುನ ತೊಗಟೆ ಪುಡಿ ಮತ್ತು ಗುಗ್ಗುಲು.",
        
        name_triglycerides: "ಟ್ರೈಗ್ಲಿಸರೈಡ್‌ಗಳು",
        desc_triglycerides: "ಟ್ರೈಗ್ಲಿಸರೈಡ್‌ಗಳು ಜೀವಕೋಶಗಳಲ್ಲಿ ಸಂಗ್ರಹವಾಗಿರುವ ಕೊಬ್ಬಿನ ವಿಧಗಳಾಗಿವೆ. ಹೆಚ್ಚಿನ ಮಟ್ಟವು ಅಪಧಮನಿಗಳ ಗಡಸುತನ ಮತ್ತು ಮೇದೋಜ್ಜೀರಕ ಗ್ರಂಥಿಯ ಒತ್ತಡಕ್ಕೆ ಸಂಬಂಧಿಸಿದೆ.",
        ayur_triglycerides: "ಕೊಬ್ಬಿನ ಜೀರ್ಣಕ್ರಿಯೆ ಮತ್ತು ಚಯಾಪಚಯ ಕ್ರಿಯೆಯನ್ನು ವೇಗಗೊಳಿಸಲು ರಾತ್ರಿ ಬೆಚ್ಚಗಿನ ನೀರಿನೊಂದಿಗೆ ತ್ರಿಫಲಾ ಚೂರ್ಣ ಮತ್ತು ಶುಂಠಿ ಚಹಾವನ್ನು ಸೇವಿಸಿ.",
        
        name_hemoglobin: "ಹಿಮೋಗ್ಲೋಬಿನ್ (Hb)",
        desc_hemoglobin: "ಹಿಮೋಗ್ಲೋಬಿನ್ ರಕ್ತಕಣಗಳಲ್ಲಿ ಆಮ್ಲಜನಕವನ್ನು ಒಯ್ಯುತ್ತದೆ. ಕಡಿಮೆ ಮಟ್ಟವು ರಕ್ತಹೀನತೆಯನ್ನು (Anemia) ಸೂಚಿಸುತ್ತದೆ, ಇದು ದೀರ್ಘಕಾಲದ ಆಯಾಸ ಮತ್ತು ದೌರ್ಬಲ್ಯಕ್ಕೆ ಕಾರಣವಾಗುತ್ತದೆ.",
        ayur_hemoglobin: "ರಕ್ತಕಣಗಳನ್ನು ಹೆಚ್ಚಿಸಲು ಧಾತ್ರಿ ಲೌಹಾ, ಲೋಹಾಸವ (ಆಯುರ್ವೇದ ಐರನ್ ಟಾನಿಕ್) ಮತ್ತು ದಾಳಿಂಬೆ ರಸವನ್ನು ನಿಯಮಿತವಾಗಿ ಸೇವಿಸಿ.",
        
        name_creatinine: "ಸೀರಮ್ ಕ್ರಿಯೇಟಿನೈನ್",
        desc_creatinine: "ಕ್ರಿಯೇಟಿನೈನ್ ಮೂತ್ರಪಿಂಡಗಳಿಂದ ಫಿಲ್ಟರ್ ಮಾಡಲ್ಪಟ್ಟ ಮಾಂಸಖಂಡಗಳ ತ್ಯಾಜ್ಯ ಉತ್ಪನ್ನವಾಗಿದೆ. ಹೆಚ್ಚಿನ ಮಟ್ಟವು ಮೂತ್ರಪಿಂಡದ ಒತ್ತಡ ಅಥವಾ ಶೋಧನೆ ಸಾಮರ್ಥ್ಯ ಇಳಿಕೆಯನ್ನು ಸೂಚಿಸುತ್ತದೆ.",
        ayur_creatinine: "ತ್ಯಾಜ್ಯವನ್ನು ಹೊರಹಾಕಲು ಮತ್ತು ಮೂತ್ರಪಿಂಡದ ಕಾರ್ಯವನ್ನು ಅತ್ಯುತ್ತಮಗೊಳಿಸಲು ನೈಸರ್ಗಿಕ ಮೂತ್ರವರ್ಧಕವಾಗಿ ಪುನರ್ನವಾ ಕಷಾಯ ಮತ್ತು ವರುಣಾ ಚಹಾವನ್ನು ಕುಡಿಯಿರಿ.",
        
        name_ast: "AST (ಯಕೃತ್ತಿನ ಕಿಣ್ವ)",
        desc_ast: "AST ಯಕೃತ್ತಿನ ಒತ್ತಡದ ಸಮಯದಲ್ಲಿ ರಕ್ತದಲ್ಲಿ ಬಿಡುಗಡೆಯಾಗುವ ಕಿಣ್ವವಾಗಿದೆ. ಹೆಚ್ಚಿನ ಮಟ್ಟವು ಯಕೃತ್ತಿನ ಜೀವಕೋಶಗಳ ಒತ್ತಡ ಅಥವಾ ಕೊಬ್ಬಿನ ಯಕೃತ್ತಿನ ಬದಲಾವಣೆಗಳನ್ನು ಸೂಚಿಸುತ್ತದೆ.",
        ayur_ast: "ಯಕೃತ್ತಿನ ಜೀವಕೋಶಗಳನ್ನು ರಕ್ಷಿಸಲು ಮತ್ತು ಪಿತ್ತರಸದ ಹರಿವನ್ನು ನಿಯಂತ್ರಿಸಲು ಭೂಮಿ ನೆಲ್ಲಿಕಾಯಿ ರಸ, ಗಿಲೋಯ್ ಸಾರ ಮತ್ತು ಕಟುಕಿ ಬೇರು.",
        
        name_tsh: "TSH (ಥೈರಾಯ್ಡ್ ಉತ್ತೇಜಿಸುವ ಹಾರ್ಮೋನ್)",
        desc_tsh: "TSH ಥೈರಾಯ್ಡ್ ಹಾರ್ಮೋನ್ ಉತ್ಪಾದನೆಯನ್ನು ನಿಯಂತ್ರಿಸುತ್ತದೆ. ಹೆಚ್ಚಿನ TSH ಎಂದರೆ ನಿಮ್ಮ ಥೈರಾಯ್ಡ್ ಗ್ರಂಥಿ ಕಡಿಮೆ ಸಕ್ರಿಯವಾಗಿದೆ (Hypothyroidism) ಎಂದರ್ಥ, ಇದು ಚಯಾಪಚಯವನ್ನು ನಿಧಾನಗೊಳಿಸುತ್ತದೆ.",
        ayur_tsh: "ಥೈರಾಯ್ಡ್ ಗ್ರಂಥಿಯನ್ನು ಸಮತೋಲನಗೊಳಿಸಲು ಕಾಂಚನಾರ ಗುಗ್ಗುಲು, ಕೊತ್ತಂಬರಿ ಬೀಜದ ನೀರು ಮತ್ತು ಬೆಚ್ಚಗಿನ ಎಳ್ಳೆಣ್ಣೆಯಿಂದ ಕುತ್ತಿಗೆಗೆ ಮಸಾಜ್ ಮಾಡಿ.",

        // Risk profiles
        risk_normal: "✅ ಎಲ್ಲಾ ಬಯೋಮಾರ್ಕರ್‌ಗಳು ಆರೋಗ್ಯಕರ ಮಿತಿಗಳಲ್ಲಿವೆ. ಸಮತೋಲಿತ ಆಹಾರವನ್ನು ಕಾಪಾಡಿಕೊಳ್ಳಿ, ಸಾಕಷ್ಟು ನೀರು ಕುಡಿಯಿರಿ ಮತ್ತು ನಿಯಮಿತವಾಗಿ ವ್ಯಾಯಾಮ ಮಾಡಿ.",
        risk_diab_anemia: "⚠️ ಆರೋಗ್ಯದ ಅಪಾಯ: ಮಧುಮೇಹದ ಪ್ರವೃತ್ತಿ ಮತ್ತು ಸೌಮ್ಯ ರಕ್ತಹೀನತೆ. ಹೆಚ್ಚಿನ ಗ್ಲುಕೋಸ್‌ಗೆ ಸಕ್ಕರೆ ನಿಯಂತ್ರಣದ ಅಗತ್ಯವಿರುತ್ತದೆ, ಕಡಿಮೆ ಹಿಮೋಗ್ಲೋಬಿನ್ ರಕ್ತಹೀನತೆಯನ್ನು ಸೂಚಿಸುತ್ತದೆ.",
        risk_lipid: "⚠️ ಆರೋಗ್ಯದ ಅಪಾಯ: ಹೈಪರ್ಲಿಪಿಡೆಮಿಯಾ ಮತ್ತು ಹೃದಯದ ಒತ್ತಡ. ಹೆಚ್ಚಿನ ಒಟ್ಟು ಕೊಲೆಸ್ಟ್ರಾಲ್ ಮತ್ತು ಟ್ರೈಗ್ಲಿಸರೈಡ್‌ಗಳು ಅಪಧಮನಿಗಳಲ್ಲಿ ಕೊಬ್ಬಿನ ಶೇಖರಣೆಯ ಅಪಾಯವನ್ನು ಹೆಚ್ಚಿಸುತ್ತವೆ.",
        risk_liver_kidney: "⚠️ ಆರೋಗ್ಯದ ಅಪಾಯ: ಯಕೃತ್ತು ಮತ್ತು ಮೂತ್ರಪಿಂಡದ ಒತ್ತಡ. ಸ್ವಲ್ಪ ಹೆಚ್ಚಿನ ಯಕೃತ್ತಿನ ಕಿಣ್ವಗಳು ಮತ್ತು ಕ್ರಿಯೇಟಿನೈನ್ ದೇಹದ ತ್ಯಾಜ್ಯ ವಿಲೇವಾರಿ ಒತ್ತಡವನ್ನು ಸೂಚಿಸುತ್ತವೆ. ವಿಶ್ರಾಂತಿ ಪಡೆಯಿರಿ, ನೀರು ಕುಡಿಯಿರಿ.",
        risk_thyroid: "⚠️ ಆರೋಗ್ಯದ ಅಪಾಯ: ಥೈರಾಯ್ಡ್ ಕೊರತೆ (Hypothyroidism). ಹೆಚ್ಚಿನ TSH ಚಯಾಪಚಯ ಮಂದಗತಿಯನ್ನು ಸೂಚಿಸುತ್ತದೆ. ಹಾರ್ಮೋನ್ ಮಟ್ಟಗಳಿಗಾಗಿ ವೈದ್ಯರನ್ನು ಸಂಪರ್ಕಿಸಿ.",
        risk_generic_abnormal: "⚠️ ಆರೋಗ್ಯದ ಅಪಾಯ: ಅಸಹಜ ಬಯೋಮಾರ್ಕರ್ ಮಟ್ಟಗಳು. ಒಂದು ಅಥವಾ ಹೆಚ್ಚಿನ ಮೌಲ್ಯಗಳು ಸಾಮಾನ್ಯ ಮಿತಿಗಿಂತ ಹೆಚ್ಚಿವೆ. ಆಹಾರಕ್ರಮವನ್ನು ಗಮನಿಸಿ.",
        
        remedy_title_lbl: "ಪದಾರ್ಥಗಳು: ",
        remedy_dose_lbl: "ಪ್ರಮಾಣ: "
      }
    };
  }

  getRanges() {
    return JSON.parse(JSON.stringify(this.biomarkerBase));
  }

  init() {
    this.setupUploadHandlers();
    this.renderInputs();
    this.setupButtons();
    this.analyze();
  }

  resetOrRender() {
    this.renderInputs();
    this.analyze();
    // Hide results panel if reset
    document.getElementById("lab-result-card").style.display = "none";
  }

  getTranslation(key) {
    const lang = window.i18n?.currentLang || "en";
    const db = this.localizedDb[lang] || this.localizedDb["en"];
    return db[key] || key;
  }

  setupUploadHandlers() {
    const dropzone = document.getElementById("lab-upload-area");
    const fileInput = document.getElementById("lab-file-input");
    const browseBtn = document.getElementById("lab-browse-btn");

    if (!dropzone || !fileInput || !browseBtn) return;

    browseBtn.addEventListener("click", () => fileInput.click());

    fileInput.addEventListener("change", (e) => {
      if (e.target.files.length > 0) {
        this.startSimulatedScan();
      }
    });

    dropzone.addEventListener("dragover", (e) => {
      e.preventDefault();
      dropzone.classList.add("drag-over");
    });

    dropzone.addEventListener("dragleave", () => {
      dropzone.classList.remove("drag-over");
    });

    dropzone.addEventListener("drop", (e) => {
      e.preventDefault();
      dropzone.classList.remove("drag-over");
      if (e.dataTransfer.files.length > 0) {
        this.startSimulatedScan();
      }
    });
  }

  startSimulatedScan() {
    const dropzone = document.getElementById("lab-upload-area");
    const scanningArea = document.getElementById("lab-scanning-area");
    const subtext = document.getElementById("lab-analyzing-subtext");

    if (!dropzone || !scanningArea) return;

    dropzone.style.display = "none";
    scanningArea.style.display = "block";

    const steps = [
      "Preprocessing document & adjusting contrast...",
      "Running AI text boundary segmentations...",
      "Extracting biomarker values via neural OCR...",
      "Validating units and ranges with laboratory database...",
      "Finalizing extraction metrics..."
    ];

    let currentStep = 0;
    const interval = setInterval(() => {
      if (subtext) subtext.innerText = steps[currentStep];
      currentStep++;
      if (currentStep >= steps.length) {
        clearInterval(interval);
        
        // Randomly load a template to simulate scanning
        const templates = ["healthy", "lipid", "diabetic"];
        const chosen = templates[Math.floor(Math.random() * templates.length)];
        this.loadSample(chosen);
        
        // Restore upload zone and hide scanner
        scanningArea.style.display = "none";
        dropzone.style.display = "flex";
      }
    }, 450);
  }

  setupButtons() {
    const runBtn = document.getElementById("lab-run-analysis-btn");
    if (runBtn) {
      runBtn.addEventListener("click", () => {
        this.analyze();
        const resultCard = document.getElementById("lab-result-card");
        if (resultCard) {
          resultCard.style.display = "block";
          resultCard.scrollIntoView({ behavior: "smooth" });
        }
      });
    }
  }

  loadSample(type) {
    const sample = this.samples[type];
    if (!sample) return;

    Object.keys(sample).forEach(key => {
      this.currentValues[key] = sample[key];
      const input = document.getElementById(`lab-input-${key}`);
      if (input) input.value = sample[key];
    });

    this.analyze();
    
    // Automatically reveal result card on template selection
    const resultCard = document.getElementById("lab-result-card");
    if (resultCard) {
      resultCard.style.display = "block";
      resultCard.scrollIntoView({ behavior: "smooth" });
    }
  }

  renderInputs() {
    const container = document.getElementById('biomarker-inputs-container');
    if (!container) return;
    const ranges = this.getRanges();
    container.innerHTML = Object.keys(ranges).map(key => {
      const bio   = ranges[key];
      const value = this.currentValues[key];
      const name  = this.getTranslation(`name_${key}`);
      return `
        <div class="form-group" style="margin-bottom:15px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
            <label class="form-label" for="lab-input-${key}" style="font-weight:600;color:var(--text-primary);">${name}</label>
            <span style="font-size:0.8rem;color:var(--text-secondary);">${bio.unit}</span>
          </div>
          <input type="number"
                 class="form-input"
                 id="lab-input-${key}"
                 value="${value}"
                 min="${bio.minLimit}"
                 max="${bio.maxLimit}"
                 step="${bio.step}"
                 oninput="window.labAnalyzer?.updateValue('${key}', this.value)" />
        </div>`;
    }).join('');
  }

  updateValue(key, val) {
    const num = parseFloat(val);
    if (!isNaN(num)) this.currentValues[key] = num;
  }

  updateName(val) {
    this.patientName = val.trim() || 'Jane Doe';
    this.analyze();
  }

  updateAge(val) {
    const n = parseInt(val);
    if (!isNaN(n) && n > 0) this.patientAge = n;
  }

  updateGender(val) {
    this.patientGender = val;
    this.renderInputs();
  }

  reRenderCurrentResult() {
    // Called when user switches language in Topbar / Sidebar
    this.renderInputs();
    this.analyze();
  }

  analyze() {
    const visualizer       = document.getElementById('lab-range-visualizer');
    const riskOutput       = document.getElementById('lab-risk-output');
    const simplifierOutput = document.getElementById('lab-simplifier-output');
    const ayurvedaOutput   = document.getElementById('lab-ayurveda-output');
    if (!visualizer) return;

    const ranges = this.getRanges();
    let abnCount = 0;

    const patientBanner = `
      <div class="patient-banner" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; padding: 12px 18px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); border-radius: var(--radius-md);">
        <div style="display: flex; align-items: center; gap: 10px;">
          <span style="font-size: 1.2rem;">👤</span>
          <div>
            <div style="font-size: 0.72rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px;" data-i18n="patientName">Full Name</div>
            <div style="font-weight: 700; font-size: 1.05rem; color: #fff;">${this.patientName}</div>
          </div>
        </div>
        <div style="text-align: right;">
          <div style="font-size: 0.72rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px;">Analysis Date</div>
          <div style="font-weight: 600; font-size: 0.9rem; color: var(--text-primary);">${new Date().toLocaleDateString()}</div>
        </div>
      </div>
    `;

    const visualizerHtml = Object.keys(ranges).map(key => {
      const bio = ranges[key];
      const val = this.currentValues[key];
      const name = this.getTranslation(`name_${key}`);
      
      // Calculate marker position percentage
      // We map the value to a progress track from minLimit to maxLimit
      const totalRange = bio.maxLimit - bio.minLimit;
      let pct = ((val - bio.minLimit) / totalRange) * 100;
      pct = Math.max(2, Math.min(98, pct)); // bound between 2% and 98% for pin layout

      // Determine Status Label
      let status = "normal";
      let badgeClass = "severity-mild";
      let statusLabel = this.getTranslation("statusNormal");

      if (val < bio.min) {
        status = "low";
        badgeClass = "severity-moderate";
        statusLabel = this.getTranslation("statusLow");
        abnCount++;
      } else if (val > bio.max) {
        status = "high";
        // Check if critically high
        const critLimit = bio.max * 1.5;
        if (val > critLimit) {
          status = "critical";
          badgeClass = "severity-critical";
          statusLabel = this.getTranslation("statusCritical");
        } else {
          status = "high";
          badgeClass = "severity-moderate";
          statusLabel = this.getTranslation("statusHigh");
        }
        abnCount++;
      }

      // Zone widths
      const lowZonePct = ((bio.min - bio.minLimit) / totalRange) * 100;
      const greenZonePct = ((bio.max - bio.min) / totalRange) * 100;
      const highZonePct = 100 - (lowZonePct + greenZonePct);

      return `
        <div class="range-gauge-container" style="margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px dashed rgba(255,255,255,0.05);">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <div style="font-weight: 700; font-family: var(--font-heading); font-size: 0.96rem; color: #fff;">${name}</div>
            <div style="display: flex; align-items: center; gap: 10px;">
              <span style="font-size: 1.1rem; font-weight: 800; color: var(--color-primary);">${val} <span style="font-size: 0.78rem; font-weight: 500; color: var(--text-secondary);">${bio.unit}</span></span>
              <span class="severity-badge ${badgeClass}" style="padding: 2px 10px; font-size: 0.7rem;">${statusLabel}</span>
            </div>
          </div>
          <div class="gauge-track-wrap" style="position: relative; height: 8px; border-radius: 4px; background: rgba(255,255,255,0.1); overflow: visible; display: flex; margin-bottom: 8px;">
            <div class="gauge-zone zone-low" style="width: ${lowZonePct}%; background: rgba(0, 212, 255, 0.2); border-radius: 4px 0 0 4px;"></div>
            <div class="gauge-zone zone-green" style="width: ${greenZonePct}%; background: rgba(16, 185, 129, 0.3);"></div>
            <div class="gauge-zone zone-high" style="width: ${highZonePct}%; background: rgba(239, 68, 68, 0.25); border-radius: 0 4px 4px 0;"></div>
            <div class="gauge-pin" style="position: absolute; top: -4px; left: calc(${pct}% - 8px); width: 16px; height: 16px; border-radius: 50%; background: #fff; border: 3px solid var(--color-primary); box-shadow: 0 0 10px var(--color-primary); transition: left 0.5s cubic-bezier(0.1, 0.8, 0.3, 1);"></div>
          </div>
          <div style="display: flex; justify-content: space-between; font-size: 0.72rem; color: var(--text-muted);">
            <span>${bio.minLimit}</span>
            <span>${this.getTranslation("normalRange")}: ${bio.min} - ${bio.max}</span>
            <span>${bio.maxLimit}</span>
          </div>
        </div>
      `;
    }).join("");

    visualizer.innerHTML = patientBanner + visualizerHtml;

    // 2. Multi-Biomarker Risk Engine Logic
    let riskText = "";
    const gl = this.currentValues.glucose;
    const ch = this.currentValues.cholesterol;
    const tr = this.currentValues.triglycerides;
    const hb = this.currentValues.hemoglobin;
    const cr = this.currentValues.creatinine;
    const ast = this.currentValues.ast;
    const tsh = this.currentValues.tsh;

    if (gl <= 100 && ch <= 200 && tr <= 150 && hb >= 12 && cr <= 1.2 && ast <= 40 && tsh <= 4.5 && tsh >= 0.4) {
      riskText = this.getTranslation("risk_normal");
    } else if (gl > 120 && hb < 11.5) {
      riskText = this.getTranslation("risk_diab_anemia");
    } else if (ch > 200 || tr > 150) {
      riskText = this.getTranslation("risk_lipid");
    } else if (ast > 40 && cr > 1.2) {
      riskText = this.getTranslation("risk_liver_kidney");
    } else if (tsh > 4.5) {
      riskText = this.getTranslation("risk_thyroid");
    } else {
      riskText = this.getTranslation("risk_generic_abnormal");
    }

    if (riskOutput) {
      riskOutput.innerHTML = `<strong>${riskText}</strong>`;
    }

    // 3. Jargon Simplifier Engine
    const simplifiedHtml = Object.keys(ranges).map(key => {
      const bio = ranges[key];
      const val = this.currentValues[key];
      const name = this.getTranslation(`name_${key}`);
      
      let highlight = false;
      if (val < bio.min || val > bio.max) highlight = true;

      const desc = this.getTranslation(`desc_${key}`);

      return `
        <div style="padding: 10px; border-radius: 8px; background: ${highlight ? "rgba(255,158,11,0.03)" : "transparent"}; border-left: 3px solid ${highlight ? "var(--color-warning)" : "rgba(255,255,255,0.05)"};">
          <div style="font-weight: 700; font-size: 0.88rem; color: ${highlight ? "var(--color-warning)" : "var(--text-primary)"}; margin-bottom: 3px;">
            ${name} (${val} ${bio.unit})
          </div>
          <p style="font-size: 0.8rem; color: var(--text-secondary); line-height: 1.4;">${desc}</p>
        </div>
      `;
    }).join("");

    if (simplifierOutput) {
      simplifierOutput.innerHTML = simplifiedHtml;
    }

    // 4. Ayurveda recommendations mapping
    const ayurvedaHtml = Object.keys(ranges).map(key => {
      const bio = ranges[key];
      const val = this.currentValues[key];
      
      // Only suggest Ayurvedic remedies for out-of-range parameters to keep focus
      if (val >= bio.min && val <= bio.max) return "";

      const name = this.getTranslation(`name_${key}`);
      const remedy = this.getTranslation(`ayur_${key}`);

      return `
        <div class="ayurveda-map-card" style="padding: var(--space-md); border-color: rgba(255, 158, 11, 0.25); background: rgba(8, 13, 26, 0.6);">
          <div class="ayurveda-map-name" style="color: var(--color-warning);">🌿 Balancing ${name}</div>
          <div class="ayurveda-map-use" style="font-size: 0.82rem; line-height: 1.4; color: var(--text-secondary);">
            ${remedy}
          </div>
        </div>
      `;
    }).join("");

    if (ayurvedaOutput) {
      const hasSuggestions = ayurvedaHtml.replace(/\s/g, "").length > 0;
      if (hasSuggestions) {
        ayurvedaOutput.style.display = "grid";
        ayurvedaOutput.innerHTML = ayurvedaHtml;
        ayurvedaOutput.parentElement.style.display = "block"; // Show box
      } else {
        ayurvedaOutput.parentElement.style.display = "none"; // Hide box if perfectly healthy
      }
    }
  }
}

// Global initialization
document.addEventListener("DOMContentLoaded", () => {
  window.labAnalyzer = new LabAnalyzer();
  window.labAnalyzer.init();

  // Extend active language changes to refresh lab analyzer too
  const originalSetLanguage = window.i18n?.setLanguage;
  if (originalSetLanguage) {
    window.i18n.setLanguage = function(lang) {
      originalSetLanguage.call(window.i18n, lang);
      window.labAnalyzer?.reRenderCurrentResult();
    };
  }
});
