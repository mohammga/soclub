package com.example.soclub.common.ext

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

fun String.isAgeValid(): Boolean {
    val age = this.toIntOrNull()
    return age != null && age in 16..100
}



