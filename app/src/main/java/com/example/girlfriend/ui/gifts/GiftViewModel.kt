package com.example.girlfriend.ui.gifts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.girlfriend.data.AppDatabase
import com.example.girlfriend.data.entity.Gift
import kotlinx.coroutines.launch

class GiftViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val giftDao = db.giftDao()

    val allGifts: LiveData<List<Gift>> = giftDao.getAll()

    fun getByStatus(status: String): LiveData<List<Gift>> {
        return giftDao.getByStatus(status)
    }

    fun save(gift: Gift) {
        viewModelScope.launch {
            giftDao.insert(gift)
        }
    }

    fun delete(gift: Gift) {
        viewModelScope.launch {
            giftDao.delete(gift)
        }
    }
}
