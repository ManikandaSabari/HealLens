package com.heallens.android.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

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

@Serializable
data class EmergencyContactDto(
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val id: String? = null,
    @SerialName("user_id") val user_id: String? = null,
    @SerialName("Patient_Name") val Patient_Name: String? = null,
    @SerialName("Contact_Name") val Contact_Name: String? = null,
    @SerialName("Relationship") val Relationship: String? = null,
    @SerialName("Phone_Number") val Phone_Number: String? = null,
    @SerialName("created_at") val created_at: String? = null
)

@Serializable
data class EmergencyContactInsertDto(
    @SerialName("user_id") val user_id: String,
    @SerialName("Patient_Name") val Patient_Name: String,
    @SerialName("Contact_Name") val Contact_Name: String,
    @SerialName("Relationship") val Relationship: String,
    @SerialName("Phone_Number") val Phone_Number: String
)

fun EmergencyContactDto.toEmergencyContact(): EmergencyContact {
    return EmergencyContact(
        id = id ?: java.util.UUID.randomUUID().toString(),
        userId = user_id ?: "",
        name = Contact_Name ?: "",
        phoneNumber = Phone_Number ?: "",
        relationship = Relationship ?: "Other"
    )
}

fun EmergencyContact.toInsertDto(patientName: String = "Self"): EmergencyContactInsertDto {
    return EmergencyContactInsertDto(
        user_id = userId,
        Patient_Name = if (patientName.isNotBlank()) patientName else "Self",
        Contact_Name = name,
        Relationship = relationship,
        Phone_Number = phoneNumber
    )
}

data class EmergencyContact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val phoneNumber: String,
    val relationship: String
)
