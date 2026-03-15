package com.example.readlater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.DateUtils
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import android.webkit.WebView
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.BarChart
import com.example.readlater.ui.ReadLaterTheme
import coil.compose.AsyncImage
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

// Tab 类型 - 调整顺序为：未读、已读、全部
enum class FilterTab(val title: String) {
    UNREAD("未读"),
    READ("已读"),
    ALL("全部")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadLaterTheme(darkTheme = false) {
                ReadLaterApp(viewModel = viewModel, activity = this)
            }
        }
        if (savedInstanceState == null) {
            handleShareIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                ?: intent.getStringExtra(Intent.EXTRA_TITLE)
            if (!text.isNullOrBlank()) {
                viewModel.saveFromShare(text, subject)
            }
        }
    }

    fun createMarkdownContent(item: SavedItem): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault())
        return buildString {
            appendLine("# ${item.title}")
            appendLine()
            if (!item.url.isNullOrBlank()) {
                appendLine("**URL:** ${item.url}")
                appendLine()
            }
            appendLine("**保存时间:** ${dateFormat.format(item.createdAt)}")
            appendLine("**状态:** ${if (item.isRead) "已读" else "未读"}")
            appendLine()
            if (!item.snippet.isNullOrBlank()) {
                appendLine("## 摘要")
                appendLine(item.snippet)
                appendLine()
            }
            if (!item.contentText.isNullOrBlank()) {
                appendLine("## 正文")
                appendLine(item.contentText)
            }
        }
    }

    fun parseMarkdownFile(content: String): SavedItemEntity? {
        return try {
            val lines = content.lines()
            val title = lines.firstOrNull { it.startsWith("# ") }?.substring(2)?.trim() ?: "Untitled"
            val url = lines.firstOrNull { it.startsWith("**URL:** ") }?.substring("**URL:** ".length)?.trim()
            val savedAt = lines.firstOrNull { it.startsWith("**保存时间:** ") }?.substring("**保存时间:** ".length)?.trim()
            val status = lines.firstOrNull { it.startsWith("**状态:** ") }?.substring("**状态:** ".length)?.trim()
            val snippetStart = lines.indexOfFirst { it == "## 摘要" }
            val contentStart = lines.indexOfFirst { it == "## 正文" }

            val snippet: String? = if (snippetStart >= 0 && contentStart > snippetStart) {
                lines.subList(snippetStart + 1, contentStart).joinToString("\n").trim()
            } else null

            val contentText: String? = if (contentStart >= 0) {
                lines.subList(contentStart + 1, lines.size).joinToString("\n").trim()
            } else null

            SavedItemEntity(
                title = title,
                url = url,
                snippet = snippet?.ifBlank { null },
                coverUrl = null,
                contentText = contentText?.ifBlank { null },
                isRead = status == "已读",
                createdAt = savedAt?.toLongOrNull() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sanitizeFileName(name: String): String {
        val invalidChars = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|', '\n', '\r', '\t')
        var sanitized = name
        invalidChars.forEach { char ->
            sanitized = sanitized.replace(char, '_')
        }
        return sanitized.trim().take(100)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadLaterApp(viewModel: MainViewModel, activity: MainActivity) {
    val items by viewModel.items.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedIdState = rememberSaveable { mutableStateOf<Long?>(null) }
    val showWebState = rememberSaveable { mutableStateOf(false) }
    val selectedTabState = rememberSaveable { mutableStateOf(FilterTab.UNREAD) }
    val showSettingsState = rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // 导出功能的 SAF launcher - 选择文件夹
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                // 获取或创建 ReadLater 子文件夹
                val tree = DocumentFile.fromTreeUri(context, uri)
                val readLaterDir = tree?.createDirectory("ReadLater")

                if (readLaterDir != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault())
                    var successCount = 0
                    var failCount = 0

                    items.forEach { item ->
                        try {
                            val fileName = "${dateFormat.format(item.createdAt)}-${activity.sanitizeFileName(item.title)}.md"
                            val file = readLaterDir.createFile("text/markdown", fileName)

                            if (file != null) {
                                val content = activity.createMarkdownContent(item)
                                context.contentResolver.openOutputStream(file.uri)?.use { output ->
                                    output.write(content.toByteArray(Charsets.UTF_8))
                                    output.flush()
                                }
                                successCount++
                            } else {
                                failCount++
                            }
                        } catch (e: Exception) {
                            failCount++
                        }
                    }

                    val message = "导出完成！成功：$successCount，失败：$failCount"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "无法创建文件夹", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 导入功能的 SAF launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    val entity = activity.parseMarkdownFile(content)
                    if (entity != null) {
                        viewModel.importItem(entity)
                        Toast.makeText(context, "导入成功！", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "文件格式错误", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导入失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val selectedItem = selectedIdState.value?.let { id ->
        items.firstOrNull { it.id == id }
    }

    // 根据选中的 Tab 过滤文章
    val filteredItems = when (selectedTabState.value) {
        FilterTab.ALL -> items
        FilterTab.UNREAD -> items.filter { !it.isRead }
        FilterTab.READ -> items.filter { it.isRead }
    }

    Scaffold(
        topBar = {
            if (showSettingsState.value) {
                null // SettingsScreen 有自己的 TopAppBar
            } else if (selectedItem == null) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Read Later",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSettingsState.value = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "设置",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                null // ReaderScreen 有自己的 TopAppBar
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            showSettingsState.value -> {
                SettingsScreen(
                    padding = padding,
                    onBack = { showSettingsState.value = false },
                    items = items,
                    onExportData = {
                        exportLauncher.launch(Uri.EMPTY)
                    },
                    onImportData = {
                        importLauncher.launch(arrayOf("*/*"))
                    },
                    onClearData = { viewModel.clearAllData() },
                    context = context
                )
            }
            selectedItem == null -> {
                ListScreen(
                items = filteredItems,
                selectedTab = selectedTabState.value,
                onTabChange = { selectedTabState.value = it },
                padding = padding,
                onItemClick = {
                    selectedIdState.value = it.id
                    viewModel.markRead(it.id, true)
                },
                onDeleteItem = { viewModel.deleteItem(it) }
            )
            }
            showWebState.value && !selectedItem.url.isNullOrBlank() -> {
                WebViewScreen(
                    url = selectedItem.url,
                    padding = padding,
                    onClose = { showWebState.value = false }
                )
            }
            else -> {
                ReaderScreen(
                    item = selectedItem,
                    padding = padding,
                    onBack = { selectedIdState.value = null },
                    onOpenInApp = { showWebState.value = true },
                    onOpenExternal = { url -> openUrl(context, url) },
                    onToggleRead = { isRead -> viewModel.markRead(selectedItem.id, isRead) },
                    onShare = { shareArticle(context, selectedItem) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListScreen(
    items: List<SavedItem>,
    selectedTab: FilterTab,
    onTabChange: (FilterTab) -> Unit,
    padding: PaddingValues,
    onItemClick: (SavedItem) -> Unit,
    onDeleteItem: (Long) -> Unit
) {
    val showDeleteDialog = remember { mutableStateOf<Long?>(null) }

    showDeleteDialog.value?.let { itemId ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog.value = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这篇文章吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteItem(itemId)
                    showDeleteDialog.value = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = null }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Tab 行
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            FilterTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabChange(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        HorizontalDivider()

        if (items.isEmpty()) {
            // 空状态 - 简化版，只保留图标和标题
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_empty_state),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = when (selectedTab) {
                        FilterTab.ALL -> "暂无文章"
                        FilterTab.UNREAD -> "暂无未读文章"
                        FilterTab.READ -> "暂无已读文章"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            // 文章列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = MaterialTheme.shapes.medium
                            )
                            .combinedClickable(
                                onClick = { onItemClick(item) },
                                onLongClick = { showDeleteDialog.value = item.id }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (item.isRead) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                val host = extractHost(item.url)
                                if (!host.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(
                                            imageVector = Icons.Filled.Public,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "$host • ${formatRelativeTime(item.createdAt)}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!item.snippet.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = item.snippet,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = if (item.isRead) {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        },
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = if (item.isRead) "read" else "unread",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (item.isRead) {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (!item.coverUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(12.dp))
                                AsyncImage(
                                    model = item.coverUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderScreen(
    item: SavedItem,
    padding: PaddingValues,
    onBack: () -> Unit,
    onOpenInApp: () -> Unit,
    onOpenExternal: (String) -> Unit,
    onToggleRead: (Boolean) -> Unit,
    onShare: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "文章详情",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Open in App
                    IconButton(
                        onClick = onOpenInApp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInBrowser,
                            contentDescription = "在应用内打开",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Open in Browser
                    IconButton(
                        onClick = { if (!item.url.isNullOrBlank()) onOpenExternal(item.url) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = "在浏览器打开",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Mark as Read/Unread
                    IconButton(
                        onClick = { onToggleRead(!item.isRead) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (item.isRead) Icons.Outlined.Check else Icons.Filled.Circle,
                            contentDescription = if (item.isRead) "标记为未读" else "标记为已读",
                            tint = if (item.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Share
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "分享",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 标题
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // URL
                if (!item.url.isNullOrBlank()) {
                    Text(
                        text = item.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 分隔线
                HorizontalDivider()

                // 摘要
                if (!item.snippet.isNullOrBlank()) {
                    Text(
                        text = "摘要",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = item.snippet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 正文内容
                if (!item.contentText.isNullOrBlank()) {
                    Text(
                        text = "正文内容",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = item.contentText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }

                Spacer(modifier = Modifier.height(80.dp)) // 为底部按钮栏留出空间
            }
        }
    }
}

@Composable
private fun WebViewScreen(
    url: String,
    padding: PaddingValues,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    text = stringResource(id = R.string.in_app_reader),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        // 设置 UserAgent 以支持微信文章和其他网站
                        val userAgent = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
                        this.userAgentString = userAgent

                        // 启用缩放
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            }
        )
    }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

private fun shareArticle(context: Context, item: SavedItem) {
    val lines = buildList {
        add(item.title)
        item.url?.let { add(it) }
        item.snippet?.let { if (it.isNotBlank()) add(it) }
    }
    val shareText = lines.joinToString("\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, item.title)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun extractHost(url: String?): String? {
    if (url.isNullOrBlank()) return null
    return try {
        Uri.parse(url).host?.removePrefix("www.")
    } catch (_: Exception) {
        null
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

data class SavedItem(
    val id: Long,
    val title: String,
    val url: String?,
    val snippet: String?,
    val coverUrl: String?,
    val contentText: String?,
    val isRead: Boolean,
    val createdAt: Long
)

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String?,
    val snippet: String?,
    val coverUrl: String?,
    val contentText: String?,
    val isRead: Boolean,
    val createdAt: Long
)

@Dao
interface SavedItemDao {
    @Query("SELECT * FROM saved_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedItemEntity>>

    @Insert
    suspend fun insert(item: SavedItemEntity): Long

    @Query("UPDATE saved_items SET title = :title, snippet = :snippet, coverUrl = :coverUrl WHERE id = :id")
    suspend fun updateMetadata(id: Long, title: String, snippet: String?, coverUrl: String?)

    @Query("UPDATE saved_items SET contentText = :contentText WHERE id = :id")
    suspend fun updateContent(id: Long, contentText: String?)

    @Query("UPDATE saved_items SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadState(id: Long, isRead: Boolean)

    @Query("DELETE FROM saved_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_items")
    suspend fun clearAll()
}

@Database(entities = [SavedItemEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedItemDao(): SavedItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_items ADD COLUMN coverUrl TEXT")
            }
        }
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_items ADD COLUMN contentText TEXT")
                db.execSQL("ALTER TABLE saved_items ADD COLUMN isRead INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "read_later.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class SavedItemRepository(private val dao: SavedItemDao) {
    val items: Flow<List<SavedItem>> = dao.observeAll().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun saveFromShare(text: String, subject: String?) {
        val parsed = parseShareText(text, subject)
        val entity = SavedItemEntity(
            title = parsed.title,
            url = parsed.url,
            snippet = parsed.snippet,
            coverUrl = null,
            contentText = null,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insert(entity)
        val url = parsed.url
        if (!url.isNullOrBlank()) {
            val metadata = fetchMetadata(url)
            if (metadata != null) {
                val finalTitle = if (parsed.titleFromSubject) parsed.title else metadata.title ?: parsed.title
                val finalSnippet = metadata.description ?: parsed.snippet
                dao.updateMetadata(id, finalTitle, finalSnippet, metadata.coverUrl)
            }
            val content = fetchArticleContent(url)
            if (!content.isNullOrBlank()) {
                dao.updateContent(id, content)
            }
        }
    }

    private fun SavedItemEntity.toModel(): SavedItem {
        return SavedItem(
            id = id,
            title = title,
            url = url,
            snippet = snippet,
            coverUrl = coverUrl,
            contentText = contentText,
            isRead = isRead,
            createdAt = createdAt
        )
    }

    private data class ParsedShare(
        val title: String,
        val url: String?,
        val snippet: String?,
        val titleFromSubject: Boolean
    )

    private fun parseShareText(text: String, subject: String?): ParsedShare {
        val urlRegex = Regex("(https?://\\S+)")
        val rawUrl = urlRegex.find(text)?.value
        val cleanedUrl = rawUrl?.trimEnd(',', '.', ')', ']', '>')
        val cleanedText = urlRegex.replace(text, "").trim()
        val cleanedLines = cleanedText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val titleFromSubject = !subject.isNullOrBlank()
        val title = when {
            titleFromSubject -> subject!!.trim().take(120)
            cleanedLines.isNotEmpty() -> cleanedLines.first().take(120)
            !cleanedUrl.isNullOrBlank() -> cleanedUrl
            else -> "Untitled"
        }
        val snippet = when {
            cleanedLines.size > 1 -> cleanedLines.drop(1).joinToString(" ").take(200)
            cleanedLines.size == 1 && cleanedLines.first().length > 120 ->
                cleanedLines.first().take(200)
            else -> cleanedText.takeIf { it.isNotBlank() }
        }
        return ParsedShare(title = title, url = cleanedUrl, snippet = snippet, titleFromSubject = titleFromSubject)
    }

    private data class Metadata(
        val title: String?,
        val description: String?,
        val coverUrl: String?
    )

    private suspend fun fetchMetadata(url: String): Metadata? = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) ReadLater/1.0")
                .timeout(8000)
                .get()

            val ogTitle = doc.selectFirst("meta[property=og:title]")?.attr("content")
            val title = ogTitle?.takeIf { it.isNotBlank() } ?: doc.title().takeIf { it.isNotBlank() }

            val ogDesc = doc.selectFirst("meta[property=og:description]")?.attr("content")
            val desc = ogDesc?.takeIf { it.isNotBlank() }
                ?: doc.selectFirst("meta[name=description]")?.attr("content")?.takeIf { it.isNotBlank() }

            val ogImage = doc.selectFirst("meta[property=og:image]")?.attr("content")
            val twImage = doc.selectFirst("meta[name=twitter:image]")?.attr("content")
            val image = ogImage?.takeIf { it.isNotBlank() } ?: twImage?.takeIf { it.isNotBlank() }

            Metadata(title = title, description = desc, coverUrl = image)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchArticleContent(url: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) ReadLater/1.0")
                .timeout(8000)
                .get()

            // 尝试获取主要内容
            val article = doc.selectFirst("article")?.text()

            // 如果没有 article 标签，尝试获取主要内容区域
            val mainContent = if (article.isNullOrBlank()) {
                doc.selectFirst("main")?.text()
                    ?: doc.selectFirst("[role=main]")?.text()
                    ?: doc.selectFirst(".content")?.text()
                    ?: doc.selectFirst("#content")?.text()
            } else {
                article
            }

            // 移除多余的空白行和空白字符
            val text = (mainContent ?: doc.body()?.text())?.trim()
                ?.replace(Regex("\\n{3,}"), "\n\n")
                ?.replace(Regex("\\s{2,}"), " ")

            text?.takeIf { it.isNotBlank() }?.take(12000)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setRead(id: Long, isRead: Boolean) {
        dao.updateReadState(id, isRead)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun insertItem(entity: SavedItemEntity): Long {
        return dao.insert(entity)
    }
}

sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
}

class MainViewModel(private val repository: SavedItemRepository) : ViewModel() {
    val items: StateFlow<List<SavedItem>> = repository.items.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun saveFromShare(text: String, subject: String?) {
        viewModelScope.launch {
            repository.saveFromShare(text, subject)
            _events.emit(UiEvent.ShowMessage("Saved to Read Later"))
        }
    }

    fun markRead(id: Long, isRead: Boolean) {
        viewModelScope.launch {
            repository.setRead(id, isRead)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            _events.emit(UiEvent.ShowMessage("文章已删除"))
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _events.emit(UiEvent.ShowMessage("所有数据已清除"))
        }
    }

    fun importItem(entity: SavedItemEntity) {
        viewModelScope.launch {
            repository.insertItem(entity)
        }
    }

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(appContext)
            val repo = SavedItemRepository(db.savedItemDao())
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repo) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    padding: PaddingValues,
    onBack: () -> Unit,
    items: List<SavedItem>,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    onClearData: () -> Unit,
    context: Context
) {
    BackHandler(onBack = onBack)

    val showClearConfirmDialog = remember { mutableStateOf(false) }

    if (showClearConfirmDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearConfirmDialog.value = false },
            title = { Text("确认清除") },
            text = { Text("确定要删除所有文章吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    onClearData()
                    showClearConfirmDialog.value = false
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog.value = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 数据统计
            SectionTitle("数据统计")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatRow("总文章数", items.size.toString())
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    StatRow("已读", items.count { it.isRead }.toString())
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    StatRow("未读", items.count { !it.isRead }.toString())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 数据管理
            SectionTitle("数据管理")
            SettingCard(
                icon = Icons.Outlined.Upload,
                title = "导出数据",
                description = "将所有文章导出为 Markdown 文件",
                onClick = { onExportData() }
            )
            SettingCard(
                icon = Icons.Outlined.Download,
                title = "导入数据",
                description = "从文件夹导入 Markdown 文件",
                onClick = { onImportData() }
            )
            SettingCard(
                icon = Icons.Outlined.Delete,
                title = "清除所有数据",
                description = "删除所有文章（不可恢复）",
                onClick = { showClearConfirmDialog.value = true },
                isError = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 关于
            SectionTitle("关于")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "关于 Read Later",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "版本 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
