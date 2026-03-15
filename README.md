# Read Later - 稍后阅读

<div align="center">

  一款简洁优雅的 Android 稍后阅读应用

  [![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
  [![Android](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)

</div>

---

## ✨ 功能特点

- 📱 **一键保存** - 通过 Android Share Sheet 从任何应用快速保存文章
- 📖 **离线阅读** - 自动获取文章完整内容，支持离线查看
- 🏷️ **智能分类** - 未读/已读/全部，轻松管理阅读状态
- 📤 **数据导出** - 导出为 Markdown 格式，数据完全掌控
- 📥 **数据导入** - 支持导入 Markdown 格式文章
- 🗑️ **长按删除** - 长按文章卡片快速删除
- 🎨 **现代设计** - Material 3 设计语言，流畅的动画
- 🔒 **本地存储** - 数据存储在本地，隐私安全

---

## 📸 应用预览

> 截图即将推出...

应用包含三个主要页面：
- **列表页**: 展示所有保存的文章，支持未读/已读/全部筛选
- **详情页**: 阅读文章完整内容，支持标记已读状态
- **设置页**: 导出/导入数据，查看应用信息

---

## 📥 下载安装

### 方式一：下载 APK

从 [Releases](../../releases) 页面下载最新的 APK 文件，直接安装到 Android 设备。

### 方式二：GitHub Actions 自动构建

每次推送代码后，GitHub Actions 会自动构建 APK，可以在 Actions 页面下载最新构建的 APK。

### 方式三：从源码编译

```bash
# 克隆仓库
git clone https://github.com/vocanog-afk/article-saver-android.git
cd article-saver-android

# 使用 Android Studio 打开项目
# 或者使用命令行编译
./gradlew assembleDebug
```

安装 `app/build/outputs/apk/debug/app-debug.apk`

---

## 🎯 使用方法

### 1. 保存文章

在任何应用（浏览器、微信、微博等）中：
1. 找到分享按钮
2. 选择"Read Later"
3. 文章自动保存并获取完整内容

### 2. 管理文章

- **切换Tab**: 查看"未读"、"已读"、"全部"文章
- **阅读文章**: 点击文章卡片查看详情
- **标记已读**: 打开文章后自动标记为已读
- **删除文章**: 长按文章卡片，确认删除

### 3. 导出/导入

- **导出**: 设置 → 导出数据 → 选择文件夹 → 导出为 Markdown 文件（每篇文章一个文件）
- **导入**: 设置 → 导入数据 → 选择 Markdown 文件 → 导入

---

## 🛠️ 技术栈

- **语言**: Kotlin 100%
- **UI框架**: Jetpack Compose + Material 3
- **架构**: MVVM + Repository Pattern
- **数据库**: Room
- **网络**: Jsoup（网页抓取）
- **最低版本**: Android 5.0 (API 21)
- **目标版本**: Android 14 (API 34)

---

## 📁 项目结构

```
app/src/main/java/com/example/readlater/
├── MainActivity.kt              # 主 Activity
├── ui/
│   └── Theme.kt                 # 主题配置
└── database/                   # 数据库相关
    ├── SavedItem.kt            # 数据实体
    ├── SavedItemDao.kt         # DAO 接口
    ├── AppDatabase.kt          # 数据库配置
    └── SavedItemRepository.kt  # 数据仓库
```

---

## 🎨 自定义主题

应用使用 Material 3 设计系统，主色调为靛蓝色（#6366F1）。

如需自定义颜色，修改 `Theme.kt`：

```kotlin
private val LightColors = lightColorScheme(
    primary = Color(0xFF6366F1),        // 主色
    secondary = Color(0xFFEC4899),      // 次要色
    tertiary = Color(0xFFF59E0B),        // 第三色
    // ...
)
```

---

## 🤝 贡献指南

欢迎任何形式的贡献！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📝 开发计划

- [ ] 全文搜索功能
- [ ] 标签/分类系统
- [ ] 深色模式
- [ ] 文章排序选项
- [ ] 云同步功能
- [ ] 桌面版（Web/Desktop）

---

## ⚠️ 常见问题

### Q: 为什么分享后没反应？
A: 检查是否选择了正确的分享目标（Read Later App）

### Q: 文章保存在哪里？
A: 保存在 App 本地数据库（Room），清除数据会丢失，请及时导出备份

### Q: 如何导出文章？
A: 设置 → 导出数据，选择文件夹后会自动导出所有文章为 Markdown 文件

### Q: 是否支持云同步？
A: 当前版本仅本地保存，支持导出/导入。后续会添加云同步功能

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代 UI 工具包
- [Room](https://developer.android.com/training/data-storage/room) - Android 数据库
- [Jsoup](https://jsoup.org/) - Java HTML 解析器
- [Material Design 3](https://m3.material.io/) - 设计系统

---

## 📮 联系方式

- 作者：[vocanog-afk](https://github.com/vocanog-afk)
- Issues：[GitHub Issues](../../issues)

---

<div align="center">

  **如果这个项目对你有帮助，请给个 ⭐️ 支持一下！**

</div>
