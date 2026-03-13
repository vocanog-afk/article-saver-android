package com.articlesaver.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ArticleStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("articles", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val articlesKey = "articles_list"

    fun addArticle(article: Article) {
        val articles = getArticles().toMutableList()
        articles.add(0, article) // 添加到开头
        saveArticles(articles)
    }

    fun getArticles(): List<Article> {
        val json = prefs.getString(articlesKey, null) ?: return emptyList()
        val type = object : TypeToken<List<Article>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteArticle(id: Long) {
        val articles = getArticles().toMutableList()
        articles.removeAll { it.id == id }
        saveArticles(articles)
    }

    fun clearAll() {
        prefs.edit().remove(articlesKey).apply()
    }

    private fun saveArticles(articles: List<Article>) {
        val json = gson.toJson(articles)
        prefs.edit().putString(articlesKey, json).apply()
    }
}
