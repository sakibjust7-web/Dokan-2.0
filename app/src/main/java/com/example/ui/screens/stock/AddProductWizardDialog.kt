package com.example.ui.screens.stock

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.UnitManager
import com.example.data.local.entity.Product
import com.example.ui.components.HeroGlassCard
import com.example.ui.localization.formatTaka
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.DokanAmber
import com.example.ui.theme.DokanDeepGreen
import com.example.ui.theme.DokanVividGreen
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

/**
 * 3-Step Add Product Wizard (Chunk 3 Requirement 4):
 * - Step 1: Basics (Names, Category, Barcode, Unit, Shelf)
 * - Step 2: Pricing (Buy price, Sell price, Wholesale, MRP) with Live Profit & Margin Card
 * - Step 3: Stock (Initial stock, Low stock threshold, Location, Notes)
 * - Retains all existing fields and data integrity.
 */
@Composable
fun AddProductWizardDialog(
    isBangla: Boolean,
    product: Product?,
    initialBarcode: String = "",
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) } // 1: Basics, 2: Pricing, 3: Stock

    // Step 1: Basics State
    var nameEn by remember { mutableStateOf(product?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(product?.nameBn ?: "") }
    var category by remember {
        mutableStateOf(product?.category ?: if (categories.isNotEmpty()) categories[0] else "Hand Tools")
    }
    var barcode by remember { mutableStateOf(if (initialBarcode.isNotBlank()) initialBarcode else (product?.barcode ?: "")) }
    var unitEn by remember { mutableStateOf(product?.unitEn ?: "pcs") }
    var unitBn by remember { mutableStateOf(product?.unitBn ?: "টি") }
    var location by remember { mutableStateOf(product?.location ?: "") }

    // Step 2: Pricing State
    var buyPriceStr by remember {
        mutableStateOf(product?.buyPrice?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var sellPriceStr by remember {
        mutableStateOf(product?.sellPrice?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var wholesalePriceStr by remember {
        mutableStateOf(product?.wholesalePrice?.let { if (it > 0) (if (it % 1 == 0.0) it.toLong().toString() else it.toString()) else "" } ?: "")
    }
    var mrpStr by remember {
        mutableStateOf(product?.mrp?.let { if (it > 0) (if (it % 1 == 0.0) it.toLong().toString() else it.toString()) else "" } ?: "")
    }

    // Step 3: Stock State
    var stockQuantityStr by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "10") }
    var lowStockThresholdStr by remember { mutableStateOf(product?.lowStockThreshold?.toString() ?: "5") }
    var description by remember { mutableStateOf(product?.description ?: "") }

    // Calculations for live profit & margin
    val buyPriceVal = buyPriceStr.toDoubleOrNull() ?: 0.0
    val sellPriceVal = sellPriceStr.toDoubleOrNull() ?: 0.0
    val profitVal = sellPriceVal - buyPriceVal
    val marginPercent = if (sellPriceVal > 0) (profitVal / sellPriceVal) * 100.0 else 0.0
    val markupPercent = if (buyPriceVal > 0) (profitVal / buyPriceVal) * 100.0 else 0.0
    val isSellingBelowCost = sellPriceVal < buyPriceVal && buyPriceVal > 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF161616)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (product == null) {
                                if (isBangla) "নতুন পণ্য যুক্ত করুন" else "Add New Product"
                            } else {
                                if (isBangla) "পণ্য সংশোধন করুন" else "Edit Product"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = when (currentStep) {
                                1 -> if (isBangla) "ধাপ ১: প্রাথমিক তথ্য (Basics)" else "Step 1: Basics"
                                2 -> if (isBangla) "ধাপ ২: মূল্য ও লাভ (Pricing)" else "Step 2: Pricing"
                                else -> if (isBangla) "ধাপ ৩: স্টক ও মজুদ (Stock)" else "Step 3: Stock"
                            },
                            fontSize = 12.sp,
                            color = DokanVividGreen
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262626))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Step Wizard Indicator Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stepNames = listOf(
                        1 to if (isBangla) "১. প্রাথমিক" else "1. Basics",
                        2 to if (isBangla) "২. মূল্য" else "2. Pricing",
                        3 to if (isBangla) "৩. স্টক" else "3. Stock"
                    )

                    stepNames.forEach { (step, label) ->
                        val isActive = currentStep == step
                        val isDone = currentStep > step

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(
                                    when {
                                        isActive -> DokanVividGreen
                                        isDone -> DokanDeepGreen.copy(alpha = 0.5f)
                                        else -> Color(0xFF222222)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isActive || isDone) DokanVividGreen else Color(0x22FFFFFF),
                                    RoundedCornerShape(17.dp)
                                )
                                .clickable { currentStep = step },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = DokanVividGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) Color(0xFF0A1805) else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wizard Step Body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentStep) {
                        // STEP 1: BASICS
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = nameEn,
                                    onValueChange = {
                                        nameEn = it
                                        if (nameBn.isBlank()) nameBn = it
                                    },
                                    label = { Text(if (isBangla) "ইংরেজি নাম *" else "Product Name (English) *") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("add_product_name_en"),
                                    colors = wizardFieldColors()
                                )

                                OutlinedTextField(
                                    value = nameBn,
                                    onValueChange = { nameBn = it },
                                    label = { Text(if (isBangla) "বাংলা নাম *" else "Product Name (Bangla) *") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("add_product_name_bn"),
                                    colors = wizardFieldColors()
                                )

                                // Category Selector
                                Text(
                                    text = if (isBangla) "ক্যাটাগরি নির্বাচন করুন:" else "Select Category:",
                                    fontSize = 12.sp,
                                    color = Color(0xBBFFFFFF)
                                )
                                val popularCategories = listOf(
                                    "Hand Tools", "Power Tools", "Nuts & Bolts", "Electric", "Paint", "Plumbing", "Hardware", "Pipes"
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    (popularCategories + categories).distinct().forEach { cat ->
                                        val isCatSelected = category.equals(cat, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isCatSelected) Color(0xFF152612) else Color(0xFF222222),
                                            border = BorderStroke(1.dp, if (isCatSelected) DokanVividGreen else Color(0x22FFFFFF)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { category = cat }
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 12.sp,
                                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCatSelected) DokanVividGreen else Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = barcode,
                                        onValueChange = { barcode = it },
                                        label = { Text(if (isBangla) "বারকোড" else "Barcode") },
                                        singleLine = true,
                                        trailingIcon = {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = DokanVividGreen)
                                        },
                                        modifier = Modifier.weight(1f).testTag("add_product_barcode"),
                                        colors = wizardFieldColors()
                                    )
                                    OutlinedTextField(
                                        value = location,
                                        onValueChange = { location = it },
                                        label = { Text(if (isBangla) "তাক / লোকেশন" else "Shelf / Rack") },
                                        placeholder = { Text("e.g. A-1", color = TextMuted) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("add_product_location"),
                                        colors = wizardFieldColors()
                                    )
                                }

                                // Unit Quick Selector
                                Text(
                                    text = if (isBangla) "পরিমাপের একক:" else "Unit of Measurement:",
                                    fontSize = 12.sp,
                                    color = Color(0xBBFFFFFF)
                                )
                                val commonUnits = listOf("pcs" to "টি", "kg" to "কেজি", "feet" to "ফুট", "box" to "বক্স", "packet" to "প্যাকেট")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    commonUnits.forEach { (en, bn) ->
                                        val isUnitSelected = unitEn.equals(en, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isUnitSelected) Color(0xFF152612) else Color(0xFF222222),
                                            border = BorderStroke(1.dp, if (isUnitSelected) DokanVividGreen else Color(0x22FFFFFF)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    unitEn = en
                                                    unitBn = bn
                                                }
                                        ) {
                                            Text(
                                                text = if (isBangla) bn else en,
                                                fontSize = 12.sp,
                                                fontWeight = if (isUnitSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isUnitSelected) DokanVividGreen else Color.White,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 2: PRICING WITH LIVE PROFIT & MARGIN CARD
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Live Profit and Margin Card (Chunk 3 Requirement 4)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = DokanVividGreen.copy(0.3f))
                                        .border(
                                            1.5.dp,
                                            if (isSellingBelowCost) LowStockRed else DokanVividGreen,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            if (isSellingBelowCost) Color(0xFF2A1515) else Color(0xFF142410)
                                        )
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isBangla) "লাইভ লাভ ও মার্জিন হিসাব" else "LIVE PROFIT & MARGIN",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSellingBelowCost) LowStockRed else DokanVividGreen,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.1f%% Margin", marginPercent),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSellingBelowCost) LowStockRed else DokanVividGreen
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (isBangla) "প্রতি এককে লাভ:" else "Profit per unit:",
                                                    fontSize = 12.sp,
                                                    color = Color(0xCCFFFFFF)
                                                )
                                                Text(
                                                    text = "৳${String.format(Locale.US, "%.2f", profitVal)}",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSellingBelowCost) LowStockRed else DokanVividGreen
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = if (isBangla) "মার্কআপ (Markup):" else "Markup on Cost:",
                                                    fontSize = 12.sp,
                                                    color = Color(0xCCFFFFFF)
                                                )
                                                Text(
                                                    text = String.format(Locale.US, "%.1f%%", markupPercent),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        if (isSellingBelowCost) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Warning, contentDescription = null, tint = LowStockRed, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = if (isBangla) "সতর্কতা: বিক্রয় মূল্য ক্রয় মূল্যের চেয়ে কম!" else "Warning: Selling below cost price!",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LowStockRed
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = buyPriceStr,
                                        onValueChange = { buyPriceStr = it },
                                        label = { Text(if (isBangla) "ক্রয় মূল্য (৳) *" else "Buy / Cost Price (৳) *") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("add_product_buy_price"),
                                        colors = wizardFieldColors()
                                    )
                                    OutlinedTextField(
                                        value = sellPriceStr,
                                        onValueChange = { sellPriceStr = it },
                                        label = { Text(if (isBangla) "বিক্রয় মূল্য (৳) *" else "Sell Price (৳) *") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("add_product_sell_price"),
                                        colors = wizardFieldColors()
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = wholesalePriceStr,
                                        onValueChange = { wholesalePriceStr = it },
                                        label = { Text(if (isBangla) "পাইকারি মূল্য (ঐচ্ছিক)" else "Wholesale Price") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("add_product_wholesale_price"),
                                        colors = wizardFieldColors()
                                    )
                                    OutlinedTextField(
                                        value = mrpStr,
                                        onValueChange = { mrpStr = it },
                                        label = { Text(if (isBangla) "গায়ের দাম (MRP)" else "MRP") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("add_product_mrp"),
                                        colors = wizardFieldColors()
                                    )
                                }
                            }
                        }

                        // STEP 3: STOCK & INVENTORY
                        3 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = stockQuantityStr,
                                        onValueChange = { stockQuantityStr = it },
                                        label = { Text(if (isBangla) "বর্তমান স্টক *" else "Stock Quantity *") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("add_product_stock_qty"),
                                        colors = wizardFieldColors()
                                    )
                                    OutlinedTextField(
                                        value = lowStockThresholdStr,
                                        onValueChange = { lowStockThresholdStr = it },
                                        label = { Text(if (isBangla) "কম স্টক সীমা" else "Low Stock Alert") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("add_product_low_limit"),
                                        colors = wizardFieldColors()
                                    )
                                }

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text(if (isBangla) "বিবরণ বা নোট" else "Notes / Description") },
                                    maxLines = 3,
                                    modifier = Modifier.fillMaxWidth().testTag("add_product_notes"),
                                    colors = wizardFieldColors()
                                )

                                // Summary Preview Card
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1E1E1E),
                                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${nameEn.ifBlank { nameBn.ifBlank { "New Product" } }} ($category)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Sell ৳$sellPriceVal · Buy ৳$buyPriceVal · Stock $stockQuantityStr $unitEn",
                                            fontSize = 12.sp,
                                            color = DokanVividGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Navigation Controls (Back / Next / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep -= 1 },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.testTag("btn_wizard_back")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBangla) "পূর্ববর্তী" else "Back")
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text(if (isBangla) "বাতিল" else "Cancel", color = Color(0x88FFFFFF))
                        }
                    }

                    if (currentStep < 3) {
                        Button(
                            onClick = {
                                if (currentStep == 1 && nameEn.isBlank() && nameBn.isBlank()) {
                                    Toast.makeText(context, if (isBangla) "পণ্যের নাম দিন" else "Please enter product name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                currentStep += 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DokanVividGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_wizard_next")
                        ) {
                            Text(if (isBangla) "পরবর্তী ধাপ" else "Next", color = Color(0xFF0A1805), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0A1805), modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                val buyP = buyPriceStr.toDoubleOrNull() ?: 0.0
                                val sellP = sellPriceStr.toDoubleOrNull() ?: 0.0
                                val stockQ = stockQuantityStr.toIntOrNull() ?: 0
                                val lowLimit = lowStockThresholdStr.toIntOrNull() ?: 5
                                val wholesaleP = wholesalePriceStr.toDoubleOrNull() ?: 0.0
                                val mrpP = mrpStr.toDoubleOrNull() ?: 0.0

                                val finalNameEn = nameEn.ifBlank { nameBn }
                                val finalNameBn = nameBn.ifBlank { nameEn }

                                if (finalNameEn.isBlank()) {
                                    Toast.makeText(context, if (isBangla) "পণ্যের নাম প্রয়োজন" else "Product name is required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val finalProduct = Product(
                                    id = product?.id ?: 0,
                                    nameEn = finalNameEn.trim(),
                                    nameBn = finalNameBn.trim(),
                                    category = category.trim(),
                                    buyPrice = buyP,
                                    sellPrice = sellP,
                                    wholesalePrice = wholesaleP,
                                    mrp = mrpP,
                                    stockQuantity = stockQ,
                                    lowStockThreshold = lowLimit,
                                    unitEn = unitEn.trim(),
                                    unitBn = unitBn.trim(),
                                    barcode = barcode.trim(),
                                    location = location.trim(),
                                    description = description.trim(),
                                    photoKey = product?.photoKey ?: ""
                                )
                                onSave(finalProduct)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DokanVividGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_wizard_save")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0A1805), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (product == null) {
                                    if (isBangla) "পণ্য সংরক্ষণ করুন" else "Save Product"
                                } else {
                                    if (isBangla) "আপডেট করুন" else "Update Product"
                                },
                                color = Color(0xFF0A1805),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun wizardFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = DokanVividGreen,
    unfocusedBorderColor = Color(0x33FFFFFF),
    focusedLabelColor = DokanVividGreen,
    unfocusedLabelColor = Color(0x88FFFFFF)
)
