package com.example.moviesearch.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class NaverSearchManager<T: Any> : RetrofitManager(NAVER_SEARCH_API_URL) {
    // T: 결과로 받을 DTO 객체 타입을 명시. ex) MovieDTO, BookDTO, newsDTO...

    protected val CLIENT_ID = "PsR96FP8pZS766LBqnl5"
    protected val CLIENT_SECRET = "hBeEXL3LqP"

    companion object {
        private const val NAVER_SEARCH_API_URL = "https://openapi.naver.com/v1/"
    }

    abstract fun searchInfo(startIndex: Int,
                            searchQuery: String,
                            onSuccess: (resultList: List<T>, nextIndex: Int) -> Unit,
                            onFailure: (errorCode: Int) -> Unit,
                            onError: (throwable: Throwable) -> Unit)

    fun getNextIndex(start: Int, display: Int, total: Int): Int = if(start + display <= total) {
        start+display
    } else -1
}
abstract class RetrofitManager(private val baseUrl: String) {
    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}