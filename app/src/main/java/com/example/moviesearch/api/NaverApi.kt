package com.example.moviesearch.api

import com.example.moviesearch.data.Request
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface NaverApi {

    @GET("search/{searchType}")
    fun searchData(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Path("searchType") searchType: String,
        @Query("query") searchQuery: String?,
        @Query("start") startIndex: Int
    ): Call<Request>
}