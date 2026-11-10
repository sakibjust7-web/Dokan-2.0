package com.example.ui.screens.cards

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsHubScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val profile = ProfileManager.activeProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "ডিজিটাল বিজনেস কার্ড ও ব্যানার" else "Digital Visiting Card",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Business Card Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(VividEmeraldPrimary, Color(0xFF064E3B), DarkSlateSurface)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = profile.name.ifBlank { "Dokan Store" },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = profile.businessType.ifBlank { "Retail & Wholesale" },
                                    fontSize = 12.sp,
                                    color = VividEmeraldAccent
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (profile.ownerName.isNotBlank()) {
                                Text(
                                    text = "${if (isBangla) "প্রোপাইটার: " else "Owner: "}${profile.ownerName}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            if (profile.phone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(14.dp))
                                    Text(text = profile.phone, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                            if (profile.address.isNotBlank() || profile.streetAddress.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = profile.address.ifBlank { profile.streetAddress },
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        val cardText = buildString {
                            appendLine("🏪 ${profile.name}")
                            if (profile.businessType.isNotBlank()) appendLine("🔹 ${profile.businessType}")
                            if (profile.ownerName.isNotBlank()) appendLine("👤 ${profile.ownerName}")
                            if (profile.phone.isNotBlank()) appendLine("📞 ${profile.phone}")
                            if (profile.address.isNotBlank()) appendLine("📍 ${profile.address}")
                            appendLine("Dokan 2.0 Digital Store Card")
                        }
                        putExtra(Intent.EXTRA_TEXT, cardText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, if (isBangla) "বিজনেস কার্ড শেয়ার করুন" else "Share Business Card"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "বিজনেস কার্ড শেয়ার করুন" else "Share Business Card",
                    style = AppTypographyScale.buttonText
                )
            }
        }
    }
}
