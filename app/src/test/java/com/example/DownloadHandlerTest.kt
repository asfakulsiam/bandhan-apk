package com.example

import com.example.download.DownloadHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadHandlerTest {

    @Test
    fun testContentDispositionFileNameExtraction() {
        val cd1 = "attachment; filename=\"Bandhan17_Statement_2024.pdf\""
        val fileName1 = DownloadHandler.resolveFileName("https://bandhan17.website/api/statement", cd1, "application/pdf")
        assertEquals("Bandhan17_Statement_2024.pdf", fileName1)

        val cd2 = "attachment; filename=monthly_report.xlsx"
        val fileName2 = DownloadHandler.resolveFileName("https://bandhan17.website/report", cd2, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        assertEquals("monthly_report.xlsx", fileName2)

        val cdUtf8 = "attachment; filename*=UTF-8''Bandhan_Member_Statement.pdf"
        val fileNameUtf8 = DownloadHandler.resolveFileName("https://bandhan17.website/statement", cdUtf8, "application/pdf")
        assertEquals("Bandhan_Member_Statement.pdf", fileNameUtf8)
    }

    @Test
    fun testFallbackFileNameGeneration() {
        val fallback = DownloadHandler.resolveFileName("https://bandhan17.website/api/download", null, "application/pdf")
        assertTrue(fallback.startsWith("Bandhan17_Statement_"))
        assertTrue(fallback.endsWith(".pdf"))

        val csvFallback = DownloadHandler.resolveFileName("https://bandhan17.website/api/export", null, "text/csv")
        assertTrue(csvFallback.startsWith("Bandhan17_Statement_"))
        assertTrue(csvFallback.endsWith(".csv"))
    }
}
