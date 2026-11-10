package com.example.ui.screens.sell

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Customer
import com.example.ui.components.DokanSegmentedControl
import com.example.ui.components.HeroGlassCard
import com.example.ui.components.SlideToConfirmSale
import com.example.ui.components.getHardwareCategoryIcon
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.DokanDeepGreen
import com.example.ui.theme.DokanTheme
import com.example.ui.theme.DokanVividGreen
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutBottomSheet(
    viewModel: SellViewModel,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    var isCredit by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var discountText by remember { mutableStateOf("0") }
    var notesText by remember { mutableStateOf("") }
    var paidAmountText by remember { mutableStateOf("") }
    var showNewCustomerDialog by remember { mutableStateOf(false) }

    val subtotal = remember(cartItems) { cartItems.sumOf { it.total } }
    val discountVal = discountText.toDoubleOrNull() ?: 0.0
    val isDiscountExcessive = discountVal > subtotal && subtotal > 0
    val netTotal = (subtotal - discountVal).coerceAtLeast(0.0)

    val invoiceId = remember {
        val num = (1000 + (System.currentTimeMillis() % 9000)).toInt()
        "INV-$num"
    }
    val currentDateStr = remember {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        sdf.format(Date())
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val contactName = if (nameIdx >= 0) cursor.getString(nameIdx) else ""
                    val contactId = if (idIdx >= 0) cursor.getString(idIdx) else ""

                    var contactPhone = ""
                    val phoneCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        val pIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (pIdx >= 0) contactPhone = phoneCursor.getString(pIdx)
                        phoneCursor.close()
                    }
                    cursor.close()

                    if (contactName.isNotBlank()) {
                        viewModel.addNewCustomer(contactName, contactPhone, "")
                        Toast.makeText(context, "Added $contactName", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not read contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141414),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Bar: [✕]  Checkout   [INV-1004 · 10 Nov 2026]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (isBangla) "চেকআউট" else "Checkout",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Invoice & Date pill badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF222222),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Text(
                        text = "$invoiceId · $currentDateStr",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xDDFFFFFF),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 2. Hero Total Card (Matching Image 1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = DokanVividGreen.copy(alpha = 0.4f),
                        spotColor = DokanVividGreen.copy(alpha = 0.5f)
                    )
                    .drawBehind {
                        // Ambient green glow behind total
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    DokanVividGreen.copy(alpha = 0.28f),
                                    DokanDeepGreen.copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width * 0.65f
                            ),
                            radius = size.width * 0.65f,
                            center = center
                        )
                    }
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                DokanVividGreen,
                                DokanVividGreen.copy(alpha = 0.6f),
                                Color(0x335DD62C)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF142410),
                                Color(0xFF101C0D)
                            )
                        )
                    )
                    .padding(vertical = 22.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isBangla) "সর্বমোট" else "TOTAL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DokanVividGreen,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "৳${netTotal.toLong()}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = DokanVividGreen,
                        textAlign = TextAlign.Center
                    )

                    val subStr = "৳${subtotal.toLong()}"
                    val discStr = "৳${discountVal.toLong()}"
                    Text(
                        text = if (isBangla) "উপমোট $subStr — ছাড় $discStr" else "Subtotal $subStr — Discount $discStr",
                        fontSize = 13.sp,
                        color = Color(0xBBFFFFFF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 3. Compact Cart Rows (Matching Image 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1C1C1C)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cartItems.isEmpty()) {
                        Text(
                            text = if (isBangla) "কার্ট খালি" else "Cart is empty",
                            color = TextMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        cartItems.forEach { item ->
                            val itemIcon = getHardwareCategoryIcon(item.product.category, item.product.name)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Circular icon container + Product title
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF282828))
                                            .border(1.dp, Color(0x33FFFFFF), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = itemIcon,
                                            contentDescription = null,
                                            tint = Color(0xFFDDDDDD),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Text(
                                        text = item.product.localizedName(isBangla),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Stepper Pill: [ −  ×2  + ]
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFF282828),
                                    border = BorderStroke(1.dp, Color(0x22FFFFFF))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    viewModel.updateCartItemQuantity(item.product.id, item.quantity - 1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                tint = Color(0xCCFFFFFF),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "×${item.quantity}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    viewModel.updateCartItemQuantity(item.product.id, item.quantity + 1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = Color(0xCCFFFFFF),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Item Total in Vivid Green
                                Text(
                                    text = "৳${item.total.toLong()}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DokanVividGreen
                                )
                            }
                        }
                    }
                }
            }

            // 4. Discount Field with Warning (Matching Image 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isDiscountExcessive) LowStockRed else Color(0x22FFFFFF),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1C)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBangla) "ছাড় " else "Discount ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "৳$discountText",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DokanVividGreen
                            )
                        }

                        // Compact inline editor
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "৳",
                                color = DokanVividGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            BasicTextField(
                                value = discountText,
                                onValueChange = { discountText = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(DokanVividGreen),
                                modifier = Modifier
                                    .width(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF282828))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("checkout_discount_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isDiscountExcessive) {
                            if (isBangla) "ছাড় পণ্যের মোট মূল্যের চেয়ে বেশি হতে পারে না" else "Cannot be more than the price"
                        } else {
                            if (isBangla) "মূল্যের চেয়ে বেশি ছাড় দেওয়া যাবে না" else "Cannot be more than the price"
                        },
                        fontSize = 12.sp,
                        color = if (isDiscountExcessive) LowStockRed else Color(0x77FFFFFF)
                    )
                }
            }

            // 5. Segmented Control: [ Paid in Full ]  [ Baki (Credit) ] (Matching Image 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paid in Full Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(
                            if (!isCredit) DokanVividGreen else Color.Transparent
                        )
                        .clickable { isCredit = false }
                        .testTag("checkout_paid_in_full_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "সম্পূর্ণ পরিশোধ (Paid in Full)" else "Paid in Full",
                        color = if (!isCredit) Color(0xFF0A1805) else Color(0x99FFFFFF),
                        fontSize = 13.5.sp,
                        fontWeight = if (!isCredit) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Baki (Credit) Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(
                            if (isCredit) Color(0xFF332020) else Color.Transparent
                        )
                        .border(
                            width = if (isCredit) 1.dp else 0.dp,
                            color = if (isCredit) LowStockRed else Color.Transparent,
                            shape = RoundedCornerShape(21.dp)
                        )
                        .clickable { isCredit = true }
                        .testTag("checkout_baki_credit_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "বাকি বিক্রি (Baki)" else "Baki (Credit)",
                        color = if (isCredit) LowStockRed else Color(0x99FFFFFF),
                        fontSize = 13.5.sp,
                        fontWeight = if (isCredit) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // 6. Customer Chips with Letter Initials (Matching Image 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cash Sale Chip [ C ]
                val isCashSelected = selectedCustomer == null
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCashSelected && !isCredit) Color(0xFF162413) else Color(0xFF1C1C1C),
                    border = BorderStroke(
                        1.dp,
                        if (isCashSelected && !isCredit) DokanVividGreen else Color(0x22FFFFFF)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (!isCredit) {
                                selectedCustomer = null
                            } else {
                                Toast.makeText(context, if (isBangla) "বাকির জন্য গ্রাহক নির্বাচন করুন" else "Select a customer for credit", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("checkout_customer_cash")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262626))
                                .border(
                                    1.dp,
                                    if (isCashSelected && !isCredit) DokanVividGreen else Color(0x33FFFFFF),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                color = if (isCashSelected && !isCredit) DokanVividGreen else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) "নগদ বিক্রি" else "Cash Sale",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCashSelected && !isCredit) DokanVividGreen else Color(0xAAFFFFFF)
                        )
                    }
                }

                // Dynamic Customers from Database
                val displayedCustomers = if (allCustomers.isNotEmpty()) allCustomers.take(6) else listOf(
                    Customer(id = -1, name = "Karim Builders", phone = "01711000000"),
                    Customer(id = -2, name = "Hasan Traders", phone = "01811000000")
                )

                displayedCustomers.forEach { cust ->
                    val isCustSelected = selectedCustomer?.id == cust.id
                    val initial = cust.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCustSelected) Color(0xFF162413) else Color(0xFF1C1C1C),
                        border = BorderStroke(
                            1.dp,
                            if (isCustSelected) DokanVividGreen else Color(0x22FFFFFF)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedCustomer = cust }
                            .testTag("checkout_customer_${cust.id}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262626))
                                    .border(
                                        1.dp,
                                        if (isCustSelected) DokanVividGreen else Color(0x33FFFFFF),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = if (isCustSelected) DokanVividGreen else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cust.name.split(" ").take(2).joinToString(" "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isCustSelected) DokanVividGreen else Color(0xAAFFFFFF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // + Contact Button (Image 1 rightmost button)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1C1C1C),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showNewCustomerDialog = true }
                        .testTag("checkout_add_contact_btn")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262626))
                                .border(1.dp, Color(0x33FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Contact",
                                tint = Color(0xAAFFFFFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) "+ কন্টাক্ট" else "+ Contact",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xAAFFFFFF)
                        )
                    }
                }
            }

            // 7. If Credit is active, show optional partial payment amount
            AnimatedVisibility(visible = isCredit) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1C1C1C),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "নগদ জমা (ঐচ্ছিক):" else "Paid Now (Optional):",
                            fontSize = 13.sp,
                            color = Color(0xBBFFFFFF)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("৳", color = DokanVividGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                            BasicTextField(
                                value = paidAmountText,
                                onValueChange = { paidAmountText = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                cursorBrush = SolidColor(DokanVividGreen),
                                modifier = Modifier
                                    .width(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF282828))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 8. Notes Text Field (Matching Image 1)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1C),
                border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (notesText.isEmpty()) {
                        Text(
                            text = if (isBangla) "নোট / মন্তব্য" else "Notes",
                            color = Color(0x66FFFFFF),
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(DokanVividGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 9. "Slide to confirm sale" Slider (Matching Image 1)
            SlideToConfirmSale(
                onConfirmed = {
                    if (isCredit && selectedCustomer == null) {
                        Toast.makeText(
                            context,
                            if (isBangla) "অনুগ্রহ করে বাকি বিক্রির জন্য একজন গ্রাহক নির্বাচন করুন" else "Please select a customer for credit sale",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@SlideToConfirmSale
                    }
                    if (isDiscountExcessive) {
                        Toast.makeText(
                            context,
                            if (isBangla) "ছাড় মূল্যের চেয়ে বেশি হতে পারে না" else "Discount cannot exceed price",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@SlideToConfirmSale
                    }

                    val paid = paidAmountText.toDoubleOrNull() ?: if (isCredit) 0.0 else netTotal
                    viewModel.confirmCartSale(
                        customer = if (isCredit) selectedCustomer else null,
                        isCredit = isCredit,
                        paidAmountInput = paid,
                        discountInput = discountVal,
                        notes = notesText
                    )
                    onDismiss()
                },
                enabled = cartItems.isNotEmpty() && !isDiscountExcessive,
                text = if (isBangla) "বিক্রি নিশ্চিত করতে স্লাইড করুন" else "Slide to confirm sale",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }

    // New Customer Dialog
    if (showNewCustomerDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newAddress by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewCustomerDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text(
                    text = if (isBangla) "নতুন গ্রাহক যোগ করুন" else "Add New Customer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(if (isBangla) "গ্রাহকের নাম *" else "Customer Name *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DokanVividGreen,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text(if (isBangla) "ফোন নম্বর" else "Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DokanVividGreen,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )
                    Button(
                        onClick = {
                            try {
                                contactPickerLauncher.launch(null)
                                showNewCustomerDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open contacts", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "ফোন বুক থেকে আনুন" else "Pick from Phone Contacts")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addNewCustomer(newName.trim(), newPhone.trim(), newAddress.trim())
                            showNewCustomerDialog = false
                        } else {
                            Toast.makeText(context, if (isBangla) "নাম আবশ্যক" else "Name is required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DokanVividGreen)
                ) {
                    Text(if (isBangla) "সংরক্ষণ" else "Save", color = Color(0xFF0A1805), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCustomerDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = Color(0x88FFFFFF))
                }
            }
        )
    }
}
