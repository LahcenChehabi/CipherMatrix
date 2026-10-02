package com.example.ciphermatrix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView Adapter to bind live cyber security news data and dynamic web images
 * to the premium dark card layout.
 */
class NewsAdapter(private var newsList: List<NewsArticle>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = newsList[position]
        holder.tvTitle.text = article.title
        holder.tvDescription.text = article.description

        //
        com.bumptech.glide.Glide.with(holder.itemView.context)
            .load(article.urlToImage)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.imgNews)
    }

    override fun getItemCount(): Int = newsList.size

    fun updateData(newNews: List<NewsArticle>) {
        this.newsList = newNews
        notifyDataSetChanged()
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_news_description)
        val imgNews: ImageView = itemView.findViewById(R.id.iv_news_image)
    }
}