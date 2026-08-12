package com.heallens.android.utils

import android.content.Context
import com.heallens.android.data.local.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String
) {
    ENGLISH("en", "English", "English", "🇬🇧"),
    HINDI("hi", "Hindi", "हिंदी", "🇮🇳"),
    TAMIL("ta", "Tamil", "தமிழ்", "🇮🇳"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", "🇮🇳");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "hi" -> HINDI
                "ta" -> TAMIL
                "kn" -> KANNADA
                else -> ENGLISH
            }
        }
    }
}

object LanguageManager {

    private val _currentLanguageFlow = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguageFlow: StateFlow<AppLanguage> = _currentLanguageFlow.asStateFlow()

    val currentLanguage: AppLanguage
        get() = _currentLanguageFlow.value

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val dataStoreManager = DataStoreManager(context.applicationContext)
        CoroutineScope(Dispatchers.Main).launch {
            dataStoreManager.languageFlow.collectLatest { langCode ->
                _currentLanguageFlow.value = AppLanguage.fromCode(langCode)
            }
        }
    }

    fun setLanguage(language: AppLanguage, context: Context) {
        _currentLanguageFlow.value = language
        val dataStoreManager = DataStoreManager(context.applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.saveLanguage(language.code)
        }
    }
}
