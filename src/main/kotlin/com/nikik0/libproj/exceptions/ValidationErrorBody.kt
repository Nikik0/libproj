package com.nikik0.libproj.exceptions

data class ValidationErrorBody(
    val message: String,
    val validationErrors: List<String?>
)
