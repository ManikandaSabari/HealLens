package com.heallens.android.data.repository

import android.util.Log
import com.heallens.android.data.model.AppointmentModel
import com.heallens.android.data.model.NearbyProvider
import com.heallens.android.data.remote.SupabaseAuthService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AppointmentRepository {
    private const val TAG = "AppointmentRepo"

    private val _appointments = MutableStateFlow<List<AppointmentModel>>(emptyList())
    val appointments: StateFlow<List<AppointmentModel>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Source of Truth Doctor Database matching PDD website (pdd/js/appointment.js)
    val websiteDoctorsDatabase = mapOf(
        "Pulmonologist" to listOf(
            NearbyProvider(
                id = "doc-pulmo-1",
                name = "Dr. Arvind Swamy",
                facilityType = "Apollo Respiratory Clinic",
                specialization = "Pulmonologist",
                address = "Greams Road, Thousand Lights, Chennai",
                phone = "+914428290200",
                website = "https://www.apollohospitals.com",
                rating = "4.9⭐",
                lat = 13.0604,
                lon = 80.2496,
                distanceKm = 1.2
            ),
            NearbyProvider(
                id = "doc-pulmo-2",
                name = "Dr. Sarah Mathew",
                facilityType = "Metro Chest Center",
                specialization = "Pulmonologist",
                address = "Anna Salai, Teynampet, Chennai",
                phone = "+914442002288",
                website = "https://www.metrohealth.in",
                rating = "4.8⭐",
                lat = 13.0402,
                lon = 80.2501,
                distanceKm = 2.4
            )
        ),
        "Orthopedic Surgeon" to listOf(
            NearbyProvider(
                id = "doc-ortho-1",
                name = "Dr. Rajesh Kumar",
                facilityType = "Fortis Bone & Joint Hospital",
                specialization = "Orthopedic Surgeon",
                address = "Arcot Road, Vadapalani, Chennai",
                phone = "+914449211000",
                website = "https://www.fortishealthcare.com",
                rating = "4.9⭐",
                lat = 13.0500,
                lon = 80.2120,
                distanceKm = 1.8
            ),
            NearbyProvider(
                id = "doc-ortho-2",
                name = "Dr. Amanda Ross",
                facilityType = "Orthocare Specialty Clinic",
                specialization = "Orthopedic Surgeon",
                address = "Poonamallee High Road, Kilpauk, Chennai",
                phone = "+914426411234",
                website = "https://www.orthocareclinic.in",
                rating = "4.7⭐",
                lat = 13.0780,
                lon = 80.2410,
                distanceKm = 3.1
            )
        ),
        "Rheumatologist" to listOf(
            NearbyProvider(
                id = "doc-rheum-1",
                name = "Dr. Priya Sharma",
                facilityType = "Care Arthritis Institute",
                specialization = "Rheumatologist",
                address = "Nungambakkam High Road, Chennai",
                phone = "+914428331111",
                website = "https://www.carearthritis.org",
                rating = "4.8⭐",
                lat = 13.0620,
                lon = 80.2400,
                distanceKm = 1.5
            ),
            NearbyProvider(
                id = "doc-rheum-2",
                name = "Dr. Katherine Lee",
                facilityType = "Global Joint & Immunology Care",
                specialization = "Rheumatologist",
                address = "Perumbakkam Main Road, Chennai",
                phone = "+914444777000",
                website = "https://www.gleneaglesglobalhospitals.com",
                rating = "4.6⭐",
                lat = 12.8990,
                lon = 80.2090,
                distanceKm = 4.2
            )
        ),
        "Dermatologist" to listOf(
            NearbyProvider(
                id = "doc-derma-1",
                name = "Dr. Divya Patel",
                facilityType = "DermaGlow Skin Hospital",
                specialization = "Dermatologist",
                address = "Khader Nawaz Khan Road, Nungambakkam, Chennai",
                phone = "+914443999888",
                website = "https://www.dermaglowskin.in",
                rating = "4.8⭐",
                lat = 13.0610,
                lon = 80.2480,
                distanceKm = 1.1
            ),
            NearbyProvider(
                id = "doc-derma-2",
                name = "Dr. Michael Chang",
                facilityType = "Advanced Skin Clinic",
                specialization = "Dermatologist",
                address = "Velachery Main Road, Chennai",
                phone = "+914422435555",
                website = "https://www.advancedskinclinic.in",
                rating = "4.9⭐",
                lat = 12.9800,
                lon = 80.2200,
                distanceKm = 2.8
            )
        ),
        "Endocrinologist" to listOf(
            NearbyProvider(
                id = "doc-endo-1",
                name = "Dr. Sunita Rao",
                facilityType = "Endocrine & Diabetes Care Center",
                specialization = "Endocrinologist",
                address = "MCTM School Road, Alwarpet, Chennai",
                phone = "+914424991122",
                website = "https://www.endocrinediabetes.in",
                rating = "4.9⭐",
                lat = 13.0330,
                lon = 80.2520,
                distanceKm = 1.6
            ),
            NearbyProvider(
                id = "doc-endo-2",
                name = "Dr. K. Raghavan",
                facilityType = "City General Hospital",
                specialization = "Endocrinologist",
                address = "Ezhilagam, Chepauk, Chennai",
                phone = "+914425363000",
                website = "https://www.citygeneralhospital.org",
                rating = "4.9⭐",
                lat = 13.0670,
                lon = 80.2830,
                distanceKm = 2.9
            )
        ),
        "Cardiologist" to listOf(
            NearbyProvider(
                id = "doc-cardio-1",
                name = "Dr. Vikram Sethi",
                facilityType = "Metro Heart Institute",
                specialization = "Cardiologist",
                address = "Jawaharlal Nehru Road, Vadapalani, Chennai",
                phone = "+914424726000",
                website = "https://www.metroheartinstitute.com",
                rating = "4.9⭐",
                lat = 13.0510,
                lon = 80.2110,
                distanceKm = 1.9
            )
        ),
        "Hepatologist" to listOf(
            NearbyProvider(
                id = "doc-hep-1",
                name = "Dr. Ananya Roy",
                facilityType = "Liver & Digestive Care Clinic",
                specialization = "Hepatologist",
                address = "Spurtank Road, Chetpet, Chennai",
                phone = "+914428362244",
                website = "https://www.liverdigestivecare.in",
                rating = "4.8⭐",
                lat = 13.0720,
                lon = 80.2380,
                distanceKm = 2.1
            )
        ),
        "Nephrologist" to listOf(
            NearbyProvider(
                id = "doc-neph-1",
                name = "Dr. Ramesh Gupta",
                facilityType = "Kidney Health Specialty Center",
                specialization = "Nephrologist",
                address = "Sterling Road, Nungambakkam, Chennai",
                phone = "+914428271199",
                website = "https://www.kidneyhealthcenter.org",
                rating = "4.8⭐",
                lat = 13.0680,
                lon = 80.2420,
                distanceKm = 1.7
            )
        ),
        "Hematologist" to listOf(
            NearbyProvider(
                id = "doc-hem-1",
                name = "Dr. Meera Nambiar",
                facilityType = "Blood Health & Hematology Clinic",
                specialization = "Hematologist",
                address = "Harrington Road, Chetpet, Chennai",
                phone = "+914428364455",
                website = "https://www.bloodhealthclinic.in",
                rating = "4.7⭐",
                lat = 13.0750,
                lon = 80.2350,
                distanceKm = 2.3
            )
        ),
        "General Physician" to listOf(
            NearbyProvider(
                id = "doc-gp-1",
                name = "Dr. K. Raghavan",
                facilityType = "City General Hospital",
                specialization = "General Physician",
                address = "Ezhilagam, Chepauk, Chennai",
                phone = "+914425363000",
                website = "https://www.citygeneralhospital.org",
                rating = "4.9⭐",
                lat = 13.0670,
                lon = 80.2830,
                distanceKm = 1.0
            ),
            NearbyProvider(
                id = "doc-gp-2",
                name = "Dr. Jessica Taylor",
                facilityType = "Care First Family Clinic",
                specialization = "General Physician",
                address = "Luz Church Road, Mylapore, Chennai",
                phone = "+914424982211",
                website = "https://www.carefirstfamily.in",
                rating = "4.7⭐",
                lat = 13.0350,
                lon = 80.2670,
                distanceKm = 2.0
            )
        )
    )

    val currentUserId: String
        get() = SupabaseAuthService.client.auth.currentUserOrNull()?.id ?: "local_user"

    init {
        CoroutineScope(Dispatchers.IO).launch {
            fetchRemoteAppointments()
        }
    }

    suspend fun fetchNearbyProviders(
        specialty: String,
        lat: Double = 13.0827,
        lon: Double = 80.2707,
        locationName: String = "Chennai, TN"
    ): List<NearbyProvider> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        // Website Source Parity First: Return exact website doctor list matching specialty immediately
        val exactMatches = websiteDoctorsDatabase[specialty] ?: websiteDoctorsDatabase["General Physician"] ?: emptyList()
        _isLoading.value = false
        return@withContext exactMatches
    }

    suspend fun searchLocationByName(queryStr: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (queryStr.isBlank()) return@withContext null
        try {
            val encoded = java.net.URLEncoder.encode(queryStr.trim(), "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&limit=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "HealLens-AndroidHealthApp/1.0")

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val arr = JSONArray(responseStr)
                if (arr.length() > 0) {
                    val item = arr.getJSONObject(0)
                    val lat = item.getString("lat").toDouble()
                    val lon = item.getString("lon").toDouble()
                    return@withContext Pair(lat, lon)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Nominatim location search error: ${e.message}")
        }
        return@withContext null
    }

    suspend fun fetchRemoteAppointments(): List<AppointmentModel> = withContext(Dispatchers.IO) {
        try {
            val client = SupabaseAuthService.client
            val userId = currentUserId
            val session = client.auth.currentSessionOrNull()
            if (session != null && userId != "local_user") {
                val list = client.from("appointments").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<AppointmentModel>()
                _appointments.value = list
                Log.d(TAG, "Successfully fetched ${list.size} appointments from Supabase")
                return@withContext list
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fetch appointments from Supabase notice: ${e.message}")
        }
        return@withContext _appointments.value
    }

    suspend fun saveAppointment(appointment: AppointmentModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = SupabaseAuthService.client
            val userId = currentUserId
            val session = client.auth.currentSessionOrNull()
            if (session != null && userId != "local_user") {
                val payload = appointment.copy(userId = userId)
                client.from("appointments").insert(payload)
                Log.d(TAG, "Successfully saved appointment ${appointment.bookingReference} to Supabase")

                val currentList = _appointments.value.toMutableList()
                currentList.removeAll { it.id == appointment.id || (it.bookingReference.isNotBlank() && it.bookingReference == appointment.bookingReference) }
                currentList.add(0, payload)
                _appointments.value = currentList
                return@withContext true
            } else {
                Log.w(TAG, "User session unavailable, cannot save to Supabase")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save appointment to Supabase: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun deleteAppointment(appointmentId: String) {
        withContext(Dispatchers.IO) {
            val currentList = _appointments.value.toMutableList()
            currentList.removeAll { it.id == appointmentId || it.bookingReference == appointmentId }
            _appointments.value = currentList

            try {
                val client = SupabaseAuthService.client
                val userId = currentUserId
                val session = client.auth.currentSessionOrNull()
                if (session != null && userId != "local_user") {
                    client.from("appointments").delete {
                        filter {
                            eq("id", appointmentId)
                            eq("user_id", userId)
                        }
                    }
                    Log.d(TAG, "Successfully deleted appointment $appointmentId from Supabase")
                }
                Unit
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete appointment from Supabase: ${e.message}", e)
            }
        }
    }
}
