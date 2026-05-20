package com.example.girlfriend.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.girlfriend.data.entity.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): LiveData<List<Note>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAll(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)
}
