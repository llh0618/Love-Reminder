package com.example.girlfriend.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.girlfriend.data.entity.Gift

@Dao
interface GiftDao {
    @Query("SELECT * FROM gifts ORDER BY createdAt DESC")
    fun getAll(): LiveData<List<Gift>>

    @Query("SELECT * FROM gifts ORDER BY createdAt DESC")
    suspend fun getAllList(): List<Gift>

    @Query("SELECT * FROM gifts WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: String): LiveData<List<Gift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gift: Gift)

    @Update
    suspend fun update(gift: Gift)

    @Delete
    suspend fun delete(gift: Gift)

    @Query("DELETE FROM gifts WHERE id = :id")
    suspend fun deleteById(id: String)
}
