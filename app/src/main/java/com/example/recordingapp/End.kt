package com.example.recordingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recordingapp.R
import java.io.File

class End : AppCompatActivity() {

    private var date = ""
    private var exerciseType = ""
    private var totalTime = ""
    private var correctCount = ""
    private lateinit var dateTextView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var countTextView: TextView
    private lateinit var commentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        dateTextView = findViewById(R.id.dateTextView)
        typeTextView = findViewById(R.id.typeTextView)
        timeTextView = findViewById(R.id.timeTextView)
        countTextView = findViewById(R.id.countTextView)
        commentTextView = findViewById(R.id.commentTextView)

        val buttonGoToStartActivity: Button = findViewById(R.id.return_page)
        buttonGoToStartActivity.setOnClickListener {
            val intentReturn = Intent(this, Choose::class.java)
            startActivity(intentReturn)
        }

        val buttonGoToRecordActivity: Button = findViewById(R.id.record)
        buttonGoToRecordActivity.setOnClickListener {
            val intent = Intent(this, Record::class.java)
            startActivity(intent)
        }

        // 获取传入的数据
        date = intent.getStringExtra("date").toString()
        exerciseType = intent.getStringExtra("ExerciseType").toString()
        totalTime = intent.getStringExtra("TotalTime").toString()
        correctCount = intent.getStringExtra("CorrectCount").toString()

        // 显示在 TextView 中
        dateTextView.text = date
        typeTextView.text = exerciseType
        timeTextView.text = totalTime
        countTextView.text = correctCount
        commentTextView.text = "您的表現不錯，請繼續加油！"

        // 日志信息
        Log.d("bug001", "運動:$exerciseType 時間:$totalTime 次數:$correctCount")

        // 保存数据到 CSV 文件
        saveWorkoutDataToCSV(this, date, exerciseType, totalTime, correctCount, commentTextView.text.toString())
    }

    private fun saveWorkoutDataToCSV(
        context: Context,
        date: String,
        exerciseType: String,
        totalTime: String,
        correctCount: String,
        comment: String
    ) {
        val fileName = "workout_records.csv"
        val file = File(context.filesDir, fileName)

        // 如果文件不存在，创建文件并写入表头
        if (!file.exists()) {
            file.appendText("Date,ExerciseType,TotalTime,CorrectCount,Comment\n")
        }

        // 写入新的运动记录
        file.appendText("$date,$exerciseType,$totalTime,$correctCount,$comment\n")
    }
}
