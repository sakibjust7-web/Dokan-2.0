package com.example.ui.screens.baki

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
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
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Customer
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakiScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val repository = remember { AppDatabase.getRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val isBangla = currentLanguage == AppLanguage.BANGLA

    val customers by repository.allCustomersBakiFirst.collectAsStateWithLifecycle(emptyList())
    val totalBaki = remember(customers) { customers.sumOf { it.totalBaki } }

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToPay by remember { mutableStateOf<Customer?>(null) }
    var payAmountText by remember { mutableStateOf("") }
    var payNoteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "কাস্টমার বাকি খাতা" else "Customer Baki Khata",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = VividEmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_baki_customer")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
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
                            text = if (isBangla) "মোট বাকি পাওনা" else "Total Receivable Baki",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalBaki, currentLanguage),
                            style = AppTypographyScale.moneyNumber.copy(fontSize = 24.sp),
                            color = LowStockRed
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VividEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${customers.filter { it.totalBaki > 0 }.size} ${if (isBangla) "জন বাকি গ্রাহক" else "Due Customers"}",
                            style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                            color = VividEmeraldAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (customers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isBangla) "কোনো গ্রাহকের তালিকা নেই" else "No customers found",
                        style = AppTypographyScale.normalText,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers) { cust ->
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
                                        text = cust.name,
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    if (cust.phone.isNotBlank()) {
                                        Text(
                                            text = cust.phone,
                                            style = AppTypographyScale.hintText,
                                            color = VividEmeraldAccent
                                        )
                                    }
                                    if (cust.address.isNotBlank()) {
                                        Text(
                                            text = cust.address,
                                            style = AppTypographyScale.hintText,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatTaka(cust.totalBaki, currentLanguage),
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = if (cust.totalBaki > 0) LowStockRed else VividEmeraldAccent
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (cust.totalBaki > 0) {
                                            IconButton(
                                                onClick = {
                                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        val msg = if (isBangla) {
                                                            "শ্রদ্ধেয় ${cust.name}, আপনার বাকি হিসাব ৳${cust.totalBaki.toLong()} টাকা। অনুগ্রহ করে পরিশোধ করুন। ধন্যবাদ।"
                                                        } else {
                                                            "Dear ${cust.name}, your outstanding dues are ৳${cust.totalBaki.toLong()}. Please settle at your earliest convenience. Thank you."
                                                        }
                                                        putExtra(Intent.EXTRA_TEXT, msg)
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, if (isBangla) "বাকি তাগাদা পাঠান" else "Send Due Reminder"))
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = "Reminder", tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                customerToPay = cust
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
                                                text = if (isBangla) "জমা" else "Receive",
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
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var bakiStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "নতুন গ্রাহক যুক্ত করুন" else "Add New Customer",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isBangla) "গ্রাহকের নাম *" else "Customer Name *") },
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
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(if (isBangla) "ঠিকানা" else "Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bakiStr,
                        onValueChange = { bakiStr = it },
                        label = { Text(if (isBangla) "পূর্বের বাকি (৳)" else "Previous Due (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = bakiStr.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                repository.insertCustomer(
                                    Customer(
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        address = address.trim(),
                                        totalBaki = amt
                                    )
                                )
                            }
                            showAddCustomerDialog = false
                            Toast.makeText(context, if (isBangla) "গ্রাহক যোগ করা হয়েছে" else "Customer added", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // Customer Payment In Dialog
    if (customerToPay != null) {
        val cust = customerToPay!!
        AlertDialog(
            onDismissRequest = { customerToPay = null },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = "${if (isBangla) "টাকা জমা: " else "Receive Payment: "}${cust.name}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${if (isBangla) "বর্তমান বাকি: " else "Current Baki: "}${formatTaka(cust.totalBaki, currentLanguage)}",
                        style = AppTypographyScale.normalText,
                        color = LowStockRed
                    )
                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        label = { Text(if (isBangla) "জমার পরিমাণ (৳) *" else "Amount Received (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = payNoteText,
                        onValueChange = { payNoteText = it },
                        label = { Text(if (isBangla) "নোট / মাধ্যম" else "Note / Method") },
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
                            coroutineScope.launch(Dispatchers.IO) {
                                repository.recordCustomerPayment(cust.id, amt, payNoteText)
                            }
                            customerToPay = null
                            Toast.makeText(context, if (isBangla) "টাকা জমা রেকর্ড হয়েছে" else "Payment received", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = (payAmountText.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "জমা করুন" else "Confirm Receive")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToPay = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}
