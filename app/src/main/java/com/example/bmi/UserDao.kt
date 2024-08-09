package com.example.bmi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): User?

    @Update
    fun update(user: User)

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Int): User?

    @Query("UPDATE user SET weightHistory = :weightHistory, bmiHistory = :bmiHistory WHERE id = :userId")
    fun updateUserDetails(userId: Int, weightHistory: List<Float>, bmiHistory: List<Float>)

    @Query("DELETE FROM user")
    fun deleteAllUsers()

    @Query("SELECT * FROM user ORDER BY date DESC LIMIT 7")
    fun getLastSevenRecords(): List<User>
}

