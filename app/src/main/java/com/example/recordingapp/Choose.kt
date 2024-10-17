package com.example.recordingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Choose : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        val intent = Intent(this, MainActivity::class.java)

        val buttonGoToMainActivitySquat: Button = findViewById(R.id.squat)
        buttonGoToMainActivitySquat.setOnClickListener {

            intent.putExtra("KEY_DATA", "squat")
            startActivity(intent)
        }
        val buttonGoToMainActivitySit: Button = findViewById(R.id.sit)
        buttonGoToMainActivitySit.setOnClickListener {
            intent.putExtra("KEY_DATA", "sit")
            startActivity(intent)
        }
        val buttonGoToMainActivityPush: Button = findViewById(R.id.push)
        buttonGoToMainActivityPush.setOnClickListener {
            intent.putExtra("KEY_DATA", "push")
            startActivity(intent)
        }
        val buttonGoToStartActivity: Button = findViewById(R.id.return_page)
        buttonGoToStartActivity.setOnClickListener {

            val intentStart = Intent(this, Start::class.java)
            startActivity(intentStart)
        }
    }
}