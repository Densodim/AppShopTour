package com.example.appshoptour.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import appshoptour.composeapp.generated.resources.Inter_Medium
import appshoptour.composeapp.generated.resources.Inter_Regular
import appshoptour.composeapp.generated.resources.Inter_SemiBold
import appshoptour.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun interFontFamily() = FontFamily(
    Font(Res.font.Inter_Regular,  FontWeight.Normal),
    Font(Res.font.Inter_Medium,   FontWeight.Medium),
    Font(Res.font.Inter_SemiBold, FontWeight.SemiBold),
)

@Composable
fun appTypography(): Typography {
    val inter = interFontFamily()
    return Typography(
        // Body/Regular 16
        bodyLarge = TextStyle(
            fontFamily = inter,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 19.2.sp,
        ),
        // Body/Medium 16
        bodyMedium = TextStyle(
            fontFamily = inter,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 19.2.sp,
        ),
        // Body/Semibold 16
        bodySmall = TextStyle(
            fontFamily = inter,
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.2.sp,
        ),
        // Caption 1/Regular
        labelMedium = TextStyle(
            fontFamily = inter,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 13.2.sp,
        ),
        // Caption 1/Medium
        labelSmall = TextStyle(
            fontFamily = inter,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 13.2.sp,
        ),
    )
}
