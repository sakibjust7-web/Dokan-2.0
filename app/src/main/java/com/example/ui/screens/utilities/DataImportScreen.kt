package com.example.ui.screens.utilities

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val repository = remember { AppDatabase.getRepository(context) }
    val backupManager = remember { BackupManager(repository) }
    val coroutineScope = rememberCoroutineScope()
    val isBangla = currentLanguage == AppLanguage.BANGLA
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "ডাটা ব্যাকআপ ও রিস্টোর" else "Backup & Data Import",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBangla) "সম্পূর্ণ ডাটাবেজ ব্যাকআপ (JSON)" else "Full Database Backup",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isBangla) "আপনার সমস্ত পণ্য, স্টক, বাকি খাতা ও বিক্রির হিসাব একটি নিরাপদ ফাইলে এক্সপোর্ট করুন।"
                        else "Export all your products, baki ledgers, and transactions into a secure backup file.",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )

                    Button(
                        onClick = {
                            isExporting = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val json = backupManager.generateBackupJson(context)
                                    val shareIntent = backupManager.createGoogleDriveShareIntent(context, json)
                                    withContext(Dispatchers.Main) {
                                        isExporting = false
                                        context.startActivity(Intent.createChooser(shareIntent, if (isBangla) "ব্যাকআপ শেয়ার করুন" else "Share Backup"))
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isExporting = false
                                        Toast.makeText(context, if (isBangla) "ব্যাকআপ ব্যর্থ হয়েছে" else "Backup failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isExporting) (if (isBangla) "এক্সপোর্ট হচ্ছে..." else "Exporting...") else (if (isBangla) "ব্যাকআপ ফাইল শেয়ার করুন" else "Export & Share Backup"))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBangla) "লোকাল ব্যাকআপ সংরক্ষণ (Save Locally)" else "Save Local Backup",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isBangla) "আপনার ফোনের ইন্টারনাল স্টোরেজে একটি অফলাইন ব্যাকআপ ফাইল সেভ করুন।"
                        else "Save an offline JSON snapshot inside app files for quick restoration.",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val json = backupManager.generateBackupJson(context)
                                    val file = backupManager.saveBackupToLocalFile(context, json)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, if (isBangla) "ব্যাকআপ সেভ হয়েছে: ${file.name}" else "Saved local backup: ${file.name}", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, if (isBangla) "ব্যাকআপ সেভ ব্যর্থ" else "Local save failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = VividEmeraldAccent)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isBangla) "লোকাল ফাইলে সেভ করুন" else "Save to Local File", color = VividEmeraldAccent)
                    }
                }
            }
        }
    }
}
