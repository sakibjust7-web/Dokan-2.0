package com.example.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val profile = ProfileManager.activeProfile

    var name by remember(profile) { mutableStateOf(profile.name) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var businessType by remember(profile) { mutableStateOf(profile.businessType) }
    var registrationNumber by remember(profile) { mutableStateOf(profile.registrationNumber) }
    var bankAccount by remember(profile) { mutableStateOf(profile.bankAccount) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "ব্যবসার প্রোফাইল ও তথ্য" else "Business Profile",
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "দোকানের মূল পরিচিতি" else "Store Details",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isBangla) "দোকানের নাম *" else "Store Name *") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = VividEmeraldAccent) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(if (isBangla) "মালিকের নাম" else "Owner Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (isBangla) "মোবাইল নম্বর" else "Contact Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = VividEmeraldAccent) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(if (isBangla) "ঠিকানা" else "Address") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = VividEmeraldAccent) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = businessType,
                        onValueChange = { businessType = it },
                        label = { Text(if (isBangla) "ব্যবসার ধরণ" else "Business Category") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = VividEmeraldAccent) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = registrationNumber,
                        onValueChange = { registrationNumber = it },
                        label = { Text(if (isBangla) "ট্রেড লাইসেন্স বা রেজিঃ নম্বর" else "Trade License / Reg No") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bankAccount,
                        onValueChange = { bankAccount = it },
                        label = { Text(if (isBangla) "ব্যাংক বা বিকাশ নম্বর" else "Bank / bKash Account") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val updated = profile.copy(
                                name = name.trim().ifBlank { "Dokan Store" },
                                ownerName = ownerName.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                businessType = businessType.trim(),
                                registrationNumber = registrationNumber.trim(),
                                bankAccount = bankAccount.trim()
                            )
                            ProfileManager.updateProfile(context, updated)
                            Toast.makeText(context, if (isBangla) "প্রোফাইল আপডেট হয়েছে!" else "Profile updated!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isBangla) "সংরক্ষণ করুন" else "Save Profile")
                    }
                }
            }
        }
    }
}
