package com.example.attendance

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.attendance.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var attendanceManager: AttendanceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        attendanceManager = AttendanceManager(this)
        updateRecordsView()

        binding.buttonCheckIn.setOnClickListener {
            val employeeName = binding.editTextEmployeeName.text?.toString()?.trim().orEmpty()
            val employeeId = binding.editTextEmployeeId.text?.toString()?.trim().orEmpty()

            if (employeeName.isEmpty() || employeeId.isEmpty()) {
                binding.textViewStatus.text = "يرجى إدخال اسم الموظف ورقم الموظف"
                return@setOnClickListener
            }

            val record = attendanceManager.recordCheckIn(employeeId, employeeName)
            binding.textViewStatus.text = "الحالة: ${record.status}"
            binding.textViewLastAction.text = "آخر تحديث: ${record.date} - ${record.checkInTime}"
            updateRecordsView()
        }

        binding.buttonCheckOut.setOnClickListener {
            val employeeName = binding.editTextEmployeeName.text?.toString()?.trim().orEmpty()
            val employeeId = binding.editTextEmployeeId.text?.toString()?.trim().orEmpty()

            if (employeeName.isEmpty() || employeeId.isEmpty()) {
                binding.textViewStatus.text = "يرجى إدخال اسم الموظف ورقم الموظف"
                return@setOnClickListener
            }

            val record = attendanceManager.recordCheckOut(employeeId, employeeName)
            binding.textViewStatus.text = "الحالة: ${record.status}"
            binding.textViewLastAction.text = "آخر تحديث: ${record.date} - ${record.checkOutTime}"
            updateRecordsView()
        }
    }

    private fun updateRecordsView() {
        val records = attendanceManager.getRecords()
        binding.textViewRecords.text = if (records.isEmpty()) {
            "لا توجد سجلات بعد"
        } else {
            records.joinToString("\n") { record ->
                "${record.employeeName} (${record.employeeId}) - ${record.date} - ${record.status} - دخول: ${record.checkInTime ?: "-"} - خروج: ${record.checkOutTime ?: "-"}"
            }
        }
    }
}

class AttendanceManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("attendance_store", Context.MODE_PRIVATE)
    private val key = "attendance_records"

    fun recordCheckIn(employeeId: String, employeeName: String): AttendanceRecord {
        val now = LocalDate.now()
        val currentTime = LocalTime.now()
        val formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        val lateThreshold = LocalTime.of(8, 30)
        val status = if (currentTime.isAfter(lateThreshold)) "متأخر" else "حاضر"

        val records = getRecords().toMutableList()
        val existingIndex = records.indexOfFirst { it.employeeId == employeeId && it.date == formattedDate }

        val record = if (existingIndex >= 0) {
            records[existingIndex].copy(checkInTime = formattedTime, status = status)
        } else {
            AttendanceRecord(
                employeeId = employeeId,
                employeeName = employeeName,
                date = formattedDate,
                checkInTime = formattedTime,
                checkOutTime = null,
                status = status
            )
        }

        if (existingIndex >= 0) {
            records[existingIndex] = record
        } else {
            records.add(record)
        }

        saveRecords(records)
        return record
    }

    fun recordCheckOut(employeeId: String, employeeName: String): AttendanceRecord {
        val now = LocalDate.now()
        val currentTime = LocalTime.now()
        val formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        val officialEnd = LocalTime.of(17, 30)
        val records = getRecords().toMutableList()
        val existingIndex = records.indexOfFirst { it.employeeId == employeeId && it.date == formattedDate }

        val existing = if (existingIndex >= 0) records[existingIndex] else AttendanceRecord(
            employeeId = employeeId,
            employeeName = employeeName,
            date = formattedDate,
            checkInTime = null,
            checkOutTime = null,
            status = "لم يتم تسجيل الدخول"
        )

        val status = if (currentTime.isBefore(officialEnd)) "انصراف مبكر" else "منتهي"
        val updated = existing.copy(
            employeeName = employeeName,
            checkOutTime = formattedTime,
            status = status
        )

        if (existingIndex >= 0) {
            records[existingIndex] = updated
        } else {
            records.add(updated)
        }

        saveRecords(records)
        return updated
    }

    fun getRecords(): List<AttendanceRecord> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        val array = JSONArray(raw)
        val records = mutableListOf<AttendanceRecord>()

        for (index in 0 until array.length()) {
            val obj = array.getJSONObject(index)
            val record = AttendanceRecord(
                employeeId = obj.getString("employeeId"),
                employeeName = obj.getString("employeeName"),
                date = obj.getString("date"),
                checkInTime = obj.optString("checkInTime", "").ifEmpty { null },
                checkOutTime = obj.optString("checkOutTime", "").ifEmpty { null },
                status = obj.getString("status")
            )
            records.add(record)
        }

        return records
    }

    private fun saveRecords(records: List<AttendanceRecord>) {
        val array = JSONArray()
        records.forEach { record ->
            val obj = JSONObject().apply {
                put("employeeId", record.employeeId)
                put("employeeName", record.employeeName)
                put("date", record.date)
                put("checkInTime", record.checkInTime ?: "")
                put("checkOutTime", record.checkOutTime ?: "")
                put("status", record.status)
            }
            array.put(obj)
        }
        prefs.edit().putString(key, array.toString()).apply()
    }
}

data class AttendanceRecord(
    val employeeId: String,
    val employeeName: String,
    val date: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val status: String = "لم يتم تسجيل الدخول"
)
