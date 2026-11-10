package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary

@Composable
fun QuickAddPartyDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSaveCustomer: (name: String, phone: String, address: String, initialBaki: Double) -> Unit,
    onSaveSupplier: (name: String, company: String, phone: String, amountOwed: Double) -> Unit
) {
    var isCustomerMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var companyOrAddress by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialAmountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন পার্টি যোগ করুন" else "Add New Party",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isCustomerMode,
                        onClick = { isCustomerMode = true },
                        label = { Text(if (isBangla) "গ্রাহক (Customer)" else "Customer") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isCustomerMode,
                        onClick = { isCustomerMode = false },
                        label = { Text(if (isBangla) "মহাজন / সাপ্লায়ার" else "Supplier") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "নাম *" else "Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = companyOrAddress,
                    onValueChange = { companyOrAddress = it },
                    label = {
                        Text(
                            if (isCustomerMode) (if (isBangla) "ঠিকানা" else "Address")
                            else (if (isBangla) "কোম্পানির নাম" else "Company Name")
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialAmountStr,
                    onValueChange = { initialAmountStr = it },
                    label = {
                        Text(
                            if (isCustomerMode) (if (isBangla) "পূর্বের বাকি পাওনা (৳)" else "Initial Baki Due (৳)")
                            else (if (isBangla) "পূর্বের দেনা (৳)" else "Initial Amount Owed (৳)")
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = initialAmountStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        if (isCustomerMode) {
                            onSaveCustomer(name.trim(), phone.trim(), companyOrAddress.trim(), amt)
                        } else {
                            onSaveSupplier(name.trim(), companyOrAddress.trim(), phone.trim(), amt)
                        }
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun QuickPaymentInDialog(
    customers: List<Customer>,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (customerId: Long, amount: Double, note: String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var isDropdownOpen by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "টাকা জমা (বাকি পরিশোধ)" else "Quick Payment In",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = selectedCustomer?.let { "${it.name} (বাকি: ৳${it.totalBaki.toLong()})" }
                            ?: if (isBangla) "গ্রাহক নির্বাচন করুন" else "Select Customer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "গ্রাহক *" else "Customer *") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownOpen = true }
                    )
                    DropdownMenu(
                        expanded = isDropdownOpen,
                        onDismissRequest = { isDropdownOpen = false }
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} - ৳${cust.totalBaki.toLong()}") },
                                onClick = {
                                    selectedCustomer = cust
                                    isDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "জমার পরিমাণ (৳) *" else "Amount Received (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(if (isBangla) "নোট (যেমনঃ বিকাশ বা ক্যাশ)" else "Note (e.g. bKash / Cash)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val cust = selectedCustomer
                    if (cust != null && amt > 0) {
                        onConfirm(cust.id, amt, noteText)
                    }
                },
                enabled = selectedCustomer != null && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "জমা করুন" else "Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun QuickSalesReturnDialog(
    products: List<Product>,
    customers: List<Customer>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (productId: Long, customerId: Long?, quantity: Int, refundAmount: Double, reason: String) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var isProductDropdownOpen by remember { mutableStateOf(false) }
    var qtyText by remember { mutableStateOf("1") }
    var refundText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "বিক্রি ফেরত (Sales Return)" else "Sales Return",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: if (isBangla) "পণ্য বেছে নিন" else "Select Product",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "পণ্য *" else "Product *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isProductDropdownOpen = true }
                    )
                    DropdownMenu(
                        expanded = isProductDropdownOpen,
                        onDismissRequest = { isProductDropdownOpen = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text(prod.name) },
                                onClick = {
                                    selectedProduct = prod
                                    refundText = prod.sellingPrice.toString()
                                    isProductDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text(if (isBangla) "ফেরত সংখ্যা *" else "Quantity Returned *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = refundText,
                    onValueChange = { refundText = it },
                    label = { Text(if (isBangla) "ফেরত মূল্য (৳)" else "Refund Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text(if (isBangla) "ফেরতের কারণ" else "Reason for Return") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prod = selectedProduct
                    val qty = qtyText.toIntOrNull() ?: 1
                    val refund = refundText.toDoubleOrNull() ?: 0.0
                    if (prod != null) {
                        onConfirm(prod.id, selectedCustomer?.id, qty, refund, reasonText)
                    }
                },
                enabled = selectedProduct != null,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "ফেরত নিশ্চিত করুন" else "Accept Return")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun QuickQuotationDialog(
    products: List<Product>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConvertToSale: (productId: Long, quantity: Int, price: Double, customerName: String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var isProductDropdownOpen by remember { mutableStateOf(false) }
    var qtyText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf(products.firstOrNull()?.sellingPrice?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "কোটেশন / প্রাক্কলন তৈরি" else "Quick Quotation",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(if (isBangla) "গ্রাহকের নাম" else "Customer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: if (isBangla) "পণ্য নির্বাচন" else "Select Product",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "পণ্য *" else "Product *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isProductDropdownOpen = true }
                    )
                    DropdownMenu(
                        expanded = isProductDropdownOpen,
                        onDismissRequest = { isProductDropdownOpen = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text(prod.name) },
                                onClick = {
                                    selectedProduct = prod
                                    priceText = prod.sellingPrice.toString()
                                    isProductDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text(if (isBangla) "পরিমাণ *" else "Quantity *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(if (isBangla) "দর / মূল্য (৳) *" else "Quoted Price (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prod = selectedProduct
                    val qty = qtyText.toIntOrNull() ?: 1
                    val prc = priceText.toDoubleOrNull() ?: (prod?.sellingPrice ?: 0.0)
                    if (prod != null) {
                        onConvertToSale(prod.id, qty, prc, customerName)
                    }
                },
                enabled = selectedProduct != null,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "বিক্রিতে রূপান্তর" else "Convert to Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
