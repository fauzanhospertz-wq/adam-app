package com.adam.fitness.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AdamTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 64.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.sp)
)
