package com.witness.agromarket.data.repository

import androidx.room.*
import com.witness.agromarket.data.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)

    @Query("SELECT * FROM wallet_transactions WHERE userPhone = :phone ORDER BY id DESC")
    fun getTransactionsForUser(phone: String): Flow<List<WalletTransaction>>
}
