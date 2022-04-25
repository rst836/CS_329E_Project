package com.example.testmap.loginInfo

import kotlinx.coroutines.flow.Flow

class LoginInfoRepository (private val loginInfoDao:LoginInfoDao){
    val allLogins: Flow<List<LoginInfo>> = loginInfoDao.getAllLogins()
}