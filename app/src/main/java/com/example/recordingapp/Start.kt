package com.example.recordingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Start : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)


        val buttonGoToChooseActivity: Button = findViewById(R.id.start)
        buttonGoToChooseActivity.setOnClickListener {
            // 创建一个 Intent 来启动 SecondActivity
            val intent = Intent(this, Choose::class.java)
            startActivity(intent)
        }
        val buttonGoToRecordActivity: Button = findViewById(R.id.record)
        buttonGoToRecordActivity.setOnClickListener {
            // 创建一个 Intent 来启动 SecondActivity
            val intent = Intent(this, Record::class.java)
            startActivity(intent)
        }
    }
}