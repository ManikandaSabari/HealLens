package com.heallens.android.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.heallens.android.data.remote.SupabaseAuthService
import com.heallens.android.model.ClinicalRecord
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object FlexibleNullableStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleNullableStringSerializer", PrimitiveKind.STRING).nullable

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }

    override fun deserialize(decoder: Decoder): String? {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            return when {
                element is JsonNull -> null
                element is JsonPrimitive -> element.content
                else -> element.toString()
            }
        }
        return try {
            decoder.decodeString()
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object FlexibleNullableIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleNullableIntSerializer", PrimitiveKind.INT).nullable

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeInt(value)
        }
    }

    override fun deserialize(decoder: Decoder): Int? {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            return when {
                element is JsonNull -> null
                element is JsonPrimitive -> {
                    element.intOrNull ?: element.content.toIntOrNull()
                }
                else -> null
            }
        }
        return try {
            decoder.decodeInt()
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class ClinicalRecordInsertDto(
    @SerialName("user_id")
    val user_id: String,
    @SerialName("Name")
    val Name: String,
    @SerialName("Age")
    @Serializable(with = FlexibleNullableIntSerializer::class)
    val Age: Int? = 30,
    @SerialName("Gender")
    val Gender: String = "Unknown",
    @SerialName("analysis_type")
    val analysis_type: String,
    @SerialName("Category")
    val Category: String = "General",
    @SerialName("Prediction")
    val Prediction: String = "Analysis Complete",
    @SerialName("Confidence")
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val Confidence: String? = "100",
    @SerialName("Severity")
    val Severity: String = "normal",
    @SerialName("Symptoms")
    val Symptoms: String = "",
    @SerialName("Recommendations")
    val Recommendations: String = ""
)

@Serializable
data class ClinicalRecordDto(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("created_at")
    val created_at: String? = null,
    @SerialName("user_id")
    val user_id: String,
    @SerialName("Name")
    val Name: String? = "Self",
    @SerialName("Age")
    @Serializable(with = FlexibleNullableIntSerializer::class)
    val Age: Int? = 30,
    @SerialName("Gender")
    val Gender: String? = "Unknown",
    @SerialName("analysis_type")
    val analysis_type: String? = null,
    @SerialName("Category")
    val Category: String? = "General",
    @SerialName("Prediction")
    val Prediction: String? = "Analysis Complete",
    @SerialName("Confidence")
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val Confidence: String? = "90",
    @SerialName("Severity")
    val Severity: String? = "normal",
    @SerialName("Symptoms")
    val Symptoms: String? = "",
    @SerialName("Recommendations")
    val Recommendations: String? = ""
)

object ClinicalHistoryRepository {

    private const val TAG = "ClinicalHistory"
    private const val PREFS_NAME = "heallens_clinical_history_prefs"
    private const val KEY_PREFIX = "user_history_"

    private var prefs: SharedPreferences? = null
    private val supabaseAuthService by lazy { SupabaseAuthService() }
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val userRecordsMap = mutableMapOf<String, MutableList<ClinicalRecord>>()

    private val _recordsFlow = MutableStateFlow<List<ClinicalRecord>>(emptyList())
    val recordsFlow: StateFlow<List<ClinicalRecord>> = _recordsFlow.asStateFlow()

    private var _currentUserId: String = ""
    val currentUserId: String
        get() = getOrFetchCurrentUserId()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d(TAG, "[ClinicalHistory] Initialized SharedPreferences repository context")
        }
    }

    private fun isUuid(str: String): Boolean {
        return str.isNotEmpty() && str.contains("-") && str.length >= 32
    }

    private fun getOrFetchCurrentUserId(): String {
        if (_currentUserId.isNotEmpty() && isUuid(_currentUserId)) {
            return _currentUserId
        }
        val supaUser = try {
            supabaseAuthService.client.auth.currentUserOrNull()
        } catch (e: Exception) {
            null
        }

        if (supaUser != null && isUuid(supaUser.id)) {
            _currentUserId = supaUser.id
            Log.d(TAG, "[ClinicalHistory] Current Supabase user UUID: ${supaUser.id}")
            return supaUser.id
        }

        Log.w(TAG, "[ClinicalHistory] WARNING: No active user session UUID identified yet")
        return ""
    }

    @Synchronized
    fun setCurrentUser(userId: String) {
        val trimmedInput = userId.trim()
        if (trimmedInput.isEmpty()) {
            _currentUserId = ""
            userRecordsMap.clear()
            _recordsFlow.value = emptyList()
            return
        }

        val supaUser = try {
            supabaseAuthService.client.auth.currentUserOrNull()
        } catch (e: Exception) {
            null
        }

        val canonicalUserId = if (supaUser != null && isUuid(supaUser.id)) {
            supaUser.id
        } else if (isUuid(trimmedInput)) {
            trimmedInput
        } else {
            ""
        }

        Log.d(TAG, "[ClinicalHistory] setCurrentUser called with input = '$userId', canonicalUserId resolved = '$canonicalUserId' (previous = '$_currentUserId')")

        if (canonicalUserId.isEmpty()) {
            userRecordsMap.clear()
            _recordsFlow.value = emptyList()
            return
        }

        if (canonicalUserId != _currentUserId) {
            _currentUserId = canonicalUserId
            _recordsFlow.value = emptyList()
        }

        // 1. Load local cache immediately for fast UI rendering
        val localRecords = userRecordsMap.getOrPut(canonicalUserId) {
            loadRecordsFromStorage(canonicalUserId).toMutableList()
        }
        val userFiltered = localRecords.filter { it.userId == canonicalUserId }
        _recordsFlow.value = userFiltered
        Log.d(TAG, "[ClinicalHistory] Local cache loaded: ${userFiltered.size} records for user = $canonicalUserId")

        // 2. Fetch remote Supabase history asynchronously and merge
        fetchRemoteHistoryAsync(canonicalUserId)
    }

    private fun fetchRemoteHistoryAsync(userId: String) {
        if (userId.isEmpty()) return
        repositoryScope.launch {
            try {
                Log.d("ClinicalHistoryPersistenceDebug", "[REMOTE_HISTORY_FETCH_STARTED] userId=$userId")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] FETCH_USER_ID=$userId")
                Log.d(TAG, "[ClinicalHistory] Fetching remote history from Supabase for user: $userId")
                val dtos = supabaseAuthService.client.from("clinical_records")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("created_at", order = Order.DESCENDING)
                    }.decodeList<ClinicalRecordDto>()

                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] REMOTE_RECORD_COUNT=${dtos.size}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] REMOTE_RECORD_IDS=${dtos.mapNotNull { it.id }.joinToString(",")}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] DTO_COUNT=${dtos.size}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] DTO_IDS=${dtos.mapNotNull { it.id }.joinToString(",")}")

                val domainRecords = dtos.map { it.toDomainRecord() }.filter { it.userId == userId }

                Log.d("ClinicalHistoryPersistenceDebug", "[REMOTE_HISTORY_FETCH_SUCCESS] count=${domainRecords.size}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] DOMAIN_COUNT=${domainRecords.size}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] DOMAIN_IDS=${domainRecords.map { it.id }.joinToString(",")}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] FILTERED_COUNT=${domainRecords.size}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] FILTERED_IDS=${domainRecords.map { it.id }.joinToString(",")}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] CURRENT_USER_ID=$_currentUserId")

                synchronized(this@ClinicalHistoryRepository) {
                    if (userId == _currentUserId) {
                        userRecordsMap[userId] = domainRecords.toMutableList()
                        saveRecordsToStorage(userId, domainRecords)

                        Log.d(TAG, "[ClinicalHistory] Loaded ${domainRecords.size} remote records for user $userId")
                        _recordsFlow.value = domainRecords
                    }
                }
            } catch (e: Exception) {
                Log.e("ClinicalHistoryPersistenceDebug", "[REMOTE_HISTORY_FETCH_FAILED] error=${e.message}", e)
                Log.w(TAG, "[ClinicalHistory] Remote history load failed: ${e.message}")
            }
        }
    }

    @Synchronized
    fun clearCurrentSession() {
        Log.d(TAG, "[ClinicalHistory] clearCurrentSession called, resetting session state")
        Log.d("ClinicalHistoryPersistenceDebug", "[LOGOUT_SUCCESS] Session cleared for user")
        Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] SESSION_CLEARED")
        _currentUserId = ""
        userRecordsMap.clear()
        _recordsFlow.value = emptyList()
    }

    @Synchronized
    fun addRecord(record: ClinicalRecord, targetUserId: String? = null) {
        val ownerId = (targetUserId ?: record.userId).ifEmpty { getOrFetchCurrentUserId() }
        Log.d(TAG, "[ClinicalHistory] addRecord called. Record title = '${record.title}', type = '${record.analysisType}', resolved owner = '$ownerId'")

        if (ownerId.isEmpty()) {
            Log.e(TAG, "[ClinicalHistory] ERROR: Cannot save record because owner user ID is missing/empty!")
            return
        }

        val recordWithOwner = record.copy(userId = ownerId)

        // 1. Immediately update local memory and SharedPreferences cache
        val userList = userRecordsMap.getOrPut(ownerId) {
            loadRecordsFromStorage(ownerId).toMutableList()
        }
        userList.add(0, recordWithOwner)
        saveRecordsToStorage(ownerId, userList)

        if (_currentUserId.isEmpty()) {
            _currentUserId = ownerId
        }

        if (ownerId == _currentUserId) {
            _recordsFlow.value = userList.filter { it.userId == _currentUserId }
            Log.d(TAG, "[ClinicalHistory] Local cache updated immediately with new record")
        }

        // 2. Upload clinical record to remote Supabase database
        repositoryScope.launch {
            try {
                val supaUuid = try {
                    supabaseAuthService.client.auth.currentUserOrNull()?.id
                } catch (e: Exception) {
                    null
                }
                val validSupabaseUserId = (supaUuid ?: if (ownerId.contains("-") && ownerId.length >= 32) ownerId else _currentUserId).trim()
                val insertDto = recordWithOwner.toInsertDto(validSupabaseUserId)

                Log.d("ClinicalHistoryPersistenceDebug", "[INSERT_STARTED] AUTH_USER_ID_BEFORE_INSERT=$validSupabaseUserId")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] recordId=${recordWithOwner.id}")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] authenticatedUserId=$validSupabaseUserId")
                val insertedDtos = supabaseAuthService.client.from("clinical_records")
                    .insert(insertDto) { select() }
                    .decodeList<ClinicalRecordDto>()

                val isFound = insertedDtos.isNotEmpty()
                Log.d("ClinicalHistoryPersistenceDebug", "[INSERT_SUCCESS] AFTER_INSERT_REMOTE_FOUND=$isFound")
                Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] AFTER_INSERT_REMOTE_FOUND=$isFound")

                if (isFound) {
                    val remoteDto = insertedDtos.first()
                    Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] remote.id=${remoteDto.id}")
                    Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] remote.user_id=${remoteDto.user_id}")
                    Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] remote.created_at=${remoteDto.created_at}")
                    Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] remote.analysis_type=${remoteDto.analysis_type}")
                    Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] remote.category=${remoteDto.Category}")
                    Log.d("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] remote.prediction=${remoteDto.Prediction}")

                    val remoteRecord = remoteDto.toDomainRecord()
                    synchronized(this@ClinicalHistoryRepository) {
                        val currentList = userRecordsMap[validSupabaseUserId] ?: mutableListOf()
                        val index = currentList.indexOfFirst { it.id == recordWithOwner.id }
                        if (index >= 0) {
                            currentList[index] = remoteRecord
                        } else {
                            currentList.add(0, remoteRecord)
                        }
                        saveRecordsToStorage(validSupabaseUserId, currentList)
                        if (validSupabaseUserId == _currentUserId) {
                            _recordsFlow.value = currentList.filter { it.userId == _currentUserId }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ClinicalHistoryPersistenceDebug", "[INSERT_FAILED] error=${e.message}", e)
                Log.e("ClinicalHistoryPersistenceDebug", "[ClinicalHistoryPersistenceDebug] AFTER_INSERT_REMOTE_FOUND=false error=${e.message}")
                Log.e(TAG, "[ClinicalHistory] Remote insert failed, local record preserved: ${e.message}")
            }
        }
    }

    @Synchronized
    fun deleteRecord(recordId: String, targetUserId: String? = null) {
        val ownerId = (targetUserId ?: getOrFetchCurrentUserId()).ifEmpty { return }
        Log.d(TAG, "[ClinicalHistory] deleteRecord called for recordId = '$recordId', owner = '$ownerId'")

        val userList = userRecordsMap.getOrPut(ownerId) {
            loadRecordsFromStorage(ownerId).toMutableList()
        }
        val targetIndex = userList.indexOfFirst { it.id == recordId }
        if (targetIndex >= 0) {
            userList.removeAt(targetIndex)
            saveRecordsToStorage(ownerId, userList)
            if (ownerId == _currentUserId) {
                _recordsFlow.value = userList.filter { it.userId == _currentUserId }
            }
        }

        // Delete from remote Supabase table
        repositoryScope.launch {
            try {
                val dbId = recordId.toLongOrNull()
                if (dbId != null) {
                    supabaseAuthService.client.from("clinical_records")
                        .delete {
                            filter {
                                eq("id", dbId)
                                eq("user_id", ownerId)
                            }
                        }
                    Log.d(TAG, "[ClinicalHistory] Deleted record $dbId from remote Supabase database")
                }
            } catch (e: Exception) {
                Log.w(TAG, "[ClinicalHistory] Remote deletion failed: ${e.message}")
            }
        }
    }

    private fun loadRecordsFromStorage(userId: String): List<ClinicalRecord> {
        val p = prefs
        if (p == null) {
            Log.w(TAG, "[ClinicalHistory] WARNING: SharedPreferences is null during loadRecordsFromStorage")
            return emptyList()
        }

        val jsonStr = p.getString("$KEY_PREFIX$userId", null)
        if (jsonStr.isNullOrEmpty()) {
            Log.d(TAG, "[ClinicalHistory] No stored records found for key: $KEY_PREFIX$userId")
            return emptyList()
        }

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ClinicalRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val rec = obj.toClinicalRecord()
                if (rec.userId == userId) {
                    list.add(rec)
                }
            }
            Log.d(TAG, "[ClinicalHistory] Successfully loaded ${list.size} records from storage key $KEY_PREFIX$userId")
            list
        } catch (e: Exception) {
            Log.e(TAG, "[ClinicalHistory] Failed to parse JSON records for key $KEY_PREFIX$userId: ${e.message}")
            emptyList()
        }
    }

    private fun saveRecordsToStorage(userId: String, records: List<ClinicalRecord>) {
        val p = prefs
        if (p == null) {
            Log.w(TAG, "[ClinicalHistory] WARNING: SharedPreferences is null during saveRecordsToStorage")
            return
        }

        val jsonArray = JSONArray()
        records.filter { it.userId == userId }.forEach { jsonArray.put(it.toJson()) }
        val key = "$KEY_PREFIX$userId"
        p.edit().putString(key, jsonArray.toString()).apply()
        Log.d(TAG, "[ClinicalHistory] Persisted ${jsonArray.length()} records to SharedPreferences key = '$key'")
    }

    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}

