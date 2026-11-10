package com.example.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@Composable
fun FirstRunSetupScreen(
    currentLanguage: AppLanguage,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    var storeName by remember { mutableStateOf("আমার দোকান (Dokan Store)") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("ঢাকা, বাংলাদেশ") }
    var businessType by remember { mutableStateOf(if (isBangla) "খুচরা ও পাইকারি দোকান" else "Retail & Wholesale Store") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = VividEmeraldPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = if (isBangla) "দোকান ২.০ সেটাপ" else "Welcome to Dokan 2.0",
                    style = AppTypographyScale.screenTitle.copy(fontSize = 22.sp),
                    color = TextPrimary
                )

                Text(
                    text = if (isBangla) "আপনার ব্যবসার প্রাথমিক তথ্য পূরণ করুন:" else "Set up your store details to begin:",
                    style = AppTypographyScale.hintText,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(if (isBangla) "দোকানের নাম *" else "Store Name *") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = VividEmeraldAccent) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setup_store_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Contact Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = VividEmeraldAccent) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setup_phone")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isBangla) "দোকানের ঠিকানা" else "Store Address") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = VividEmeraldAccent) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setup_address")
                )

                OutlinedTextField(
                    value = businessType,
                    onValueChange = { businessType = it },
                    label = { Text(if (isBangla) "ব্যবসার ধরন" else "Business Category") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = VividEmeraldAccent) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setup_business_type")
                )

                Button(
                    onClick = {
                        ProfileManager.completeFirstRunSetup(
                            context = context,
                            storeName = storeName,
                            phone = phone,
                            address = address,
                            businessType = businessType
                        )
                        onSetupComplete()
                    },
                    enabled = storeName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_complete_setup"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "দোকান চালু করুন" else "Get Started",
                        style = AppTypographyScale.buttonText
                    )
                }
            }
        }
    }
}
