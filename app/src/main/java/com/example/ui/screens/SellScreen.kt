package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.Category
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.ui.components.DokanBackground
import com.example.ui.components.HardwareGlowingIcon
import com.example.ui.components.SearchPill
import com.example.ui.components.StatusTag
import com.example.ui.components.StatusTagType
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.localization.toBengaliNumerals
import com.example.ui.screens.sell.CartCheckoutBottomSheet
import com.example.ui.screens.sell.SellViewModel
import com.example.ui.theme.DokanAmber
import com.example.ui.theme.DokanDeepGreen
import com.example.ui.theme.DokanVividGreen
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    viewModel: SellViewModel = viewModel()
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val completedSaleOrder by viewModel.completedSaleOrder.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    var isCartSheetOpen by remember { mutableStateOf(false) }
    var showBarcodeScanDialog by remember { mutableStateOf(false) }

    // Speech to text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.onSearchQueryChanged(spoken)
            }
        }
    }

    DokanBackground(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_sell")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // 1. Search Pill with Mic and Barcode Icons (Matching Image 2)
                SearchPill(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = if (isBangla) "পণ্য খুঁজুন বা স্ক্যান করুন..." else "Search or scan...",
                    trailingAction = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBangla) "bn-BD" else "en-US")
                                    }
                                    try {
                                        speechLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Voice search unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(34.dp).testTag("btn_sell_mic")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = Color(0xBBFFFFFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { showBarcodeScanDialog = true },
                                modifier = Modifier.size(34.dp).testTag("btn_sell_barcode")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Barcode Scan",
                                    tint = Color(0xBBFFFFFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("sell_search_pill")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Category Chips with Small Icons (Matching Image 2)
                SellCategoryChipsRow(
                    selectedCategory = selectedCategory,
                    dbCategories = categories,
                    onCategorySelected = { viewModel.onCategorySelected(it) },
                    isBangla = isBangla
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. 2-Column Glass Product Tiles (Matching Image 2)
                if (products.isEmpty()) {
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
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (isBangla) "কোন পণ্য পাওয়া যায়নি" else "No products found",
                                color = TextMuted,
                                fontSize = 14.sp
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
                        contentPadding = PaddingValues(bottom = 96.dp) // Generous space so floating cart doesn't block cards
                    ) {
                        items(products, key = { it.id }) { product ->
                            val cartItem = cartItems.find { it.product.id == product.id }
                            val cartQty = cartItem?.quantity ?: 0
                            val isOutOfStock = product.stockQuantity <= 0

                            SellGlassProductTile(
                                product = product,
                                cartQty = cartQty,
                                isOutOfStock = isOutOfStock,
                                isBangla = isBangla,
                                currentLanguage = currentLanguage,
                                onTileClick = {
                                    if (!isOutOfStock) {
                                        viewModel.addToCart(product, 1)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isBangla) "পণ্যটি স্টকে নেই!" else "Out of stock!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onAddClick = {
                                    if (!isOutOfStock) {
                                        viewModel.addToCart(product, 1)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 4. Floating Green Cart Pill Above Dock (Matching Image 2)
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp, start = 16.dp, end = 16.dp)
            ) {
                val totalQty = cartItems.sumOf { it.quantity }
                val totalAmount = cartItems.sumOf { it.total }.toLong()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = DokanVividGreen.copy(alpha = 0.5f),
                            spotColor = DokanVividGreen.copy(alpha = 0.6f)
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(DokanVividGreen)
                        .clickable { isCartSheetOpen = true }
                        .padding(horizontal = 20.dp, vertical = 13.dp)
                        .testTag("floating_cart_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Cart icon + count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFF0A1805),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "$totalQty ${if (isBangla) "পণ্য" else "items"}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A1805)
                            )
                        }

                        // Center: Total Amount
                        Text(
                            text = "৳$totalAmount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0A1805)
                        )

                        // Right: Checkout ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isBangla) "চেকআউট" else "Checkout",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A1805)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF0A1805),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Checkout Bottom Sheet Modal
        if (isCartSheetOpen) {
            CartCheckoutBottomSheet(
                viewModel = viewModel,
                isBangla = isBangla,
                currentLanguage = currentLanguage,
                onDismiss = { isCartSheetOpen = false }
            )
        }

        // 6. Sale Success Confirmation Dialog
        if (completedSaleOrder != null) {
            SaleSuccessDialog(
                order = completedSaleOrder!!,
                isBangla = isBangla,
                currentLanguage = currentLanguage,
                onDismiss = { viewModel.dismissSuccessDialog() }
            )
        }

        // 7. Barcode Scanner Dialog
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
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = DokanVividGreen
                        )
                        Text(
                            text = if (isBangla) "বারকোড দিয়ে খুঁজুন" else "Barcode Lookup",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("sell_barcode_search_input"),
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
                            val prod = viewModel.findProductByBarcode(barcodeInput)
                            if (prod != null) {
                                viewModel.addToCart(prod, 1)
                                Toast.makeText(
                                    context,
                                    if (isBangla) "${prod.localizedName(true)} কার্টে যোগ করা হয়েছে" else "Added ${prod.name} to cart",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showBarcodeScanDialog = false
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "পণ্য পাওয়া যায়নি" else "No product found with this barcode",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DokanVividGreen)
                    ) {
                        Text(if (isBangla) "কার্টে যোগ করুন" else "Add to Cart", color = Color(0xFF0A1805), fontWeight = FontWeight.Bold)
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
 * Category Chips row with small icons matching Image 2
 */
@Composable
private fun SellCategoryChipsRow(
    selectedCategory: String?,
    dbCategories: List<Category>,
    onCategorySelected: (String?) -> Unit,
    isBangla: Boolean
) {
    // Categories with iconic representations
    val defaultCategories = listOf(
        CategoryChipItem("All", if (isBangla) "সব" else "All", null, Icons.Default.GridView),
        CategoryChipItem("Hand Tools", if (isBangla) "হ্যান্ড টুলস" else "Hand Tools", "Hand Tools", Icons.Default.Build),
        CategoryChipItem("Power Tools", if (isBangla) "পাওয়ার টুলস" else "Power Tools", "Power Tools", Icons.Default.PrecisionManufacturing),
        CategoryChipItem("Nuts & Bolts", if (isBangla) "নাট ও বোল্ট" else "Nuts & Bolts", "Nuts & Bolts", Icons.Default.Construction),
        CategoryChipItem("Electric", if (isBangla) "ইলেকট্রিক" else "Electric", "Electric", Icons.Default.ElectricBolt),
        CategoryChipItem("Paint", if (isBangla) "পেইন্ট" else "Paint", "Paint", Icons.Default.FormatPaint),
        CategoryChipItem("Plumbing", if (isBangla) "প্লাম্বিং" else "Plumbing", "Plumbing", Icons.Default.Plumbing)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        defaultCategories.forEach { item ->
            val isSelected = (item.catFilter == null && selectedCategory == null) ||
                    (item.catFilter != null && selectedCategory.equals(item.catFilter, ignoreCase = true))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) Color(0xFF152612) else Color(0xFF1E1E1E),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) DokanVividGreen else Color(0x22FFFFFF)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onCategorySelected(item.catFilter) }
                    .testTag("cat_chip_${item.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) DokanVividGreen else Color(0xAAFFFFFF),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = item.label,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DokanVividGreen else Color(0xDDFFFFFF)
                    )
                }
            }
        }
    }
}

private data class CategoryChipItem(
    val id: String,
    val label: String,
    val catFilter: String?,
    val icon: ImageVector
)

/**
 * 2-Column Glass Product Tile matching Image 2:
 * - Large glowing circular category icon in center
 * - Product title
 * - Vivid green price
 * - Stock dot (green normal, amber low) + unit text
 * - Bottom-right "+" square button
 * - Top-right badge "×N" when in cart
 * - Red diagonal "OUT OF STOCK" ribbon when out of stock
 * - Amber "Low" tag when low stock
 */
@Composable
private fun SellGlassProductTile(
    product: Product,
    cartQty: Int,
    isOutOfStock: Boolean,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onTileClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val isLowStock = product.isLowStock && !isOutOfStock
    val isAddedToCart = cartQty > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isAddedToCart) 1.5.dp else 1.dp,
                color = when {
                    isAddedToCart -> DokanVividGreen
                    isOutOfStock -> Color(0x33FF453A)
                    else -> Color(0x22FFFFFF)
                },
                shape = RoundedCornerShape(20.dp)
            )
            .background(Color(0xFF191919))
            .clickable(enabled = !isOutOfStock) { onTileClick() }
            .alpha(if (isOutOfStock) 0.55f else 1f)
            .testTag("sell_tile_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Large Glowing Icon in Center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                HardwareGlowingIcon(
                    category = product.category,
                    productName = product.nameEn.ifBlank { product.nameBn },
                    photoKey = product.photoKey,
                    containerSize = 80.dp,
                    iconSize = 38.dp,
                    tint = if (isOutOfStock) Color(0x77FFFFFF) else DokanVividGreen
                )
            }

            // Product Title
            Text(
                text = product.localizedName(isBangla),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Price in Vivid Green
            Text(
                text = "৳${product.sellPrice.toLong()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOutOfStock) TextMuted else DokanVividGreen
            )

            // Stock Dot, Unit & Bottom-Right "+" Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dot + Stock Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isOutOfStock -> Color(0xFFFF453A)
                                    isLowStock -> DokanAmber
                                    else -> DokanVividGreen
                                }
                            )
                    )
                    Text(
                        text = "${product.stockQuantity} ${product.localizedUnit(isBangla)}",
                        fontSize = 12.sp,
                        color = Color(0xBBFFFFFF)
                    )
                }

                // Square "+" button in bottom-right (Matching Image 2)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAddedToCart) DokanVividGreen else Color(0xFF262626))
                        .clickable(enabled = !isOutOfStock) { onAddClick() }
                        .testTag("btn_tile_add_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = if (isAddedToCart) Color(0xFF0A1805) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Top-Right Badges:

        // 1. In Cart Badge: "×N" (Image 2)
        if (isAddedToCart && !isOutOfStock) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF242424))
                    .border(1.dp, DokanVividGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "×$cartQty",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Amber "Low" tag (Image 2)
        if (isLowStock && !isAddedToCart) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                StatusTag(
                    text = if (isBangla) "স্বল্প" else "Low",
                    type = StatusTagType.WARNING
                )
            }
        }

        // 3. Diagonal Red "OUT OF STOCK" Ribbon (Image 2)
        if (isOutOfStock) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(72.dp)
                    .clipToBounds()
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .offset(x = 10.dp, y = 8.dp)
                        .rotate(45f)
                        .background(Color(0xFFD32F2F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "স্টক শেষ" else "OUT OF STOCK",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Sale Success Confirmation Dialog
 */
@Composable
private fun SaleSuccessDialog(
    order: SaleOrder,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DokanDeepGreen.copy(alpha = 0.3f))
                    .border(1.dp, DokanVividGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DokanVividGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isBangla) "বিক্রি সফল হয়েছে!" else "Sale Completed!",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${order.invoiceNumber} • ${order.customerName}",
                    fontSize = 13.sp,
                    color = Color(0xAAFFFFFF)
                )

                Surface(
                    color = Color(0xFF142410),
                    border = BorderStroke(1.dp, DokanVividGreen.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "মোট টাকা:" else "Total Amount:",
                            fontSize = 14.sp,
                            color = Color(0xCCFFFFFF)
                        )
                        Text(
                            text = "৳${order.totalAmount.toLong()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DokanVividGreen
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DokanVividGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBangla) "ঠিক আছে" else "Done",
                    color = Color(0xFF0A1805),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
