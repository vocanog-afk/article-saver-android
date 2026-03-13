package com.articlesaver.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class ArticleListActivity : Activity() {

    private lateinit var articleStorage: ArticleStorage
    private lateinit var listView: ListView
    private lateinit var adapter: ArticleAdapter
    private var articles: List<Article> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            articleStorage = ArticleStorage(this)
            Log.d("ArticleSaver", "ArticleListActivity onCreate success")
        } catch (e: Exception) {
            Log.e("ArticleSaver", "Error in ArticleListActivity onCreate: ${e.message}", e)
            Toast.makeText(this, "初始化错误: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 创建简单的ListView
        listView = ListView(this)
        listView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContentView(listView)

        // 长按删除
        listView.setOnItemLongClickListener { _, _, position, _ ->
            showDeleteDialog(articles[position])
            true
        }

        // 点击打开链接
        listView.setOnItemClickListener { _, _, position, _ ->
            openArticle(articles[position])
        }

        loadArticles()
    }

    override fun onResume() {
        super.onResume()
        loadArticles()
    }

    private fun loadArticles() {
        articles = articleStorage.getArticles()
        adapter = ArticleAdapter(articles)
        listView.adapter = adapter

        if (articles.isEmpty()) {
            showToast("暂无收藏，从其他App分享文章到这里")
        }
    }

    private fun openArticle(article: Article) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(article.url))
        startActivity(intent)
    }

    private fun showDeleteDialog(article: Article) {
        AlertDialog.Builder(this)
            .setTitle("删除收藏")
            .setMessage("确定要删除「${article.title}」吗？")
            .setPositiveButton("删除") { _, _ ->
                articleStorage.deleteArticle(article.id)
                loadArticles()
                showToast("已删除")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private inner class ArticleAdapter(private val articles: List<Article>) : BaseAdapter() {

        override fun getCount() = articles.size

        override fun getItem(position: Int) = articles[position]

        override fun getItemId(position: Int) = articles[position].id

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: createView(parent)
            val holder = view.tag as ViewHolder
            val article = articles[position]

            holder.titleTextView.text = article.title
            holder.urlTextView.text = article.url
            holder.dateTextView.text = article.createdAt

            return view
        }

        private fun createView(parent: ViewGroup): View {
            val view = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 30, 40, 30)
                setBackgroundColor(0xFFFFFF)
            }

            val titleView = TextView(parent.context).apply {
                textSize = 16f
                setTextColor(0xFF000000.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val urlView = TextView(parent.context).apply {
                textSize = 12f
                setTextColor(0xFF666666.toInt())
                maxLines = 1
            }

            val dateView = TextView(parent.context).apply {
                textSize = 11f
                setTextColor(0xFF999999.toInt())
            }

            view.addView(titleView)
            view.addView(urlView)
            view.addView(dateView)

            view.tag = ViewHolder(titleView, urlView, dateView)
            return view
        }

        private data class ViewHolder(
            val titleTextView: TextView,
            val urlTextView: TextView,
            val dateTextView: TextView
        )
    }
}
