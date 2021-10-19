package com.jingtian.lchat.Service

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jingtian.lchat.BaseApp
import com.jingtian.lchat.Net.OKHttpHelper
import com.jingtian.lchat.R
import com.jingtian.lchat.mainActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*

class NotifyService: Service() {

    var timer:Timer? = null //每隔1s请求一次
    class unread_user(val id: String, val name: String) {}
    class unreadlist(val unread: Map<String, unread_user>) {} //返回的数据格式
    override fun onCreate() {
        super.onCreate()
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                OKHttpHelper.get_unread().enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body!!.string()
                        Log.d("body", body)
                        val res = OKHttpHelper.gson.fromJson(body, unreadlist::class.java).unread
                        if (res.size > 0) {
                            //准备通知的数据
                            var count = 0
                            var info = ""
                            var title = "您有新消息"
                            var users = res.values.toList()
                            while ((count < 3) && (count < users.size)) {
                                info = info + users[count].name
                                count++
                            }
                            info = info + if (count == 3) {
                                "等人"
                            } else {
                                ""
                            } + " 给您发送消息"
                            //发送通知
                            val notification = with(
                                NotificationCompat.Builder(
                                    this@NotifyService.application,
                                    BaseApp.chanelID
                                )
                            ) {
                                setAutoCancel(true)
                                setTicker(title)
                                setSubText(info)
                                setContentIntent(PendingIntent.getActivity(this@NotifyService,0, intentFor<mainActivity>(),0))
                                setSmallIcon(R.mipmap.ic_launcher)
                                setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                                setWhen(System.currentTimeMillis())
                                setContentTitle(title)
                                setContentText(info).build()
                            }
                            this@NotifyService.notificationManager.notify(1, notification)
                        }
                    }
                })
            }
        }, 0, 1 * 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? = null
}