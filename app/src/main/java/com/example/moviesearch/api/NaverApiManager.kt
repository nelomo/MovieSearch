package com.example.moviesearch.api

import com.example.moviesearch.data.Movie
import com.example.moviesearch.data.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NaverApiManager : NaverSearchManager<Movie>() {
    private val movieSearchApi = getRetrofit().create(NaverApi::class.java)

    // 영화 검색 API
    override fun searchInfo(
        startIndex: Int,
        searchQuery: String,
        onSuccess: (resultList: List<Movie>, nextIndex: Int) -> Unit,
        onFailure: (errorCode: Int) -> Unit,
        onError: (throwable: Throwable) -> Unit
    ) {

        // 호출할 URL 구성
        val call = movieSearchApi.searchData(
            CLIENT_ID,
            CLIENT_SECRET,
            "movie.json",
            searchQuery,
            startIndex
        )

        call.enqueue(object: Callback<Request> {
            override fun onResponse(call: Call<Request>, response: Response<Request>) {
                when {
                    response.isSuccessful -> {
                        val results = response.body()!!

                        val nextIndex = getNextIndex(results.start, results.display, results.total)

                        for(Movie in results.items) {
                            Movie.title = Movie.title.replace("&amp;", "&") // '&amp;'로 출력되는 결과 -> '&'으로 변경
                            Movie.title = Movie.title.replace("(<b>|</b>)".toRegex(), "") // 불필요한 태그 삭제
                        }
                        onSuccess(results.items, nextIndex)
                    }

                    // 응답에는 성공했지만 에러 코드인 경우
                    response.code() == 400 -> { // 잘못된 검색어 입력
                        onFailure(400)
                    }

                    response.code() == 500 -> { // 네이버 Open API 서버 에러
                        onFailure(500)
                    }
                }
            }

            override fun onFailure(call: Call<Request>, throwable: Throwable) {
                onError(throwable)
            }
        })
    }
}

