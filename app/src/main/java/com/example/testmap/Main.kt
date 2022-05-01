package com.example.testmap

import android.app.Application
import com.example.testmap.loginInfo.LoginInfoDatabase
import com.example.testmap.loginInfo.LoginInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class Main: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val db by lazy {LoginInfoDatabase.getDatabase(this, applicationScope)}
    val repository by lazy {LoginInfoRepository(db.loginInfoDao())}

}