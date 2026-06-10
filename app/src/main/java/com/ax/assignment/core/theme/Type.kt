package com.ax.assignment.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ax.assignment.R

// Figma 전역 서체: Pretendard (Regular 400 / Medium 500 / Bold 700)
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

private val defaults = Typography()

private fun TextStyle.withPretendard() = copy(fontFamily = Pretendard)

val Typography = Typography(
    displayLarge = defaults.displayLarge.withPretendard(),
    displayMedium = defaults.displayMedium.withPretendard(),
    displaySmall = defaults.displaySmall.withPretendard(),
    headlineLarge = defaults.headlineLarge.withPretendard(),
    headlineMedium = defaults.headlineMedium.withPretendard(),
    headlineSmall = defaults.headlineSmall.withPretendard(),
    titleLarge = defaults.titleLarge.withPretendard(),
    titleMedium = defaults.titleMedium.withPretendard(),
    titleSmall = defaults.titleSmall.withPretendard(),
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = defaults.bodyMedium.withPretendard(),
    bodySmall = defaults.bodySmall.withPretendard(),
    labelLarge = defaults.labelLarge.withPretendard(),
    labelMedium = defaults.labelMedium.withPretendard(),
    labelSmall = defaults.labelSmall.withPretendard(),
)
