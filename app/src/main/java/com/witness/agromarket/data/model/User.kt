package com.witness.agromarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phone: String,
    val location: String,
    val role: String,
    val farmOrBusinessName: String = "",
    val password: String = "",
    val walletBalanceZmw: Double = 0.0,
    val linkedProvider: String = "",
    val linkedAccountNumber: String = ""
)
