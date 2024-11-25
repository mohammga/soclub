package com.example.soclub.common.ext

import java.util.Date

private const val MIN_TITLE_LENGTH = 3
private const val MAX_TITLE_LENGTH = 50
private const val MIN_PASS_LENGTH = 8


fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return Regex(emailRegex).matches(this)
}

fun String.isPasswordLongEnough(): Boolean {
    return this.length >= MIN_PASS_LENGTH
}

fun String.containsUpperCase(): Boolean {
    return this.any { it.isUpperCase() }
}

fun String.containsLowerCase(): Boolean {
    return this.any { it.isLowerCase() }
}

fun String.containsDigit(): Boolean {
    return this.any { it.isDigit() }
}

fun String.containsNoWhitespace(): Boolean {
    return !this.contains(" ")
}

fun String.isValidName(): Boolean {
    return this.matches(Regex("^[A-Za-zÆØÅæøå ]+$"))
}

fun String.isAgeNumeric(): Boolean {
    return this.toIntOrNull() != null
}

fun String.isValidTitle(): Boolean {
    return this.length in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH
}

fun String.isValidCategory(): Boolean {
    return this.matches(Regex("^[A-Za-zÆØÅæøå ]+$"))
}

fun String.isValidDescription(): Boolean {
    return this.isNotBlank()
}

fun String.isValidMaxParticipants(): Boolean {
    val num = this.toIntOrNull()
    return num != null && num in 1..999
}

fun String.isValidAgeLimit(): Boolean {
    val age = this.toIntOrNull()
    return age != null && age in 16..100
}

fun String.isValidStartTime(): Boolean {
    return Regex("^([01]\\d|2[0-3]):([0-5]\\d)$").matches(this)
}

fun Long.isValidDate(): Boolean {
    val selectedDate = Date(this)
    val currentDate = Date()
    val diff = selectedDate.time - currentDate.time
    return diff >= 24 * 60 * 60 * 1000 // minst 24 timer fram i tid
}
