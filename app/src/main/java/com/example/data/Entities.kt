package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val unit: String,
    val supplierName: String
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val discount: Double,
    val customerNamePhone: String, // Optional format (e.g. "রহিম / 01700")
    val paymentMethod: String,     // Cash/Card/Mobile Banking
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val supplierName: String,
    val memoNo: String,             // চালান নম্বর / Memo No
    val quantity: Double,
    val purchasePrice: Double,
    val paymentStatus: String,      // Paid/Due
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expenseCategory: String,    // ভাড়া, বেতন, বিদ্যুৎ ইত্যাদি
    val amount: Double,             // পরিমাণ (টাকা)
    val notes: String,              // মন্তব্য (ঐচ্ছিক)
    val timestamp: Long             // তারিখ (Time in millis)
)
