package com.jingtian.lchat

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jingtian.lchat.Net.OKHttpHelper
import com.jingtian.lchat.Net.OKHttpHelper.Companion.gson
import com.jingtian.lchat.Service.NotifyService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.properties.Delegates

class mainActivity : AppCompatActivity() {
    fun checkIp(list: List<String>):Boolean {
        var ip = ""
        if (list.size == 4) {
            for (l in list) {
                if ((l.toInt() < 0 ) or (l.toInt() >= 256)) {
                    return false
                }
                ip = ip + l + "."
            }
        } else {
            return false
        }
        return true
    }


    // 弹出注册窗口，并发注册请求
    fun regist(){
        alert {
            title = "注册"
            isCancelable = false
            customView {
                linearLayout {
                    orientation = LinearLayout.VERTICAL
                    val et_yr_name = editText {
                        hint = "你的名字"
                    }
                    val et_ip = editText {
                        hint = "主机ip"
                    }
                    positiveButton("确认") {
                        val ips = et_ip.text.toString().trim().split('.')
                        if(checkIp(ips)) {
                            BaseApp.sps.server_ip = et_ip.text.toString().trim()
                            doAsync {
                                OKHttpHelper.regist(et_yr_name.text.toString(), et_ip.text.toString()).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        this@mainActivity.fail()
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        try {
                                            BaseApp.sps.id = gson.fromJson<ID>(response.body!!.string(), ID::class.java).id
                                            this@mainActivity.start_beat()
                                            startService<NotifyService>()
                                        } catch (e:Exception) {
                                            runOnUiThread {
                                                toast("ip已经失效")
                                                regist()
                                            }
                                        }

                                    }

                                })
                            }
                        } else {
                            toast("ip格式错误")
                        }

                    }
                    //不注册则退出程序
                    negativeButton("取消") {
                        it.dismiss()
                        this@mainActivity.finish()
                    }
                }
            }
        }.show()
    }
    var beat:Timer? = null
    // 注册后开始心跳
    fun start_beat() {
        beat = Timer()
        beat!!.schedule(object : TimerTask() {
            override fun run() {
                OKHttpHelper.online().enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    override fun onResponse(call: Call, response: Response) {
                        val data = response.body!!.string()
                        Log.d("online", data)
                        val users = gson.fromJson(data, online::class.java).users

                        datas.clear()
                        for (item in users) {
                            datas.add(rv_data(item.value.name, item.key))
                            Log.d("online", item.value.name)
                        }
                        runOnUiThread {
//                            for (index in datas.count() - 1 downTo 0) {
//                                if (users.containsKey(datas[index].name)) {
//                                    datas.removeAt(index)
//                                    rv.adapter!!.notifyItemRemoved(index)
//                                }
//                            }
                            rv.adapter?.notifyDataSetChanged()
                        }
                    }

                })
            }
        }, 0, 2 * 1000)
    }
    //注册失败退出程序
    fun fail() {
        runOnUiThread {
            alert {
                title = "失败"
                isCancelable = false
                positiveButton("退出") {
                    it.dismiss()
                    this@mainActivity.finish()
                }

            }.show()
        }
    }
    class online {
        var users = hashMapOf<String, User>()
    }
    val datas = arrayListOf<rv_data>()
    var rv:RecyclerView by Delegates.notNull()
    // 请求悬浮窗权限
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {

            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, BaseApp.chanelID);
            alert {
                title = "请打卡悬浮窗权限"
                isCancelable = false
                positiveButton("确认") {
                    startActivity(intent)
                }
                negativeButton("取消") {}
            }.show()

        }
    }
    fun set_ip(){
        alert {
            title = "设置ip"
            isCancelable = false
            customView {
                val et_ip = editText() {
                    hint = "主机ip"
                }
                positiveButton("确认") {
                    if(checkIp(et_ip.text.toString().trim().split("."))){
                        toast("设置成功，正在重启app")
                        BaseApp.sps.server_ip = et_ip.text.toString().trim()
                        activityManager.killBackgroundProcesses(packageName)
                        finish()
//                        var LaunchIntent = packageManager.getLaunchIntentForPackage(this@mainActivity.packageName)
//                        LaunchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        startActivity(LaunchIntent)
                    } else {
                        toast("ip格式错误")
                    }
                }
                negativeButton("取消") {}
            }
        }.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "LChat"
        toolbar.subtitle = "当前在线"
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener {
            if(it.itemId == R.id.setServer_ip) {
                set_ip()
            } else if (it.itemId == R.id.more) {
                longToast("要监督卷卷考研！不许卷卷写安卓了")
            } else if (it.itemId == R.id.my_id) {
                alert {
                    title = "您的id"
                    customView {
                        linearLayout {
                            orientation = LinearLayout.VERTICAL
                            val text = editText {
                                setText(BaseApp.sps.id)
                            }
                            text.addTextChangedListener(object :TextWatcher {
                                override fun beforeTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    count: Int,
                                    after: Int
                                ) {}

                                override fun onTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) {}

                                override fun afterTextChanged(s: Editable?) {
                                    text.setText(BaseApp.sps.id)
                                }
                            })
                            positiveButton("关闭") {}
                        }
                    }
                }.show()
            }
            true
        }
        //设置 recyclerlayout 的adapter
        rv = findViewById(R.id.rv_main)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object :RecyclerView.Adapter<rv_holder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): rv_holder {
                return rv_holder(layoutInflater, parent, item_click(this@mainActivity))
            }
            override fun onBindViewHolder(holder: rv_holder, position: Int) {
                holder.bind(datas[position])
            }
            override fun getItemCount(): Int = datas.size

        }
        if(!BaseApp.sps.id.equals("empty")) {
            OKHttpHelper.checkip(BaseApp.sps.server_ip).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val data = response.body!!.string()
                        OKHttpHelper.gson.fromJson(data, OKHttpHelper.Companion.ip_check_beam::class.java)
                        BaseApp.sps.ip_valid = true
                        runOnUiThread {
                            //未注册弹出注册窗口，已注册开始心跳
                            startService(Intent(this@mainActivity, NotifyService::class.java))
                            start_beat()

                        }
                    } catch (e: Exception) {
                        BaseApp.sps.ip_valid = false
                        runOnUiThread {
                            toast("ip失效，请重新输入")
                            set_ip()
                        }
                    }
                }

            })
        } else {
            regist()
        }

        //没有弹窗权限，则申请
        if (
            !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            var intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid)
            alert {
                title = "请打开通知权限"
                isCancelable = false
                positiveButton("确认") {
                    startActivityForResult(intent, 1)
                }
                negativeButton("取消") {}
            }.show()
        }

    }
    class rv_data(var name: String, var id: String) {}
    class ID(val id: String){}
    class rv_holder(inflater: LayoutInflater, itemView: ViewGroup, val listener: item_click):
        RecyclerView.ViewHolder(inflater.inflate(R.layout.main_item, itemView, false)){
        fun bind(data: rv_data) {
            itemView.findViewById<TextView>(R.id.tv_item_main).text = data.name
            itemView.findViewById<TextView>(R.id.tv_id).text = data.id
            val dab = BitmapFactory.decodeResource(itemView.resources, R.drawable.cat)
            val mat = Matrix()
            mat.postScale(0.2f,0.2f)
            val pic = Bitmap.createBitmap(dab, 0,0,dab.width, dab.height, mat,true)
            itemView.findViewById<ImageView>(R.id.iv_main_item).setImageBitmap(pic)
            itemView.onClick {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onclick(data)
                }
            }
        }
    }
    class item_click(val context: Context) {
        fun onclick(data: rv_data) {
            context.startActivity<chatActivity>(
                    "id" to data.id,
                    "name" to data.name
            )
        }
    }
    class User(var password: String, var name: String) {}

    override fun onDestroy() {
        super.onDestroy()
        beat?.cancel()
    }

}