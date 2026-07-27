// voice.js — Web Speech API: STT + TTS
class VoiceEngine {
  constructor() {
    this.recognition = null;
    this.synthesis = window.speechSynthesis;
    this.isListening = false;
    this.onResultCallback = null;
    this.isMuted = localStorage.getItem("heallens_voice_muted") === "true";
    this.initSpeechRecognition();
    this.initMuteToggle();
  }

  initMuteToggle() {
    const btn = document.getElementById("voice-toggle-btn");
    if (btn) {
      btn.innerText = this.isMuted ? "🔇" : "🔊";
      btn.addEventListener("click", () => this.toggleMute());
    }
  }

  toggleMute() {
    this.isMuted = !this.isMuted;
    localStorage.setItem("heallens_voice_muted", this.isMuted);
    const btn = document.getElementById("voice-toggle-btn");
    if (btn) btn.innerText = this.isMuted ? "🔇" : "🔊";
    if (this.isMuted) this.synthesis.cancel();
  }

  initSpeechRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      console.warn("SpeechRecognition not supported in this browser.");
      return;
    }
    this.recognition = new SpeechRecognition();
    this.recognition.continuous = false;
    this.recognition.interimResults = true;
    this.recognition.maxAlternatives = 1;

    this.recognition.onresult = (event) => {
      let transcript = "";
      for (let i = event.resultIndex; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }
      if (event.results[event.results.length - 1].isFinal) {
        if (this.onResultCallback) this.onResultCallback(transcript.trim());
        this.stopListening();
      }
    };

    this.recognition.onerror = (e) => {
      console.error("Speech recognition error:", e.error);
      this.stopListening();
    };

    this.recognition.onend = () => {
      this.isListening = false;
      this.updateMicButton(false);
    };
  }

  startListening(lang, onResult) {
    if (!this.recognition) {
      alert("Voice input is not supported in your browser. Please use Chrome.");
      return;
    }
    this.onResultCallback = onResult;
    this.recognition.lang = lang || window.i18n?.getVoiceLang() || "en-IN";
    this.isListening = true;
    this.updateMicButton(true);
    try {
      this.recognition.start();
    } catch (e) {
      console.error("Could not start recognition:", e);
    }
  }

  stopListening() {
    if (this.recognition && this.isListening) {
      this.recognition.stop();
    }
    this.isListening = false;
    this.updateMicButton(false);
  }

  toggleListening(onResult) {
    if (this.isListening) {
      this.stopListening();
    } else {
      this.startListening(window.i18n?.getVoiceLang(), onResult);
    }
  }

  updateMicButton(active) {
    const btns = document.querySelectorAll(".mic-btn");
    btns.forEach(btn => {
      if (active) {
        btn.classList.add("listening");
        btn.innerHTML = `<span class="mic-icon">🔴</span> ${window.i18n?.t('stopListening') || 'Stop'}`;
      } else {
        btn.classList.remove("listening");
        btn.innerHTML = `<span class="mic-icon">🎙️</span> ${window.i18n?.t('speakSymptoms') || 'Speak'}`;
      }
    });
  }

  // Text-to-speech
  speak(text, lang) {
    if (!this.synthesis || this.isMuted) return;
    this.synthesis.cancel(); // stop any current speech
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang || window.i18n?.getVoiceLang() || "en-IN";
    utterance.rate = 0.9;
    utterance.pitch = 1;
    utterance.volume = 1;

    // Try to find appropriate voice
    const voices = this.synthesis.getVoices();
    const targetLang = utterance.lang.split("-")[0];
    const matchedVoice = voices.find(v => v.lang.startsWith(targetLang));
    if (matchedVoice) utterance.voice = matchedVoice;

    this.synthesis.speak(utterance);
  }

  // Build and speak diagnosis result
  speakResult(result) {
    if (!result) return;
    const lang = window.i18n?.currentLang || "en";
    let text = "";
    if (lang === "hi") {
      let concl = result.findingCategory === "Bone Fracture" ? "AI ने हड्डी के फ्रैक्चर की पहचान की है।" : (result.findingCategory ? "AI ने इसे बिना फ्रैक्चर वाली हड्डी की स्थिति के रूप में पहचाना है।" : "");
      text = `आपके स्कैन में ${result.diseaseName} का पता चला है। ${concl} गंभीरता स्तर: ${result.severity}। 
      कृपया ${result.doctorType} से मिलें।`;
    } else if (lang === "ta") {
      let concl = result.findingCategory === "Bone Fracture" ? "AI எலும்பு முறிவைக் கண்டறிந்துள்ளது." : (result.findingCategory ? "AI இது எலும்பு முறிவு இல்லாத நிலை என்று கண்டறிந்துள்ளது." : "");
      text = `உங்கள் ஸ்கேனில் ${result.diseaseName} கண்டறியப்பட்டது. ${concl} தீவிர நிலை: ${result.severity}. 
      ${result.doctorType} ஐ சந்திக்கவும்.`;
    } else if (lang === "kn") {
      let concl = result.findingCategory === "Bone Fracture" ? "AI ಮೂಳೆ ಮುರಿತವನ್ನು ಗುರುತಿಸಿದೆ." : (result.findingCategory ? "AI ಇದು ಮೂಳೆ ಮುರಿತವಲ್ಲದ ಸ್ಥಿತಿ ಎಂದು ಗುರುತಿಸಿದೆ." : "");
      text = `ನಿಮ್ಮ ಸ್ಕ್ಯಾನ್‌ನಲ್ಲಿ ${result.diseaseName} ಕಂಡುಹಿಡಿಯಲಾಗಿದೆ. ${concl} ತೀವ್ರತೆ: ${result.severity}. 
      ${result.doctorType} ಅನ್ನು ಭೇಟಿ ಮಾಡಿ.`;
    } else {
      let conclusionText = "";
      if (result.findingCategory === "Bone Fracture") {
        conclusionText = "The AI has identified a clear bone fracture.";
      } else if (result.findingCategory === "Non-Fracture Condition") {
        conclusionText = "The AI has identified this as a non-fracture bone condition.";
      }

      text = `Your scan detected ${result.diseaseName} in the ${result.bodyPartLabel}. 
      ${conclusionText}
      Severity level is ${result.severity}. Confidence is ${result.confidence}%. 
      ${result.description}. It is recommended to consult a ${result.doctorType}.`;
    }
    this.speak(text, window.i18n?.getVoiceLang());
  }

  isSupported() {
    return !!(window.SpeechRecognition || window.webkitSpeechRecognition);
  }
}

window.voiceEngine = new VoiceEngine();
