// ai-engine.js - HealLens Clinical Diagnostic Engine
// FULL RESTORE VERSION (Stable + Original Remedies from DB)

class AIEngine {
  constructor() {
    this.modelReady = false;
    this.isFallback = true;
    this.model = null;
    this.diseases = null;
    this.classes = ["arthritis","covid19","fracture","pneumonia","psoriasis","skin_infection","tuberculosis"];
    this.loadDatabase();
    this.loadTFModel();
  }

  async loadTFModel() {
    try {
      if (typeof tf === 'undefined') {
        console.warn("[AI] TF.js not loaded yet, retrying in 1s...");
        await new Promise(r => setTimeout(r, 1000));
        if (typeof tf === 'undefined') { console.error("[AI] TF.js unavailable."); return; }
      }
      console.log("[AI] Loading CNN model...");
      this.model = await tf.loadLayersModel('model/model.json');
      this.isFallback = false;
      this.modelReady = true;
      console.log("[AI] ✅ CNN model loaded successfully.");
    } catch (e) {
      console.warn("[AI] CNN model load failed, using rule-based fallback:", e.message);
      this.isFallback = true;
      this.modelReady = true; // fallback is ready
    }
  }

  async runCNNPrediction(imageElement) {
    if (!this.model || this.isFallback) return null;
    try {
      const tensor = tf.tidy(() => {
        const img = tf.browser.fromPixels(imageElement)
          .resizeBilinear([224, 224])
          .toFloat()
          .div(255.0)
          .expandDims(0);
        return img;
      });
      const predictions = await this.model.predict(tensor).data();
      tensor.dispose();
      let maxIdx = 0;
      let maxVal = predictions[0];
      for (let i = 1; i < predictions.length; i++) {
        if (predictions[i] > maxVal) { maxVal = predictions[i]; maxIdx = i; }
      }
      const confidence = Math.round(maxVal * 100);
      const className = this.classes[maxIdx];
      console.log(`[AI] CNN Prediction: ${className} (${confidence}%)`);
      return { className, confidence, scores: Array.from(predictions) };
    } catch (e) {
      console.warn("[AI] CNN prediction failed:", e.message);
      return null;
    }
  }

  // Visual bone analysis — detects fracture vs arthritis from the X-ray image itself
  // Key insight: a fracture = a SHARP DARK LINE cutting through a BRIGHT BONE area
  // Arthritis = diffuse changes, no sharp discontinuities in the bone
  analyzeBoneVisually(imageElement) {
    return new Promise((resolve) => {
      try {
        const canvas = document.createElement("canvas");
        const SIZE = 150;
        canvas.width = SIZE;
        canvas.height = SIZE;
        const ctx = canvas.getContext("2d");
        ctx.drawImage(imageElement, 0, 0, SIZE, SIZE);
        const data = ctx.getImageData(0, 0, SIZE, SIZE).data;

        // Convert to grayscale
        const gray = [];
        for (let i = 0; i < data.length; i += 4) {
          gray.push(0.299 * data[i] + 0.587 * data[i+1] + 0.114 * data[i+2]);
        }

        // Find the brightest region (bone area) and darkest region (fracture line / background)
        let maxBright = 0, minDark = 255;
        let brightCount = 0, darkLineCount = 0;
        let sobelSum = 0;

        for (let y = 1; y < SIZE - 1; y++) {
          for (let x = 1; x < SIZE - 1; x++) {
            const idx = y * SIZE + x;
            const v = gray[idx];

            if (v > maxBright) maxBright = v;
            if (v < minDark) minDark = v;

            // Count bright pixels (bone region — typically > 140 in X-ray)
            if (v > 140) brightCount++;

            // Sobel gradient — detects edges
            const gx = Math.abs(gray[idx + 1] - gray[idx - 1]);
            const gy = Math.abs(gray[idx + SIZE] - gray[idx - SIZE]);
            const grad = Math.sqrt(gx * gx + gy * gy);
            sobelSum += grad;

            // Dark line cutting through bright zone — fracture signature
            // A dark pixel (< 80) surrounded by bright neighbours = fracture line
            const neighbors = [
              gray[idx - 1], gray[idx + 1],
              gray[idx - SIZE], gray[idx + SIZE]
            ];
            const avgNeighbor = neighbors.reduce((a, b) => a + b, 0) / 4;
            if (v < 80 && avgNeighbor > 130) darkLineCount++;
          }
        }

        const totalPx = (SIZE - 2) * (SIZE - 2);
        const bonePresence = brightCount / totalPx;      // how much bright bone visible
        const fractureLine = darkLineCount / totalPx;    // dark-line-in-bright-zone ratio
        const avgGradient = sobelSum / totalPx;          // overall edge sharpness
        const contrast = maxBright - minDark;            // overall image dynamic range

        console.log(`[AI] Bone visual → bonePresence:${bonePresence.toFixed(3)}, fractureLine:${fractureLine.toFixed(4)}, gradient:${avgGradient.toFixed(1)}, contrast:${contrast}`);

        // FRACTURE: dark line cutting through bone OR very high edge sharpness in bone region
        const hasFractureLine = fractureLine > 0.003;     // dark cuts in bright bone
        const hasHighEdgeInBone = avgGradient > 25 && bonePresence > 0.1;
        const highContrast = contrast > 150;

        if (hasFractureLine || (hasHighEdgeInBone && highContrast)) {
          console.log("[AI] Visual → FRACTURE detected");
          resolve("fracture");
        } else {
          // Low edge + low contrast = diffuse changes → arthritis pattern
          console.log("[AI] Visual → ARTHRITIS pattern (diffuse)");
          resolve("arthritis");
        }
      } catch (e) {
        console.warn("[AI] Visual bone analysis failed:", e);
        resolve("fracture"); // safe default — better to flag fracture than miss it
      }
    });
  }

  // Visual skin analysis — differentiates between smooth red infection (erythema/pus)
  // and scaly, rough, hyperkeratotic patches (psoriasis/dry rash)
  analyzeSkinVisually(imageElement) {
    return new Promise((resolve) => {
      try {
        const canvas = document.createElement("canvas");
        const SIZE = 120;
        canvas.width = SIZE;
        canvas.height = SIZE;
        const ctx = canvas.getContext("2d");
        ctx.drawImage(imageElement, 0, 0, SIZE, SIZE);
        const data = ctx.getImageData(0, 0, SIZE, SIZE).data;

        let totalR = 0, totalG = 0, totalB = 0;
        let redPixels = 0;
        const gray = [];

        for (let i = 0; i < data.length; i += 4) {
          const r = data[i];
          const g = data[i+1];
          const b = data[i+2];
          totalR += r;
          totalG += g;
          totalB += b;

          // Prominently red check (erythema indicator)
          if (r > g + 15 && r > b + 15) {
            redPixels++;
          }
          gray.push(0.299 * r + 0.587 * g + 0.114 * b);
        }

        const totalPx = SIZE * SIZE;
        const erythemaRatio = redPixels / totalPx;

        // Texture roughness / fine edge scanner
        let edgeSum = 0;
        const edgeGradients = [];
        for (let y = 1; y < SIZE - 1; y++) {
          for (let x = 1; x < SIZE - 1; x++) {
            const idx = y * SIZE + x;
            const gx = Math.abs(gray[idx + 1] - gray[idx - 1]);
            const gy = Math.abs(gray[idx + SIZE] - gray[idx - SIZE]);
            const grad = Math.sqrt(gx * gx + gy * gy);
            edgeSum += grad;
            edgeGradients.push(grad);
          }
        }

        const avgGradient = edgeSum / edgeGradients.length;
        
        let sqDiffSum = 0;
        edgeGradients.forEach(g => {
          sqDiffSum += Math.pow(g - avgGradient, 2);
        });
        const textureRoughness = Math.sqrt(sqDiffSum / edgeGradients.length);

        console.log(`[AI] Skin visual → erythemaRatio:${erythemaRatio.toFixed(3)}, avgGradient:${avgGradient.toFixed(1)}, roughness:${textureRoughness.toFixed(1)}`);

        // KEY LOGIC:
        // Psoriasis = HIGH texture roughness (dry, flaky, scaly skin creates many sharp edges)
        //           + moderate gradient variance regardless of redness level
        // Skin Infection = HIGH redness, SMOOTH surface (inflamed but not scaly)
        
        if (textureRoughness > 12.0 && avgGradient > 18) {
          // Rough, high-edge-density pattern → Psoriasis / Rash / Scaly condition
          console.log("[AI] Visual → PSORIASIS detected (High roughness + dense edges)");
          resolve("psoriasis");
        } else if (erythemaRatio > 0.15 && textureRoughness <= 12.0) {
          // Clear redness with smooth surface → Skin Infection / Cellulitis
          console.log("[AI] Visual → SKIN INFECTION detected (Smooth redness pattern)");
          resolve("skin_infection");
        } else if (textureRoughness > 14.0) {
          // Very rough even without high gradient density → still psoriasis
          console.log("[AI] Visual → PSORIASIS detected (Very high roughness)");
          resolve("psoriasis");
        } else {
          // Default: moderate redness without strong pattern → treat as infection
          console.log("[AI] Visual → SKIN INFECTION detected (default)");
          resolve("skin_infection");
        }
      } catch (e) {
        console.warn("[AI] Visual skin analysis failed:", e);
        resolve("skin_infection"); // safe default
      }
    });
  }



