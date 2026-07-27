// chatbot.js — AI Health Chatbot with multilingual keyword matching
class HealthChatbot {
  constructor() {
    this.context = { lastDiagnosis: null, userName: "" };
    this.knowledgeBase = this.buildKnowledgeBase();
    this.conversationHistory = [];
  }

  buildKnowledgeBase() {
    return [
      {
        patterns: [/hello|hi|hey|namaste|vanakkam|namaskara/i],
        responses: {
          en: ["Hello! I'm HealLens AI, your personal health assistant. How are you feeling today?", "Hi there! Ready to help with your health questions. What's on your mind?"],
          hi: ["नमस्ते! मैं HealLens AI हूं। आज आप कैसा महसूस कर रहे हैं?"],
          ta: ["வணக்கம்! நான் HealLens AI. இன்று நீங்கள் எப்படி உணர்கிறீர்கள்?"],
          kn: ["ನಮಸ್ಕಾರ! ನಾನು HealLens AI. ಇಂದು ನೀವು ಹೇಗೆ ಅನುಭವಿಸುತ್ತಿದ್ದೀರಿ?"]
        }
      },
      {
        patterns: [/cough|coughing|khasi|இருமல்|ಕೆಮ್ಮು/i],
        responses: {
          en: ["Coughing can be a sign of respiratory infection, allergies, or asthma. If it persists more than 7 days or has blood, see a doctor immediately. Try steam inhalation and ginger-honey tea.", "Persistent cough may indicate pneumonia or bronchitis. Keep yourself hydrated and avoid cold drinks. Use turmeric milk at night."],
          hi: ["खांसी श्वसन संक्रमण का संकेत हो सकती है। 7 दिन से अधिक रहे या खून आए तो तुरंत डॉक्टर से मिलें।"],
          ta: ["இருமல் சுவாச தொற்றின் அறிகுறியாக இருக்கலாம். 7 நாட்களுக்கு மேல் நீடித்தால் மருத்துவரை அணுகவும்।"],
          kn: ["ಕೆಮ್ಮು ಉಸಿರಾಟ ತೊಂದರೆಯ ಸಂಕೇತ ಆಗಿರಬಹುದು. 7 ದಿನಕ್ಕಿಂತ ಹೆಚ್ಚಿದ್ದರೆ ವೈದ್ಯರನ್ನು ಭೇಟಿ ಮಾಡಿ."]
        }
      },
      {
        patterns: [/fever|temperature|bukhar|காய்ச்சல்|ಜ್ವರ/i],
        responses: {
          en: ["Fever is the body's natural defense against infection. For adults, temperature above 103°F needs medical attention. Stay hydrated, rest, and take paracetamol if needed. Monitor for 2-3 days.", "A fever above 100.4°F typically indicates an infection. Cool the body with a damp cloth, drink plenty of fluids, and avoid self-medicating antibiotics."],
          hi: ["बुखार शरीर की प्राकृतिक रक्षा है। 103°F से ऊपर हो तो चिकित्सा ध्यान दें। पर्याप्त पानी पिएं।"],
          ta: ["காய்ச்சல் உடலின் இயற்கை பாதுகாப்பு. 103°F க்கு மேல் இருந்தால் மருத்துவ கவனிப்பு தேவை."],
          kn: ["ಜ್ವರ ದೇಹದ ನೈಸರ್ಗಿಕ ರಕ್ಷಣೆ. 103°F ಮೇಲಿದ್ದರೆ ವೈದ್ಯಕೀಯ ಗಮನ ಬೇಕು."]
        }
      },
      {
        patterns: [/headache|head pain|sir dard|தலைவலி|ತಲೆನೋವು/i],
        responses: {
          en: ["Headaches can result from stress, dehydration, poor sleep, or eye strain. Drink water, rest in a dark room, and apply peppermint oil on temples. If it's severe or comes with vision problems, see a doctor immediately."],
          hi: ["सिरदर्द तनाव, निर्जलीकरण या नींद की कमी से हो सकता है। पानी पिएं और आराम करें।"],
          ta: ["தலைவலி மன அழுத்தம் அல்லது நீர் பற்றாக்குறையால் ஏற்படலாம்."],
          kn: ["ತಲೆನೋವು ಒತ್ತಡ ಅಥವಾ ನಿರ್ಜಲೀಕರಣದಿಂದ ಆಗಬಹುದು."]
        }
      },
      {
        patterns: [/diabetes|sugar|madhumeh|சர்க்கரை|ಮಧುಮೇಹ/i],
        responses: {
          en: ["Diabetes requires careful blood sugar management. Eat low-glycemic foods, exercise regularly, and monitor blood sugar daily. Bitter gourd, fenugreek, and cinnamon can help regulate blood sugar naturally. Regular checkups with an endocrinologist are essential."],
          hi: ["मधुमेह के लिए रक्त शर्करा प्रबंधन जरूरी है। करेला, मेथी और दालचीनी रक्त शर्करा नियंत्रित करने में मदद करते हैं।"],
          ta: ["நீரிழிவுக்கு இரத்த சர்க்கரை நிர்வாகம் தேவை. பாகற்காய், வெந்தயம் உதவும்."],
          kn: ["ಮಧುಮೇಹಕ್ಕೆ ರಕ್ತದ ಸಕ್ಕರೆ ನಿರ್ವಹಣೆ ಅಗತ್ಯ. ಹಾಗಲಕಾಯಿ, ಮೆಂತ್ಯ ಸಹಾಯ ಮಾಡುತ್ತದೆ."]
        }
      },
      {
        patterns: [/blood pressure|bp|hypertension|उच्च रक्तचाप|இரத்த அழுத்தம்|ರಕ್ತ ಒತ್ತಡ/i],
        responses: {
          en: ["High blood pressure (above 140/90) can lead to heart attacks and strokes. Reduce salt, exercise 30 mins daily, manage stress through yoga and meditation, and take prescribed medications regularly. Garlic and hibiscus tea may help lower BP naturally."],
          hi: ["उच्च रक्तचाप (140/90 से ऊपर) हृदय रोग का कारण बन सकता है। नमक कम करें, व्यायाम करें।"],
          ta: ["உயர் இரத்த அழுத்தம் இதய நோய்க்கு வழிவகுக்கும். உப்பை குறைக்கவும், உடற்பயிற்சி செய்யவும்."],
          kn: ["ಅಧಿಕ ರಕ್ತ ಒತ್ತಡ ಹೃದಯ ರೋಗಕ್ಕೆ ಕಾರಣವಾಗಬಹುದು. ಉಪ್ಪು ಕಡಿಮೆ ಮಾಡಿ, ವ್ಯಾಯಾಮ ಮಾಡಿ."]
        }
      },
      {
        patterns: [/joint pain|arthritis|joint|knee|moot|jodo|மூட்டு|ಕೀಲು/i],
        responses: {
          en: ["Joint pain can be caused by arthritis, injury, or inflammation. Warm sesame oil massage, turmeric with milk, and fenugreek seeds help reduce pain. Low-impact exercises like swimming and yoga keep joints flexible. See a rheumatologist if pain persists beyond 6 weeks."],
          hi: ["जोड़ों का दर्द गठिया, चोट या सूजन के कारण हो सकता है। तिल के तेल से मालिश करें।"],
          ta: ["மூட்டு வலி கீல்வாத, காயம் அல்லது அழற்சியால் ஏற்படலாம்."],
          kn: ["ಕೀಲು ನೋವು ಸಂಧಿವಾತ, ಗಾಯ ಅಥವಾ ಉರಿಯೂತದಿಂದ ಆಗಬಹುದು."]
        }
      },
      {
        patterns: [/skin rash|itching|rash|allergy|twacha|தோல்|ಚರ್ಮ/i],
        responses: {
          en: ["Skin rashes can result from allergies, infections, heat prickle, or eczema. Apply neem leaf paste or aloe vera gel for relief. Avoid scratching and use mild soap. If rash spreads or has pus, see a dermatologist immediately."],
          hi: ["त्वचा पर चकत्ते एलर्जी, संक्रमण या एक्जिमा के कारण हो सकते हैं। नीम पत्ती का लेप लगाएं।"],
          ta: ["தோல் தடிப்பு ஒவ்வாமை அல்லது தொற்றால் ஏற்படலாம். வேப்பிலை பேஸ்ட் தடவுங்கள்."],
          kn: ["ಚರ್ಮ ಅಲರ್ಜಿ ಅಥವಾ ಸೋಂಕಿನಿಂದ ಆಗಬಹುದು. ಬೇವಿನ ಎಲೆ ಲೇಪ ಹಚ್ಚಿ."]
        }
      },
      {
        patterns: [/covid|corona|sars|pandemic/i],
        responses: {
          en: ["If you suspect COVID-19: isolate yourself, monitor oxygen levels (stay above 95%), drink Kadha (tulsi, ginger, cinnamon decoction), and take paracetamol for fever. Get tested immediately and contact health authorities at 1075."],
          hi: ["COVID-19 का संदेह हो तो खुद को अलग करें, ऑक्सीजन स्तर (95% से ऊपर) मॉनिटर करें। 1075 पर कॉल करें।"],
          ta: ["COVID-19 சந்தேகமிருந்தால் தனிமைப்படுத்திக்கொள்ளுங்கள், 1075ல் தொடர்புகொள்ளுங்கள்."],
          kn: ["COVID-19 ಅನುಮಾನವಿದ್ದರೆ ಪ್ರತ್ಯೇಕವಾಗಿ ಇರಿ ಮತ್ತು 1075 ಗೆ ಕರೆ ಮಾಡಿ."]
        }
      },
      {
        patterns: [/ayurveda|natural remedy|herb|home remedy|kadha|gharelu/i],
        responses: {
          en: ["Ayurvedic remedies work best when used consistently. Key herbs: Ashwagandha for stress and immunity, Tulsi for respiratory health, Neem for skin and blood purification, Giloy for viral infections, and Triphala for digestion. Always consult a qualified Ayurvedic practitioner."],
          hi: ["आयुर्वेदिक उपाय नियमित उपयोग से सबसे अच्छे काम करते हैं। अश्वगंधा तनाव के लिए, तुलसी श्वसन के लिए।"],
          ta: ["ஆயுர்வேத மருத்துவம் நிலையான பயன்பாட்டில் சிறப்பாக செயல்படுகிறது."],
          kn: ["ಆಯುರ್ವೇದ ಔಷಧಿಗಳು ನಿಯಮಿತ ಬಳಕೆಯಲ್ಲಿ ಉತ್ತಮವಾಗಿ ಕೆಲಸ ಮಾಡುತ್ತವೆ."]
        }
      },
      {
        patterns: [/sleep|insomnia|nind|தூக்கம்|ನಿದ್ರೆ/i],
        responses: {
          en: ["Poor sleep affects immunity and mental health. Maintain a consistent sleep schedule, avoid screens 1 hour before bed, drink warm milk with nutmeg, and practice deep breathing. Brahmi Ghrita in Ayurveda helps with sleep disorders."],
          hi: ["नींद की कमी प्रतिरोधक क्षमता को प्रभावित करती है। सोने से पहले गर्म दूध पिएं।"],
          ta: ["தூக்கமின்மை நோய் எதிர்ப்பு சக்தியை பாதிக்கும். தூங்கும் முன் வெதுவெதுப்பான பால் குடிக்கவும்."],
          kn: ["ನಿದ್ರಾಹೀನತೆ ರೋಗ ನಿರೋಧಕ ಶಕ್ತಿ ಮೇಲೆ ಪರಿಣಾಮ ಬೀರುತ್ತದೆ."]
        }
      },
      {
        patterns: [/emergency|urgent|serious|critical|hospital/i],
        responses: {
          en: ["For medical emergencies, call 108 (Ambulance) or 112 (Emergency Services) immediately. Use the SOS feature in HealLens to alert your emergency contacts. Don't delay — early treatment saves lives."],
          hi: ["चिकित्सा आपातकाल के लिए 108 (एम्बुलेंस) या 112 पर तुरंत कॉल करें।"],
          ta: ["மருத்துவ அவசர நிலையில் 108 (ஆம்புலன்ஸ்) அல்லது 112 ஐ உடனடியாக அழைக்கவும்."],
          kn: ["ವೈದ್ಯಕೀಯ ತುರ್ತು ಪರಿಸ್ಥಿತಿಯಲ್ಲಿ ತಕ್ಷಣ 108 (ಆಂಬ್ಯುಲೆನ್ಸ್) ಅಥವಾ 112 ಗೆ ಕರೆ ಮಾಡಿ."]
        }
      },
      {
        patterns: [/thank|thanks|shukriya|நன்றி|ಧನ್ಯವಾದ/i],
        responses: {
          en: ["You're welcome! Stay healthy and take care. Remember, I'm always here if you have more health questions. 💚", "Happy to help! Your health is our priority. Don't hesitate to ask more questions anytime."],
          hi: ["आपका स्वागत है! स्वस्थ रहें। हम हमेशा यहाँ हैं।"],
          ta: ["மகிழ்ச்சியுடன்! ஆரோக்கியமாக இருங்கள்."],
          kn: ["ಸ್ವಾಗತ! ಆರೋಗ್ಯವಾಗಿ ಇರಿ."]
        }
      }
    ];
  }

