package com.example.cherrysumer.retrofit

data class ErrorResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)
