package com.androiddevs.mvvmnewsapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.models.Source
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.util.Resource
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(val newsRepository : NewsRepository): ViewModel() {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse?= null

    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse?= null

    init {
        getBreakingNews("us")
    }
    fun getBreakingNews(countryCode: String) = viewModelScope.launch { //start coroutine
        breakingNews.postValue(Resource.Loading())
        val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
        breakingNews.postValue(handleBreakingNewsResponse(response))
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())
        val response = newsRepository.searchNews(searchQuery, searchNewsPage)
        breakingNews.postValue(handleSearchNewsResponse(response))
    }

    fun filterNews(countryCode: String, filterQuery: String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())
        val response = newsRepository.filterNews(countryCode, filterQuery)
        breakingNews.postValue(handleFilteredNewsResponse(response))
    }
    private fun handleBreakingNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse> {
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->
                if (breakingNewsPage < 6) {
                    breakingNewsPage++
                }
                if(breakingNewsResponse == null){
                    breakingNewsResponse = resultResponse
                }else{
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleFilteredNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse> {
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->

                if (breakingNewsPage < 6) {
                    breakingNewsPage++
                }
                searchNewsResponse = resultResponse
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse> {
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->

                if (breakingNewsPage < 6) {
                    breakingNewsPage++
                }
                    searchNewsResponse = resultResponse
                if (resultResponse.articles.isEmpty()) {
                    val source = Source(
                        id = "",
                        name = ""
                    )
                    val article = Article(
                        author = "",
                        content = "",
                        description = "No results found. Please check your spelling and try again((",
                        publishedAt = "",
                        source = source,
                        title = "No Articles Found",
                        url = "",
                        urlToImage = "https://cdn-icons-png.flaticon.com/512/6134/6134065.png"
                    )
                    resultResponse.articles.add(0,article)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

}