package com.example.ciphermatrix

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * NewsActivity fetches live cybersecurity intelligence directly from the NewsAPI gateway.
 */
class NewsActivity : AppCompatActivity() {

    private lateinit var rvNews: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRefresh: ImageButton

    private var articlesList = ArrayList<NewsArticle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        rvNews = findViewById(R.id.rv_news)
        progressBar = findViewById(R.id.progress_bar)
        btnRefresh = findViewById(R.id.btn_refresh_news)

        rvNews.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(articlesList)
        rvNews.adapter = newsAdapter

        // Fetch production cyber logs on launch
        fetchLiveCyberNews()

        btnRefresh.setOnClickListener {
            fetchLiveCyberNews()
        }
    }

    /**
     * Dispatches an asynchronous HTTP network payload pull to NewsAPI cloud servers.
     */
    private fun fetchLiveCyberNews() {
        progressBar.visibility = View.VISIBLE
        rvNews.visibility = View.GONE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)


        val myApiKey = BuildConfig.NEWS_API_KEY

        apiService.getCyberNews(apiKey = myApiKey).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                progressBar.visibility = View.GONE
                rvNews.visibility = View.VISIBLE

                if (response.isSuccessful && response.body() != null) {
                    val fetchedArticles = response.body()!!.articles

                    articlesList.clear()
                    articlesList.addAll(fetchedArticles)
                    newsAdapter.updateData(articlesList)
                } else {
                    Toast.makeText(this@NewsActivity, "API Error Code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                rvNews.visibility = View.VISIBLE
                Toast.makeText(this@NewsActivity, "Gateway Offline. Check Connection.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}