package com.witness.agromarket.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.witness.agromarket.data.model.User

@Database(
    entities = [User::class],
    version = 1
)
abstract class AgroMarketDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
}