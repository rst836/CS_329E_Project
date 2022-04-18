package com.example.testmap.LoginInfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import kotlin.IllegalArgumentException

class LoginInfoViewModel(private val repository:LoginInfoRepository):ViewModel() {
    val allLogins: LiveData<List<LoginInfo>> = repository.allLogins.asLiveData()
}

class LoginInfoViewModelFactory(private val repository:LoginInfoRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginInfoViewModel::class.java)) {
            return LoginInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewModel class type")
    }
}