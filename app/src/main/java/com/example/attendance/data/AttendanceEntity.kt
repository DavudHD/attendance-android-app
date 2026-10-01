package com.example.attendance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.attendance.data.AttendanceDatabase
import com.example.attendance.data.AttendanceEntity
import com.example.attendance.data.AttendanceRepository
import com.example.attendance.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: AttendanceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AttendanceRepository(AttendanceDatabase.getDatabase(this).attendanceDao())
        loadRecords()

        binding.buttonCheckIn.setOnClickListener {
            val employeeName = binding.editTextEmployeeName.text?.toString()?.trim().orEmpty()
            val employeeId = binding.editTextEmployeeId.text?.toString()?.trim().orEmpty()

            if (employeeName.isEmpty() || employeeId.isEmpty()) {
                binding.textViewStatus.text = "يرجى إدخال اسم الموظف ورقم الموظف"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val status = calculateCheckInStatus(LocalTime.now())
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

                val existing = withContext(Dispatchers.IO) { repository.getTodayRecord(employeeId, today) }
                val record = if (existing != null) {
                    existing.copy(
                        employeeName = employeeName,
                        checkInTime = currentTime,
                        status = status,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    AttendanceEntity(
                        employeeId = employeeId,
                        employeeName = employeeName,
                        date = today,
                        checkInTime = currentTime,
                        checkOutTime = null,
                        status = status,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                }

                withContext(Dispatchers.IO) { repository.insertOrUpdate(record) }
                binding.textViewStatus.text = "الحالة: $status"
                binding.textViewLastAction.text = "آخر تحديث: $today - $currentTime"
                loadRecords()
            }
        }

        binding.buttonCheckOut.setOnClickListener {
            val employeeName = binding.editTextEmployeeName.text?.toString()?.trim().orEmpty()
            val employeeId = binding.editTextEmployeeId.text?.toString()?.trim().orEmpty()

            if (employeeName.isEmpty() || employeeId.isEmpty()) {
                binding.textViewStatus.text = "يرجى إدخال ا��م الموظف ورقم الموظف"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                val existing = withContext(Dispatchers.IO) { repository.getTodayRecord(employeeId, today) }

                val record = if (existing != null) {
                    val finalStatus = if (LocalTime.now().isBefore(LocalTime.of(17, 30))) "انصراف مبكر" else "منتهي"
                    existing.copy(
                        employeeName = employeeName,
                        checkOutTime = currentTime,
                        status = finalStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    AttendanceEntity(
                        employeeId = employeeId,
                        employeeName = employeeName,
                        date = today,
                        checkInTime = null,
                        checkOutTime = currentTime,
                        status = "انصراف مبكر",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                }

                withContext(Dispatchers.IO) { repository.insertOrUpdate(record) }
                binding.textViewStatus.text = "الحالة: ${record.status}"
                binding.textViewLastAction.text = "آخر تحديث: $today - $currentTime"
                loadRecords()
            }
        }
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) { repository.getAllRecords() }
            binding.textViewRecords.text = if (records.isEmpty()) {
                "لا توجد سجلات بعد"
            } else {
                records.joinToString("\n") { item ->
                    "${item.employeeName} (${item.employeeId}) - ${item.date} - ${item.status} - دخول: ${item.checkInTime ?: "-"} - خروج: ${item.checkOutTime ?: "-"}"
                }
            }
        }
    }

    private fun calculateCheckInStatus(time: LocalTime): String {
        val officialStart = LocalTime.of(8, 30)
        return if (time.isAfter(officialStart)) "متأخر" else "حاضر"
    }
}
