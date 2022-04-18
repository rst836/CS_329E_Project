package com.example.testmap

import android.app.Application
import com.example.testmap.LoginInfo.LoginInfoDatabase
import com.example.testmap.LoginInfo.LoginInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class Main: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val db by lazy {LoginInfoDatabase.getDatabase(this, applicationScope)}
    val repository by lazy {LoginInfoRepository(db.loginInfoDao())}

}