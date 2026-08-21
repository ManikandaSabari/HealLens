package com.heallens.android.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.data.model.AppointmentModel
import com.heallens.android.data.model.NearbyProvider
import com.heallens.android.data.repository.AppointmentRepository
import com.heallens.android.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpecialistDiscoveryWidget(
    specialty: String,
    contextualNote: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentLocationName by remember { mutableStateOf("Chennai, TN") }
    var currentLat by remember { mutableStateOf(13.0827) }
    var currentLon by remember { mutableStateOf(80.2707) }
    var locationInput by remember { mutableStateOf("") }

    var providers by remember { mutableStateOf<List<NearbyProvider>>(emptyList()) }
    var isLoadingProviders by remember { mutableStateOf(true) }

    var selectedProviderForBooking by remember { mutableStateOf<NearbyProvider?>(null) }
    var showBookingModal by remember { mutableStateOf(false) }
    var confirmedAppointment by remember { mutableStateOf<AppointmentModel?>(null) }

    fun refreshProviders() {
        isLoadingProviders = true
        scope.launch {
            val list = AppointmentRepository.fetchNearbyProviders(
                specialty = specialty,
                lat = currentLat,
                lon = currentLon,
                locationName = currentLocationName
            )
            providers = list
            isLoadingProviders = false
        }
    }

    LaunchedEffect(specialty, currentLat, currentLon) {
        refreshProviders()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(Color(0x0A00D4FF), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x3300D4FF), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Header Row 1: Title + Informational Aid Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🩺 Consult a Specialist",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary,
                modifier = Modifier.weight(1f)
            )

            Surface(
                color = Color(0x1AFFFFFF),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Text(
                    text = "ⓘ Informational Aid",
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 2: Suggested Specialist
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Suggested Specialist: ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = specialty,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (contextualNote.isNotBlank()) contextualNote else "This recommendation is based on evaluated metrics and is provided for educational routing purposes. It does not constitute a confirmed diagnosis.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Location Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x33000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📍", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = currentLocationName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = locationInput,
                onValueChange = { locationInput = it },
                placeholder = { Text("Search city or PIN...", fontSize = 11.sp, color = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = Color(0x33FFFFFF)
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = {
                    if (locationInput.isNotBlank()) {
                        scope.launch {
                            val coords = AppointmentRepository.searchLocationByName(locationInput)
                            if (coords != null) {
                                currentLat = coords.first
                                currentLon = coords.second
                                currentLocationName = locationInput.trim()
                                refreshProviders()
                            } else {
                                Toast.makeText(context, "Location '$locationInput' not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Text("Search", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoadingProviders) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔍 Searching nearby $specialty providers...", fontSize = 12.sp, color = TextMuted)
                }
            }
        } else if (providers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x10FF9800), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x33FF9800), RoundedCornerShape(8.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🩺", fontSize = 20.sp)
                Text("No nearby specialists found", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), fontSize = 13.sp)
                Text("Try searching using another city, district, or PIN code above.", fontSize = 11.sp, color = TextSecondary)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                providers.take(5).forEach { provider ->
                    ProviderCardItem(
                        provider = provider,
                        onCallClick = {
                            if (!provider.phone.isNullOrBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        onWebsiteClick = {
                            if (!provider.website.isNullOrBlank()) {
                                var url = provider.website
                                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                    url = "https://$url"
                                }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        },
                        onDirectionsClick = {
                            val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${provider.lat},${provider.lon}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        onBookClick = {
                            selectedProviderForBooking = provider
                            showBookingModal = true
                        }
                    )
                }
            }
        }
    }

    // Booking Dialog Modal
    if (showBookingModal && selectedProviderForBooking != null) {
        val provider = selectedProviderForBooking!!
        var bookingDate by remember {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            mutableStateOf(sdf.format(cal.time))
        }
        var patientName by remember { mutableStateOf("Self") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBookingModal = false },
            title = {
                Text("📅 Book / Schedule Appointment", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Provider: ${provider.name}", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 13.sp)
                    Text(text = "Specialty: ${provider.specialization}", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "Facility: ${provider.facilityType}", color = TextMuted, fontSize = 11.sp)
                    Text(text = "📍 ${provider.address}", color = TextMuted, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Select Appointment Date (YYYY-MM-DD):", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = bookingDate,
                        onValueChange = { bookingDate = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )

                    Text("Select Patient:", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            val bookingId = "HL-APT-" + (10000 + (Math.random() * 90000).toInt())
                            val status = "Pending Confirmation"

                            val newAppt = AppointmentModel(
                                id = bookingId,
                                userId = AppointmentRepository.currentUserId,
                                providerName = provider.name,
                                specialization = provider.specialization,
                                facilityName = provider.facilityType,
                                address = provider.address,
                                phone = provider.phone ?: "",
                                website = provider.website ?: "",
                                latitude = provider.lat,
                                longitude = provider.lon,
                                appointmentDate = bookingDate,
                                appointmentTime = "10:00 AM",
                                status = status,
                                bookingReference = bookingId,
                                externalBookingUrl = provider.website ?: "",
                                source = "HealLens Specialist Discovery",
                                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()),
                                patientName = patientName
                            )

                            val success = AppointmentRepository.saveAppointment(newAppt)
                            if (success) {
                                confirmedAppointment = newAppt
                                showBookingModal = false
                            } else {
                                android.widget.Toast.makeText(context, "Failed to save appointment to server. Please verify network and login session.", android.widget.Toast.LENGTH_LONG).show()
                            }
                            isSubmitting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    enabled = !isSubmitting
                ) {
                    Text("Confirm Booking", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingModal = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirmation Success Dialog
    if (confirmedAppointment != null) {
        val appt = confirmedAppointment!!
        AlertDialog(
            onDismissRequest = { confirmedAppointment = null },
            title = {
                Text("✅ Appointment Scheduled", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Booking Reference: ${appt.bookingReference}", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 13.sp)
                    Text("Provider: ${appt.providerName}", color = TextPrimary, fontSize = 12.sp)
                    Text("Specialty: ${appt.specialization}", color = TextSecondary, fontSize = 12.sp)
                    Text("Date: ${appt.appointmentDate}", color = TextSecondary, fontSize = 12.sp)
                    Text("Status: ${appt.status}", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { confirmedAppointment = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ProviderCardItem(
    provider: NearbyProvider,
    onCallClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x05FFFFFF), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(provider.facilityType, fontSize = 11.sp, color = CyanPrimary, fontWeight = FontWeight.SemiBold)
            }
            Text("📏 %.1f km".format(provider.distanceKm), fontSize = 11.sp, color = TextMuted)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("📍 ${provider.address}", fontSize = 11.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: Action Buttons (Call, Website, Directions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!provider.phone.isNullOrBlank()) {
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier.height(42.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D10B981))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📞", fontSize = 11.sp, maxLines = 1)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }

            if (!provider.website.isNullOrBlank()) {
                OutlinedButton(
                    onClick = onWebsiteClick,
                    modifier = Modifier.height(42.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D00D4FF))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🌐", fontSize = 11.sp, maxLines = 1)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Website", fontSize = 11.sp, color = CyanPrimary, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }

            OutlinedButton(
                onClick = onDirectionsClick,
                modifier = Modifier.height(42.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4DA78BFA))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🧭", fontSize = 11.sp, maxLines = 1)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", fontSize = 11.sp, color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Full-Width Primary CTA (Book Appointment Button)
        Button(
            onClick = onBookClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("📅 Book Appointment", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
