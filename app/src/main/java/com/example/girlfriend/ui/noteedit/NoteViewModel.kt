package com.example.girlfriend.ui.noteedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.girlfriend.data.AppDatabase
import com.example.girlfriend.data.entity.Note
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val noteDao = db.noteDao()

    fun getNotesByCategory(category: String): LiveData<List<Note>> {
        return noteDao.getByCategory(category)
    }

    fun save(note: Note) {
        viewModelScope.launch {
            noteDao.insert(note)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }
}
