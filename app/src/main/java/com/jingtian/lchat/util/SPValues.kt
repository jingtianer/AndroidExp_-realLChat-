package com.jingtian.lchat.util

import android.content.Context

//持久化保存的变量
class SPValues(val context: Context) {
    var id:String by SP(context, "id", "empty")
    //用户id
    var createChannel:Boolean by SP(context, "createChannel", false)
    //是否创建通知channel
    var server_ip:String by SP(context, "server_ip", "0.0.0.0")
    var ip_valid:Boolean by SP(context, "ip_valid", false)

}