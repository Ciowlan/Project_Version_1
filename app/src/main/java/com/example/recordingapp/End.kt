package com.example.recordingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class End : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)


//        val buttonGoToChooseActivity: Button = findViewById(R.id.start)
//        buttonGoToChooseActivity.setOnClickListener {
//            // 创建一个 Intent 来启动 SecondActivity
//            val intent = Intent(this, Choose::class.java)
//            startActivity(intent)
//        }
//        val buttonGoToRecordActivity: Button = findViewById(R.id.record)
//        buttonGoToRecordActivity.setOnClickListener {
//            // 创建一个 Intent 来启动 SecondActivity
//            val intent = Intent(this, Record::class.java)
//            startActivity(intent)
//        }

        val buttonGoToStartActivity: Button = findViewById(R.id.return_page)
        buttonGoToStartActivity.setOnClickListener {

            val intentReturn = Intent(this, Choose::class.java)
            startActivity(intentReturn)
        }

        val buttonGoToRecordActivity: Button = findViewById(R.id.record)
        buttonGoToRecordActivity.setOnClickListener {
            // 创建一个 Intent 来启动 SecondActivity
            val intent = Intent(this, Record::class.java)
            startActivity(intent)
        }
    }
}