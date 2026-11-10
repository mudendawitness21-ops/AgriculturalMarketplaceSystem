package com.witness.agromarket.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.witness.agromarket.data.model.Order
import com.witness.agromarket.data.model.Product
import com.witness.agromarket.data.model.User
import com.witness.agromarket.data.model.WalletTransaction

@Database(
    entities = [User::class, Product::class, Order::class, WalletTransaction::class],
    version = 5,
    exportSchema = false
)
abstract class AgroMarketDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: AgroMarketDatabase? = null

        fun getDatabase(context: Context): AgroMarketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgroMarketDatabase::class.java,
                    "agromarket_permanent_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
