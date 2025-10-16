package com.example.newsapp.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.newsapp.Article
import com.example.newsapp.toArticle // Нам знадобиться маппер

class NewsPagingSource(
    private val newsApiService: NewsApiService,
    private val category: String
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        // Отримуємо номер поточної сторінки. Якщо це перший запит, page = 1.
        val page = params.key ?: 1
        return try {
            val response = newsApiService.getTopHeadlines(
                page = page,
                pageSize = params.loadSize,
                category = category
            )
            val articles = response.articles.mapNotNull { it.toArticle() }

            LoadResult.Page(
                data = articles,
                // Якщо це перша сторінка, попередньої немає (null)
                prevKey = if (page == 1) null else page - 1,
                // Якщо ми отримали дані, то є наступна сторінка (page + 1)
                nextKey = if (articles.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // Ця функція допомагає Paging відновити завантаження, якщо дані були втрачені.
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}