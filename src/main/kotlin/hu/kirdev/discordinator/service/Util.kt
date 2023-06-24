package hu.kirdev.discordinator.service

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun convertToDate(dateString: String?): Instant? {
    if (dateString == null)
        return null

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val localDate = LocalDate.parse(dateString, formatter)

    return localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
}