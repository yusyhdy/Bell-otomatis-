package com.example.data.cloud

data class AcademicCalendarEvent(
    val id: String,
    val title: String,
    val dateString: String,
    val eventType: String, // "EXAM", "HOLIDAY", "CEREMONY", "HALF_DAY", "EXTRACURRICULAR"
    val affectedPreset: String, // "UJIAN", "RAMADHAN", "UPACARA", "MUTED"
    val isAutoSyncEnabled: Boolean = true
)

object GoogleCalendarSyncManager {
    fun getSampleCalendarEvents(): List<AcademicCalendarEvent> {
        return listOf(
            AcademicCalendarEvent(
                id = "CAL-01",
                title = "Upacara Bendera Hari Senin & Apel Pagi",
                dateString = "Setiap Hari Senin",
                eventType = "CEREMONY",
                affectedPreset = "UPACARA",
                isAutoSyncEnabled = true
            ),
            AcademicCalendarEvent(
                id = "CAL-02",
                title = "Penilaian Tengah Semester (PTS Ganjil)",
                dateString = "15 Sep - 20 Sep 2026",
                eventType = "EXAM",
                affectedPreset = "UJIAN",
                isAutoSyncEnabled = true
            ),
            AcademicCalendarEvent(
                id = "CAL-03",
                title = "Jadwal KBM Khusus Bulan Suci Ramadhan",
                dateString = "12 Mar - 10 Apr 2027",
                eventType = "HALF_DAY",
                affectedPreset = "RAMADHAN",
                isAutoSyncEnabled = true
            ),
            AcademicCalendarEvent(
                id = "CAL-04",
                title = "Libur Nasional & Cuti Bersama Hari Guru",
                dateString = "25 Nov 2026",
                eventType = "HOLIDAY",
                affectedPreset = "MUTED",
                isAutoSyncEnabled = true
            ),
            AcademicCalendarEvent(
                id = "CAL-05",
                title = "Simulasi Tanggap Darurat & Kebakaran",
                dateString = "08 Okt 2026 (10:00 WIB)",
                eventType = "CEREMONY",
                affectedPreset = "UPACARA",
                isAutoSyncEnabled = true
            )
        )
    }
}
