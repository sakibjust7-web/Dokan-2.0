package com.example.ui.screens.utilities

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isBangla = currentLanguage == AppLanguage.BANGLA

    var costStr by remember { mutableStateOf("1000") }
    var sellStr by remember { mutableStateOf("1250") }
    var vatPercentStr by remember { mutableStateOf("5") }

    val cost = costStr.toDoubleOrNull() ?: 0.0
    val sell = sellStr.toDoubleOrNull() ?: 0.0
    val vatPercent = vatPercentStr.toDoubleOrNull() ?: 0.0

    val profit = sell - cost
    val margin = if (sell > 0) (profit / sell) * 100 else 0.0
    val markup = if (cost > 0) (profit / cost) * 100 else 0.0
    val vatAmount = sell * (vatPercent / 100)
    val totalWithVat = sell + vatAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "ব্যবসায়িক ক্যালকুলেটর" else "Business Calculators",
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
            // Margin Calculator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBangla) "লাভ ও মার্জিন ক্যালকুলেটর" else "Profit Margin & Markup",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = costStr,
                            onValueChange = { costStr = it },
                            label = { Text(if (isBangla) "ক্রয়মূল্য (৳)" else "Cost Price (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellStr,
                            onValueChange = { sellStr = it },
                            label = { Text(if (isBangla) "বিক্রয়মূল্য (৳)" else "Selling Price (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isBangla) "মোট লাভ" else "Profit", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(text = formatTaka(profit, currentLanguage), style = AppTypographyScale.moneyNumber, color = VividEmeraldAccent)
                        }
                        Column {
                            Text(text = if (isBangla) "মার্জিন" else "Margin", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(text = "%.1f%%".format(margin), style = AppTypographyScale.moneyNumber, color = VividEmeraldAccent)
                        }
                        Column {
                            Text(text = if (isBangla) "মার্কআপ" else "Markup", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(text = "%.1f%%".format(markup), style = AppTypographyScale.moneyNumber, color = TextPrimary)
                        }
                    }
                }
            }

            // VAT / Tax Calculator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBangla) "ভ্যাট ও ট্যাক্স হিসাব (VAT / Tax)" else "VAT & Tax Calculator",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = vatPercentStr,
                        onValueChange = { vatPercentStr = it },
                        label = { Text(if (isBangla) "ভ্যাট হার (%)" else "VAT Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isBangla) "ভ্যাট পরিমাণ" else "VAT Amount", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(text = formatTaka(vatAmount, currentLanguage), style = AppTypographyScale.moneyNumber, color = VividEmeraldAccent)
                        }
                        Column {
                            Text(text = if (isBangla) "ভ্যাটসহ মোট" else "Total with VAT", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(text = formatTaka(totalWithVat, currentLanguage), style = AppTypographyScale.moneyNumber, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
