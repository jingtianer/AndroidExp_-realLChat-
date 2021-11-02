package com.jingtian.lchat.Net

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.jingtian.lchat.BaseApp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.io.IOUtils
import org.json.JSONStringer
import java.io.IOException
import java.lang.Exception

class OKHttpHelper {
    companion object {
        fun get_server():String {
            return "http://${BaseApp.sps.server_ip}:8080/chatlet"
        }
        //服务器地址
        val client = OkHttpClient()
        //OKHttp 客户端
        val gson = Gson()
        /*
        * 注册接口
        * 传入 name， password
        * 返回 id -- 永久保存
        * */
        fun regist(name: String, password:String):Call {
            val body = (gson.toJson(mapOf("name" to name, "password" to password)).toString()).toRequestBody(
                "application/json;charset=utf-8".toMediaType()
            )
            val request = Request.Builder()
                .url(get_server() + "/regist")
                .post(body)
                .build()

            return client.newCall(request)
        }

        /*
        * 心跳接口
        * 传入 id
        * 返回 当前所有在线用户
        * */
        fun online():Call {
            val body = (gson.toJson(mapOf("id" to BaseApp.sps.id)).toString()).toRequestBody(
                "application/json;charset=utf-8".toMediaType()
            )
            val request = Request.Builder()
                .url(get_server() + "/online")
                .post(body)
                .build()

            return client.newCall(request)
        }
        /*
        * 获取新信息
        * 传入 id
        * 返回 {
        *           "mesg" : {[
        *               "time"      :time
        *               "content"   :content
        *               "to_id"     :to_id
        *               "from_id"   :from_id
        *            ]}
        * }
        * */
        fun get_new_mesg():Call {
            val body = (gson.toJson(mapOf("id" to BaseApp.sps.id)).toString()).toRequestBody(
                    "application/json;charset=utf-8".toMediaType()
            )
            val request = Request.Builder()
                    .url(get_server() + "/new_mesg")
                    .post(body)
                    .build()

            return client.newCall(request)
        }

        /**
         * 发送消息接口
         * 传入
         * "time"      :time
         * "content"   :content
         * "to_id"     :to_id
         * "from_id"   :from_id
         *
         * */
        fun send_mesg(id:String, content:String, time:Long):Call {
            val body = (gson.toJson(mapOf(
                    "from_id" to BaseApp.sps.id,
                    "to_id" to id,
                    "content" to content,
                    "time" to time
            )).toString()).toRequestBody(
                    "application/json;charset=utf-8".toMediaType())

            val request = Request.Builder()
                    .url(get_server() + "/send_mesg" +
                            "")
                    .post(body)
                    .build()

            return client.newCall(request)
        }

        /*
        * 返回当前未读消息的发送人
        * 传入 id
        * 返回
        * class unread_user(val id: String, val name: String) {}
        * class unreadlist(val unread: Map<String, unread_user>) {}
        * */
        fun get_unread():Call {
            val body = (gson.toJson(mapOf(
                    "id" to BaseApp.sps.id
            )).toString()).toRequestBody(
                    "application/json;charset=utf-8".toMediaType())

            val request = Request.Builder()
                    .url(get_server() + "/has_unread" +
                            "")
                    .post(body)
                    .build()

            return client.newCall(request)
        }

        fun checkip(ip:String):Call {
            val body = (gson.toJson(mapOf<String, String>()).toString()).toRequestBody(
                    "application/json;charset=utf-8".toMediaType())

            val request = Request.Builder()
                    .url("http://${BaseApp.sps.server_ip}:8080/chatlet" + "/ip_check" +
                            "")
                    .post(body)
                    .build()

            return client.newCall(request)
        }
        class ip_check_beam(val check:Boolean)
    }

}