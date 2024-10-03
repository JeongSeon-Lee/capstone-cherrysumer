package com.example.cherrysumer.retrofit

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: T? // 데이터는 성공 시에만 존재
)
