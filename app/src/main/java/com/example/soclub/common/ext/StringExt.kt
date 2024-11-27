package com.example.soclub.common.ext

private const val MIN_TITLE_LENGTH = 3
private const val MAX_TITLE_LENGTH = 50
private const val MIN_PASS_LENGTH = 8

//hentet fra eksempel i forelesning/github
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
