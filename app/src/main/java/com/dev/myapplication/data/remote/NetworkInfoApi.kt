package com.dev.myapplication.data.remote

import retrofit2.http.GET

data class NetworkInfoResponse(
    val ip: String = "",
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val org: String? = null
)

interface NetworkInfoApi {
    @GET("?format=json")
    suspend fun getNetworkInfo(): NetworkInfoResponse
}

sealed class Resource<out T> {
    object Idle : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
