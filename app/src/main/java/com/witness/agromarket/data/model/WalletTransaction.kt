package com.witness.agromarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val referenceId: String,
    val userPhone: String,
    val type: String, // "DEPOSIT", "PAYMENT_SENT", "PAYMENT_RECEIVED", "WITHDRAWAL"
    val channel: String,
    val accountNumber: String,
    val amountZmw: Double,
    val status: String,
    val description: String
)
