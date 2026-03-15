package com.example.readlater.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 🎨 现代蓝紫配色方案
private val LightColors = lightColorScheme(
    // 主色调 - 现代蓝紫色
    primary = Color(0xFF6366F1),        // 靛蓝色 #6366F1
    onPrimary = Color(0xFFFFFFFF),

    // 次要色 - 柔和的粉色
    secondary = Color(0xFFEC4899),      // 粉色 #EC4899
    onSecondary = Color(0xFFFFFFFF),

    // 第三色 - 琥珀色（用于强调）
    tertiary = Color(0xFFF59E0B),       // 琥珀色 #F59E0B
    onTertiary = Color(0xFFFFFFFF),

    // 背景色
    background = Color(0xFFFAFAFA),     // 接近白色 #FAFAFA
    onBackground = Color(0xFF1F2937),   // 深灰 #1F2937

    // 表面色
    surface = Color(0xFFFFFFFF),        // 纯白
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF3F4F6), // 浅灰 #F3F4F6
    onSurfaceVariant = Color(0xFF6B7280),

    // 边框和分割线
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFF3F4F6),

    // 错误色
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    // 主色调 - 亮紫色
    primary = Color(0xFF818CF8),        // 亮靛蓝 #818CF8
    onPrimary = Color(0xFF1F2937),

    // 次要色 - 亮粉色
    secondary = Color(0xFFF472B6),      // 亮粉 #F472B6
    onSecondary = Color(0xFF1F2937),

    // 第三色 - 亮琥珀色
    tertiary = Color(0xFFFBBF24),       // 亮琥珀 #FBBF24
    onTertiary = Color(0xFF1F2937),

    // 背景色
    background = Color(0xFF0F172A),     // 深蓝黑 #0F172A
    onBackground = Color(0xFFF8F4F0),

    // 表面色
    surface = Color(0xFF1E293B),        // 深蓝灰 #1E293B
    onSurface = Color(0xFFF8F4F0),
    surfaceVariant = Color(0xFF334155), // 中蓝灰 #334155
    onSurfaceVariant = Color(0xFFCBD5E1),

    // 边框和分割线
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),

    // 错误色
    error = Color(0xFFF87171),
    onError = Color(0xFF1F2937)
)

// 字体排版 - 更现代的字重和间距
private val AppTypography = Typography(
    // 大标题 - 用于页面标题
    titleLarge = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    ),
    // 中标题 - 用于卡片标题
    titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    // 小标题 - 用于次要标题
    titleSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    ),
    // 大正文 - 用于主要内容
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp,
        lineHeight = 24.sp
    ),
    // 中正文 - 用于次要内容
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp,
        lineHeight = 20.sp
    ),
    // 小正文 - 用于辅助文本
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp,
        lineHeight = 16.sp
    ),
    // 大标签 - 用于按钮、标签
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    // 中标签
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    ),
    // 小标签
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)

// 圆角形状 - 更现代的圆角
private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun ReadLaterTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
