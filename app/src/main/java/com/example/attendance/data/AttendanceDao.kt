package com.example.attendance.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val date: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)
