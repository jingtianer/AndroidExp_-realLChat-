package com.jingtian.lchat.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import android.util.Log
import com.jingtian.lchat.BaseApp

class DBHelper(context: Context, name:String, private val createSQL:String): SQLiteOpenHelper(context, name, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createSQL)
        //创建数据库
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    companion object {
        val dbs:HashMap<String, DBHelper> = HashMap()
        val db_creat:Map<String,String> = mutableMapOf(
                BaseApp.dbName to "create table mesg(id integer primary key autoincrement, content text, time integer, orient integer, to_id text);"
        )
        //所有数据库的创建语句
        fun get_db(context: Context, name: String):DBHelper {
            if (!dbs.containsKey(name)) {
                dbs.put(name, DBHelper(context, name, db_creat[name]!!))
                Log.d("sql", dbs.containsKey(name).toString())
            }
            return dbs.get(name)!!
        }
        //数据库单例
    }
}