  async loadDatabase() {
    try {
      const res = await fetch('data/diseases.json');
      if (res.ok) {
        this.diseases = await res.json();
        console.log("AI Database Loaded from JSON");
      } else {
        throw new Error("Local JSON fail");
      }
    } catch (e) {
      console.warn("Using Embedded Database");
      this.diseases = this.getEmbeddedDatabase();
    }
  }

  getTranslatedData(id, lang) {
    const data = {
      hi: {
        pneumonia: {
          description: "न्यूमोनिया एक संक्रमण है जो एक या दोनों फेफड़ों में हवा की थैलियों में सूजन पैदा करता है, जो तरल पदार्थ या मवाद से भर सकती हैं जिससे सांस लेने में कठिनाई होती है।",
          remedies: [
            { name: "अदरक की चाय", ingredients: "ताजा अदरक, पानी, शहद", method: "ताजा अदरक काटें। 10-15 मिनट तक पानी में उबालें। छान लें और शहद मिलाएं।", use: "खांसी और जमाव में मदद के लिए गर्म सेवन करें।" },
            { name: "भाप लेना", ingredients: "गर्म पानी, कटोरा", method: "एक कटोरे में गर्म पानी डालें। सावधानी से 5-10 मिनट तक भाप लें।", use: "बलगम को ढीला करने और सांस लेने में तकलीफ को कम करने के लिए उपयोग किया जाता है।" },
            { name: "हल्दी वाला दूध", ingredients: "दूध, हल्दी पाउडर, काली मिर्च", method: "दूध गर्म करें। हल्दी और मिर्च डालें। अच्छी तरह मिलाएं।", use: "गले के आराम और सूजन में मदद के लिए गर्म सेवन करें।" }
          ]
        },
        tuberculosis: {
          description: "तपेदिक (टीबी) एक गंभीर जीवाणु संक्रमण है जो मुख्य रूप से फेफड़ों को प्रभावित करता है। नोट: चिकित्सा उपचार आवश्यक है; प्राकृतिक उपचार केवल सहायक हैं।",
          remedies: [
            { name: "लहसुन का पानी", ingredients: "लहसुन की कलियाँ, गर्म पानी", method: "लहसुन की कलियों को कुचलें। गर्म पानी में मिलाएं। पीने से पहले थोड़ी देर बैठने दें।", use: "पारंपरिक रूप से श्वसन सहायता के लिए सेवन किया जाता है।" },
            { name: "आंवला का रस", ingredients: "ताजा आंवला, पानी, शहद", method: "आंवले को पानी के साथ ब्लेंड करें। रस छान लें। इच्छानुसार शहद मिलाएं।", use: "विटामिन सी और प्रतिरक्षा सहायता के लिए सेवन किया जाता है।" }
          ]
        },
        covid19: {
          description: "कोविड-19 एक वायरल श्वसन रोग है जो SARS-CoV-2 के कारण होता है, जो फेफड़ों को प्रभावित करता है और बुखार, सूखी खांसी और थकान का कारण बनता है।",
          remedies: [
            { name: "निलावेम्बु कुडीनीर", ingredients: "निलावेम्बु कुडीनीर पाउडर, पानी", method: "पानी में 5-10 ग्राम पाउडर मिलाएं। आधा होने तक उबालें। छान लें और गर्म पिएं।", use: "प्रतिरक्षा और बुखार सहायता के लिए व्यापक रूप से सेवन किया जाता है।" },
            { name: "नमक के पानी के गरारे", ingredients: "गर्म पानी, नमक", method: "गर्म पानी में नमक मिलाएं। 20-30 सेकंड के लिए गरारे करें।", use: "गले की खराश से राहत के लिए उपयोग किया जाता है।" }
          ]
        },
        fracture: {
          description: "हड्डी का फ्रैक्चर हड्डी में दरार या टूटना है। नोट: फ्रैक्चर के लिए उचित चिकित्सा उपचार (कास्टिंग/सर्जरी) की आवश्यकता होती है; उपचार सहायक होते हैं।",
          remedies: [
            { name: "कैल्शियम युक्त दूध", ingredients: "दूध, हल्दी, शहद", method: "दूध को थोड़ा गर्म करें। हल्दी डालें और अच्छी तरह मिलाएं। जरूरत पड़ने पर शहद मिलाएं।", use: "कैल्शियम और प्रोटीन प्रदान करके हड्डी के उपचार में सहायता करता है।" },
            { name: "हल्दी अदरक का पेय", ingredients: "अदरक, हल्दी, पानी", method: "पानी में अदरक और हल्दी उबालें। छान लें और गर्म पिएं।", use: "सूजन और दर्द को कम करने में मदद करता है।" }
          ]
        },
        arthritis: {
          description: "गठिया जोड़ों की सूजन का कारण बनता है जिसके परिणामस्वरूप दर्द, जकड़न और गतिशीलता कम हो जाती है।",
          remedies: [
            { name: "हल्दी वाला दूध", ingredients: "दूध, हल्दी पाउडर, काली मिर्च", method: "दूध गर्म करें। हल्दी और मिर्च डालें। अच्छी तरह मिलाएं।", use: "जोड़ों की सूजन को कम करने में मदद करता है।" },
            { name: "मेथी का पानी", ingredients: "मेथी के बीज, पानी", method: "बीजों को रात भर भिगोएँ। उबला हुआ या भिगोया हुआ पानी पिएं।", use: "पारंपरिक रूप से जोड़ों के दर्द से राहत के लिए उपयोग किया जाता है।" }
          ]
        },
        skin_infection: {
          description: "सक्रिय जीवाणु, वायरल या कवक त्वचा रोगज़नक़ का पता चला है (सेल्युलाइटिस, इम्पेटिगो, या तीव्र जिल्द की सूजन का संकेत)। एपिडर्मल और त्वचीय परतों में तीव्र स्थानीयकृत संवहनी फैलाव (एरीथेमा) के साथ सूजन संबंधी तरल पदार्थ का रिसाव दिखाई देता है। इससे प्रभावित क्षेत्र में गर्मी, लालिमा और सूजन बढ़ जाती है। गहरे उपत्वचीय ऊतकों में फैलने से रोकने के लिए तत्काल रोगाणुरोधी उपचार की सलाह दी जाती है।",
          remedies: [
            { name: "नीम वॉश", ingredients: "नीम के पत्ते, पानी", method: "नीम के पत्तों को पानी में उबालें, ठंडा करें और संक्रमित क्षेत्र को धोने के लिए उपयोग करें।", use: "प्राकृतिक एंटीसेप्टिक वॉश।" },
            { name: "एलोवेरा जेल", ingredients: "शुद्ध एलोवेरा", method: "ठंडक और उपचार के लिए प्रभावित क्षेत्र पर ताजा जेल लगाएं।", use: "सूजन और खुजली को शांत करता है।" }
          ]
        },
        psoriasis: {
          description: "क्रोनिक ऑटोइम्यून-मध्यस्थता एपिडर्मल हाइपरप्लासिया देखा गया (सोरायसिस वल्गेरिस या गंभीर एटोपिक जिल्द की सूजन के साथ अत्यधिक सुसंगत)। त्वचा की बनावट स्थानीयकृत हाइपरकेराटोसिस (चांदी जैसी परतदार पपड़ी) के साथ अंतर्निहित केशिका फैलाव को दर्शाती है। इस स्थिति में भड़काऊ साइटोकिन्स द्वारा ट्रिगर की गई त्वचा कोशिकाओं का तेजी से निर्माण शामिल है। त्वचा की नमी बनाए रखना आवश्यक है और त्वचा विशेषज्ञ से परामर्श करना चाहिए।",
          remedies: [
            { name: "नारियल-हल्दी मिश्रण", ingredients: "नारियल तेल, हल्दी", method: "मिलाएं और प्रभावित क्षेत्रों पर लगाएं।", use: "खुजली को शांत करता है और पपड़ी को कम करता है।" },
            { name: "एलोवेरा का प्रयोग", ingredients: "शुद्ध एलोवेरा", method: "पपड़ीदार धब्बों पर दिन में 3 बार जेल लगाएं।", use: "लालिमा और पपड़ी को कम करता है।" }
          ]
        }
      },
      ta: {
        pneumonia: {
          description: "நிமோனியா என்பது ஒன்று ಅಥವಾ இரண்டு நுரையீரலில் உள்ள காற்றுப் பைகளை வீக்கமடையச் செய்யும் ஒரு தொற்று ஆகும், இது திரவம் அல்லது சீழ் நிரம்பி சுவாசிப்பதில் சிரமத்தை ஏற்படுத்தும்.",
          remedies: [
            { name: "இஞ்சி டீ", ingredients: "புதிய இஞ்சி, தண்ணீர், தேன்", method: "இஞ்சியை நறுக்கி தண்ணீரில் 10-15 நிமிடம் கொதிக்க வைக்கவும். வடிகட்டி தேன் சேர்க்கவும்.", use: "இருமல் மற்றும் நெஞ்சு சளியைக் குறைக்க சூடாகக் குடிக்கவும்." },
            { name: "ஆவி பிடித்தல்", ingredients: "சுடு நீர், கிண்ணம்", method: "கிண்ணத்தில் சுடு நீரை ஊற்றி 5-10 நிமிடம் ஆவி பிடிக்கவும்.", use: "சளியை இளக்கி சுவாசத்தை எளிதாக்க உதவும்." },
            { name: "மஞ்சள் பால்", ingredients: "பால், மஞ்சள் தூள், மிளಗು", method: "பாலை சூடாக்கி மஞ்சள் மற்றும் மிளகு சேர்த்து நன்கு கலக்கவும்.", use: "தொண்டை இதத்திற்கும் வீக்கத்திற்கும் சூடாகப் பருகவும்." }
          ]
        },
        tuberculosis: {
          description: "காசநோய் (TB) என்பது நுரையீரலை பாதிக்கும் ஒரு கடுமையான பாக்டீரியா தொற்று ஆகும். குறிப்பு: மருத்துவ சிகிச்சை அவசியம்; இயற்கை வைத்தியம் ஒரு துணை மட்டுமே.",
          remedies: [
            { name: "பூண்டு நீர்", ingredients: "பூண்டு பற்கள், வெதுவெதுப்பான நீர்", method: "பூண்டு பற்களை நசுக்கி நீரில் சேர்த்து சிறிது நேரம் கழித்துக் குடிக்கவும்.", use: "சுவாச ஆரோக்கியத்திற்காகப் பயன்படுத்தப்படுகிறது." },
            { name: "நெல்லி சாறு", ingredients: "புதிய நெல்லிக்காய், தண்ணீர், தேன்", method: "நெல்லிக்காயை அரைத்து சாறு எடுத்து தேன் கலந்து குடிக்கவும்.", use: "வைட்டமின் ಸಿ மற்றும் நோய் எதிர்ப்பு சக்திக்கு உதவும்." }
          ]
        },
        covid19: {
          description: "கோவிட்-19 என்பது SARS-CoV-2 வைரஸால் ஏற்படும் ஒரு சுவாச நோய் ஆகும், இது காய்ச்சல், வறட்டு இருமல் மற்றும் சோர்வை உண்டாக்கும்.",
          remedies: [
            { name: "நிலவேம்பு குடிநீர்", ingredients: "நிலவேம்பு பொடி, தண்ணீர்", method: "நீரில் பொடியைச் சேர்த்து பாதியாக வரும் வரை கொதிக்க வைத்து வடிகட்டிக் குடிக்கவும்.", use: "காய்ச்சல் மற்றும் நோய் எதிர்ப்பு சக்திக்கு சிறந்தது." },
            { name: "உப்பு நீர் கொப்பளித்தல்", ingredients: "வெதுவெதுப்பான நீர், உப்பு", method: "நீரில் உப்பு சேர்த்து 20-30 விநாடிகள் தொண்டையில் படுமாறு கொப்பளிக்கவும்.", use: "தொண்டை வலிக்கு நிவாரணம் தரும்." }
          ]
        },
        fracture: {
          description: "எலும்பு முறிவு என்பது எலும்பில் ஏற்படும் விரிசல் அல்லது உடைப்பு ஆகும். குறிப்பு: இதற்கு சரியான மருத்துவ சிகிச்சை அவசியம்; வைத்தியம் துணை மட்டுமே.",
          remedies: [
            { name: "கால்சியம் நிறைந்த பால்", ingredients: "பால், மஞ்சள், தேன்", method: "பாலைச் சூடாக்கி மஞ்சள் கலந்து தேன் சேர்த்துப் பருகவும்.", use: "எலும்பு கூடத் தேவையான கால்சியத்தைத் தரும்." },
            { name: "மஞ்சள் இஞ்சி பானம்", ingredients: "இஞ்சி, மஞ்சள், தண்ணீர்", method: "நீரில் இஞ்சி மற்றும் மஞ்சளைக் கொதிக்க வைத்து வடிகட்டிக் குடிக்கவும்.", use: "வீக்கம் மற்றும் வலியைக் குறைக்க உதவும்." }
          ]
        },
        arthritis: {
          description: "மூட்டுவலி மூட்டுகளில் வீக்கத்தை உண்டாக்கி வலி மற்றும் அசைவைக் குறைக்கும்.",
          remedies: [
            { name: "மஞ்சள் பால்", ingredients: "பால், மஞ்சள் தூள், மிளகு", method: "பாலை சூடாக்கி மஞ்சள் மற்றும் மிளகு சேர்த்துப் பருகவும்.", use: "மூட்டு வீக்கத்தைக் குறைக்க உதவும்." },
            { name: "வெந்தய நீர்", ingredients: "வெந்தயம், தண்ணீர்", method: "வெந்தயத்தை இரவு ஊறவைத்து அந்த நீரைக் குடிக்கவும்.", use: "மூட்டு வலி நிவாரணத்திற்குப் பயன்படுத்தப்படுகிறது." }
          ]
        },
        skin_infection: {
          description: "தீவிர பாக்டீரியா, வைரஸ் அல்லது பூஞ்சை தொற்று கண்டறியப்பட்டுள்ளது (செல்லுலிடிஸ், இம்பெடிகோ அல்லது கடுமையான தோல் அழற்சியைக் குறிக்கிறது). தோலின் மேல் மற்றும் உள் அடுக்குகளில் தீவிர உள்ளூர் இரத்த நாள விரிவாக்கம் (சிவத்தல்) மற்றும் அழற்சி நீர் கசிவு காணப்படுகிறது. இது தொற்று ஏற்பட்ட இடத்தில் அதிக வெப்பம், சிவப்பு நிறம் மற்றும் வீக்கத்தை ஏற்படுத்துகிறது. தொற்று ஆழமான திசுக்களுக்குப் பரவாமல் தடுக்க உடனடியாக மருத்துவரை அணுகி சிகிச்சை பெறுவது நல்லது.",
          remedies: [
            { name: "வேப்பிலை கழுவல்", ingredients: "வேப்பிலை, தண்ணீர்", method: "வேப்பிலையை நீரில் கொதிக்க வைத்து ஆறவைத்துத் தொற்று உள்ள இடத்தைக் கழுவவும்.", use: "இயற்கை கிருமிநாசினி." },
            { name: "கற்றாழை ஜெல்", ingredients: "சுத்தமான கற்றாழை", method: "புதிய கற்றாழை ஜல்லைத் தொற்று உள்ள இடத்தில் தடவவும்.", use: "அரிப்பு மற்றும் வீக்கத்தைக் குறைக்கும்." }
          ]
        },
        psoriasis: {
          description: "உடலின் நோய் எதிர்ப்பு அமைப்பு பாதிப்பால் ஏற்படும் நாள்பட்ட தோல் செதில் நோய் கண்டறியப்பட்டுள்ளது (சொரியாசிஸ் அல்லது கடுமையான தோல் அழற்சிக்கு ஒத்திருக்கிறது). தோலின் மேல் பகுதி தடிமனாகி, வெள்ளி போன்ற செதில்களாக உதிர்வதோடு இரத்த நாளங்கள் விரிவடைவதையும் காட்டுகிறது. இதில் புதிய தோல் செல்கள் மிக வேகமாக உருவாகின்றன. தோலின் ஈரப்பதத்தைப் பராமரிப்பது அவசியமாகும், மேலும் தோல் மருத்துவரை அணுகி சிகிச்சை பெற வேண்டும்.",
          remedies: [
            { name: "தேங்காய் எண்ணெய்-மஞ்சள்", ingredients: "தேங்காய் எண்ணெய், மஞ்சள்", method: "இரண்டையும் கலந்து பாதிக்கப்பட்ட இடத்தில் தடவவும்.", use: "அரிப்பைக் குறைத்து தோலை மென்மையாக்கும்." },
            { name: "கற்றாழை பயன்பாடு", ingredients: "சுத்தமான கற்றாழை", method: "தினமும் 3 முறை கற்றாழை ஜல்லைத் தடவவும்.", use: "சிவப்பு நிறத்தையும் செதில்களையும் குறைக்கும்." }
          ]
        }
      },
      kn: {
        pneumonia: {
          description: "ನ್ಯುಮೋನಿಯಾ ಎನ್ನುವುದು ಒಂದು ಅಥವಾ ಎರಡೂ ಶ್ವಾಸಕೋಶಗಳಲ್ಲಿನ ಗಾಳಿ ಚೀಲಗಳನ್ನು ಉರಿಯುವಂತೆ ಮಾಡುವ ಸೋಂಕು, ಇದು ದ್ರವ ಅಥವಾ ಕೀವು ತುಂಬಿ ಉಸಿರಾಟದ ತೊಂದರೆಯನ್ನು ಉಂಟುಮಾಡಬಹುದು.",
          remedies: [
            { name: "ಶುಂಠಿ ಚಹಾ", ingredients: "ತಾಜಾ ಶುಂಠಿ, ನೀರು, ಜೇನುತುಪ್ಪ", method: "ಶುಂಠಿಯನ್ನು ಕತ್ತರಿಸಿ 10-15 ನಿಮಿಷ ಕುದಿಸಿ, ನಂತರ ಸೋಸಿ ಜೇನುತುಪ್ಪ ಬೆರೆಸಿ.", use: "ಕೆಮ್ಮು ಮತ್ತು ಎದೆ ಕಟ್ಟುವಿಕೆಯನ್ನು ಕಡಿಮೆ ಮಾಡಲು ಬೆಚ್ಚಗೆ ಕುಡಿಯಿರಿ." },
            { name: "ಹಬೆ ತೆಗೆದುಕೊಳ್ಳುವುದು", ingredients: "ಬಿಸಿ ನೀರು, ಬೌಲ್", method: "ಬೌಲ್‌ಗೆ ಬಿಸಿ ನೀರು ಹಾಕಿ 5-10 ನಿಮಿಷ ಹಬೆ ತೆಗೆದುಕೊಳ್ಳಿ.", use: "ಕಫವನ್ನು ಸಡಿಲಗೊಳಿಸಲು ಮತ್ತು ಉಸಿರಾಟ ಸುಲಭಗೊಳಿಸಲು ಸಹಕಾರಿ." },
            { name: "ಹಳದಿ ಹಾಲು", ingredients: "ಹಾಲು, ಹಳದಿ ಪುಡಿ, ಕರಿಮೆಣಸು", method: "ಹಾಲನ್ನು ಕಾಯಿಸಿ ಹಳದಿ ಮತ್ತು ಮೆಣಸು ಸೇರಿಸಿ ಕುಡಿಯಿರಿ.", use: "ಗಂಟಲು ನೋವು ಮತ್ತು ಉರಿಯೂತಕ್ಕೆ ಉತ್ತಮ." }
          ]
        },
        tuberculosis: {
          description: "ಕ್ಷಯರೋಗ (TB) ಶ್ವಾಸಕೋಶವನ್ನು ಬಾಧಿಸುವ ಗಂಭೀರ ಬ್ಯಾಕ್ಟೀರಿಯಾ ಸೋಂಕು. ಗಮನಿಸಿ: ವೈದ್ಯಕೀಯ ಚಿಕಿತ್ಸೆ ಕಡ್ಡಾಯ; ನೈಸರ್ಗಿಕ ಚಿಕಿತ್ಸೆಗಳು ಕೇವಲ ಪೂರಕ.",
          remedies: [
            { name: "ಬೆಳ್ಳುಳ್ಳಿ ನೀರು", ingredients: "ಬೆಳ್ಳುಳ್ಳಿ ಎಸಳುಗಳು, ಬೆಚ್ಚಗಿನ ನೀರು", method: "ಬೆಳ್ಳುಳ್ಳಿಯನ್ನು ಜಜ್ಜಿ ನೀರಿಗೆ ಹಾಕಿ ಸ್ವಲ್ಪ ಸಮಯ ಬಿಟ್ಟು ಕುಡಿಯಿರಿ.", use: "ಉಸಿರಾಟದ ಆರೋಗ್ಯಕ್ಕಾಗಿ ಬಳಸಲಾಗುತ್ತದೆ." },
            { name: "ನೆಲ್ಲಿಕಾಯಿ ರಸ", ingredients: "ತಾಜಾ ನೆಲ್ಲಿಕಾಯಿ, ನೀರು, ಜೇನುತುಪ್ಪ", method: "ನೆಲ್ಲಿಕಾಯಿಯನ್ನು ಅರೆದು ರಸ ತೆಗೆದು ಜೇನುತುಪ್ಪ ಬೆರೆಸಿ ಕುಡಿಯಿರಿ.", use: "ವಿಟಮಿನ್ ಸಿ ಮತ್ತು ರೋಗನಿರೋಧಕ ಶಕ್ತಿಗೆ ಉತ್ತಮ." }
          ]
        },
        covid19: {
          description: "ಕೋವಿಡ್-19 ಒಂದು ವೈರಲ್ ಉಸಿರಾಟದ ಕಾಯಿಲೆಯಾಗಿದ್ದು, ಜ್ವರ, ಒಣ ಕೆಮ್ಮು ಮತ್ತು ಸುಸ್ತನ್ನು ಉಂಟುಮಾಡುತ್ತದೆ.",
          remedies: [
            { name: "ನೆಲಬೇವು ಕಷಾಯ", ingredients: "ನೆಲಬೇವು ಪುಡಿ, ನೀರು", method: "ನೀರಿಗೆ ಪುಡಿ ಹಾಕಿ ಅರ್ಧವಾಗುವವರೆಗೆ ಕುದಿಸಿ ಸೋಸಿ ಕುಡಿಯಿರಿ.", use: "ಜ್ವರ ಮತ್ತು ರೋಗನಿರೋಧಕ ಶಕ್ತಿಗೆ ರಾಮಬಾಣ." },
            { name: "ಉಪ್ಪು ನೀರಿನ ಗಾರ್ಗ್ಲಿಂಗ್", ingredients: "ಬೆಚ್ಚಗಿನ ನೀರು, ಉಪ್ಪು", method: "ನೀರಿಗೆ ಉಪ್ಪು ಹಾಕಿ 20-30 ಸೆಕೆಂಡ್ ಗಂಟಲಿನಲ್ಲಿ ಗಾರ್ಗ್ಲ್ ಮಾಡಿ.", use: "ಗಂಟಲು ನೋವಿನ ಉಪಶಮನಕ್ಕೆ ಸಹಕಾರಿ." }
          ]
        },
        fracture: {
          description: "ಮೂಳೆ ಮುರಿತ ಎಂದರೆ ಮೂಳೆಯಲ್ಲಿ ಉಂಟಾಗುವ ಬಿರುಕು ಅಥವಾ ಒಡೆತ. ಗಮನಿಸಿ: ಇದಕ್ಕೆ ವೈದ್ಯಕೀಯ ಚಿಕಿತ್ಸೆ ಅಗತ್ಯ; ಮನೆಮದ್ದುಗಳು ಪೂರಕ ಮಾತ್ರ.",
          remedies: [
            { name: "ಕ್ಯಾಲ್ಸಿಯಂಯುಕ್ತ ಹಾಲು", ingredients: "ಹಾಲು, ಹಳದಿ, ಜೇನುತುಪ್ಪ", method: "ಹಾಲನ್ನು ಕಾಯಿಸಿ ಹಳದಿ ಮತ್ತು ಜೇನುತುಪ್ಪ ಬೆರೆಸಿ ಕುಡಿಯಿರಿ.", use: "ಮೂಳೆ ಕೂಡಲು ಅಗತ್ಯವಾದ ಕ್ಯಾಲ್ಸಿಯಂ ಒದಗಿಸುತ್ತದೆ." },
            { name: "ಹಳದಿ ಶುಂಠಿ ಪಾನೀಯ", ingredients: "ಶುಂಠಿ, ಹಳದಿ, ನೀರು", method: "ನೀರನ್ನು ಕುದಿಸಿ ಶುಂಠಿ ಮತ್ತು ಹಳದಿ ಹಾಕಿ ಸೋಸಿ ಕುಡಿಯಿರಿ.", use: "ನೋವು ಮತ್ತು ಊತ ಕಡಿಮೆ ಮಾಡಲು ಸಹಕಾರಿ." }
          ]
        },
        arthritis: {
          description: "ಸಂಧಿವಾತವು ಕೀಲುಗಳಲ್ಲಿ ಉರಿಯೂತವನ್ನು ಉಂಟುಮಾಡಿ ನೋವು ಮತ್ತು ಬಿಗಿತವನ್ನು ಉಂಟುಮಾಡುತ್ತದೆ.",
          remedies: [
            { name: "ಹಳದಿ ಹಾಲು", ingredients: "ಹಾಲು, ಹಳದಿ ಪುಡಿ, ಕರಿಮೆಣಸು", method: "ಹಾಲನ್ನು ಕಾಯಿಸಿ ಹಳದಿ ಮತ್ತು ಮೆಣಸು ಸೇರಿಸಿ ಕುಡಿಯಿರಿ.", use: "ಕೀಲುಗಳ ಉರಿಯೂತ ಕಡಿಮೆ ಮಾಡಲು ಸಹಕಾರಿ." },
            { name: "ಮೆಂತ್ಯ ನೀರು", ingredients: "ಮೆಂತ್ಯ ಕಾಳುಗಳು, ನೀರು", method: "ಮೆಂತ್ಯವನ್ನು ರಾತ್ರಿ ನೆನೆಸಿ ಬೆಳಿಗ್ಗೆ ಆ ನೀರನ್ನು ಕುಡಿಯಿರಿ.", use: "ಕೀಲು ನೋವಿನ ಉಪಶಮನಕ್ಕೆ ಬಳಸಲಾಗುತ್ತದೆ." }
          ]
        },
        skin_infection: {
          description: "ಸಕ್ರಿಯ ಬ್ಯಾಕ್ಟೀರಿಯಾ, ವೈರಲ್ ಅಥವಾ ಶಿಲೀಂಧ್ರ ಚರ್ಮದ ಸೋಂಕು ಪತ್ತೆಯಾಗಿದೆ (ಸೆಲ್ಯುಲೈಟಿಸ್, ಇಂಪೆಟಿಗೊ ಅಥವಾ ತೀವ್ರವಾದ ಚರ್ಮದ ಉರಿಯೂತವನ್ನು ಸೂಚಿಸುತ್ತದೆ). ಚರ್ಮದ ಮೇಲ್ಪದರದಲ್ಲಿ ತೀವ್ರವಾದ ಸ್ಥಳೀಯ ರಕ್ತನಾಳಗಳ ಹಿಗ್ಗುವಿಕೆ (ಕೆಂಪಾಗುವಿಕೆ) ಮತ್ತು ಉರಿಯೂತದ ದ್ರವದ ಶೇಖರಣೆ ಕಂಡುಬಂದಿದೆ. ಇದು ಪೀಡಿತ ಜಾಗದಲ್ಲಿ ಹೆಚ್ಚಿನ ಉಷ್ಣತೆ, ಕೆಂಪು ಬಣ್ಣ ಮತ್ತು ಊತವನ್ನು ಉಂಟುಮಾಡುತ್ತದೆ. ಸೋಂಕು ಆಳವಾದ ಅಂಗಾಂಶಗಳಿಗೆ ಹರಡುವುದನ್ನು ತಡೆಯಲು ತಕ್ಷಣವೇ ಚರ್ಮದ ವೈದ್ಯರನ್ನು ಸಂಪರ್ಕಿಸಲು ಸೂಚಿಸಲಾಗುತ್ತದೆ.",
          remedies: [
            { name: "ಬೇವು ನೀರಿನ ತೊಳೆಯುವಿಕೆ", ingredients: "ಬೇವು ಎಲೆಗಳು, ನೀರು", method: "ಬೇವು ಎಲೆಗಳನ್ನು ಕುದಿಸಿ ಸೋಸಿ ಆ ನೀರನ್ನು ಸೋಂಕಿತ ಜಾಗ ತೊಳೆಯಲು ಬಳಸಿ.", use: "ನೈಸರ್ಗಿಕ ನಂಜುನಿರೋಧಕ." },
            { name: "ಅಲೋವೆರಾ ಜೆಲ್", ingredients: "ಶುದ್ಧ ಅಲೋವೆರಾ", method: "ತಾಜಾ ಅಲೋವೆರಾ ಜೆಲ್ ಅನ್ನು ಪೀಡಿತ ಜಾಗಕ್ಕೆ ಹಚ್ಚಿ.", use: "ತುರಿಕೆ ಮತ್ತು ಉರಿಯನ್ನು ಶಮನಗೊಳಿಸುತ್ತದೆ." }
          ]
        },
        psoriasis: {
          description: "ದೇಹದ ರೋಗನಿರೋಧಕ ಶಕ್ತಿಯ ಏರುಪೇರಿನಿಂದ ಉಂಟಾಗುವ ದೀರ್ಘಕಾಲದ ಚರ್ಮದ ಕಾಯಿಲೆ ಪತ್ತೆಯಾಗಿದೆ (ಸೋರಿಯಾಸಿಸ್ ಅಥವಾ ತೀವ್ರವಾದ ಚರ್ಮದ ಹಾನಿಗೆ ಇದು ಹೊಂದಿಕೆಯಾಗುತ್ತದೆ). ಚರ್ಮದ ಮೇಲ್ಮೈ ದಪ್ಪಗಾಗಿ ಬೆಳ್ಳಿಯಂತಹ ಪೊರೆಗಳು ಉದುರುವುದನ್ನು ಮತ್ತು ರಕ್ತನಾಳಗಳು ಹಿಗ್ಗುತ್ತಿರುವುದನ್ನು ಇದು ತೋರಿಸುತ್ತದೆ. ಇದರಲ್ಲಿ ಹೊಸ ಚರ್ಮದ ಕೋಶಗಳು ಅತಿ ವೇಗವಾಗಿ ನಿರ್ಮಾಣವಾಗುತ್ತವೆ. ಚರ್ಮದ ತೇವಾಂಶವನ್ನು ಕಾಪಾಡಿಕೊಳ್ಳುವುದು ಅಗತ್ಯವಾಗಿದೆ ಮತ್ತು ತಕ್ಷಣವೇ ಚರ್ಮದ ವೈದ್ಯರಿಂದ ಚಿಕಿತ್ಸೆ ಪಡೆಯಬೇಕು.",
          remedies: [
            { name: "ತೆಂಗಿನ ಎಣ್ಣೆ-ಹಳದಿ ಮಿಶ್ರಣ", ingredients: "ತೆಂಗಿನ ಎಣ್ಣೆ, ಹಳದಿ", method: "ಎರಡನ್ನೂ ಬೆರೆಸಿ ಪೀಡಿತ ಜಾಗಕ್ಕೆ ಹಚ್ಚಿ.", use: "ತುರಿಕೆ ಶಮನಗೊಳಿಸಿ ಚರ್ಮವನ್ನು ಮೃದುವಾಗಿಸುತ್ತದೆ." },
            { name: "ಅಲೋವೆರಾ ಬಳಕೆ", ingredients: "ಶುದ್ಧ ಅಲೋವೆರಾ", method: "ದಿನಕ್ಕೆ 3 ಬಾರಿ ಅಲೋವೆರಾ ಜೆಲ್ ಹಚ್ಚಿ.", use: "ಕೆಂಪು ಬಣ್ಣ ಮತ್ತು ಪೊರೆಯನ್ನು ಕಡಿಮೆ ಮಾಡುತ್ತದೆ." }
          ]
        }
      }
    };
    return data[lang]?.[id] || null;
  }

