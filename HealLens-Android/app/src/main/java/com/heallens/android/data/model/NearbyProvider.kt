package com.heallens.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NearbyProvider(
    val id: String,
    val name: String,
    val facilityType: String,
    val specialization: String,
    val address: String,
    val phone: String? = null,
    val website: String? = null,
    val rating: String? = null,
    val lat: Double,
    val lon: Double,
    val distanceKm: Double
)
