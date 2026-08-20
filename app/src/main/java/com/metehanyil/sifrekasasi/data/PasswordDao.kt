package com.metehanyil.sifrekasasi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM password_entries ORDER BY siteName COLLATE NOCASE ASC")
    fun getAll(): Flow<List<PasswordEntry>>

    @Query("SELECT * FROM password_entries WHERE id = :id")
    suspend fun getById(id: Long): PasswordEntry?

    @Insert
    suspend fun insert(entry: PasswordEntry): Long

    @Update
    suspend fun update(entry: PasswordEntry)

    @Delete
    suspend fun delete(entry: PasswordEntry)
}
