package com.example.bmi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BMIHistoryActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_history)

        // Apply animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        // Initialize AppDatabase using singleton pattern
        appDatabase = AppDatabase.getDatabase(applicationContext)
        userDao = appDatabase.userDao()

        // Setup weight history graph
        setupWeightHistoryGraph()
    }

    private fun setupWeightHistoryGraph() {
        val chart = findViewById<LineChart>(R.id.weight_history_chart)
        val entries = ArrayList<Entry>()

        lifecycleScope.launch(Dispatchers.IO) {
            val user = userDao.getUser()

            withContext(Dispatchers.Main) {
                if (user != null && user.bmiHistory.isNotEmpty()) {
                    Log.d("BMIHistoryActivity", "Fetched User: $user")

                    val lastSevenBmi = user.bmiHistory.takeLast(7)
                    entries.clear()
                    for (i in lastSevenBmi.indices) {
                        entries.add(Entry(i.toFloat(), lastSevenBmi[i].toFloat()))
                    }

                    val dataSet = LineDataSet(entries, "BMI History")

                    // Retrieve theme colors
                    val primaryColor = ContextCompat.getColor(this@BMIHistoryActivity, R.color.primaryColor)
                    val textColor = ContextCompat.getColor(this@BMIHistoryActivity, R.color.textColor)

                    // Customize LineDataSet
                    dataSet.color = primaryColor
                    dataSet.valueTextColor = textColor
                    dataSet.lineWidth = 5f

                    // Customize chart appearance
                    chart.axisLeft.textColor = textColor
                    chart.xAxis.textColor = textColor
                    chart.legend.textColor = textColor
                    chart.setBackgroundColor(ContextCompat.getColor(this@BMIHistoryActivity, R.color.backgroundColor))

                    // Remove grid lines
                    chart.xAxis.setDrawGridLines(false)
                    chart.axisLeft.setDrawGridLines(false)
                    chart.axisRight.setDrawGridLines(false)

                    val lineData = LineData(dataSet)
                    chart.data = lineData
                    chart.invalidate() // refresh
                } else {
                    Log.d("BMIHistoryActivity", "No BMI history available for the user.")
                }
            }
        }
    }
}