private fun normalizeConfidenceForDatabase(value: String?): String {
    return value
        ?.replace("%", "")
        ?.trim()
        ?.ifEmpty { "100" }
        ?: "100"
}

private fun ClinicalRecord.toInsertDto(supabaseUserId: String): ClinicalRecordInsertDto {
    val typeMapped = if (analysisType.equals("report", ignoreCase = true)) "Report" else "Image"
    val resolvedName = if (patientName.isNullOrBlank()) "Self" else patientName.trim()
    val resolvedCategory = if (typeMapped == "Report") "Blood Biomarker Report" else if (category.isNullOrBlank()) "General" else category.trim()
    val resolvedPrediction = if (prediction.isNullOrBlank()) (title.ifEmpty { "Analysis Complete" }) else prediction.trim()

    return ClinicalRecordInsertDto(
        user_id = supabaseUserId,
        Name = resolvedName,
        Age = patientAge ?: 30,
        Gender = if (patientGender.isNullOrBlank()) "Unknown" else patientGender.trim(),
        analysis_type = typeMapped,
        Category = resolvedCategory,
        Prediction = resolvedPrediction,
        Confidence = normalizeConfidenceForDatabase(confidence),
        Severity = if (severity.contains("critical", ignoreCase = true)) "critical" else if (severity.contains("normal", ignoreCase = true) || severity.contains("healthy", ignoreCase = true)) "normal" else "moderate",
        Symptoms = description ?: "",
        Recommendations = remedies.joinToString("; ")
    )
}

