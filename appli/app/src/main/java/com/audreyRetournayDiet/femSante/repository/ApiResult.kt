package com.audreyRetournayDiet.femSante.repository

import org.json.JSONObject

sealed class ApiResult {
    data class Success(val data : JSONObject?, val message: String) : ApiResult()
    data class Failure(val message: String) : ApiResult()
}