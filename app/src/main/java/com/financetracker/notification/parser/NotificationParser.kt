package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

interface NotificationParser {
    /** Package name this parser handles */
    val supportedPackages: List<String>

    /** Try to parse a notification. Returns null if the text doesn't match. */
    fun parse(packageName: String, title: String, text: String): ParsedNotification?
}
