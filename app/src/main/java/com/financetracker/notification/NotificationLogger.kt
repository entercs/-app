package com.financetracker.notification

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotificationLogEntry(
    val timestamp: Long,
    val packageName: String,
    val title: String,
    val text: String,
    val parsed: Boolean,
    val parsedAmount: Double? = null,
) {
    val formatted: String get() {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
        val status = if (parsed) "✅ ${parsedAmount?.let { "$it" } ?: ""}" else "❌ 未识别"
        return "${sdf.format(Date(timestamp))} | $status\n来源: $packageName\n标题: $title\n内容: $text"
    }
    val shortForm: String get() {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
        val status = if (parsed) "✅ ${String.format("%.2f", parsedAmount ?: 0.0)}" else "❌"
        return "${sdf.format(Date(timestamp))} $status $packageName"
    }
}

object NotificationLogger {
    private val entries = mutableListOf<NotificationLogEntry>()
    private const val MAX = 30

    fun log(entry: NotificationLogEntry) {
        synchronized(entries) {
            entries.add(0, entry)
            if (entries.size > MAX) entries.removeAt(entries.size - 1)
        }
    }

    fun getAll(): List<NotificationLogEntry> = synchronized(entries) {
        entries.toList()
    }

    fun clear() = synchronized(entries) { entries.clear() }
}
