package com.example.testmap.fragmentsManage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TestViewModel: ViewModel() {
    val currBird: MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>()
    }
    val currLime: MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>()
    }
    val nextFrag: MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>()
    }
    val inManage: MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>()
    }
}