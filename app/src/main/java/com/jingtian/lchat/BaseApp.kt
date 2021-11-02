package com.jingtian.lchat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import com.jingtian.lchat.Net.OKHttpHelper
import com.jingtian.lchat.Service.NotifyService
import com.jingtian.lchat.util.DBHelper
import com.jingtian.lchat.util.SP
import com.jingtian.lchat.util.SPValues
import okhttp3.Call
import java.util.*
import kotlin.properties.Delegates
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.runOnUiThread
import java.io.IOException
import java.lang.Exception
import java.time.LocalDate

class BaseApp: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        sps = SPValues(this.applicationContext)
        // 创建必要实例
        if(!sps.createChannel) {
            val channel = NotificationChannel(chanelID, "LChat Notification Chanel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            sps.createChannel = true
            // 首次启动， 创建通知channel

        }
    }
    companion object {
        var instance:BaseApp by Delegates.notNull()
        var sps : SPValues by Delegates.notNull()
        val dbName = "chat.db"
        val db_unRead = "unread.db"
        val chanelID= "LChat Notification Chanel ID"
        // 相关常用数据
    }
}