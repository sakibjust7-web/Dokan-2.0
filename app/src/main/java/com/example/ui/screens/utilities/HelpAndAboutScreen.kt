package com.example.ui.screens.utilities

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndAboutScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "সাহায্য ও পরিচিতি" else "Help & About",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VividEmeraldPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(32.dp))
                        }
                    }
                    Text(text = "Dokan 2.0 (দোকান ২.০)", style = AppTypographyScale.screenTitle, color = TextPrimary)
                    Text(text = "সংস্করণ: ২.০.০ (Build 2026)", style = AppTypographyScale.hintText, color = VividEmeraldAccent)
                    Text(
                        text = if (isBangla) "বাংলাদেশের খুচরা ও পাইকারি দোকানের জন্য আধুনিক পিওএস, ইনভেন্টরি, বাকি খাতা ও ক্লাউড ডাটাবেজ সলিউশন।"
                        else "Next-generation store POS, inventory, supplier due, and customer credit ledger solution.",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = if (isBangla) "ব্যবহার সহায়িকা" else "Quick User Guide", style = AppTypographyScale.sectionHeading, color = TextPrimary)
                    Text(
                        text = if (isBangla)
                            "১. 'বিক্রি' ট্যাবে গিয়ে যেকোনো পণ্য নির্বাচন করে সরাসরি চালান তৈরি ও প্রিন্ট/শেয়ার করুন।\n" +
                            "২. বাকিতে বিক্রি করতে 'বাকি বিক্রি' নির্বাচন করে গ্রাহক সিলেক্ট করুন।\n" +
                            "৩. 'স্টক' ট্যাবে পণ্য যোগ, ক্যাটাগরি ফিল্টার এবং স্টক সংখ্যা রিয়েলটাইমে আপডেট করুন।\n" +
                            "৪. 'হিসাব' ও 'ব্যবসা' ট্যাবে দিনপঞ্জিকা, লাভ-লোকসান ও মহাজন দেনা যাচাই করুন।"
                        else
                            "1. Go to 'Sell' tab to tap products, enter quantities, and generate receipts.\n" +
                            "2. For credit sales, select Credit Sale and choose/add customer.\n" +
                            "3. In 'Stock' tab, add new items, barcode lookups, and track low stocks.\n" +
                            "4. View overall P&L and customer dues in 'Business' & 'Reports'.",
                        style = AppTypographyScale.normalText,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
