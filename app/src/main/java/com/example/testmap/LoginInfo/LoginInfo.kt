package com.example.testmap.LoginInfo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName="login_table")
data class LoginInfo(@PrimaryKey @ColumnInfo(name = "app") val app:String,
                     @ColumnInfo(name = "email") val email:String?=null,
                     @ColumnInfo(name = "phone") val phone:Int?=null)
