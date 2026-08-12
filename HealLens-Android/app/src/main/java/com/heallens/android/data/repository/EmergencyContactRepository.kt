package com.heallens.android.data.repository

import android.content.Context
import android.util.Log
import com.heallens.android.data.remote.SupabaseAuthService
import com.heallens.android.model.EmergencyContact
import com.heallens.android.model.EmergencyContactDto
import com.heallens.android.model.toEmergencyContact
import com.heallens.android.model.toInsertDto
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object EmergencyContactRepository {

    private const val TAG = "EmergencyContact"
    private val supabaseAuthService by lazy { SupabaseAuthService() }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _contactsFlow = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contactsFlow: StateFlow<List<EmergencyContact>> = _contactsFlow.asStateFlow()

    private var _currentUserId: String = ""
    val currentUserId: String
        get() = getOrFetchCurrentUserId()

    fun init(context: Context) {
        Log.d(TAG, "[EmergencyContact] Initialized Supabase EmergencyContactRepository context")
        val userId = getOrFetchCurrentUserId()
        if (userId.isNotEmpty()) {
            fetchContactsFromSupabase(userId)
        }
    }

    private fun getOrFetchCurrentUserId(): String {
        if (_currentUserId.isNotEmpty()) {
            return _currentUserId
        }
        val supaUser = try {
            supabaseAuthService.client.auth.currentUserOrNull()
        } catch (e: Exception) {
            null
        }

        if (supaUser != null && supaUser.id.isNotEmpty()) {
            _currentUserId = supaUser.id
            Log.d(TAG, "[EmergencyContact] Authenticated user ID identified: ${supaUser.id}")
            return supaUser.id
        }

        Log.w(TAG, "[EmergencyContact] WARNING: No active authenticated Supabase user session identified yet")
        return ""
    }

    private fun getOrFetchCurrentPatientName(): String {
        return try {
            val supaUser = supabaseAuthService.client.auth.currentUserOrNull()
            val nameObj = supaUser?.userMetadata?.get("full_name")
            val nameStr = nameObj?.toString()?.replace("\"", "")?.trim()
            if (!nameStr.isNullOrEmpty()) nameStr else "Self"
        } catch (e: Exception) {
            "Self"
        }
    }

    @Synchronized
    fun setCurrentUser(userId: String) {
        val cleanUserId = userId.trim()
        Log.d(TAG, "[EmergencyContact] setCurrentUser called with: '$cleanUserId' (previous: '$_currentUserId')")

        if (cleanUserId != _currentUserId) {
            _currentUserId = cleanUserId
            _contactsFlow.value = emptyList()
        }

        if (cleanUserId.isEmpty()) {
            Log.d(TAG, "[EmergencyContact] Cleared contactsFlow because userId is empty")
            return
        }

        fetchContactsFromSupabase(cleanUserId)
    }

    @Synchronized
    fun clearCurrentSession() {
        Log.d(TAG, "[EmergencyContact] clearCurrentSession called, resetting active user session")
        _currentUserId = ""
        _contactsFlow.value = emptyList()
    }

    fun fetchContactsFromSupabase(userId: String = getOrFetchCurrentUserId()) {
        val targetUserId = userId.ifEmpty { getOrFetchCurrentUserId() }
        if (targetUserId.isEmpty()) {
            Log.w(TAG, "[EmergencyContact] Cannot fetch contacts: authenticated user ID is empty")
            return
        }

        repositoryScope.launch {
            try {
                Log.d(TAG, "[EmergencyContact] Fetching Emergency SOS contacts from Supabase for userId=$targetUserId")
                val dtos = supabaseAuthService.client.from("emergency_contacts")
                    .select {
                        filter {
                            eq("user_id", targetUserId)
                        }
                        order("created_at", order = Order.DESCENDING)
                    }.decodeList<EmergencyContactDto>()

                val contacts = dtos.map { it.toEmergencyContact() }
                _contactsFlow.value = contacts
                Log.d(TAG, "[EmergencyContact] Successfully loaded ${contacts.size} contacts from Supabase for userId=$targetUserId")
            } catch (e: Exception) {
                Log.e(TAG, "[EmergencyContact] SOS Supabase query failed for userId=$targetUserId: ${e.message}")
            }
        }
    }

    suspend fun addContact(contact: EmergencyContact, targetUserId: String? = null): Result<EmergencyContact> {
        return withContext(Dispatchers.IO) {
            val ownerId = (targetUserId ?: contact.userId).ifEmpty { getOrFetchCurrentUserId() }
            Log.d(TAG, "[EmergencyContact] addContact called for ownerId=$ownerId")

            if (ownerId.isEmpty()) {
                val err = "Cannot add contact because owner user ID is missing/empty!"
                Log.e(TAG, "[EmergencyContact] ERROR: $err")
                return@withContext Result.failure(IllegalStateException(err))
            }

            val contactWithOwner = contact.copy(userId = ownerId)

            try {
                val patientName = getOrFetchCurrentPatientName()
                val insertDto = contactWithOwner.toInsertDto(patientName)
                val insertedDtos = supabaseAuthService.client.from("emergency_contacts")
                    .insert(insertDto) { select() }
                    .decodeList<EmergencyContactDto>()

                Log.d(TAG, "[EmergencyContact] SOS Supabase insert successful for userId=$ownerId, count=${insertedDtos.size}")
                val createdContact = if (insertedDtos.isNotEmpty()) insertedDtos.first().toEmergencyContact() else contactWithOwner
                fetchContactsFromSupabase(ownerId)
                Result.success(createdContact)
            } catch (e: Exception) {
                Log.e(TAG, "[EmergencyContact] SOS Supabase insert failed for userId=$ownerId: ${e.message}")
                Result.failure(e)
            }
        }
    }

    fun deleteContact(contactId: String, targetUserId: String? = null) {
        val ownerId = (targetUserId ?: getOrFetchCurrentUserId()).ifEmpty { return }
        Log.d(TAG, "[EmergencyContact] deleteContact called for contactId=$contactId, ownerId=$ownerId")

        repositoryScope.launch {
            try {
                supabaseAuthService.client.from("emergency_contacts")
                    .delete {
                        filter {
                            eq("id", contactId)
                            eq("user_id", ownerId)
                        }
                    }

                Log.d(TAG, "[EmergencyContact] SOS Supabase delete successful for contactId=$contactId, userId=$ownerId")
                fetchContactsFromSupabase(ownerId)
            } catch (e: Exception) {
                Log.e(TAG, "[EmergencyContact] SOS Supabase delete failed for contactId=$contactId, userId=$ownerId: ${e.message}")
            }
        }
    }
}