private fun ClinicalRecord.toDto(supabaseUserId: String): ClinicalRecordDto {
    val typeMapped = if (analysisType.equals("report", ignoreCase = true)) "Report" else "Image"
    val resolvedName = if (patientName.isNullOrBlank()) "Self" else patientName.trim()
    val resolvedCategory = if (typeMapped == "Report") "Blood Biomarker Report" else if (category.isNullOrBlank()) "General" else category.trim()
    val resolvedPrediction = if (prediction.isNullOrBlank()) (title.ifEmpty { "Analysis Complete" }) else prediction.trim()

    return ClinicalRecordDto(
        user_id = supabaseUserId,
        Name = resolvedName,
        Age = patientAge ?: 30,
        Gender = if (patientGender.isNullOrBlank()) "Unknown" else patientGender.trim(),
        analysis_type = typeMapped,
        Category = resolvedCategory,
        Prediction = resolvedPrediction,
        Confidence = normalizeConfidenceForDatabase(confidence),
        Severity = if (severity.contains("critical", ignoreCase = true)) "critical" else if (severity.contains("normal", ignoreCase = true) || severity.contains("healthy", ignoreCase = true)) "normal" else "moderate",
        Symptoms = description ?: "",
        Recommendations = remedies.joinToString("; ")
    )
}

