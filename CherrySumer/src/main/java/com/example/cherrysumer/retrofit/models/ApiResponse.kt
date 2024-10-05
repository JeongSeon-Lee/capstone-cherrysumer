package com.example.cherrysumer.retrofit.models

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: T? = null
)