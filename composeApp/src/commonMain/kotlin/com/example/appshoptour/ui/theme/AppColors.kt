package com.example.appshoptour.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Primary (бирюзовый бренд-цвет) ──────────────────────────
val Primary        = Color(0xFF51CFC4)
val PrimaryStrong  = Color(0xFF42BEB3)
val PrimaryStronger= Color(0xFF3DADA3)
val PrimaryWeak    = Color(0xFFD8FBF8)
val PrimaryWeaker  = Color(0xFFEFFBFA)

// ── Text (светлая тема) ──────────────────────────────────────
val TextStrongest  = Color(0xFF111111)
val TextStrong     = Color(0xFF434355)
val TextNormal     = Color(0xFF646773)
val TextWeak       = Color(0xFFA9ABB1)
val TextWeaker     = Color(0xFFCBCDD6)

// ── Text (тёмная тема) ───────────────────────────────────────
val TextStrongestDark = Color(0xFFFFFFFF)
val TextStrongDark    = Color(0xFFE2E4E9)
val TextNormalDark    = Color(0xFFCBCDD6)
val TextWeakDark      = Color(0xFF9195A1)
val TextWeakerDark    = Color(0xFF62646A)

// ── Background (светлая тема) ────────────────────────────────
val BgWhite        = Color(0xFFFFFFFF)
val BgWeak         = Color(0xFFFAFCFF)
val BgNormal       = Color(0xFFF2F4F7)
val BgStrong       = Color(0xFFE5E8ED)
val BgStronger     = Color(0xFFD7DCE0)

// ── Background (тёмная тема) ─────────────────────────────────
val BgDarkest      = Color(0xFF161617)
val BgDarkWeak     = Color(0xFF1C1C1E)
val BgDarkNormal   = Color(0xFF2C2C2E)
val BgDarkStrong   = Color(0xFF34343A)
val BgDarkStronger = Color(0xFF43434B)

// ── Border (светлая тема) ────────────────────────────────────
val BorderStrong   = Color(0xFFCCCED9)
val BorderNormal   = Color(0xFFECEEF2)

// ── Border (тёмная тема) ─────────────────────────────────────
val BorderDarkStrong  = Color(0xFF3B3C40)
val BorderDarkNormal  = Color(0xFF4B4F58)

// ════════════════════════════════════════════════════════════
// Material3 color schemes
// ════════════════════════════════════════════════════════════


val LightColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = PrimaryWeak,
    onPrimaryContainer   = PrimaryStronger,

    background           = BgWhite,
    onBackground         = TextStrongest,

    surface              = BgWeak,
    onSurface            = TextStrongest,
    surfaceVariant       = BgNormal,
    onSurfaceVariant     = TextNormal,

    outline              = BorderStrong,
    outlineVariant       = BorderNormal,

    inverseSurface       = BgDarkWeak,
    inverseOnSurface     = TextStrongestDark,
)

val DarkColorScheme = darkColorScheme(
    primary              = Primary,
    onPrimary            = Color(0xFF111111),
    primaryContainer     = PrimaryStrong,
    onPrimaryContainer   = PrimaryWeaker,

    background           = BgDarkest,
    onBackground         = TextStrongestDark,

    surface              = BgDarkWeak,
    onSurface            = TextStrongestDark,
    surfaceVariant       = BgDarkNormal,
    onSurfaceVariant     = TextNormalDark,

    outline              = BorderDarkStrong,
    outlineVariant       = BorderDarkNormal,

    inverseSurface       = BgWeak,
    inverseOnSurface     = TextStrongest,
)