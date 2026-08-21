package com.heallens.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentModel(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("provider_name")
    val providerName: String = "",
    @SerialName("specialization")
    val specialization: String = "",
    @SerialName("facility_name")
    val facilityName: String = "",
    @SerialName("address")
    val address: String = "",
    @SerialName("phone")
    val phone: String = "",
    @SerialName("website")
    val website: String = "",
    @SerialName("latitude")
    val latitude: Double = 0.0,
    @SerialName("longitude")
    val longitude: Double = 0.0,
    @SerialName("appointment_date")
    val appointmentDate: String = "",
    @SerialName("appointment_time")
    val appointmentTime: String = "10:00 AM",
    @SerialName("status")
    val status: String = "Pending Confirmation",
    @SerialName("booking_reference")
    val bookingReference: String = "",
    @SerialName("external_booking_url")
    val externalBookingUrl: String = "",
    @SerialName("source")
    val source: String = "HealLens Specialist Discovery",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("patient_name")
    val patientName: String = "Self"
)
