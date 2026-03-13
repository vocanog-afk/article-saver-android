# 文章收藏助手 Android App

一个简单的Android应用，可以接收任意App的分享并保存文章到本地。

---

## 功能特点

- ✅ **接收分享**：从任意App（微信、浏览器、新闻App等）分享文章
- ✅ **自动提取**：自动识别文章标题和链接
- ✅ **本地保存**：保存在手机本地，无需登录
- ✅ **列表查看**：在App内查看所有收藏
- ✅ **一键打开**：点击收藏直接打开原文
- ✅ **长按删除**：长按可删除不需要的收藏

---

## 如何使用

### 1. 收藏文章

在任何App中：
1. 找到分享按钮
2. 选择"文章收藏"
3. 自动保存到本地

### 2. 查看收藏

- 打开"文章收藏"App
- 查看所有收藏列表
- 点击任意收藏打开原文

### 3. 删除收藏

- 长按收藏项
- 确认删除

---

## 编译方法

### 前提条件

- 安装 Android Studio
- Android SDK API 34
- JDK 8+

### 编译步骤

1. **打开项目**
   ```bash
   cd /path/to/article-saver-android
   ```

2. **用Android Studio打开**
   - 启动 Android Studio
   - 选择 "Open an Existing Project"
   - 选择项目目录

3. **同步Gradle**
   - Android Studio会自动提示同步
   - 等待依赖下载完成

4. **连接手机**
   - 打开手机开发者选项
   - 启用USB调试
   - 用USB连接电脑

5. **运行App**
   - 点击Android Studio的"Run"按钮（绿色三角形）
   - 或使用快捷键：Shift + F10

6. **安装APK**
   - 编译成功后会自动安装到手机
   - 也可以手动复制APK安装

---

## APK位置

编译成功后，APK文件位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 项目结构

```
article-saver-android/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml    # 应用配置
│   │       ├── Article.kt             # 数据模型
│   │       ├── ArticleStorage.kt      # 本地存储
│   │       ├── MainActivity.kt         # 接收分享
│   │       └── ArticleListActivity.kt  # 收藏列表
│   ├── build.gradle                    # 模块构建配置
│   └── proguard-rules.pro             # 混淆规则
├── build.gradle                        # 项目构建配置
├── settings.gradle                     # Gradle设置
└── README.md                           # 本文件
```

---

## 技术栈

- **Kotlin** - 主要开发语言
- **Android SDK** - 原生Android开发
- **Coroutines** - 异步处理
- **Gson** - JSON解析
- **SharedPreferences** - 本地存储

---

## 系统要求

- **最低版本**: Android 5.0 (API 21)
- **推荐版本**: Android 8.0 (API 26)+
- **权限**: 互联网（可选）

---

## 常见问题

### Q: 为什么分享后没反应？
A: 检查是否选择了正确的分享目标（文章收藏App）

### Q: 收藏的文章保存在哪里？
A: 保存在App本地（SharedPreferences），清除数据会丢失

### Q: 如何导出收藏？
A: 当前版本不支持导出，后续会添加功能

### Q: 是否支持同步到云端？
A: 当前版本仅本地保存，后续会添加云同步功能

---

## 开源协议

MIT License

---

## 更新日志

**v1.0** (2026-03-13)
- 初始版本发布
- 支持接收分享
- 本地保存收藏
- 列表查看功能
