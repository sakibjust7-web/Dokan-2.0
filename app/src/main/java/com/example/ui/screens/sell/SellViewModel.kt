package com.example.ui.screens.sell

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Category
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.data.repository.HardwareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CartItem(
    val product: Product,
    val quantity: Int,
    val unitPrice: Double = product.sellPrice,
    val total: Double = quantity * unitPrice
)

class SellViewModel(application: Application) : AndroidViewModel(application) {
    val repository: HardwareRepository = AppDatabase.getRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.visibleCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory
    ) { all, query, cat ->
        all.filter { prod ->
            val matchQuery = query.isBlank() ||
                    prod.nameBn.contains(query, ignoreCase = true) ||
                    prod.nameEn.contains(query, ignoreCase = true) ||
                    prod.barcode.contains(query, ignoreCase = true)
            val matchCat = cat == null || prod.category.equals(cat, ignoreCase = true)
            matchQuery && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _isSellSheetOpen = MutableStateFlow(false)
    val isSellSheetOpen: StateFlow<Boolean> = _isSellSheetOpen.asStateFlow()

    private val _completedSaleOrder = MutableStateFlow<SaleOrder?>(null)
    val completedSaleOrder: StateFlow<SaleOrder?> = _completedSaleOrder.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Form inputs for single sell sheet
    private val _quantityText = MutableStateFlow("1")
    val quantityText: StateFlow<String> = _quantityText.asStateFlow()

    private val _sellingPriceText = MutableStateFlow("")
    val sellingPriceText: StateFlow<String> = _sellingPriceText.asStateFlow()

    private val _discountText = MutableStateFlow("0")
    val discountText: StateFlow<String> = _discountText.asStateFlow()

    private val _paidNowText = MutableStateFlow("")
    val paidNowText: StateFlow<String> = _paidNowText.asStateFlow()

    private val _notesText = MutableStateFlow("")
    val notesText: StateFlow<String> = _notesText.asStateFlow()

    private val _isCreditSale = MutableStateFlow(false)
    val isCreditSale: StateFlow<Boolean> = _isCreditSale.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _dueDateDays = MutableStateFlow(30)
    val dueDateDays: StateFlow<Int> = _dueDateDays.asStateFlow()

    private val _showNewCustomerDialog = MutableStateFlow(false)
    val showNewCustomerDialog: StateFlow<Boolean> = _showNewCustomerDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
        if (product != null) {
            _quantityText.value = "1"
            _sellingPriceText.value = product.sellPrice.toString()
            _discountText.value = "0"
            _paidNowText.value = product.sellPrice.toString()
            _isCreditSale.value = false
            _notesText.value = ""
        }
    }

    fun openSellForm() {
        val prod = _selectedProduct.value ?: return
        _quantityText.value = "1"
        _sellingPriceText.value = prod.sellPrice.toString()
        _paidNowText.value = prod.sellPrice.toString()
        _isSellSheetOpen.value = true
    }

    fun closeSellForm() {
        _isSellSheetOpen.value = false
    }

    fun setQuantityText(text: String) {
        _quantityText.value = text
        recalculateTotals()
    }

    fun incrementQuantity() {
        val current = _quantityText.value.toIntOrNull() ?: 1
        val maxStock = _selectedProduct.value?.stockQuantity ?: 999
        if (current < maxStock) {
            _quantityText.value = (current + 1).toString()
            recalculateTotals()
        }
    }

    fun decrementQuantity() {
        val current = _quantityText.value.toIntOrNull() ?: 1
        if (current > 1) {
            _quantityText.value = (current - 1).toString()
            recalculateTotals()
        }
    }

    fun setSellingPriceText(text: String) {
        _sellingPriceText.value = text
        recalculateTotals()
    }

    fun setDiscountText(text: String) {
        _discountText.value = text
        recalculateTotals()
    }

    fun setPaidNowText(text: String) {
        _paidNowText.value = text
    }

    fun setNotesText(text: String) {
        _notesText.value = text
    }

    fun setCreditSale(isCredit: Boolean) {
        _isCreditSale.value = isCredit
        if (!isCredit) {
            val total = calculateCurrentTotal()
            _paidNowText.value = total.toString()
        } else {
            _paidNowText.value = "0"
        }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    private fun calculateCurrentTotal(): Double {
        val qty = _quantityText.value.toIntOrNull() ?: 1
        val price = _sellingPriceText.value.toDoubleOrNull() ?: (_selectedProduct.value?.sellPrice ?: 0.0)
        val discount = _discountText.value.toDoubleOrNull() ?: 0.0
        return ((qty * price) - discount).coerceAtLeast(0.0)
    }

    private fun recalculateTotals() {
        val total = calculateCurrentTotal()
        if (!_isCreditSale.value) {
            _paidNowText.value = total.toString()
        }
    }

    fun confirmSale(isBangla: Boolean) {
        val product = _selectedProduct.value ?: return
        val qty = _quantityText.value.toIntOrNull() ?: 1
        val unitPrice = _sellingPriceText.value.toDoubleOrNull() ?: product.sellPrice
        val discount = _discountText.value.toDoubleOrNull() ?: 0.0
        val grossTotal = qty * unitPrice
        val netTotal = (grossTotal - discount).coerceAtLeast(0.0)

        val paidAmount = if (_isCreditSale.value) {
            _paidNowText.value.toDoubleOrNull() ?: 0.0
        } else {
            netTotal
        }
        val dueAmount = (netTotal - paidAmount).coerceAtLeast(0.0)
        val totalCost = product.buyPrice * qty

        val invoiceNumber = "INV-${System.currentTimeMillis() % 1000000}"
        val cust = _selectedCustomer.value

        val order = SaleOrder(
            invoiceNumber = invoiceNumber,
            customerId = if (_isCreditSale.value) cust?.id else null,
            customerName = if (_isCreditSale.value) (cust?.name ?: "Customer") else "Cash Customer",
            customerPhone = if (_isCreditSale.value) (cust?.phone ?: "") else "",
            summaryItems = "${product.name} x $qty",
            totalAmount = netTotal,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            totalCost = totalCost,
            isCredit = _isCreditSale.value,
            totalItemsCount = qty,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.completeSale(order, listOf(product.id to qty))
            withContext(Dispatchers.Main) {
                _completedSaleOrder.value = order
                _isSellSheetOpen.value = false
                _selectedProduct.value = null
            }
        }
    }

    fun dismissSuccessDialog() {
        _completedSaleOrder.value = null
    }

    fun openNewCustomerDialog() {
        _showNewCustomerDialog.value = true
    }

    fun closeNewCustomerDialog() {
        _showNewCustomerDialog.value = false
    }

    fun addNewCustomer(name: String, phone: String, address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertCustomer(
                Customer(
                    name = name,
                    phone = phone,
                    address = address,
                    totalBaki = 0.0
                )
            )
            val newCustomer = Customer(
                id = id,
                name = name,
                phone = phone,
                address = address,
                totalBaki = 0.0
            )
            withContext(Dispatchers.Main) {
                _selectedCustomer.value = newCustomer
                _showNewCustomerDialog.value = false
            }
        }
    }

    // Cart operations
    fun addToCart(product: Product, quantity: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stockQuantity)
            currentList[index] = existing.copy(quantity = newQty, total = newQty * existing.unitPrice)
        } else {
            val qty = quantity.coerceAtMost(product.stockQuantity)
            currentList.add(CartItem(product = product, quantity = qty, unitPrice = product.sellPrice, total = qty * product.sellPrice))
        }
        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(productId: Long, qty: Int) {
        if (qty <= 0) {
            _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
        } else {
            _cartItems.value = _cartItems.value.map { item ->
                if (item.product.id == productId) {
                    val finalQty = qty.coerceAtMost(item.product.stockQuantity)
                    item.copy(quantity = finalQty, total = finalQty * item.unitPrice)
                } else item
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun confirmCartSale(
        customer: Customer?,
        isCredit: Boolean,
        paidAmountInput: Double,
        discountInput: Double,
        notes: String
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val gross = items.sumOf { it.total }
        val netTotal = (gross - discountInput).coerceAtLeast(0.0)
        val paid = if (isCredit) paidAmountInput.coerceAtMost(netTotal) else netTotal
        val due = (netTotal - paid).coerceAtLeast(0.0)
        val totalCost = items.sumOf { it.product.buyPrice * it.quantity }

        val summary = items.joinToString(", ") { "${it.product.name} x ${it.quantity}" }
        val invoiceNumber = "INV-${System.currentTimeMillis() % 1000000}"

        val order = SaleOrder(
            invoiceNumber = invoiceNumber,
            customerId = if (isCredit) customer?.id else null,
            customerName = if (isCredit) (customer?.name ?: "Customer") else "Cash Customer",
            customerPhone = if (isCredit) (customer?.phone ?: "") else "",
            summaryItems = summary,
            totalAmount = netTotal,
            paidAmount = paid,
            dueAmount = due,
            totalCost = totalCost,
            isCredit = isCredit,
            totalItemsCount = items.sumOf { it.quantity },
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.completeSale(order, items.map { it.product.id to it.quantity })
            withContext(Dispatchers.Main) {
                _cartItems.value = emptyList()
                _completedSaleOrder.value = order
            }
        }
    }

    fun findProductByBarcode(barcode: String): Product? {
        return products.value.find { it.barcode.equals(barcode.trim(), ignoreCase = true) }
    }
}
