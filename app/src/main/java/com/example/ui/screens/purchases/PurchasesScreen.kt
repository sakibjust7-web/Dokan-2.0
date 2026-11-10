package com.example.ui.screens.purchases

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PurchaseRecord
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val repository = remember { AppDatabase.getRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val isBangla = currentLanguage == AppLanguage.BANGLA

    val purchases by repository.allPurchases.collectAsStateWithLifecycle(emptyList())
    val products by repository.allProducts.collectAsStateWithLifecycle(emptyList())
    val suppliers by repository.allSuppliersOwedFirst.collectAsStateWithLifecycle(emptyList())

    val totalPurchasesCost = remember(purchases) { purchases.sumOf { it.totalCost } }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "পণ্য ক্রয় চালান (Purchases)" else "Purchases & Invoices",
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
                onClick = { showAddDialog = true },
                containerColor = VividEmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Purchase")
            }
        },
        containerColor = DarkSlateBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                            text = if (isBangla) "মোট মালামাল ক্রয়" else "Total Purchases",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalPurchasesCost, currentLanguage),
                            style = AppTypographyScale.moneyNumber.copy(fontSize = 24.sp),
                            color = VividEmeraldAccent
                        )
                    }
                    Text(
                        text = "${purchases.size} ${if (isBangla) "টি চালান" else "Records"}",
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            if (purchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isBangla) "কোনো ক্রয়ের তথ্য পাওয়া যায়নি" else "No purchase records",
                        style = AppTypographyScale.normalText,
                        color = TextMuted
                    )
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(purchases) { record ->
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
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = record.productName,
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = formatTaka(record.totalCost, currentLanguage),
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = VividEmeraldAccent
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${if (isBangla) "সংখ্যা: " else "Qty: "}${record.quantity} • ${record.supplierName.ifBlank { "Supplier" }}",
                                        style = AppTypographyScale.hintText,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = dateFormat.format(Date(record.timestamp)),
                                        style = AppTypographyScale.hintText,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
        var isProductOpen by remember { mutableStateOf(false) }
        var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
        var isSupplierOpen by remember { mutableStateOf(false) }
        var qtyStr by remember { mutableStateOf("1") }
        var unitPriceStr by remember { mutableStateOf("") }
        var paidStr by remember { mutableStateOf("") }
        var noteStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "নতুন ক্রয় চালান যুক্ত করুন" else "Record Purchase",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: if (isBangla) "পণ্য বেছে নিন" else "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBangla) "পণ্য *" else "Product *") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isProductOpen,
                            onDismissRequest = { isProductOpen = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text(prod.name) },
                                    onClick = {
                                        selectedProduct = prod
                                        unitPriceStr = prod.costPrice.toString()
                                        isProductOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        OutlinedTextField(
                            value = selectedSupplier?.name ?: if (isBangla) "মহাজন / সাপ্লায়ার" else "Select Supplier",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBangla) "মহাজন" else "Supplier") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isSupplierOpen,
                            onDismissRequest = { isSupplierOpen = false }
                        ) {
                            suppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = { Text("${sup.name} (${sup.company})") },
                                    onClick = {
                                        selectedSupplier = sup
                                        isSupplierOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = qtyStr,
                            onValueChange = { qtyStr = it },
                            label = { Text(if (isBangla) "সংখ্যা *" else "Qty *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitPriceStr,
                            onValueChange = { unitPriceStr = it },
                            label = { Text(if (isBangla) "ক্রয়দর (৳) *" else "Cost (৳) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = paidStr,
                        onValueChange = { paidStr = it },
                        label = { Text(if (isBangla) "পরিশোধিত টাকা (৳)" else "Paid Amount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noteStr,
                        onValueChange = { noteStr = it },
                        label = { Text(if (isBangla) "চালান নোট" else "Invoice Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = selectedProduct
                        val qty = qtyStr.toIntOrNull() ?: 1
                        val unitPrice = unitPriceStr.toDoubleOrNull() ?: (prod?.costPrice ?: 0.0)
                        val total = qty * unitPrice
                        val paid = paidStr.toDoubleOrNull() ?: total
                        val due = (total - paid).coerceAtLeast(0.0)

                        if (prod != null) {
                            coroutineScope.launch(Dispatchers.IO) {
                                repository.insertPurchase(
                                    PurchaseRecord(
                                        productId = prod.id,
                                        productName = prod.name,
                                        quantity = qty,
                                        unitBuyingPrice = unitPrice,
                                        totalCost = total,
                                        paidAmount = paid,
                                        dueAmount = due,
                                        supplierId = selectedSupplier?.id,
                                        supplierName = selectedSupplier?.name ?: "Supplier",
                                        note = noteStr
                                    )
                                )
                            }
                            showAddDialog = false
                            Toast.makeText(context, if (isBangla) "ক্রয় চালান যুক্ত হয়েছে" else "Purchase recorded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = selectedProduct != null,
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}
