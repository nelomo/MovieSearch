package com.example.moviesearch.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviesearch.adapter.HistoryAdapter
import com.example.moviesearch.databinding.ActivityHistoryBinding
import com.example.moviesearch.util.Custom
import com.example.moviesearch.viewmodel.HistoryViewModel

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val appDatabaseViewModel by lazy {
        ViewModelProvider(this).get(HistoryViewModel::class.java)
    }
    private val historyAdapter by lazy { HistoryAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Setup RecyclerView
        binding.historyRecyclerView.apply {
            layoutManager = GridLayoutManager(this@HistoryActivity, 3)
            adapter = historyAdapter
            addItemDecoration(Custom(10))
        }
        // 검색 이력 Observer
        appDatabaseViewModel.searchHistory.observe(this, {
            if(it.isEmpty()) binding.noSearchHistoryTextView.visibility = View.VISIBLE
            else historyAdapter.addItem(it) // Add item
        })
        appDatabaseViewModel.getSearchHistory(10) // 검색 이력 가져오기
        // 리사이클러뷰 아이템 클릭 이벤트
        historyAdapter.setOnItemClickListener(object: HistoryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // 검색명을 MainActivity 로 반환
                val keyword = historyAdapter.getItem(position)
                val intent = intent.putExtra("SEARCH_WORD", keyword)
                setResult(RESULT_OK, intent)
                finish()
            }
        })

        // Room DB - 검색 이력 호출 에러 발생 observer
        appDatabaseViewModel.isSelectFailed.observe(this, {
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
        })
    }
}