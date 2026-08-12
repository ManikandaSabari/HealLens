package com.heallens.android.ui.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.data.repository.EmergencyContactRepository
import com.heallens.android.model.EmergencyContact
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EmergencySosScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        EmergencyContactRepository.init(context)
    }

    val contacts by EmergencyContactRepository.contactsFlow.collectAsState()

    // Form State
    var contactName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedRelationship by remember { mutableStateOf("Father") }
    var isRelationshipExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedContactToDelete by remember { mutableStateOf<EmergencyContact?>(null) }

    val relationshipKeys = listOf(
        "Father" to "rel_father",
        "Mother" to "rel_mother",
        "Son" to "rel_son",
        "Daughter" to "rel_daughter",
        "Brother" to "rel_brother",
        "Sister" to "rel_sister",
        "Husband" to "rel_husband",
        "Wife" to "rel_wife",
        "Friend" to "rel_friend",
        "Guardian" to "rel_guardian",
        "Other" to "rel_other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Red Emergency Alert Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency SOS",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = com.heallens.android.utils.AppStrings.get("sos_banner_title", activeLanguage),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEF4444),
                        fontSize = 20.sp
                    )
                )

                Text(
                    text = com.heallens.android.utils.AppStrings.get("sos_banner_desc", activeLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Big Red Call Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEF4444))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call 108",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = com.heallens.android.utils.AppStrings.get("sos_call_108", activeLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Emergency Response Helplines
        Text(
            text = "Emergency Response Helplines",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        EmergencyHelplineCard("Ambulance Dispatch Service 🚑", "102", "Free 24/7 Medical Transport & Paramedics") {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:102"))
            context.startActivity(intent)
        }

        Spacer(modifier = Modifier.height(10.dp))

        EmergencyHelplineCard("National Poison Information Center ☣️", "1800-116-117", "Toll-free 24/7 Toxicological Helpline") {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1800116117"))
            context.startActivity(intent)
        }

        Spacer(modifier = Modifier.height(10.dp))

        EmergencyHelplineCard("Mental Health Crisis Helpline 🧠", "14416", "24/7 Tele-MANAS Counseling Support") {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:14416"))
            context.startActivity(intent)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PART 2 — Emergency Contact Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphicCard(cornerRadius = 20.dp)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🚨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("sos_contact_title", activeLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("sos_contact_subtitle", activeLanguage),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Name Input
            Text(
                text = com.heallens.android.utils.AppStrings.get("sos_contact_name", activeLanguage),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = contactName,
                onValueChange = {
                    contactName = it
                    errorMessage = null
                },
                placeholder = { Text("e.g. John Doe", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyanPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SurfaceGlassBorder,
                    focusedContainerColor = SurfaceGlass,
                    unfocusedContainerColor = SurfaceGlass,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number Input
            Text(
                text = com.heallens.android.utils.AppStrings.get("sos_contact_phone", activeLanguage),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    errorMessage = null
                },
                placeholder = { Text("e.g. 9876543210", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CyanPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SurfaceGlassBorder,
                    focusedContainerColor = SurfaceGlass,
                    unfocusedContainerColor = SurfaceGlass,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Relationship Dropdown
            Text(
                text = com.heallens.android.utils.AppStrings.get("sos_contact_relationship", activeLanguage),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceGlass)
                    .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(12.dp))
                    .clickable { isRelationshipExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                val selectedKey = relationshipKeys.firstOrNull { it.first == selectedRelationship }?.second ?: "rel_other"
                val displayRel = com.heallens.android.utils.AppStrings.get(selectedKey, activeLanguage)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayRel,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Relationship",
                        tint = CyanPrimary
                    )
                }

                DropdownMenu(
                    expanded = isRelationshipExpanded,
                    onDismissRequest = { isRelationshipExpanded = false },
                    modifier = Modifier.background(Color(0xFF10192D))
                ) {
                    relationshipKeys.forEach { (rawVal, key) ->
                        val optionText = com.heallens.android.utils.AppStrings.get(key, activeLanguage)
                        DropdownMenuItem(
                            text = { Text(text = optionText, color = TextPrimary, fontSize = 13.sp) },
                            onClick = {
                                selectedRelationship = rawVal
                                isRelationshipExpanded = false
                                errorMessage = null
                            }
                        )
                    }
                }
            }

            // Error Message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Contact Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyanPrimary)
                    .clickable(enabled = !isSaving) {
                        val nameClean = contactName.trim()
                        val phoneClean = phoneNumber.trim()

                        if (nameClean.isEmpty()) {
                            errorMessage = "Please enter contact name."
                            return@clickable
                        }
                        if (phoneClean.isEmpty()) {
                            errorMessage = "Please enter valid phone number."
                            return@clickable
                        }

                        val contact = EmergencyContact(
                            userId = EmergencyContactRepository.currentUserId,
                            name = nameClean,
                            phoneNumber = phoneClean,
                            relationship = selectedRelationship
                        )

                        isSaving = true
                        coroutineScope.launch {
                            val result = EmergencyContactRepository.addContact(contact)
                            isSaving = false
                            if (result.isSuccess) {
                                contactName = ""
                                phoneNumber = ""
                                errorMessage = null
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to save emergency contact to cloud."
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSaving) "Saving to Cloud..." else com.heallens.android.utils.AppStrings.get("sos_add_contact_btn", activeLanguage),
                    color = DarkBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contacts Display List
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("sos_no_contacts", activeLanguage),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                contacts.forEach { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
                            context.startActivity(intent)
                        },
                        onDelete = { selectedContactToDelete = contact }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Delete Confirmation Dialog
        selectedContactToDelete?.let { contact ->
            AlertDialog(
                onDismissRequest = { selectedContactToDelete = null },
                title = {
                    Text(
                        text = com.heallens.android.utils.AppStrings.get("sos_delete_contact_title", activeLanguage),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "${contact.name} (${contact.relationship})?",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            EmergencyContactRepository.deleteContact(contact.id)
                            selectedContactToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text(text = com.heallens.android.utils.AppStrings.get("btn_delete", activeLanguage), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedContactToDelete = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                    ) {
                        Text(text = com.heallens.android.utils.AppStrings.get("btn_cancel", activeLanguage))
                    }
                },
                containerColor = Color(0xFF10192D),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun EmergencyContactCard(
    contact: EmergencyContact,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceGlass, RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "👤", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = contact.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(CyanPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = contact.relationship,
                    color = CyanPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "📞 ${contact.phoneNumber}",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons: Call & Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanPrimary.copy(alpha = 0.15f))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clickable { onCall() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Contact",
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call",
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clickable { onDelete() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Contact",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyHelplineCard(title: String, number: String, subtitle: String, onCall: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphicCard()
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CyanPrimary.copy(alpha = 0.15f))
                .border(1.dp, CyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { onCall() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = CyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = number, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
