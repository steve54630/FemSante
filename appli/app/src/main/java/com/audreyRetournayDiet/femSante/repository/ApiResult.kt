package com.audreyRetournayDiet.femSante.repository

sealed class ApiResult<out T> {
    data class Success<T>(val data : T?, val message: String) : ApiResult<T>()
    data class Failure(val message: String) : ApiResult<Nothing>()
}