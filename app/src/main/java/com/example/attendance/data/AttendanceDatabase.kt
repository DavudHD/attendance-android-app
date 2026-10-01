package com.example.attendance.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceEntity)

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, createdAt DESC")
    suspend fun getAll(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getByEmployeeAndDate(employeeId: String, date: String): AttendanceEntity?
}
