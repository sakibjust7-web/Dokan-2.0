package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Category
import com.example.data.local.entity.Product
import com.example.data.repository.HardwareRepository
import com.example.ui.components.DokanBackground
import com.example.ui.components.ProgressRing
import com.example.ui.components.SearchPill
import com.example.ui.components.StatusTag
import com.example.ui.components.StatusTagType
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.screens.stock.AddProductWizardDialog
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.DokanAmber
import com.example.ui.theme.DokanDeepGreen
import com.example.ui.theme.DokanVividGreen
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun StockScreen(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember {
        val db = AppDatabase.getDatabase(context)
        HardwareRepository(
            categoryDao = db.categoryDao(),
            productDao = db.productDao(),
            customerDao = db.customerDao(),
            customerPaymentDao = db.customerPaymentDao(),
            supplierDao = db.supplierDao(),
            supplierPaymentDao = db.supplierPaymentDao(),
            saleOrderDao = db.saleOrderDao(),
            purchaseDao = db.purchaseDao(),
            expenseDao = db.expenseDao()
        )
    }

    val products by repository.allProducts.collectAsState(initial = emptyList())
    val dbCategories by repository.allCategories.collectAsState(initial = emptyList())

    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var onlyLowStock by remember { mutableStateOf(false) }

    var showAddWizardDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productForQuickStock by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showBarcodeScanDialog by remember { mutableStateOf(false) }
    var prefilledBarcodeForAdd by remember { mutableStateOf("") }

    // File picker for CSV import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                reader.close()

                if (lines.isNotEmpty()) {
                    scope.launch {
                        var importedCount = 0
                        var skippedCount = 0
                        for (i in 1 until lines.size) {
                            val line = lines[i].trim()
                            if (line.isBlank()) continue
                            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                            if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                                val name = parts[0]
                                val cat = if (parts.size > 1 && parts[1].isNotBlank()) parts[1] else "General"
                                val buy = if (parts.size > 2) parts[2].toDoubleOrNull() ?: 0.0 else 0.0
                                val sell = if (parts.size > 3) parts[3].toDoubleOrNull() ?: (buy * 1.25) else (buy * 1.25)
                                val qty = if (parts.size > 4) parts[4].toIntOrNull() ?: 10 else 10
                                val unit = if (parts.size > 5 && parts[5].isNotBlank()) parts[5] else "pcs"
                                val bar = if (parts.size > 6) parts[6] else ""

                                val exists = products.any { p ->
                                    p.nameBn.equals(name, ignoreCase = true) ||
                                            p.nameEn.equals(name, ignoreCase = true) ||
                                            (bar.isNotBlank() && p.barcode == bar)
                                }
                                if (exists) {
                                    skippedCount++
                                } else {
                                    repository.insertProduct(
                                        Product(
                                            nameBn = name,
                                            nameEn = name,
                                            category = cat,
                                            buyPrice = buy,
                                            sellPrice = sell,
                                            stockQuantity = qty,
                                            unitBn = unit,
                                            unitEn = unit,
                                            barcode = bar
                                        )
                                    )
                                    importedCount++
                                }
                            }
                        }
                        Toast.makeText(
                            context,
                            if (isBangla) "$importedCount টি পণ্য যুক্ত হয়েছে" else "Imported $importedCount products",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredProducts = products.filter { product ->
        val matchesQuery = searchQuery.isBlank() ||
                product.nameBn.contains(searchQuery, ignoreCase = true) ||
                product.nameEn.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true) ||
                product.barcode.contains(searchQuery, ignoreCase = true) ||
                product.location.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == null || product.category.equals(selectedCategory, ignoreCase = true)
        val matchesLowStock = !onlyLowStock || product.isLowStock
        matchesQuery && matchesCategory && matchesLowStock
    }

    val totalStockUnits = products.sumOf { it.stockQuantity }
    val lowStockCount = products.count { it.isLowStock }
    val totalInventoryValue = products.sumOf { it.buyPrice * it.stockQuantity }

    val categories = remember(products, dbCategories) {
        val fromProds = products.map { it.category }
        val fromDb = dbCategories.map { cat: Category -> cat.nameBn.ifBlank { cat.nameEn } }
        (fromProds + fromDb).distinct().filter { it.isNotBlank() }
    }

    DokanBackground(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_stock")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // 1. Stat Bubbles (ITEMS, LOW STOCK, STOCK VALUE) - Matching Image 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bubble 1: ITEMS
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF1B1B1B)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isBangla) "মোট পণ্য" else "ITEMS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0x99FFFFFF),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${products.size}",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "$totalStockUnits ${if (isBangla) "একক" else "units"}",
                                fontSize = 11.sp,
                                color = Color(0x77FFFFFF)
                            )
                        }
                    }

                    // Bubble 2: LOW STOCK (Amber glowing border - Image 3)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .drawBehind {
                                if (lowStockCount > 0) {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                DokanAmber.copy(alpha = 0.20f),
                                                Color.Transparent
                                            ),
                                            center = center,
                                            radius = size.width * 0.7f
                                        ),
                                        radius = size.width * 0.7f,
                                        center = center
                                    )
                                }
                            }
                            .border(
                                width = 1.5.dp,
                                color = if (onlyLowStock || lowStockCount > 0) DokanAmber else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onlyLowStock = !onlyLowStock },
                        shape = RoundedCornerShape(18.dp),
                        color = if (onlyLowStock) Color(0xFF2C2213) else Color(0xFF1B1B1B)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isBangla) "কম স্টক" else "LOW STOCK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DokanAmber,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$lowStockCount",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                color = DokanAmber
                            )
                            Text(
                                text = if (isBangla) "সতর্কতা" else "alerts",
                                fontSize = 11.sp,
                                color = Color(0x77FFFFFF)
                            )
                        }
                    }

                    // Bubble 3: STOCK VALUE (Image 3)
                    Surface(
                        modifier = Modifier
                            .weight(1.3f)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF1B1B1B)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isBangla) "স্টক মূল্য" else "STOCK VALUE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0x99FFFFFF),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "৳${totalInventoryValue.toLong()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF252525)
                            ) {
                                Text(
                                    text = if (isBangla) "কেনা দামে" else "at cost",
                                    fontSize = 10.5.sp,
                                    color = Color(0xAAFFFFFF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Search Pill with Barcode + View Toggle & Import Buttons (Matching Image 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchPill(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = if (isBangla) "নাম, বারকোড বা তাক খুঁজুন..." else "Search name, barcode, shelf...",
                        trailingAction = {
                            IconButton(
                                onClick = { showBarcodeScanDialog = true },
                                modifier = Modifier.size(34.dp).testTag("btn_stock_barcode_scan")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan Barcode",
                                    tint = DokanVividGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("stock_search_pill")
                    )

                    // Grid / List Toggle Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E1E1E))
                            .clickable { isGridView = !isGridView }
                            .testTag("btn_stock_grid_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = Color(0xDDFFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Import / Download Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E1E1E))
                            .clickable { filePickerLauncher.launch("*/*") }
                            .testTag("btn_stock_file_import"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Import CSV",
                            tint = Color(0xDDFFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Category Filter Chips (All, Low Stock [N], Hand Tools, etc. - Matching Image 3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // All Chip
                    val isAllActive = selectedCategory == null && !onlyLowStock
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isAllActive) Color(0xFF162513) else Color(0xFF1E1E1E),
                        border = BorderStroke(1.dp, if (isAllActive) DokanVividGreen else Color(0x22FFFFFF)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                selectedCategory = null
                                onlyLowStock = false
                            }
                    ) {
                        Text(
                            text = if (isBangla) "সব পণ্য" else "All",
                            fontSize = 12.5.sp,
                            fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAllActive) DokanVividGreen else Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }

                    // Low Stock [N] Chip (Amber Outline)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (onlyLowStock) Color(0xFF2C2213) else Color(0xFF1E1E1E),
                        border = BorderStroke(1.dp, if (onlyLowStock || lowStockCount > 0) DokanAmber else Color(0x22FFFFFF)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onlyLowStock = !onlyLowStock }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isBangla) "কম স্টক" else "Low Stock",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DokanAmber
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DokanAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$lowStockCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DokanAmber
                                )
                            }
                        }
                    }

                    // Dynamic and standard categories
                    val defaultCats = listOf("Hand Tools", "Nuts & Bolts", "Power Tools", "Electric", "Paint", "Plumbing")
                    (defaultCats + categories).distinct().forEach { cat ->
                        val isCatActive = selectedCategory.equals(cat, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isCatActive) Color(0xFF162513) else Color(0xFF1E1E1E),
                            border = BorderStroke(1.dp, if (isCatActive) DokanVividGreen else Color(0x22FFFFFF)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    selectedCategory = if (isCatActive) null else cat
                                }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.5.sp,
                                fontWeight = if (isCatActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCatActive) DokanVividGreen else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Products List or Grid with Progress Ring (Matching Image 3)
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = TextMuted, modifier = Modifier.size(46.dp))
                            Text(text = if (isBangla) "কোন পণ্য পাওয়া যায়নি" else "No products found", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                } else if (!isGridView) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 110.dp) // Generous padding so floating "+ Add Product" pill never covers cards!
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            StockProductRowCard(
                                product = product,
                                isBangla = isBangla,
                                currentLanguage = currentLanguage,
                                onAddStockClick = { productForQuickStock = product },
                                onCardClick = { productToEdit = product }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 110.dp) // Generous padding so floating "+ Add Product" pill never covers cards!
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            StockProductGridCard(
                                product = product,
                                isBangla = isBangla,
                                currentLanguage = currentLanguage,
                                onAddStockClick = { productForQuickStock = product },
                                onCardClick = { productToEdit = product }
                            )
                        }
                    }
                }
            }

            // 5. Floating "+ Add Product" Pill Button (Matching Image 3)
            // Positioned above dock and never covering a card due to 110.dp list bottom padding
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 14.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = DokanVividGreen.copy(alpha = 0.5f),
                        spotColor = DokanVividGreen.copy(alpha = 0.6f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(DokanVividGreen)
                    .clickable {
                        prefilledBarcodeForAdd = ""
                        showAddWizardDialog = true
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("btn_stock_add_product_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color(0xFF0A1805),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isBangla) "পণ্য যোগ করুন" else "Add Product",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A1805)
                    )
                }
            }
        }

        // 6. 3-Step Add / Edit Product Wizard Dialog
        if (showAddWizardDialog) {
            AddProductWizardDialog(
                isBangla = isBangla,
                product = null,
                initialBarcode = prefilledBarcodeForAdd,
                categories = categories,
                onDismiss = { showAddWizardDialog = false },
                onSave = { newProd ->
                    scope.launch {
                        repository.insertProduct(newProd)
                        showAddWizardDialog = false
                        Toast.makeText(context, if (isBangla) "পণ্য যোগ হয়েছে!" else "Product Added!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        productToEdit?.let { prod ->
            AddProductWizardDialog(
                isBangla = isBangla,
                product = prod,
                initialBarcode = prod.barcode,
                categories = categories,
                onDismiss = { productToEdit = null },
                onSave = { updatedProd ->
                    scope.launch {
                        repository.updateProduct(updatedProd)
                        productToEdit = null
                        Toast.makeText(context, if (isBangla) "পণ্য আপডেট হয়েছে!" else "Product Updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 7. Quick Restock Modal Dialog
        productForQuickStock?.let { prod ->
            QuickRestockModalDialog(
                product = prod,
                isBangla = isBangla,
                onDismiss = { productForQuickStock = null },
                onConfirm = { addedQty, unitCost ->
                    scope.launch {
                        repository.addStock(
                            productId = prod.id,
                            productName = prod.localizedName(isBangla),
                            addedQty = addedQty,
                            unitBuyingPrice = unitCost,
                            supplierName = "Direct Restock"
                        )
                        productForQuickStock = null
                        Toast.makeText(context, if (isBangla) "স্টক বাড়ানো হয়েছে!" else "Stock Added!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 8. Barcode Scanner / Search Dialog
        if (showBarcodeScanDialog) {
            var barcodeInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showBarcodeScanDialog = false },
                containerColor = Color(0xFF1E1E1E),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = DokanVividGreen)
                        Text(if (isBangla) "বারকোড স্ক্যান / খুঁজুন" else "Barcode Scan / Search", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("stock_barcode_input"),
                        placeholder = { Text("e.g. 894112345678", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DokanVividGreen,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val match = products.find { it.barcode.equals(barcodeInput.trim(), ignoreCase = true) }
                            if (match != null) {
                                searchQuery = match.barcode
                                showBarcodeScanDialog = false
                                Toast.makeText(context, "${match.name} found!", Toast.LENGTH_SHORT).show()
                            } else {
                                prefilledBarcodeForAdd = barcodeInput.trim()
                                showBarcodeScanDialog = false
                                showAddWizardDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DokanVividGreen)
                    ) {
                        Text(if (isBangla) "খুঁজুন" else "Search", color = Color(0xFF0A1805), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBarcodeScanDialog = false }) {
                        Text(if (isBangla) "বাতিল" else "Cancel", color = Color(0x88FFFFFF))
                    }
                }
            )
        }
    }
}

/**
 * Product Card Matching Image 3:
 * - Circular ProgressRing with Quantity & Unit inside (green normal, amber low)
 * - Product Title
 * - Category / Status Tag
 * - Price line: "Sell ৳... · Buy ৳..."
 * - "+ Stock" pill button on the right
 */
@Composable
private fun StockProductRowCard(
    product: Product,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onAddStockClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val isLow = product.isLowStock
    val ringColor = if (isLow) DokanAmber else DokanVividGreen
    val maxProgressRef = (product.lowStockThreshold * 3).coerceAtLeast(10)
    val progressFraction = (product.stockQuantity.toFloat() / maxProgressRef.toFloat()).coerceIn(0.15f, 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() }
            .testTag("stock_item_${product.id}"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF191919)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Circular ProgressRing with Quantity & Unit inside
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressRing(
                        progress = progressFraction,
                        strokeWidth = 4.5.dp,
                        trackColor = Color(0xFF262626),
                        progressColor = ringColor,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${product.stockQuantity}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = product.localizedUnit(isBangla),
                                fontSize = 11.sp,
                                color = Color(0xAAFFFFFF)
                            )
                        }
                    }
                }

                // Middle: Title, Category / Tag, and Prices
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.localizedName(isBangla),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category pill badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF262626)
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 11.sp,
                                color = Color(0xBBFFFFFF),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (isLow) {
                            StatusTag(
                                text = if (isBangla) "স্বল্প" else "Low",
                                type = StatusTagType.WARNING
                            )
                        }
                    }

                    // Prices row: "Sell ৳... · Buy ৳..."
                    Text(
                        text = "Sell ৳${product.sellPrice.toLong()} · Buy ৳${product.buyPrice.toLong()}",
                        fontSize = 12.5.sp,
                        color = Color(0x99FFFFFF)
                    )
                }
            }

            // Right: "+ Stock" Pill Button (Matching Image 3)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E281C),
                border = BorderStroke(1.dp, DokanVividGreen),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAddStockClick() }
                    .testTag("btn_stock_quick_add_${product.id}")
            ) {
                Text(
                    text = if (isBangla) "+ স্টক" else "+ Stock",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DokanVividGreen,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

/**
 * Grid Card Variant
 */
@Composable
private fun StockProductGridCard(
    product: Product,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onAddStockClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val isLow = product.isLowStock
    val ringColor = if (isLow) DokanAmber else DokanVividGreen
    val maxProgressRef = (product.lowStockThreshold * 3).coerceAtLeast(10)
    val progressFraction = (product.stockQuantity.toFloat() / maxProgressRef.toFloat()).coerceIn(0.15f, 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() }
            .testTag("stock_grid_${product.id}"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF191919)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                ProgressRing(
                    progress = progressFraction,
                    strokeWidth = 4.5.dp,
                    trackColor = Color(0xFF262626),
                    progressColor = ringColor,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${product.stockQuantity}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = product.localizedUnit(isBangla),
                            fontSize = 10.sp,
                            color = Color(0xAAFFFFFF)
                        )
                    }
                }
            }

            Text(
                text = product.localizedName(isBangla),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sell ৳${product.sellPrice.toLong()}",
                fontSize = 13.sp,
                color = DokanVividGreen,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E281C),
                border = BorderStroke(1.dp, DokanVividGreen),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onAddStockClick() }
            ) {
                Text(
                    text = if (isBangla) "+ স্টক" else "+ Stock",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DokanVividGreen,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Quick Restock Modal Dialog
 */
@Composable
private fun QuickRestockModalDialog(
    product: Product,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (addedQty: Int, unitCost: Double) -> Unit
) {
    var addedQtyStr by remember { mutableStateOf("10") }
    var unitCostStr by remember {
        mutableStateOf(if (product.buyPrice % 1 == 0.0) product.buyPrice.toLong().toString() else product.buyPrice.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = if (isBangla) "স্টক বৃদ্ধি করুন" else "Quick Restock",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${product.localizedName(isBangla)} (মজুদ: ${product.stockQuantity} ${product.localizedUnit(isBangla)})",
                    fontSize = 13.sp,
                    color = Color(0xBBFFFFFF)
                )

                OutlinedTextField(
                    value = addedQtyStr,
                    onValueChange = { addedQtyStr = it },
                    label = { Text(if (isBangla) "যুক্ত করার পরিমাণ *" else "Quantity to Add *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DokanVividGreen,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )

                OutlinedTextField(
                    value = unitCostStr,
                    onValueChange = { unitCostStr = it },
                    label = { Text(if (isBangla) "ক্রয় মূল্য (প্রতি একক)" else "Unit Buy Price (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DokanVividGreen,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = addedQtyStr.toIntOrNull() ?: 0
                    val cost = unitCostStr.toDoubleOrNull() ?: product.buyPrice
                    if (qty > 0) {
                        onConfirm(qty, cost)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DokanVividGreen)
            ) {
                Text(if (isBangla) "স্টক যোগ করুন" else "Add Stock", color = Color(0xFF0A1805), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = Color(0x88FFFFFF))
            }
        }
    )
}
