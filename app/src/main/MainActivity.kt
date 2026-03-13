package com.articlesaver.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var articleStorage: ArticleStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        articleStorage = ArticleStorage(this)

        // 处理分享的Intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                // 接收分享内容
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""

                if (text.isNotEmpty()) {
                    val url = extractUrl(text)
                    val title = if (subject.isNotEmpty()) subject else extractTitle(text)

                    if (url.isNotEmpty()) {
                        saveArticle(title, url, text)
                    } else {
                        showToast("未找到链接")
                        finish()
                    }
                } else {
                    showToast("无法获取分享内容")
                    finish()
                }
            }
            else -> {
                // 正常打开，跳转到收藏列表
                openArticleList()
                finish()
            }
        }
    }

    private fun extractUrl(text: String): String {
        val urlPattern = Regex("(https?://[^\\s]+)")
        val match = urlPattern.find(text)
        return match?.value ?: ""
    }

    private fun extractTitle(text: String): String {
        val urlPattern = Regex("(https?://[^\\s]+)")
        val title = text.replace(urlPattern, "").trim()
        return if (title.isNotEmpty() && title.length < 100) title else "未命名文章"
    }

    private fun saveArticle(title: String, url: String, content: String) {
        activityScope.launch {
            try {
                showToast("正在保存...")

                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val article = Article(
                    id = System.currentTimeMillis(),
                    title = title,
                    url = url,
                    content = content,
                    createdAt = date
                )

                articleStorage.addArticle(article)

                showToast("✅ 收藏成功！")

                // 延迟关闭，让用户看到提示
                delay(1500)
                finish()

            } catch (e: Exception) {
                showToast("保存失败: ${e.message}")
                delay(2000)
                finish()
            }
        }
    }

    private fun openArticleList() {
        val intent = Intent(this, ArticleListActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}
