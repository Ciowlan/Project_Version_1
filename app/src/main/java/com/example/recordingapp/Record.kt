package com.example.recordingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Record : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        // 初始化 TextView
        //val textViewSecondActivity: TextView = findViewById(R.id.textViewSecondActivity)
       // textViewSecondActivity.text = "这是第二页"

        // 初始化返回按钮
        val buttonBackToStartActivity: Button = findViewById(R.id.exit_button)
        buttonBackToStartActivity.setOnClickListener {
            // 创建一个 Intent 来返回 MainActivity
            val intent = Intent(this, Start::class.java)
            startActivity(intent)
            finish() // 可选：关闭当前活动
        }
    }
}