private fun ClinicalRecordDto.toDomainRecord(): ClinicalRecord {
    val recordId = id?.toString() ?: java.util.UUID.randomUUID().toString()
    val isReport = analysis_type.equals("Report", ignoreCase = true) || analysis_type.equals("Report Analysis", ignoreCase = true)

    val mappedType = if (isReport) "report" else "image"
    val defaultTitle = if (isReport) (Category ?: "Blood Biomarker Report") else (Prediction ?: "AI Skin Scan")
    val mappedColorHex = when {
        Severity?.contains("critical", ignoreCase = true) == true -> "#EF4444"
        Severity?.contains("moderate", ignoreCase = true) == true || Severity?.contains("mild", ignoreCase = true) == true -> "#F59E0B"
        else -> "#10B981"
    }

    val formattedDate = formatDateString(created_at)

    val remediesList = if (!Recommendations.isNullOrBlank()) {
        Recommendations.split(";").map { it.trim() }.filter { it.isNotEmpty() }
    } else {
        emptyList()
    }

    return ClinicalRecord(
        id = recordId,
        userId = user_id,
        title = defaultTitle,
        date = formattedDate,
        patientName = Name ?: "Self",
        patientAge = Age,
        patientGender = Gender,
        analysisType = mappedType,
        category = Category ?: "General",
        prediction = Prediction ?: "Analysis Complete",
        severity = Severity ?: "NORMAL",
        severityColorHex = mappedColorHex,
        confidence = Confidence ?: "94%",
        description = Symptoms ?: "",
        remedies = remediesList
    )
}

