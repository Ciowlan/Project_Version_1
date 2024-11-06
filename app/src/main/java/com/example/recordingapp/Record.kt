package com.example.recordingapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.io.File

class Record : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        // 從 CSV 文件加載數據
        val workoutRecords = loadWorkoutDataFromCSV(this)

        // 設置 RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WorkoutRecordAdapter(workoutRecords)

        // 返回按鈕
        val buttonBackToStartActivity: Button = findViewById(R.id.exit_button)
        buttonBackToStartActivity.setOnClickListener {
            finish() // 返回並關閉當前頁面
        }
    }

    // 從 CSV 文件中加載數據
    private fun loadWorkoutDataFromCSV(context: Context): List<WorkoutRecord> {
        val fileName = "workout_records.csv"
        val file = File(context.filesDir, fileName)
        val workoutRecords = mutableListOf<WorkoutRecord>()

        if (file.exists()) {
            file.forEachLine { line ->
                if (line.startsWith("Date")) return@forEachLine // 跳過表頭

                val data = line.split(",")
                if (data.size == 5) {
                    val record = WorkoutRecord(data[0], data[1], data[2], data[3],data[4])
                    workoutRecords.add(0, record) // 新紀錄插入到開頭
                }
            }
        }

        return workoutRecords
    }

    // 用於保存每條數據記錄的數據類
    data class WorkoutRecord(val date: String, val exerciseType: String, val totalTime: String, val correctCount: String,val commentTextView: String)

    // Adapter 類
    inner class WorkoutRecordAdapter(private val records: List<WorkoutRecord>) : RecyclerView.Adapter<WorkoutRecordAdapter.ViewHolder>() {

        // ViewHolder 類
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dateTextView: TextView = view.findViewById(R.id.dateTextView)
            val typeTextView: TextView = view.findViewById(R.id.typeTextView)
            val timeTextView: TextView = view.findViewById(R.id.timeTextView)
            val countTextView: TextView = view.findViewById(R.id.countTextView)
            val commentTextView: TextView = view.findViewById(R.id.commentTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_record_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = records[position]
            holder.dateTextView.text = record.date
            holder.typeTextView.text = "運動類型: ${record.exerciseType}"
            holder.timeTextView.text = "總時間: ${record.totalTime}"
            holder.countTextView.text = "正確次數: ${record.correctCount}"
            holder.commentTextView.text = "評語: ${record.commentTextView}"
        }

        override fun getItemCount() = records.size
    }
}
