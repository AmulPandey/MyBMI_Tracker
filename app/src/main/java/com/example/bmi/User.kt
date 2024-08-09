package com.example.bmi

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    var weightHistory: List<Float>,  // Example: List of weights
    var bmiHistory: List<Float>      // Example: List of BMI values
)