private fun formatDateString(createdAtStr: String?): String {
    if (createdAtStr.isNullOrBlank()) {
        return ClinicalHistoryRepository.getCurrentTimestamp()
    }

    if (createdAtStr.contains(",") && (createdAtStr.contains("am", ignoreCase = true) || createdAtStr.contains("pm", ignoreCase = true))) {
        return createdAtStr
    }

    val formats = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    )

    val cleanInput = if (createdAtStr.contains(".")) {
        val dotIndex = createdAtStr.indexOf(".")
        val tzPlus = createdAtStr.indexOf("+", dotIndex)
        val tzMinus = if (tzPlus < 0) createdAtStr.indexOf("-", dotIndex) else tzPlus
        val tzIndex = if (tzMinus < 0) createdAtStr.indexOf("Z", dotIndex) else tzMinus
        if (tzIndex > dotIndex) {
            val millisPart = createdAtStr.substring(dotIndex + 1, tzIndex).take(3)
            createdAtStr.substring(0, dotIndex + 1) + millisPart + createdAtStr.substring(tzIndex)
        } else {
            createdAtStr
        }
    } else {
        createdAtStr
    }

    val outputSdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val parsedDate = sdf.parse(cleanInput) ?: sdf.parse(createdAtStr)
            if (parsedDate != null) {
                return outputSdf.format(parsedDate)
            }
        } catch (e: Exception) {
            // Continue testing formats
        }
    }

    return createdAtStr
}

private fun ClinicalRecord.toJson(): JSONObject {
    val json = JSONObject()
    json.put("id", id)
    json.put("userId", userId)
    json.put("title", title)
    json.put("date", date)
    json.put("patientName", patientName)
    json.put("analysisType", analysisType)
    json.put("category", category)
    json.put("prediction", prediction)
    json.put("severity", severity)
    json.put("severityColorHex", severityColorHex)
    json.put("confidence", confidence)
    json.put("description", description)
    val remediesArray = JSONArray()
    remedies.forEach { remediesArray.put(it) }
    json.put("remedies", remediesArray)
    return json
}

private fun JSONObject.toClinicalRecord(): ClinicalRecord {
    val remediesList = mutableListOf<String>()
    val remediesArray = optJSONArray("remedies")
    if (remediesArray != null) {
        for (i in 0 until remediesArray.length()) {
            remediesList.add(remediesArray.getString(i))
        }
    }
    return ClinicalRecord(
        id = optString("id", java.util.UUID.randomUUID().toString()),
        userId = optString("userId", ""),
        title = optString("title", ""),
        date = optString("date", ""),
        patientName = optString("patientName", "Self"),
        analysisType = optString("analysisType", "image"),
        category = optString("category", "General"),
        prediction = optString("prediction", ""),
        severity = optString("severity", "NORMAL"),
        severityColorHex = optString("severityColorHex", "#00D4FF"),
        confidence = optString("confidence", "94%"),
        description = optString("description", ""),
        remedies = remediesList
    )
}
