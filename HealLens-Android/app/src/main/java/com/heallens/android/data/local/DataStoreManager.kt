package com.heallens.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "heallens_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

        private val KEY_PATIENT_NAME = stringPreferencesKey("patient_name")
        private val KEY_PATIENT_AGE = stringPreferencesKey("patient_age")
        private val KEY_PATIENT_GENDER = stringPreferencesKey("patient_gender")
        private val KEY_PATIENT_BLOOD_GROUP = stringPreferencesKey("patient_blood_group")
        private val KEY_PATIENT_EMERGENCY_CONTACT = stringPreferencesKey("patient_emergency_contact")
        private val KEY_PATIENT_CONDITIONS = stringPreferencesKey("patient_conditions")
        private val KEY_PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        private val KEY_DISCLAIMER_ACCEPTED = booleanPreferencesKey("heallens_disclaimer_accepted")
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userEmailFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PREFERRED_LANGUAGE] ?: "en"
    }

    val disclaimerAcceptedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DISCLAIMER_ACCEPTED] ?: false
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_REFRESH_TOKEN]
    }

    val patientProfileFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        mapOf(
            "name" to (prefs[KEY_PATIENT_NAME] ?: ""),
            "age" to (prefs[KEY_PATIENT_AGE] ?: ""),
            "gender" to (prefs[KEY_PATIENT_GENDER] ?: "Self"),
            "bloodGroup" to (prefs[KEY_PATIENT_BLOOD_GROUP] ?: "O+"),
            "emergencyContact" to (prefs[KEY_PATIENT_EMERGENCY_CONTACT] ?: ""),
            "conditions" to (prefs[KEY_PATIENT_CONDITIONS] ?: "")
        )
    }

    suspend fun saveSession(userId: String, email: String, accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_EMAIL] = email
            accessToken?.let { prefs[KEY_ACCESS_TOKEN] = it }
            refreshToken?.let { prefs[KEY_REFRESH_TOKEN] = it }
        }
    }

    suspend fun savePatientProfile(
        name: String,
        age: String,
        gender: String,
        bloodGroup: String,
        emergencyContact: String,
        conditions: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PATIENT_NAME] = name
            prefs[KEY_PATIENT_AGE] = age
            prefs[KEY_PATIENT_GENDER] = gender
            prefs[KEY_PATIENT_BLOOD_GROUP] = bloodGroup
            prefs[KEY_PATIENT_EMERGENCY_CONTACT] = emergencyContact
            prefs[KEY_PATIENT_CONDITIONS] = conditions
        }
    }

    suspend fun saveLanguage(langCode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PREFERRED_LANGUAGE] = langCode
        }
    }

    suspend fun saveDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DISCLAIMER_ACCEPTED] = accepted
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            val disclaimerAccepted = prefs[KEY_DISCLAIMER_ACCEPTED]
            val language = prefs[KEY_PREFERRED_LANGUAGE]
            prefs.clear()
            disclaimerAccepted?.let { prefs[KEY_DISCLAIMER_ACCEPTED] = it }
            language?.let { prefs[KEY_PREFERRED_LANGUAGE] = it }
        }
    }
}

