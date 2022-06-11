package com.example.moviesearch.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.adapter.MovieAdapter
import com.example.moviesearch.databinding.ActivityMainBinding
import com.example.moviesearch.room.History
import com.example.moviesearch.viewmodel.SearchViewModel
import com.example.moviesearch.viewmodel.HistoryViewModel
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val movieAdapter by lazy { MovieAdapter(this) }
    private val historyViewModel by lazy {
        ViewModelProvider(this).get(HistoryViewModel::class.java)
    }
    private val searchViewModel by lazy {
        ViewModelProvider(this).get(SearchViewModel::class.java)
    }
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var keyword: String
    private var nextIndex: Int = -1
    private val keyboard by lazy { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = movieAdapter

        // 검색 결과 Observer
        searchViewModel.searchResult.observe(this, { movieList ->
            if(movieList.isEmpty())  Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            else movieAdapter.addItem(movieList) // Add item
        })
        searchViewModel.nextIndex.observe(this, { nextIndex ->
            this.nextIndex = nextIndex // 다음 검색 결과를 위해 nextIndex 저장
        })

        searchViewModel.errorCode.observe(this, { errorCode ->
            showToastFailureResponse(errorCode)
        })
        searchViewModel.throwable.observe(this, { throwable ->
            showToastErrorResponse(throwable)
        })

        binding.toolbar.btnSearch.setOnClickListener { // '검색' 버튼을 클릭했을 때
            movieAdapter.clearItem()
            keyboard.hideSoftInputFromWindow(binding.toolbar.inputKeyword.windowToken, 0) // 키보드 숨기기

            if(binding.toolbar.inputKeyword.text.isEmpty()) {
                Toast.makeText(this, "최소 한 자 이상의 검색어를 입력해 주세요.", Toast.LENGTH_SHORT).show()

            } else { // EditText 에 글자가 입력된 경우
                keyword = binding.toolbar.inputKeyword.text.toString()
                insertSearchHistory(keyword) // 검색 이력 저장

                // 검색 결과 호출(rest api)
                searchViewModel.searchMovie(searchQuery = keyword)
            }
        }

        // 리사이클러뷰 스크롤을 마지막까지 내렸을 때 검색 결과 추가
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                // 리사이클러뷰에 보여지고 있는 마지막 아이템의 position
                val lastVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                val totalItemCount = linearLayoutManager.itemCount // 전체 아이템 개수

                // 최소 한 개 이상의 아이템이 있고, 마지막 아이템을 보고 있는 경우
                if(totalItemCount != 0 && lastVisibleItemPosition == totalItemCount-1) {
                    // 결과가 더 있는 경우에만 API call
                    if(nextIndex > 0) searchViewModel.searchMovie(startIndex = nextIndex, searchQuery = keyword)
                    else Toast.makeText(this@MainActivity, "마지막 결과입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // RecyclerView Item click event
        movieAdapter.setOnItemClickListener(object: MovieAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // 영화 정보 인텐트
                val movieInfoLink = movieAdapter.getItem(position).infoLink
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(movieInfoLink))
                startActivity(intent)
            }
        })

        // Start 'HistoryActivity' for Result
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK) { // 최근 검색 이력을 클릭했을 때만 실행
                movieAdapter.clearItem()
                result.data?.getStringExtra("SEARCH_WORD")?.let {
                    keyword = it // 다음 검색 결과를 위해 전역 변수에 검색어를 저장
                    binding.toolbar.inputKeyword.setText(keyword)
                    insertSearchHistory(keyword) // 검색 이력 저장
                    // 검색 결과 호출(rest api)
                    searchViewModel.searchMovie(searchQuery = keyword)
                }
            }
        }

        // '최근검색' button click event
        binding.toolbar.btnSearchHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            activityResultLauncher.launch(intent)
        }

        // Room DB - 검색 이력 삽입 에러 발생 observer
        historyViewModel.isInsertFailed.observe(this, {
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
        })
    }

    // 검색 이력 저장
    private fun insertSearchHistory(searchWord: String) {
        val searchHistoryDTO = History(
            dateTime = Date(System.currentTimeMillis()),
            searchWord = searchWord
        )
        historyViewModel.insertHistory(searchHistoryDTO)
    }

    // rest 서버에서 에러 코드 응답 시 토스트
    private fun showToastFailureResponse(errorCode: Int) {
        when(errorCode) {
            400 -> Toast.makeText(this, "잘못된 검색어입니다.", Toast.LENGTH_SHORT).show()
            500 -> Toast.makeText(this, "서버 내부 에러가 발생하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // rest 서버 응답 실패 시 토스트
    private fun showToastErrorResponse(throwable: Throwable) {
        when(throwable) {
            is UnknownHostException ->
                Toast.makeText(this, "네트워크 연결을 확인해 주세요.", Toast.LENGTH_SHORT).show()

            is SocketTimeoutException ->
                Toast.makeText(this, "네트워크 연결을 확인해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}