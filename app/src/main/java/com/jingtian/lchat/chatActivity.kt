package com.jingtian.lchat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jingtian.lchat.Net.OKHttpHelper
import com.jingtian.lchat.util.DBHelper
import com.jingtian.lchat.util.Translator
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onLongClick

class chatActivity : AppCompatActivity() {
    var mIsRefreshing = false   //当前列表是否正在刷新
    var rv :RecyclerView by Delegates.notNull() //显示聊天记录
    val datas = arrayListOf<mesg>() //与当前用户的聊天记录
    var to_id:String by Delegates.notNull() //当前聊天用户的id
    var get_new_mesg:Timer? = null // 每隔几秒获取全部新消息
    var language = Translator.PLAIN
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(findViewById(R.id.toolbar))
        val bundle = intent.extras
        if (bundle == null) {
            toast("发生错误")
            finish()
        }
        val name = bundle!!.get("name") as String
        to_id = bundle.get("id") as String
        supportActionBar!!.title = name
        //获取参数，取得当前聊天用户的姓名与id
        //给RecyclerView设置 layoutmanager， adapter
        rv = findViewById(R.id.chat_histry)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object : RecyclerView.Adapter<rv_holder>() {
            override fun getItemViewType(position: Int): Int {
                return position
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): rv_holder {
                return rv_holder(layoutInflater, parent)
            }
            override fun onBindViewHolder(holder: rv_holder, position: Int) {
                holder.bind(dialog_data(datas[position].content, datas[position].orient))
            }
            override fun getItemCount(): Int = datas.size
        }
        // 数据更新时不允许滑动
        rv.onTouch { v, event ->
            if (mIsRefreshing) {
                true;
            } else {
                false;
            }
        }
        // 点击发送按钮后的操作
        fun update(text: String, orient: Int) {
            datas.add(mesg("", text, 0, mesg.right))
            val db = DBHelper.get_db(this,BaseApp.dbName).writableDatabase
            val time = System.currentTimeMillis()
            //发送的数据写入数据库
            db.execSQL("insert into mesg(content, time, orient, to_id) values('${text}',${time},${orient}, '${to_id}')")
            //新消息发送给服务器
            OKHttpHelper.send_mesg(to_id, text, time).enqueue(object :Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        toast("发送失败：${text}")
                    }
                }
                override fun onResponse(call: Call, response: Response) {}
            })
        }
        var radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        var et_mesg = findViewById<EditText>(R.id.chat_msg)
        radioGroup.check(R.id.rb_plain)
        et_mesg.isEnabled = true
        //发送按钮点击事件
        findViewById<Button>(R.id.chat_send).setOnClickListener {
            if (!et_mesg.text.isEmpty()) {
                radioGroup.check(R.id.rb_plain)
                et_mesg.isEnabled = true
                mIsRefreshing = true
                update(et_mesg.text.toString(), dialog_data.right)
                et_mesg.setText("")
                rv.adapter!!.notifyItemInserted(rv.adapter!!.itemCount - 1)
                rv.scrollToPosition(datas.size-1)
                mIsRefreshing = false
                language = Translator.PLAIN
            } else {
                toast("输入发送内容")
            }
        }
        findViewById<Button>(R.id.rb_cat).setOnClickListener {
            try {
                var text = et_mesg.text.toString()
                text = when(language) {
                    Translator.CAT->{
                        text
                    }
                    Translator.DOG->{
                        Translator.toCat(Translator.fromDog(text))
                    }
                    Translator.BASE64->{
                        Translator.toCat(Translator.fromBase64(text))
                    }
                    else->{
                        Translator.toCat(text)
                    }
                }

                et_mesg.isEnabled = false
                et_mesg.setText(text)
                language = Translator.CAT
            } catch (e:Exception) {
            }

        }
        findViewById<Button>(R.id.rb_dog).setOnClickListener {
            try {
                var text = et_mesg.text.toString()
                text = when (language) {
                    Translator.CAT -> {
                        Translator.toDog(Translator.fromCat(text))
                    }

                    Translator.DOG -> {
                        text
                    }

                    Translator.BASE64 -> {
                        Translator.toDog(Translator.fromBase64(text))
                    }

                    else -> {
                        Translator.toDog(text)
                    }
                }
                et_mesg.isEnabled = false
                et_mesg.setText(text)
                language = Translator.DOG
            } catch (e:Exception) {

            }

        }
        findViewById<Button>(R.id.rb_base64).setOnClickListener {
            try {
                var text = et_mesg.text.toString()
                text = when (language) {
                    Translator.CAT -> {
                        Translator.toBase64(Translator.fromCat(text))
                    }

                    Translator.DOG -> {
                        Translator.toBase64(Translator.fromDog(text))
                    }

                    Translator.BASE64 -> {
                        text
                    }

                    else -> {
                        Translator.toBase64(text)
                    }
                }
                et_mesg.isEnabled = false
                et_mesg.setText(text)
                language = Translator.BASE64
            }catch (e:Exception) {

            }

        }
        findViewById<Button>(R.id.rb_plain).setOnClickListener {
            try {
                var text = et_mesg.text.toString()
                text = when (language) {
                    Translator.CAT -> {
                        Translator.fromCat(text)
                    }

                    Translator.DOG -> {
                        Translator.fromDog(text)
                    }

                    Translator.BASE64 -> {
                        Translator.fromBase64(text)
                    }

                    else -> {
                        text
                    }
                }
                et_mesg.isEnabled = true
                et_mesg.setText(text)
                language = Translator.PLAIN
            } catch (e:Exception) {

            }

        }
        //启动页面时，读取数据库中当前用户的对话， 并从服务器获取最新未读信息
        doAsync{
            val db = DBHelper.get_db(this@chatActivity, BaseApp.dbName).readableDatabase
            val cursor = db.query("mesg", null, "to_id == '${to_id}'", null, null, null, "time")

            mIsRefreshing = true
            while (cursor.moveToNext()) {
                val orient = cursor.getInt(cursor.getColumnIndex("orient"))
                val text = cursor.getString(cursor.getColumnIndex("content"))
                datas.add(mesg("", text, 0, orient))
                Log.d("datas", "${datas[datas.size-1].toString()}")
            }
            cursor.close()
            mIsRefreshing = false
//            et_mesg.doAfterTextChanged {
//                language = Translator.PLAIN
//                radioGroup.check(R.id.rb_plain)
//                et_mesg.isEnabled = true
//            }
            rv.adapter!!.notifyDataSetChanged()
            rv.scrollToPosition(datas.size-1)
            get_new_mesg()
        }



    }
    class dialog_data(var text: String, var orient: Int) {
        companion object {
            val left = 1
            val right = 0
        }
    }
    class mesg(val id:String, val content:String, val time:Long, val orient:Int) {

        companion object {
            val left = 1
            val right = 0
        }

        override fun toString(): String {
            return "mesg(id='$id', content='$content', time=$time, orient=$orient)"
        }
    }
    class new_mesg(val time:Long, val  content: String, val  to_id:String, val from_id:String)
    class new_mesgs() {
        val data = arrayListOf<new_mesg>()
    }
    //每隔几秒获取消息
    fun get_new_mesg() {
        get_new_mesg = Timer()
        get_new_mesg!!.schedule(object : TimerTask() {
            override fun run() {
                OKHttpHelper.get_new_mesg().enqueue(object :Callback{
                    override fun onFailure(call: Call, e: IOException) {
                    }
                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body!!.string()
                        Log.d("body",body)
                        val res = OKHttpHelper.gson.fromJson<new_mesgs>(body, new_mesgs::class.java).data

                        mIsRefreshing = true
                        for(data in res) {
                            //未读消息写入数据库
                            DBHelper.get_db(this@chatActivity, BaseApp.dbName).writableDatabase
                                    .execSQL("insert into mesg(content, time, orient, to_id) values('${data.content}', ${data.time}, ${mesg.left}, '${data.from_id}')")
                            //如果未读消息中有与当前用户的， 显示到界面上
                            if(data.from_id.equals(to_id)) {
                                datas.add(mesg("", data.content, 0, mesg.left))
                                runOnUiThread {
                                    rv.adapter!!.notifyItemInserted(datas.size - 1)
                                    rv.scrollToPosition(datas.size - 1)
                                }
                            }
                        }
                        mIsRefreshing = false
                    }
                })
            }
        },0,1*1000)
    }
    class rv_holder(inflater: LayoutInflater, itemView: ViewGroup) :
            RecyclerView.ViewHolder(inflater.inflate(R.layout.item_rv_exp1, itemView, false)) {
        fun bind(data: dialog_data) {

            var textview = this.itemView.findViewById<TextView>(R.id.tv_item_exp1)
            textview.text = data.text
            textview.onClick { tv->

                textview.context.alert {
                    title = "翻译"
                    isCancelable = true
                    var lang = Translator.PLAIN
                    customView {
                        radioGroup {
                            radioButton {
                                text = "猫语"
                                onClick {
                                    lang = Translator.CAT
                                }
                            }
                            radioButton {
                                text = "狗语"
                                onClick {
                                    lang = Translator.DOG
                                }
                            }
                            radioButton {
                                text = "base64"
                                onClick {
                                    lang = Translator.BASE64
                                }
                            }
                        }
                        positiveButton("确认") {bt->
                            try {
                                var text = textview.text.toString()
                                text = when (lang) {
                                    Translator.CAT -> {
                                        Translator.fromCat(text)
                                    }

                                    Translator.DOG -> {
                                        Translator.fromDog(text)
                                    }

                                    Translator.BASE64 -> {
                                        Translator.fromBase64(text)
                                    }

                                    else -> {
                                        text
                                    }
                                }
                                textview.text = text
                            }catch (e:Exception) {

                            }
                        }
                        negativeButton("取消") {
                        }
                    }
                }?.show()
            }
            //设置图片
            var dab = BitmapFactory.decodeResource(itemView.resources, R.drawable.dog)
            var mat = Matrix()
            mat.postScale(0.2f,0.2f)
            var pic = Bitmap.createBitmap(dab, 0,0,dab.width, dab.height, mat,true)
            this.itemView.findViewById<ImageView>(R.id.iv_dog).setImageBitmap(pic)
            //设置图片
            dab = BitmapFactory.decodeResource(itemView.resources, R.drawable.cat)
            mat = Matrix()
            mat.postScale(0.2f,0.2f)
            pic = Bitmap.createBitmap(dab, 0,0,dab.width, dab.height, mat,true)
            itemView.findViewById<ImageView>(R.id.iv_cat).setImageBitmap(pic)
            //隐藏不需要的组件
            if (data.orient == dialog_data.left) {
                itemView.findViewById<LinearLayout>(R.id.item_exp1_ll).gravity = Gravity.START
//                    (itemView.findViewById<LinearLayout>(R.id.item_exp1_ll).layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
                this.itemView.findViewById<ImageView>(R.id.iv_cat).visibility = View.GONE
                textview.setBackgroundResource(R.drawable.dialog_left)
            } else if (data.orient ==  dialog_data.right) {
                itemView.findViewById<LinearLayout>(R.id.item_exp1_ll).gravity = Gravity.END
//                    (itemView.findViewById<LinearLayout>(R.id.item_exp1_ll).layoutParams as LinearLayout.LayoutParams).gravity = Gravity.END
                this.itemView.findViewById<ImageView>(R.id.iv_dog).visibility = View.GONE
                textview.setBackgroundResource(R.drawable.dialog_right)
            } else {
                this.itemView.findViewById<ImageView>(R.id.iv_cat).visibility = View.GONE
                this.itemView.findViewById<ImageView>(R.id.iv_dog).visibility = View.GONE
            }
        }
    }
    //关闭页面时计数器关闭
    override fun onDestroy() {
        super.onDestroy()
        get_new_mesg?.cancel()
    }
}