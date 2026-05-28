package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ShopRepository(database)
    }

    // Reactively observe tables
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<Purchase>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dropdown Option Constants in Bengali
    val categories = listOf("মুদি পণ্য (Groceries)", "ইলেকট্রনিক্স (Electronics)", "কাঁচাবাজার (Fruits/Veg)", "ঔষধ (Medicine)", "ডেইরি ও বেকারি (Dairy/Bakery)", "অন্যান্য (Others)")
    val units = listOf("কেজি (kg)", "পিস (piece)", "লিটার (liter)", "প্যাকেট (packet)", "হালি / ডজন", "গ্রাম (gram)")
    val expenseCategories = listOf("দোকান ভাড়া (Rent)", "বেতন (Salary)", "বিদ্যুৎ বিল (Electricity)", "পরিবহন (Transport)", "অন্যান্য (Others)")
    val paymentMethods = listOf("Cash (নগদ)", "bKash/Nagad/Rocket (মোবাইল ব্যাংকিং)", "Card (কার্ড)")
    val paymentStatuses = listOf("Paid (পরিশোধিত)", "Due (বাকি)")

    // Dashboard calculations from Flows
    val totalSalesVal: StateFlow<Double> = sales.map { list ->
        list.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpensesVal: StateFlow<Double> = expenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalStockVal: StateFlow<Double> = products.map { list ->
        list.sumOf { it.stockQuantity * it.purchasePrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val estimatedProfitVal: StateFlow<Double> = sales.map { list ->
        // Sales total - calculated purchase cost of sold goods to approximate actual sales profit
        list.sumOf { sale ->
            val totalSelling = sale.totalPrice
            // We find product's purchase price to get cost, or use default profit ratio of 15% if product not found
            val unitPurchasePrice = findProductPurchasePrice(sale.productId)
            val costPrice = unitPurchasePrice * sale.quantity
            val profit = totalSelling - costPrice
            profit
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private fun findProductPurchasePrice(productId: Int): Double {
        return products.value.find { it.id == productId }?.purchasePrice ?: 0.0
    }

    // Operations
    fun addProduct(
        name: String,
        category: String,
        purchasePrice: Double,
        sellingPrice: Double,
        stockQuantity: Double,
        unit: String,
        supplierName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("দয়া করে পণ্যের নাম দিন")
            return
        }
        if (category.isBlank()) {
            onError("দয়া করে ক্যাটাগরি নির্বাচন করুন")
            return
        }
        if (purchasePrice < 0) {
            onError("ক্রয় মূল্য নেতিবাচক হতে পারে না")
            return
        }
        if (sellingPrice < 0) {
            onError("বিক্রয় মূল্য নেতিবাচক হতে পারে না")
            return
        }
        if (stockQuantity < 0) {
            onError("স্টক শূন্যের চেয়ে কম হতে পারে না")
            return
        }
        if (unit.isBlank()) {
            onError("দয়া করে পণ্যের ইউনিট নির্বাচন করুন")
            return
        }

        viewModelScope.launch {
            try {
                val newProd = Product(
                    name = name.trim(),
                    category = category,
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    stockQuantity = stockQuantity,
                    unit = unit,
                    supplierName = supplierName.trim()
                )
                repository.insertProduct(newProd)
                onSuccess()
            } catch (e: Exception) {
                onError("পণ্য যোগ করতে সমস্যা হয়েছে: ${e.message}")
            }
        }
    }

    fun makeSale(
        productId: Int,
        quantity: Double,
        discount: Double,
        customerNamePhone: String,
        paymentMethod: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val product = products.value.find { it.id == productId }
        if (product == null) {
            onError("নির্বাচিত পণ্যটি পাওয়া যায়নি")
            return
        }
        if (quantity <= 0) {
            onError("পরিমাণ শুন্যের চেয়ে বেশি হতে হবে")
            return
        }
        if (quantity > product.stockQuantity) {
            onError("যথেষ্ট স্টক নেই! বর্তমান স্টক: ${product.stockQuantity} ${product.unit}")
            return
        }
        if (discount < 0) {
            onError("ডিসকাউন্ট নেতিবাচক হতে পারে না")
            return
        }

        val totalPrice = (product.sellingPrice * quantity) - discount
        if (totalPrice < 0) {
            onError("ডিসকাউন্ট মোট মূল্যের চেয়ে বেশি হতে পারে না")
            return
        }

        viewModelScope.launch {
            try {
                val newSale = Sale(
                    productId = productId,
                    productName = product.name,
                    quantity = quantity,
                    unitPrice = product.sellingPrice,
                    totalPrice = totalPrice,
                    discount = discount,
                    customerNamePhone = customerNamePhone.trim(),
                    paymentMethod = paymentMethod
                )
                repository.insertSale(newSale)
                onSuccess()
            } catch (e: Exception) {
                onError("বিক্রয় হিসাব সংরক্ষণ করতে সমস্যা হয়েছে: ${e.message}")
            }
        }
    }

    fun makePurchase(
        supplierName: String,
        memoNo: String,
        productId: Int,
        quantity: Double,
        purchasePrice: Double,
        paymentStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val product = products.value.find { it.id == productId }
        if (product == null) {
            onError("অনুগ্রহ করে একটি পণ্য নির্বাচন করুন")
            return
        }
        if (quantity <= 0) {
            onError("পরিমাণ শুন্যের চেয়ে বেশি হতে হবে")
            return
        }
        if (purchasePrice <= 0) {
            onError("ক্রয়মূল্য শুন্য বা তার বেশি হতে হবে")
            return
        }
        if (supplierName.isBlank()) {
            onError("মনোনীত সাপ্লায়ারের নাম লিখুন")
            return
        }
        if (memoNo.isBlank()) {
            onError("চালান নম্বর বা মেমো নং লিখুন")
            return
        }

        viewModelScope.launch {
            try {
                val newPurchase = Purchase(
                    productId = productId,
                    productName = product.name,
                    supplierName = supplierName.trim(),
                    memoNo = memoNo.trim(),
                    quantity = quantity,
                    purchasePrice = purchasePrice,
                    paymentStatus = paymentStatus
                )
                repository.insertPurchase(newPurchase)
                onSuccess()
            } catch (e: Exception) {
                onError("ক্রয় ও স্টক আপডেট সংরক্ষণ করতে সমস্যা হয়েছে: ${e.message}")
            }
        }
    }

    fun addExpense(
        category: String,
        amount: Double,
        notes: String,
        dateMillis: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (category.isBlank()) {
            onError("দয়া করে খরচের খাত নির্বাচন করুন")
            return
        }
        if (amount <= 0) {
            onError("খরচের পরিমাণ ০ এর চেয়ে বেশি হতে হবে")
            return
        }

        viewModelScope.launch {
            try {
                val newExpense = Expense(
                    expenseCategory = category,
                    amount = amount,
                    notes = notes.trim(),
                    timestamp = dateMillis
                )
                repository.insertExpense(newExpense)
                onSuccess()
            } catch (e: Exception) {
                onError("খরচ সংরক্ষণ করতে সমস্যা হয়েছে: ${e.message}")
            }
        }
    }

    // Rollback / deletion operations (for high reliability)
    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    fun deleteSale(sale: Sale) {
        viewModelScope.launch { repository.deleteSale(sale) }
    }

    fun deletePurchase(purchase: Purchase) {
        viewModelScope.launch { repository.deletePurchase(purchase) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}
