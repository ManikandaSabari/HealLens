package com.heallens.android.ui.ayurveda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun AyurvedaMapScreen() {
    val scrollState = rememberScrollState()
    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphicCard()
                .padding(16.dp)
        ) {
            Text(
                text = "🌿 " + com.heallens.android.utils.AppStrings.get("remediesTitle", activeLanguage),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary,
                    fontSize = 20.sp
                )
            )
            Text(
                text = com.heallens.android.utils.AppStrings.get("remediesSubtitle", activeLanguage),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dosha Balancing Overview
        Text(
            text = com.heallens.android.utils.AppStrings.get("ayurveda_tridosha_title", activeLanguage),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DoshaCard(
                com.heallens.android.utils.AppStrings.get("vata_title", activeLanguage),
                com.heallens.android.utils.AppStrings.get("vata_element", activeLanguage),
                com.heallens.android.utils.AppStrings.get("vata_note", activeLanguage),
                CyanPrimary,
                Modifier.weight(1f)
            )
            DoshaCard(
                com.heallens.android.utils.AppStrings.get("pitta_title", activeLanguage),
                com.heallens.android.utils.AppStrings.get("pitta_element", activeLanguage),
                com.heallens.android.utils.AppStrings.get("pitta_note", activeLanguage),
                PurpleAccent,
                Modifier.weight(1f)
            )
            DoshaCard(
                com.heallens.android.utils.AppStrings.get("kapha_title", activeLanguage),
                com.heallens.android.utils.AppStrings.get("kapha_element", activeLanguage),
                com.heallens.android.utils.AppStrings.get("kapha_note", activeLanguage),
                Color(0xFF10B981),
                Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Clinical Botanical Directory
        Text(
            text = com.heallens.android.utils.AppStrings.get("ayurveda_botanical_title", activeLanguage),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        HerbItemCard(
            herb = com.heallens.android.utils.AppStrings.get("herb_tulsi_name", activeLanguage),
            scientific = "Ocimum sanctum",
            benefits = com.heallens.android.utils.AppStrings.get("herb_tulsi_benefits", activeLanguage),
            dose = com.heallens.android.utils.AppStrings.get("herb_tulsi_dose", activeLanguage)
        )

        Spacer(modifier = Modifier.height(10.dp))

        HerbItemCard(
            herb = com.heallens.android.utils.AppStrings.get("herb_ashwa_name", activeLanguage),
            scientific = "Withania somnifera",
            benefits = com.heallens.android.utils.AppStrings.get("herb_ashwa_benefits", activeLanguage),
            dose = com.heallens.android.utils.AppStrings.get("herb_ashwa_dose", activeLanguage)
        )

        Spacer(modifier = Modifier.height(10.dp))

        HerbItemCard(
            herb = com.heallens.android.utils.AppStrings.get("herb_turmeric_name", activeLanguage),
            scientific = "Curcuma longa",
            benefits = com.heallens.android.utils.AppStrings.get("herb_turmeric_benefits", activeLanguage),
            dose = com.heallens.android.utils.AppStrings.get("herb_turmeric_dose", activeLanguage)
        )

        Spacer(modifier = Modifier.height(10.dp))

        HerbItemCard(
            herb = com.heallens.android.utils.AppStrings.get("herb_triphala_name", activeLanguage),
            scientific = "Amalaki + Bibhitaki + Haritaki",
            benefits = com.heallens.android.utils.AppStrings.get("herb_triphala_benefits", activeLanguage),
            dose = com.heallens.android.utils.AppStrings.get("herb_triphala_dose", activeLanguage)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DoshaCard(title: String, element: String, note: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .glassmorphicCard()
            .padding(12.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, color = accent, fontSize = 13.sp)
        Text(text = element, fontSize = 10.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = note, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
private fun HerbItemCard(herb: String, scientific: String, benefits: String, dose: String) {
    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphicCard()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = herb, fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 15.sp)
            Text(text = scientific, fontSize = 11.sp, color = TextMuted)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = benefits, fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .background(SurfaceGlass, RoundedCornerShape(8.dp))
                .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = "${com.heallens.android.utils.AppStrings.get("ayurveda_usage_label", activeLanguage)} $dose", fontSize = 10.sp, color = CyanPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}
