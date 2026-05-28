package com.example.data

import kotlinx.coroutines.flow.Flow

class ShopRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val saleDao = database.saleDao()
    private val purchaseDao = database.purchaseDao()
    private val expenseDao = database.expenseDao()

    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // Sales
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()

    suspend fun insertSale(sale: Sale): Long {
        // Automatically deduct stock quantity from the product when a sale is recorded
        productDao.adjustStock(sale.productId, -sale.quantity)
        return saleDao.insertSale(sale)
    }

    suspend fun deleteSale(sale: Sale) {
        // Rollback stock when a sale is deleted/canceled
        productDao.adjustStock(sale.productId, sale.quantity)
        saleDao.deleteSale(sale)
    }

    // Purchases
    val allPurchases: Flow<List<Purchase>> = purchaseDao.getAllPurchases()

    suspend fun insertPurchase(purchase: Purchase): Long {
        // Automatically add stock quantity to the product when restocked
        productDao.adjustStock(purchase.productId, purchase.quantity)
        return purchaseDao.insertPurchase(purchase)
    }

    suspend fun deletePurchase(purchase: Purchase) {
        // Rollback stock when a purchase is deleted/canceled
        productDao.adjustStock(purchase.productId, -purchase.quantity)
        purchaseDao.deletePurchase(purchase)
    }

    // Expenses
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
}
