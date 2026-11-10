package com.example.ui.screens.marketplace

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
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

data class WholesaleItem(
    val nameEn: String,
    val nameBn: String,
    val supplierName: String,
    val wholesalePrice: Double,
    val minOrderQty: Int,
    val discountTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onNavigateToSell: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    val sampleWholesale = remember {
        listOf(
            WholesaleItem("Premium Sanitary Pipe 1/2\"", "প্রিমিয়াম স্যানিটারি পাইপ ১/২ ইঞ্চি", "মেঘনা পাইপ ইন্ডাস্ট্রিজ", 185.0, 50, "15% ছাড়"),
            WholesaleItem("Brass Bib Cock Deluxe", "ব্রাস ওয়াটার ট্যাপ ডিলাক্স", "গাজী স্যানিটারি কোং", 290.0, 20, "হট ডিল"),
            WholesaleItem("Heavy Duty Angle Valve", "হেভি ডিউটি এঙ্গেল ভাল্ব", "আকিজ হার্ডওয়্যার", 340.0, 30, "ফ্রি ডেলিভারি"),
            WholesaleItem("LED Tube Light 20W", "এলইডি টিউব লাইট ২০ ওয়াট", "ওয়ালটন ইলেকট্রিক", 125.0, 40, "পাইকারি রেট"),
            WholesaleItem("Cement Super Crete 50KG", "সুপারক্রিট প্রিমিয়াম সিমেন্ট ৫০ কেজি", "শাহ সিমেন্ট লিমিটেড", 480.0, 100, "ভেন্ডর স্পেশাল")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "মার্কেটপ্লেস ও পাইকারি বাজার" else "Wholesale Marketplace",
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
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VividEmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isBangla) "সরাসরি পাইকারি রেটে স্টক কিনুন" else "Buy Directly at Wholesale Rates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (isBangla) "বাংলাদেশের শীর্ষ সাপ্লায়ার ও মহাজনদের যাচাইকৃত অফার" else "Verified offers from verified wholesalers & manufacturers",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Text(
                text = if (isBangla) "সেরা পাইকারি ডিলসমূহ" else "Top Wholesale Offers",
                style = AppTypographyScale.sectionHeading,
                color = TextPrimary
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sampleWholesale) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                        border = BorderStroke(1.dp, DarkSlateCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBangla) item.nameBn else item.nameEn,
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "মহাজন: ${item.supplierName}",
                                        style = AppTypographyScale.hintText,
                                        color = TextMuted
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VividEmeraldPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = item.discountTag,
                                        style = AppTypographyScale.hintText.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = VividEmeraldAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "৳${item.wholesalePrice.toLong()} / পিস",
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = VividEmeraldAccent
                                    )
                                    Text(
                                        text = "মিনিমাম অর্ডার: ${item.minOrderQty} পিস",
                                        style = AppTypographyScale.hintText,
                                        color = TextMuted
                                    )
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            if (isBangla) "অর্ডার অনুসন্ধান পাঠানো হয়েছে!" else "Inquiry sent to supplier!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "অর্ডার করুন" else "Order",
                                        style = AppTypographyScale.buttonText.copy(fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
