package com.witness.agromarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val quantity: String,
    val totalPriceZmw: Double,
    val status: String,
    val buyerOrFarmerName: String,
    val buyerPhone: String = "",
    val sellerPhone: String = "",
    val sellerName: String = "",
    val paymentStatus: String = "Unpaid",
    val paymentMethod: String = ""
)
