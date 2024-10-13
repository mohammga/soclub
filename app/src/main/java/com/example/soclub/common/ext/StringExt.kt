package com.example.soclub.common.ext

import android.util.Patterns
import java.util.regex.Pattern

private const val MIN_PASS_LENGTH = 8
private const val PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$"

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
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

fun String.isValidPassword(): Boolean {
    return this.isPasswordLongEnough() &&
            this.containsUpperCase() &&
            this.containsLowerCase() &&
            this.containsDigit() &&
            this.containsNoWhitespace()
}


fun String.isValidName(): Boolean {
    return this.matches(Regex("^[A-Za-zÆØÅæøå ]+$"))
}

fun String.isAgeNumeric(): Boolean {
    return this.toIntOrNull() != null
}

fun String.isAgeValidMinimum(): Boolean {
    val age = this.toIntOrNull()
    return age != null && age >= 16
}
