package com.example.ui.screens.utilities

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillGalleryScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "ভাউচার ও বিলের গ্যালারি" else "Receipts & Bill Gallery",
                        style = AppTypographyScale.screenTitle,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateSurface)
            )
        },
        containerColor = DarkSlateBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = VividEmeraldAccent,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (isBangla) "ডিজিটাল ভাউচার আর্কাইভ" else "Digital Invoice Archive",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isBangla) "বিক্রি ও খরচের সমস্ত ভাউচার ও রসিদের ছবি নিরাপদ স্টোরেজে সংরক্ষিত থাকে"
                        else "All photos and scanned receipts of purchases & expenses are safely stored here.",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                    Text(
                        text = if (isBangla) "এখনো কোনো ছবির রসিদ নেই" else "No captured bill photos yet",
                        style = AppTypographyScale.normalText,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
