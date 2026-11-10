package com.witness.agromarket.data.repository

import androidx.room.*
import com.witness.agromarket.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: User)

    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun getUserByCredentials(phone: String, password: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    fun observeUserByPhone(phone: String): Flow<User?>

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}
