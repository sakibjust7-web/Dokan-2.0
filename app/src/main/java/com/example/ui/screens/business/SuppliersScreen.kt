package com.example.ui.screens.business

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Supplier
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    viewModel: BusinessViewModel,
    currentLanguage: AppLanguage,
    onBackToDashboard: () -> Unit
) {
    BackHandler { onBackToDashboard() }
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var supplierToPay by remember { mutableStateOf<Supplier?>(null) }
    var payAmountText by remember { mutableStateOf("") }
    var payNoteText by remember { mutableStateOf("") }

    val totalDena = remember(suppliers) { suppliers.sumOf { it.amountOwed } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "মহাজন / সরবরাহকারী খাতা" else "Suppliers & Payables",
                        style = AppTypographyScale.screenTitle,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = VividEmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_supplier")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }
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
            // Overview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "মোট মহাজন দেনা" else "Total Supplier Due",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalDena, currentLanguage),
                            style = AppTypographyScale.moneyNumber.copy(fontSize = 24.sp),
                            color = LowStockRed
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VividEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${suppliers.size} ${if (isBangla) "জন মহাজন" else "Suppliers"}",
                            style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                            color = VividEmeraldAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (suppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isBangla) "কোনো মহাজনের তথ্য পাওয়া যায়নি" else "No suppliers found",
                        style = AppTypographyScale.normalText,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suppliers) { sup ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                            border = BorderStroke(1.dp, DarkSlateCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sup.name,
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    if (sup.company.isNotBlank()) {
                                        Text(
                                            text = sup.company,
                                            style = AppTypographyScale.hintText,
                                            color = VividEmeraldAccent
                                        )
                                    }
                                    if (sup.phone.isNotBlank()) {
                                        Text(
                                            text = sup.phone,
                                            style = AppTypographyScale.hintText,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatTaka(sup.amountOwed, currentLanguage),
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = if (sup.amountOwed > 0) LowStockRed else VividEmeraldAccent
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            supplierToPay = sup
                                            payAmountText = ""
                                            payNoteText = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (isBangla) "টাকা দিন" else "Pay",
                                            style = AppTypographyScale.buttonText.copy(fontSize = 11.sp)
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

    // Add Supplier Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var company by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var owedStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "নতুন মহাজন যুক্ত করুন" else "Add New Supplier",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isBangla) "মহাজনের নাম *" else "Supplier Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text(if (isBangla) "কোম্পানি / প্রতিষ্ঠান" else "Company / Brand") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = owedStr,
                        onValueChange = { owedStr = it },
                        label = { Text(if (isBangla) "পূর্বের দেনা (৳)" else "Previous Due (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = owedStr.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            viewModel.addSupplier(name.trim(), company.trim(), phone.trim(), amt)
                            showAddDialog = false
                            Toast.makeText(context, if (isBangla) "মহাজন যোগ করা হয়েছে" else "Supplier added", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "যোগ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // Pay Supplier Dialog
    if (supplierToPay != null) {
        val sup = supplierToPay!!
        AlertDialog(
            onDismissRequest = { supplierToPay = null },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = "${if (isBangla) "মহাজনকে টাকা প্রদান: " else "Pay Supplier: "}${sup.name}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${if (isBangla) "বর্তমান দেনা: " else "Current Due: "}${formatTaka(sup.amountOwed, currentLanguage)}",
                        style = AppTypographyScale.normalText,
                        color = LowStockRed
                    )
                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        label = { Text(if (isBangla) "প্রদত্ত টাকা (৳) *" else "Amount Paid (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = payNoteText,
                        onValueChange = { payNoteText = it },
                        label = { Text(if (isBangla) "মাধ্যম বা নোট" else "Payment Note / Method") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordSupplierPayment(sup.id, amt, payNoteText)
                            supplierToPay = null
                            Toast.makeText(context, if (isBangla) "টাকা পরিশোধ রেকর্ড হয়েছে" else "Payment recorded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = (payAmountText.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "পরিশোধ করুন" else "Confirm Pay")
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierToPay = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}
