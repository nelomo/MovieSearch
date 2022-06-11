package com.example.moviesearch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moviesearch.R
import com.example.moviesearch.data.Movie

class MovieAdapter(private val context: Context) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {
    private val movies by lazy { ArrayList<Movie>() }
    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val movieTitle: TextView = view.findViewById(R.id.movie_title)
        val moviePubDate: TextView = view.findViewById(R.id.movie_pubDate)
        val movieRate: TextView = view.findViewById(R.id.movie_rate)
        val movieImg: ImageView = view.findViewById(R.id.movie_img)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onItemClickListener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movies = movies[position]
        holder.apply {
            movieTitle.text = movies.title
            moviePubDate.text = movies.pubDate
            movieRate.text = movies.userRating

            // Glide
            Glide.with(context)
                .load(movies.imageLink)
                .into(movieImg)
        }
    }

    override fun getItemCount(): Int = movies.size

    fun getItem(position: Int): Movie = movies[position]

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun addItem(movieList: List<Movie>) {
        movieList.forEach { movies.add(it) }
        notifyItemInserted(movies.size-1)
    }

    fun clearItem() {
        movies.clear()
        notifyDataSetChanged()
    }
}