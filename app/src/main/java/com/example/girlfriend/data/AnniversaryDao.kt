package com.example.girlfriend.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.girlfriend.data.entity.Anniversary

@Dao
interface AnniversaryDao {
    @Query("SELECT * FROM anniversaries ORDER BY date ASC")
    fun getAll(): LiveData<List<Anniversary>>

    @Query("SELECT * FROM anniversaries WHERE id = :id")
    suspend fun getById(id: String): Anniversary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anniversary: Anniversary)

    @Update
    suspend fun update(anniversary: Anniversary)

    @Delete
    suspend fun delete(anniversary: Anniversary)

    @Query("DELETE FROM anniversaries WHERE id = :id")
    suspend fun deleteById(id: String)
}
