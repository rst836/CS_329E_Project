package com.example.testmap.loginInfo

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities=[LoginInfo::class], version = 1, exportSchema = false)
public abstract class LoginInfoDatabase:RoomDatabase() {

    abstract fun loginInfoDao(): LoginInfoDao

    private class LoginInfoDatabaseCallback (
        private val scope:CoroutineScope
            ): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.loginInfoDao())
                }
            }
        }

        suspend fun populateDatabase(loginInfoDao: LoginInfoDao) {
            loginInfoDao.deleteAll()
            var newLogin = LoginInfo("bird", "mariojjuguilon@gmail.com")
            loginInfoDao.insertLogin(newLogin)

            newLogin = LoginInfo("lime", "mariojjuguilon@gmail.com")
            loginInfoDao.insertLogin(newLogin)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LoginInfoDatabase? = null

        fun getDatabase(context:Context, scope:CoroutineScope): LoginInfoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    LoginInfoDatabase::class.java,
                    "login_info_database"
                ).addCallback(LoginInfoDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }

}