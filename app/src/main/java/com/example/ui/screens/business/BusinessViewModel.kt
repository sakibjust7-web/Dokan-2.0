package com.example.ui.screens.business

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Supplier
import com.example.data.repository.HardwareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FinancialSummary(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val customerReceivableDue: Double = 0.0,
    val supplierPayableDena: Double = 0.0
)

class BusinessViewModel(application: Application) : AndroidViewModel(application) {
    val repository: HardwareRepository = AppDatabase.getRepository(application)

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliersOwedFirst
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialSummary: StateFlow<FinancialSummary> = combine(
        repository.allSales,
        repository.allExpenses,
        repository.allCustomers,
        repository.allSuppliersOwedFirst
    ) { sales, exps, customers, sups ->
        val revenue = sales.sumOf { it.totalAmount }
        val gross = sales.sumOf { it.profit }
        val totalExp = exps.sumOf { it.amount }
        val net = gross - totalExp
        val custDue = customers.sumOf { it.totalBaki }
        val supDena = sups.sumOf { it.amountOwed }

        FinancialSummary(
            totalRevenue = revenue,
            totalExpenses = totalExp,
            grossProfit = gross,
            netProfit = net,
            customerReceivableDue = custDue,
            supplierPayableDena = supDena
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun addExpense(title: String, amount: Double, category: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(
                Expense(
                    title = title,
                    amount = amount,
                    category = category,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expenseId)
        }
    }

    fun addSupplier(name: String, company: String, phone: String, amountOwed: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSupplier(
                Supplier(
                    name = name,
                    company = company,
                    phone = phone,
                    amountOwed = amountOwed
                )
            )
        }
    }

    fun recordSupplierPayment(supplierId: Long, amount: Double, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordSupplierPayment(
                supplierId = supplierId,
                amount = amount,
                note = note
            )
        }
    }
}