  getResponse(userMessage) {
    const lang = window.i18n?.currentLang || "en";
    const msg = userMessage.toLowerCase();

    // Check patterns
    for (const entry of this.knowledgeBase) {
      for (const pattern of entry.patterns) {
        if (pattern.test(msg)) {
          const responses = entry.responses[lang] || entry.responses.en;
          return responses[Math.floor(Math.random() * responses.length)];
        }
      }
    }

    // If last diagnosis is in context
    if (this.context.lastDiagnosis) {
      const d = this.context.lastDiagnosis;
      if (msg.match(/more|detail|explain|tell me|what|how/i)) {
        return this.getDetailedResponse(d, lang);
      }
    }

    // Fallback response
    const fallbacks = {
      en: [
        "I understand you're concerned about your health. Could you describe your symptoms in more detail? I can help better with more information.",
        "That's an important health concern. For accurate diagnosis, I recommend using the Scan feature to upload an image. Meanwhile, staying hydrated and resting is always a good start.",
        "I'm here to help with your health questions. Could you share more symptoms so I can give you better guidance?"
      ],
      hi: ["मैं आपकी स्वास्थ्य समस्या समझता हूं। कृपया अपने लक्षणों का विवरण दें।"],
      ta: ["உங்கள் சுகாதார அக்கறையை புரிந்துகொள்கிறேன். உங்கள் அறிகுறிகளை விரித்துரையுங்கள்."],
      kn: ["ನಿಮ್ಮ ಆರೋಗ್ಯ ಕಾಳಜಿ ಅರ್ಥವಾಗುತ್ತದೆ. ರೋಗಲಕ್ಷಣಗಳನ್ನು ವಿವರಿಸಿ."]
    };

    const fbArr = fallbacks[lang] || fallbacks.en;
    return fbArr[Math.floor(Math.random() * fbArr.length)];
  }

  getDetailedResponse(diagnosis, lang) {
    const templates = {
      en: `Based on your recent scan that detected ${diagnosis.diseaseName} with ${diagnosis.confidence}% confidence: ${diagnosis.description} The severity is ${diagnosis.severity}. ${diagnosis.doctorType} consultation is recommended.`,
      hi: `आपके हालिया स्कैन में ${diagnosis.diseaseName} पाया गया। ${diagnosis.description}`,
      ta: `உங்கள் சமீபத்திய ஸ்கேனில் ${diagnosis.diseaseName} கண்டறியப்பட்டது. ${diagnosis.description}`,
      kn: `ನಿಮ್ಮ ಇತ್ತೀಚಿನ ಸ್ಕ್ಯಾನ್‌ನಲ್ಲಿ ${diagnosis.diseaseName} ಪತ್ತೆಯಾಯಿತು. ${diagnosis.description}`
    };
    return templates[lang] || templates.en;
  }

  setContext(diagnosis) {
    this.context.lastDiagnosis = diagnosis;
  }

  addToHistory(role, message) {
    this.conversationHistory.push({ role, message, time: new Date() });
  }
}

window.healthChatbot = new HealthChatbot();
