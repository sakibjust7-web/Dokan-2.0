package com.example.ui.screens.reports

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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessReportsScreen(
    currentLanguage: AppLanguage,
    onNavigateToBaki: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val repository = remember { AppDatabase.getRepository(context) }
    val isBangla = currentLanguage == AppLanguage.BANGLA

    val allSales by repository.allSales.collectAsStateWithLifecycle(emptyList())
    val allExpenses by repository.allExpenses.collectAsStateWithLifecycle(emptyList())
    val allPurchases by repository.allPurchases.collectAsStateWithLifecycle(emptyList())
    val allCustomers by repository.allCustomers.collectAsStateWithLifecycle(emptyList())
    val allSuppliers by repository.allSuppliersOwedFirst.collectAsStateWithLifecycle(emptyList())

    val totalSales = remember(allSales) { allSales.sumOf { it.totalAmount } }
    val totalProfit = remember(allSales) { allSales.sumOf { it.profit } }
    val totalExpenses = remember(allExpenses) { allExpenses.sumOf { it.amount } }
    val totalPurchases = remember(allPurchases) { allPurchases.sumOf { it.totalCost } }
    val netIncome = totalProfit - totalExpenses

    val totalCustomerBaki = remember(allCustomers) { allCustomers.sumOf { it.totalBaki } }
    val totalSupplierDena = remember(allSuppliers) { allSuppliers.sumOf { it.amountOwed } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "ব্যবসায়িক রিপোর্ট ও হিসাব" else "Business Reports & P&L",
                        style = AppTypographyScale.screenTitle,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            val summary = buildString {
                                appendLine(if (isBangla) "=== ব্যবসায়িক রিপোর্ট ===" else "=== BUSINESS REPORT ===")
                                appendLine("${if (isBangla) "মোট বিক্রয়: " else "Total Sales: "}৳${totalSales.toLong()}")
                                appendLine("${if (isBangla) "মোট লাভ: " else "Gross Profit: "}৳${totalProfit.toLong()}")
                                appendLine("${if (isBangla) "দোকানের খরচ: " else "Total Expenses: "}৳${totalExpenses.toLong()}")
                                appendLine("${if (isBangla) "নিট মুনাফা: " else "Net Profit: "}৳${netIncome.toLong()}")
                                appendLine("${if (isBangla) "মোট বাকি পাওনা: " else "Customer Baki: "}৳${totalCustomerBaki.toLong()}")
                                appendLine("${if (isBangla) "মহাজন দেনা: " else "Supplier Due: "}৳${totalSupplierDena.toLong()}")
                            }
                            putExtra(Intent.EXTRA_TEXT, summary)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, if (isBangla) "রিপোর্ট শেয়ার করুন" else "Share Report"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report", tint = VividEmeraldAccent)
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Net Income Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (netIncome >= 0) VividEmeraldPrimary else LowStockRed
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isBangla) "মোট নিট মুনাফা (Net Profit)" else "Overall Net Profit",
                        style = AppTypographyScale.hintText,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = formatTaka(netIncome, currentLanguage),
                        style = AppTypographyScale.moneyNumber.copy(fontSize = 32.sp),
                        color = Color.White
                    )
                    Text(
                        text = "${if (isBangla) "বিক্রি লাভ: " else "Gross: "}${formatTaka(totalProfit, currentLanguage)} | ${if (isBangla) "খরচ: " else "Exp: "}${formatTaka(totalExpenses, currentLanguage)}",
                        style = AppTypographyScale.hintText,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Key Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = if (isBangla) "মোট বিক্রয়" else "Total Sales", style = AppTypographyScale.hintText, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(text = formatTaka(totalSales, currentLanguage), style = AppTypographyScale.moneyNumber, color = VividEmeraldAccent)
                        Text(text = "${allSales.size} ${if (isBangla) "টি অর্ডার" else "orders"}", style = AppTypographyScale.hintText, color = TextMuted)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = if (isBangla) "মোট মালামাল ক্রয়" else "Purchases", style = AppTypographyScale.hintText, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(text = formatTaka(totalPurchases, currentLanguage), style = AppTypographyScale.moneyNumber, color = TextPrimary)
                        Text(text = "${allPurchases.size} ${if (isBangla) "টি চালান" else "invoices"}", style = AppTypographyScale.hintText, color = TextMuted)
                    }
                }
            }

            // Dues & Payables Quick Jump Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    onClick = onNavigateToBaki,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isBangla) "গ্রাহকের বাকি >" else "Customer Baki >", style = AppTypographyScale.hintText, color = TextMuted)
                            Icon(Icons.AutoMirrored.Filled.CallReceived, contentDescription = null, tint = LowStockRed, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(text = formatTaka(totalCustomerBaki, currentLanguage), style = AppTypographyScale.moneyNumber, color = LowStockRed)
                    }
                }

                Card(
                    onClick = onNavigateToSuppliers,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isBangla) "মহাজন দেনা >" else "Supplier Due >", style = AppTypographyScale.hintText, color = TextMuted)
                            Icon(Icons.AutoMirrored.Filled.CallMade, contentDescription = null, tint = LowStockRed, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(text = formatTaka(totalSupplierDena, currentLanguage), style = AppTypographyScale.moneyNumber, color = LowStockRed)
                    }
                }
            }

            // Store Expenses Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) "খরচের বিবরণী" else "Store Expense Breakdown",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )
                    val groupedExpenses = remember(allExpenses) {
                        allExpenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
                    }
                    if (groupedExpenses.isEmpty()) {
                        Text(text = if (isBangla) "কোনো খরচের রেকর্ড নেই" else "No recorded expenses", style = AppTypographyScale.hintText, color = TextMuted)
                    } else {
                        groupedExpenses.forEach { (cat, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = cat, style = AppTypographyScale.normalText, color = TextPrimary)
                                Text(text = formatTaka(amount, currentLanguage), style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold), color = LowStockRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
