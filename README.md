package com.example.attendance.data

class AttendanceRepository(private val dao: AttendanceDao) {
    suspend fun insertOrUpdate(record: AttendanceEntity) {
        dao.insert(record)
    }

    suspend fun getAllRecords(): List<AttendanceEntity> = dao.getAll()

    suspend fun getTodayRecord(employeeId: String, date: String): AttendanceEntity? =
        dao.getByEmployeeAndDate(employeeId, date)
}
