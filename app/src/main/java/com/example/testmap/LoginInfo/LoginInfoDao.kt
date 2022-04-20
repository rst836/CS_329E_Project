package com.example.testmap.loginInfo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginInfoDao {
    @Query("SELECT * FROM login_table WHERE app='bird'")
    fun getBirdLogin(): Flow<List<LoginInfo>>

    @Query("SELECT * FROM login_table WHERE app='bird'")
    fun getLimeLogin(): Flow<List<LoginInfo>>

    @Query("SELECT * FROM login_table")
    fun getAllLogins(): Flow<List<LoginInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogin(record: LoginInfo)

    @Query("DELETE FROM login_table")
    suspend fun deleteAll()

}