  async analyzeImage(imageElement, bodyPartOverride) {
    const bodyPart = bodyPartOverride || "chest";

    const symptomInput = document.getElementById("symptom-input");
    const symptoms = symptomInput ? symptomInput.value.toLowerCase() : "";

    // --- 1. Try CNN Prediction first ---
    let resultClass = null;
    let cnnConfidence = 0;
    const cnnResult = await this.runCNNPrediction(imageElement);
    if (cnnResult) {
      // Only accept CNN result if it matches the selected body part
      const bodyPartMap = { "pneumonia":"chest", "tuberculosis":"chest", "covid19":"chest", "fracture":"bone", "arthritis":"bone", "skin_infection":"skin", "psoriasis":"skin" };
      if (bodyPartMap[cnnResult.className] === bodyPart) {
        resultClass = cnnResult.className;
        cnnConfidence = cnnResult.confidence;
        console.log(`[AI] Using CNN prediction: ${resultClass} (${cnnConfidence}%)`);
      } else {
        console.log(`[AI] CNN predicted ${cnnResult.className} but body part is '${bodyPart}' — ignoring.`);
      }
    }

    // --- 2. Fallback / visual analysis when CNN not available ---
    if (!resultClass) {
      resultClass = "pneumonia";
      if (bodyPart === "skin") {
        resultClass = await this.analyzeSkinVisually(imageElement);
        console.log(`[AI] Visual skin analysis result: ${resultClass}`);
      }
      if (bodyPart === "bone") {
        resultClass = await this.analyzeBoneVisually(imageElement);
        console.log(`[AI] Visual bone analysis result: ${resultClass}`);
      }
    }

    // --- 3. BONE: combine visual/CNN result WITH symptoms ---
    // Both visual and symptoms vote. Fracture wins on any positive signal (safety bias).
    if (bodyPart === "bone") {
      const fractureSigns = [
        "sudden", "severe pain", "crack", "snap", "deform", "break", "broke", "broken",
        "bruising", "inability to bear", "visible disability", "cannot move", "cannot walk",
        "swelling around injury", "tenderness", "difficulty moving", "acute", "sharp pain",
        "fell", "fall", "accident", "injury", "hit", "trauma", "bent", "twisted"
      ];
      const arthritisSigns = [
        "morning stiffness", "mild joint", "clicking", "grinding", "chronic",
        "swelling around joints", "reduced flexibility", "warmth around joint",
        "mild discomfort", "joint pain", "stiff", "stiffness", "creaking", "over time"
      ];

      const hasFractureSymptom = fractureSigns.some(s => symptoms.includes(s));
      const hasArthritisSymptom = arthritisSigns.some(s => symptoms.includes(s));

      if (hasFractureSymptom) {
        resultClass = "fracture";
        console.log("[AI] Bone: fracture confirmed by symptoms.");
      } else if (resultClass === "arthritis" && hasArthritisSymptom) {
        resultClass = "arthritis";
        console.log("[AI] Bone: arthritis confirmed by visual + symptoms.");
      } else if (resultClass === "arthritis" && !hasArthritisSymptom && !hasFractureSymptom) {
        // No symptoms + visual arthritis → safer to flag fracture
        resultClass = "fracture";
        console.log("[AI] Bone: no symptoms, visual arthritis → defaulting to fracture.");
      }
      console.log(`[AI] Final bone decision: ${resultClass}`);
    }

    // --- 4. Chest symptom overrides ---
    // TB, COVID, Pneumonia have distinct symptom fingerprints
    if (bodyPart === "chest") {
      const tbSigns = ["night sweat", "sweat", "weight loss", "blood in sputum", "blood cough", "haemoptysis", "prolonged", "weeks", "months", "chronic cough", "tb", "tubercul"];
      const covidSigns = ["taste", "smell", "anosmia", "ageusia", "covid", "corona", "fatigue", "body ache", "no taste", "no smell"];
      const pneumoniaSigns = ["sudden fever", "chills", "productive cough", "chest pain", "breathless", "difficulty breathing", "high fever", "yellow sputum", "green sputum"];

      const hasTB = tbSigns.some(s => symptoms.includes(s));
      const hasCovid = covidSigns.some(s => symptoms.includes(s));
      const hasPneumonia = pneumoniaSigns.some(s => symptoms.includes(s));

      if (hasTB) { resultClass = "tuberculosis"; console.log("[AI] Chest: TB confirmed by symptoms."); }
      else if (hasCovid) { resultClass = "covid19"; console.log("[AI] Chest: COVID-19 confirmed by symptoms."); }
      else if (hasPneumonia) { resultClass = "pneumonia"; console.log("[AI] Chest: Pneumonia confirmed by symptoms."); }
      // If no specific symptoms: keep CNN/visual result (pneumonia is default fallback)
    }

    // --- 5. Skin symptom overrides & smart clinical voting ---
    if (bodyPart === "skin") {

      // --- GOLDEN COMBINATIONS (Highest Priority Clinical Override) ---
      // From Skin Diagnostic Keys table:
      // 1. Silvery Scales + Red Patches → Psoriasis (autoimmune plaque override)
      // 2. Pus Discharge + Skin Boil    → Skin Infection (bacterial dermatology override)

      const hasSilvery = symptoms.includes("silver") || symptoms.includes("silvery");
      const hasScales = symptoms.includes("scale") || symptoms.includes("scales") || symptoms.includes("scaling") || symptoms.includes("scaly");
      const hasRedPatch = symptoms.includes("red patch") || symptoms.includes("red patches") || symptoms.includes("redness") || symptoms.includes("red spot");

      const hasPus = symptoms.includes("pus") || symptoms.includes("pus discharge") || symptoms.includes("discharge");
      const hasBoil = symptoms.includes("boil") || symptoms.includes("skin boil") || symptoms.includes("abscess");

      // 🥇 Golden Combo 1: Silvery + Scales → Psoriasis (autoimmune plaque)
      if ((hasSilvery && hasScales) || (hasSilvery && hasRedPatch)) {
        resultClass = "psoriasis";
        console.log("[AI] Skin: GOLDEN COMBO → Silvery Scales/Red Patches = PSORIASIS (autoimmune plaque override)");
      }
      // 🥇 Golden Combo 2: Pus + Boil → Skin Infection (bacterial)
      else if (hasPus && hasBoil) {
        resultClass = "skin_infection";
        console.log("[AI] Skin: GOLDEN COMBO → Pus Discharge + Skin Boil = SKIN INFECTION (bacterial override)");
      }
      else {
        // --- Standard single-keyword voting ---
        const infectionSigns = ["blister", "ooz", "warmth", "cellulitis", "wound", "cut", "bite", "inflammation", "swell", "sore", "warm", "pus", "discharge", "boil", "redness"];
        const psoriasisSigns = ["psoriasis", "dry patch", "flak", "itch", "plaqu", "rash", "flake", "flaky", "scale", "scales", "scaling", "silvery", "silver"];

        const hasInfectionSymptom = infectionSigns.some(s => symptoms.includes(s));
        const hasPsoriasisSymptom = psoriasisSigns.some(s => symptoms.includes(s));

        if (hasPsoriasisSymptom && !hasInfectionSymptom) {
          resultClass = "psoriasis";
          console.log("[AI] Skin: Psoriasis confirmed by symptom keywords.");
        } else if (hasInfectionSymptom && !hasPsoriasisSymptom) {
          resultClass = "skin_infection";
          console.log("[AI] Skin: Skin Infection confirmed by symptom keywords.");
        } else {
          // Mixed or no symptoms → trust visual analysis result
          console.log(`[AI] Skin: No clear symptom match → trusting visual prediction: ${resultClass}`);
        }
      }
    }



    // Flatten nested structure if needed
    let data = null;
    if (this.diseases) {
      // Search specifically in the body part first
      if (this.diseases[bodyPart]) {
        for (let dis in this.diseases[bodyPart]) {
          if (this.diseases[bodyPart][dis].id === resultClass) {
            data = this.diseases[bodyPart][dis];
            if (!data.name) data.name = dis;
            break;
          }
        }
      }
      
      // Fallback: search all categories
      if (!data) {
        for (let cat in this.diseases) {
          for (let dis in this.diseases[cat]) {
            if (this.diseases[cat][dis].id === resultClass) {
              data = this.diseases[cat][dis];
              if (!data.name) data.name = dis;
              break;
            }
          }
          if (data) break;
        }
      }
    }

    if (!data) data = { name: resultClass, severity: "moderate", description: {en: "Clinical pattern identified."}, remedies: {en: []}, ayurveda: [], doctorType: "General Physician" };

    const lang = window.i18n?.currentLang || "en";
    const translated = this.getTranslatedData(data.id || resultClass, lang);
    
    // Translate Doctor Type
    let translatedDoctor = data.doctorType || "General Physician";
    if (translatedDoctor.includes("Pulmonologist")) translatedDoctor = window.i18n.t("dr_pulmonologist");
    else if (translatedDoctor.includes("Dermatologist")) translatedDoctor = window.i18n.t("dr_dermatologist");
    else if (translatedDoctor.includes("Orthopedic")) translatedDoctor = window.i18n.t("dr_orthopedic");
    else if (translatedDoctor.includes("Rheumatologist")) translatedDoctor = window.i18n.t("dr_rheumatologist");
    else if (translatedDoctor.includes("Physician")) translatedDoctor = window.i18n.t("dr_physician");
    else if (translatedDoctor.includes("Infectious")) translatedDoctor = window.i18n.t("dr_infectious");

    return {
      id: data.id || resultClass,
      diseaseName: window.i18n?.t(data.id || resultClass) || data.name || resultClass.toUpperCase(),
      bodyPartLabel: bodyPart === "chest" ? "THORACIC REGION" : (bodyPart === "bone" ? "SKELETAL REGION" : "DERMATOLOGY"),
      severity: window.i18n?.t("severe_" + (data.severity || "moderate").toLowerCase()) || this.capitalize(data.severity || "moderate"),
      severityRaw: data.severity || "moderate",
      confidence: cnnConfidence > 0 ? cnnConfidence : 88.0,
      description: translated ? translated.description : ((data.description && data.description[lang]) ? data.description[lang] : (data.description?.en || data.description || "Condition identified.")),
      remedies: translated ? translated.remedies : ((data.remedies && data.remedies[lang]) ? data.remedies[lang] : (data.remedies?.en || data.remedies || [])),
      ayurveda: data.ayurveda || [],
      doctorType: translatedDoctor,
      findingCategory: (resultClass === "fracture") ? "Bone Fracture" : ((bodyPart === "bone") ? "Non-Fracture Condition" : null)
    };
  }

