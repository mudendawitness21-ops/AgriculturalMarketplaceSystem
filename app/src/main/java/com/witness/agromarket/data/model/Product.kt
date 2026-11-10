package com.witness.agromarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val category: String,
    val price: String,
    val quantity: String,
    val sellerPhone: String = "",
    val sellerName: String = ""
)
