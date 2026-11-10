package com.example.ui.screens.parties

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
fun PartiesScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val repository = remember { AppDatabase.getRepository(context) }
    val isBangla = currentLanguage == AppLanguage.BANGLA

    val customers by repository.allCustomers.collectAsStateWithLifecycle(emptyList())
    val suppliers by repository.allSuppliersOwedFirst.collectAsStateWithLifecycle(emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0: Customers, 1: Suppliers
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "পার্টি তালিকা (গ্রাহক ও মহাজন)" else "Parties Directory",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSlateSurface,
                contentColor = VividEmeraldAccent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "${if (isBangla) "গ্রাহকগণ" else "Customers"} (${customers.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "${if (isBangla) "মহাজনগণ" else "Suppliers"} (${suppliers.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBangla) "নাম বা মোবাইল দিয়ে খুঁজুন..." else "Search by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedTab == 0) {
                val filteredCust = remember(customers, searchQuery) {
                    customers.filter {
                        searchQuery.isBlank() ||
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.phone.contains(searchQuery)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCust) { cust ->
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = VividEmeraldPrimary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = VividEmeraldAccent)
                                        }
                                    }
                                    Column {
                                        Text(text = cust.name, style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                        if (cust.phone.isNotBlank()) {
                                            Text(text = cust.phone, style = AppTypographyScale.hintText, color = TextMuted)
                                        }
                                    }
                                }
                                Text(
                                    text = if (cust.totalBaki > 0) formatTaka(cust.totalBaki, currentLanguage) else "৳0",
                                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                    color = if (cust.totalBaki > 0) LowStockRed else VividEmeraldAccent
                                )
                            }
                        }
                    }
                }
            } else {
                val filteredSup = remember(suppliers, searchQuery) {
                    suppliers.filter {
                        searchQuery.isBlank() ||
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.company.contains(searchQuery, ignoreCase = true) ||
                                it.phone.contains(searchQuery)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSup) { sup ->
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = VividEmeraldPrimary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Business, contentDescription = null, tint = VividEmeraldAccent)
                                        }
                                    }
                                    Column {
                                        Text(text = sup.name, style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                        if (sup.company.isNotBlank()) {
                                            Text(text = sup.company, style = AppTypographyScale.hintText, color = VividEmeraldAccent)
                                        }
                                    }
                                }
                                Text(
                                    text = formatTaka(sup.amountOwed, currentLanguage),
                                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                    color = if (sup.amountOwed > 0) LowStockRed else VividEmeraldAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