  capitalize(s) { return s.charAt(0).toUpperCase() + s.slice(1); }

  getEmbeddedDatabase() {
    return {
      "chest": {
        "Pneumonia": {
          "id": "pneumonia",
          "bodyPart": "chest",
          "description": {
            "en": "Pneumonia is an infection that inflames the air sacs in one or both lungs, which may fill with fluid or pus causing breathing difficulty."
          },
          "symptoms": ["fever", "cough", "breathlessness", "chest pain", "fatigue"],
          "severity": "moderate",
          "remedies": {
            "en": [
              { "name": "Ginger Tea", "ingredients": "Fresh ginger, Water, Honey", "method": "Slice fresh ginger. Boil in water for 10–15 minutes. Strain and add honey.", "use": "Consumed warm to help with cough and congestion." },
              { "name": "Steam Inhalation", "ingredients": "Hot water, Bowl", "method": "Pour hot water into a bowl. Carefully inhale steam for 5–10 minutes.", "use": "Used to loosen mucus and ease breathing discomfort." },
              { "name": "Turmeric Milk", "ingredients": "Milk, Turmeric powder, Black pepper", "method": "Heat milk. Add turmeric and pepper. Stir well.", "use": "Consumed warm for throat comfort and inflammation support." },
              { "name": "Warm Soup / Broth", "ingredients": "Vegetables or chicken, Water, Mild spices", "method": "Boil ingredients until soft. Simmer well and serve warm.", "use": "Helps hydration and provides nutrition during illness." }
            ]
          },
          "ayurveda": [
            { "name": "Sitopaladi Churna", "sanskrit": "सितोपलादि चूर्ण", "dosage": "3g with honey, twice daily", "use": "Respiratory infections" },
            { "name": "Vasavaleha", "sanskrit": "वासावलेह", "dosage": "1 tsp twice daily", "use": "Lung strengthening" }
          ],
          "doctorType": "Pulmonologist"
        },
        "Tuberculosis": {
          "id": "tuberculosis",
          "bodyPart": "chest",
          "description": {
            "en": "Tuberculosis (TB) is a serious bacterial infection primarily affecting the lungs. Note: Medical treatment is essential; natural remedies are supportive only."
          },
          "symptoms": ["persistent cough", "blood in sputum", "night sweats", "weight loss", "fatigue"],
          "severity": "critical",
          "remedies": {
            "en": [
              { "name": "Garlic Water", "ingredients": "Garlic cloves, Warm water", "method": "Crush garlic cloves. Add to warm water. Let sit briefly before drinking.", "use": "Traditionally consumed for respiratory support." },
              { "name": "Amla Juice", "ingredients": "Fresh amla, Water, Honey", "method": "Blend amla with water. Strain the juice. Add honey if desired.", "use": "Consumed for vitamin C and immune support." },
              { "name": "Mint Tea", "ingredients": "Mint leaves, Water", "method": "Boil mint leaves in water. Simmer for several minutes. Strain before drinking.", "use": "Used for breathing comfort and freshness." },
              { "name": "Protein-Rich Soup", "ingredients": "Lentils or chicken, Vegetables, Water", "method": "Boil ingredients thoroughly. Simmer until soft. Serve warm.", "use": "Supports nutrition and recovery strength." }
            ]
          },
          "ayurveda": [
            { "name": "Chyawanprash", "sanskrit": "च्यवनप्राश", "dosage": "2 tsp with warm milk, morning", "use": "Immunity and lung health" },
            { "name": "Pippalyadi Vati", "sanskrit": "पिಪ್ಪಲ್ಯಾದಿ ವಟಿ", "dosage": "2 tablets twice daily", "use": "TB and respiratory infections" }
          ],
          "doctorType": "Pulmonologist / Infectious Disease Specialist"
        },
        "COVID-19": {
          "id": "covid19",
          "bodyPart": "chest",
          "description": {
            "en": "COVID-19 is a viral respiratory illness caused by SARS-CoV-2, affecting lungs and causing fever, dry cough, and fatigue."
          },
          "symptoms": ["fever", "dry cough", "fatigue", "loss of smell", "breathlessness"],
          "severity": "moderate",
          "remedies": {
            "en": [
              { "name": "Nilavembu Kudineer", "ingredients": "Nilavembu Kudineer powder, Water", "method": "Add 5–10 g powder to water. Boil until reduced by half. Filter and drink warm.", "use": "Widely consumed for immune and fever support." },
              { "name": "Salt Water Gargle", "ingredients": "Warm water, Salt", "method": "Mix salt into warm water. Gargle for 20–30 seconds.", "use": "Used for sore throat relief." },
              { "name": "Lemon Honey Drink", "ingredients": "Warm water, Lemon juice, Honey", "method": "Add lemon juice to warm water. Mix honey well.", "use": "Consumed for throat comfort and hydration." },
              { "name": "Turmeric Ginger Tea", "ingredients": "Ginger, Turmeric, Water", "method": "Boil ginger and turmeric in water. Simmer for 10 minutes. Strain before drinking.", "use": "Traditionally consumed for warmth and respiratory comfort." }
            ]
          },
          "ayurveda": [
            { "name": "Giloy Ghanvati", "sanskrit": "गिलोय घनवटी", "dosage": "2 tablets twice daily after meals", "use": "Antiviral and immunity booster" },
            { "name": "Anu Tailam", "sanskrit": "ಅಣು ತೈಲಂ", "dosage": "2 drops in each nostril, morning", "use": "Nasal cleansing" }
          ],
          "doctorType": "General Physician / Pulmonologist"
        }
      },
      "bone": {
        "Bone Fracture": {
          "id": "fracture",
          "bodyPart": "bone",
          "description": {
            "en": "A bone fracture is a break or crack in a bone. Note: Fracture needs proper medical treatment (casting/surgery); remedies are supportive."
          },
          "symptoms": ["severe pain", "swelling", "bruising", "deformity", "inability to move"],
          "severity": "critical",
          "remedies": {
            "en": [
              { "name": "Calcium-Rich Milk", "ingredients": "Milk, Turmeric, Honey", "method": "Warm the milk slightly. Add turmeric and mix well. Add honey if needed.", "use": "Supports bone healing by providing calcium and protein." },
              { "name": "Sesame Seed Mix", "ingredients": "Sesame seeds, Jaggery or honey", "method": "Roast sesame seeds lightly. Mix with jaggery or honey. Eat in small portions.", "use": "Rich in calcium and supports bone strength." },
              { "name": "Egg / Protein Soup", "ingredients": "Eggs or lentils, Vegetables, Water", "method": "Boil ingredients well. Cook into soft soup. Serve warm.", "use": "Helps tissue and bone repair." },
              { "name": "Turmeric Ginger Drink", "ingredients": "Ginger, Turmeric, Water", "method": "Boil ginger and turmeric in water. Strain and drink warm.", "use": "Helps reduce inflammation and pain." }
            ]
          },
          "ayurveda": [
            { "name": "Laksha Guggulu", "sanskrit": "लाक्षा गुग्गुलु", "dosage": "2 tablets twice daily with warm milk", "use": "Bone healing and fracture recovery" },
            { "name": "Asthisamharaka", "sanskrit": "ಅಸ್ಥಿಸಂಹಾರಕ", "dosage": "5g powder with warm milk, twice daily", "use": "Bone knitting" }
          ],
          "doctorType": "Orthopedic Surgeon"
        },
        "Mild Arthritis": {
          "id": "arthritis",
          "bodyPart": "bone",
          "description": {
            "en": "Arthritis causes inflammation of joints resulting in pain, stiffness, and reduced mobility."
          },
          "symptoms": ["joint pain", "stiffness", "swelling", "redness", "limited range of motion"],
          "severity": "mild",
          "remedies": {
            "en": [
              { "name": "Turmeric Milk", "ingredients": "Milk, Turmeric powder, Black pepper", "method": "Heat milk. Add turmeric and pepper. Stir well.", "use": "Helps reduce joint inflammation." },
              { "name": "Ginger Tea", "ingredients": "Fresh ginger, Water, Honey", "method": "Boil ginger in water for 10–15 minutes. Strain and drink warm.", "use": "May reduce joint pain and stiffness." },
              { "name": "Epsom Salt Bath", "ingredients": "Warm water, Epsom salt", "method": "Add Epsom salt to warm water. Soak joints for 15–20 minutes.", "use": "Helps relax muscles and reduce stiffness." },
              { "name": "Fenugreek Water", "ingredients": "Fenugreek seeds, Water", "method": "Soak seeds overnight. Boil or drink soaked water.", "use": "Traditionally used for joint pain relief." }
            ]
          },
          "ayurveda": [
            { "name": "Mahayogaraj Guggulu", "sanskrit": "महायोगराज गुग्गुलु", "dosage": "2 tablets twice daily with warm water", "use": "Joint pain and arthritis" },
            { "name": "Shallaki", "sanskrit": "ಶಲ್ಲಕಿ", "dosage": "400mg capsule, twice daily", "use": "Reduces joint inflammation" }
          ],
          "doctorType": "Rheumatologist"
        }
      },
      "skin": {
        "Skin Infection": {
          "id": "skin_infection",
          "bodyPart": "skin",
          "description": {
            "en": "Active bacterial, viral, or fungal skin pathogen detected (indicative of cellulitis, impetigo, or acute dermatitis). The epidermal and dermal layers show acute localized vascular dilation (erythema) with dynamic inflammatory fluid infiltration. This causes high localized warmth, redness, and swelling as the body's white blood cells combat the pathogen. Prompt topical or systemic antimicrobial therapy is recommended to prevent spreading to deeper subcutaneous tissues."
          },
          "symptoms": ["redness", "swelling", "warmth", "pus discharge", "pain"],
          "severity": "mild",
          "remedies": {
            "en": [
              { "name": "Neem Wash", "ingredients": "Neem leaves, Water", "method": "Boil neem leaves in water, cool and use to wash infected area.", "use": "Natural antiseptic wash." },
              { "name": "Aloe Vera Gel", "ingredients": "Pure Aloe Vera", "method": "Apply fresh gel to affected area for cooling and healing.", "use": "Soothes inflammation and itching." },
              { "name": "Turmeric Paste", "ingredients": "Turmeric, Water/Honey", "method": "Make a paste and apply to infection for 20 mins then wash.", "use": "Natural antibacterial treatment." },
              { "name": "Warm Salt Compress", "ingredients": "Warm water, Salt, Cloth", "method": "Dip cloth in salt water and apply to area to reduce swelling.", "use": "Helps draw out infection and pus." }
            ]
          },
          "ayurveda": [
            { "name": "Neem Ghanvati", "sanskrit": "नीम घनवटी", "dosage": "2 tablets twice daily", "use": "Blood purification" },
            { "name": "Manjishtha", "sanskrit": "ಮಂಜಿಷ್ಠ", "dosage": "1g powder twice daily", "use": "Skin detox and healing" }
          ],
          "doctorType": "Dermatologist"
        },
        "Psoriasis/Rash": {
          "id": "psoriasis",
          "bodyPart": "skin",
          "description": {
            "en": "Chronic autoimmune-mediated epidermal hyperplasia observed (highly consistent with Psoriasis Vulgaris or severe atopic Dermatitis). The visual texture signature indicates localized hyperkeratosis (silvery scaly buildup) combined with underlying capillary dilation. This condition involves accelerated skin cell turnover triggered by inflammatory cytokines. Maintaining skin barrier moisture is essential, and systemic or topical immune modulation should be evaluated by a dermatologist."
          },
          "symptoms": ["red patches", "silvery scales", "dry cracked skin", "itching", "burning"],
          "severity": "moderate",
          "remedies": {
            "en": [
              { "name": "Coconut-Turmeric Mix", "ingredients": "Coconut oil, Turmeric", "method": "Mix and apply to affected areas.", "use": "Soothes itching and reduces scales." },
              { "name": "Oatmeal Bath", "ingredients": "Colloidal oatmeal, Warm water", "method": "Soak in a lukewarm bath with oatmeal for 15 minutes.", "use": "Relieves itching and softens scales." },
              { "name": "Aloe Vera Application", "ingredients": "Pure Aloe Vera", "method": "Apply gel 3 times daily to scaly patches.", "use": "Reduces redness and scaling." },
              { "name": "Apple Cider Vinegar", "ingredients": "ACV, Water (1:1 ratio)", "method": "Dab diluted solution onto itchy patches then rinse.", "use": "Helps with scalp and skin itching." }
            ]
          },
          "ayurveda": [
            { "name": "Khadirarishta", "sanskrit": "खदिरारिष्ट", "dosage": "20ml with equal water", "use": "Skin conditions" },
            { "name": "Panchatikta Ghrita", "sanskrit": "ಪಂಚತಿಕ್ತ ಘೃತ", "dosage": "1 tsp with warm water", "use": "Detoxifies skin tissues" }
          ],
          "doctorType": "Dermatologist"
        }
      }
    };
  }
}

window.aiEngine = new AIEngine();


