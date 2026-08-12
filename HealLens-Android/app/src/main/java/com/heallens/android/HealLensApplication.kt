package com.heallens.android

import android.app.Application
import com.heallens.android.data.repository.ClinicalHistoryRepository
import com.heallens.android.data.repository.EmergencyContactRepository
import com.heallens.android.utils.LanguageManager

class HealLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ClinicalHistoryRepository.init(this)
        EmergencyContactRepository.init(this)
        LanguageManager.init(this)
    }
}
