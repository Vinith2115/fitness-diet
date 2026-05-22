package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Liquid Glass Palette Colors
val PitchBlack = Color(0xFF000000)
val DarkGreyBg = Color(0xFF0A0A0A)
val GlassBg = Color(0x13FFFFFF)          // 7% transparency for frosted look
val GlassBgSelected = Color(0x2BFFFFFF)  // 17% transparency for selected elements
val GlassBorder = Color(0x26FFFFFF)      // 15% white border
val GlassBorderSelected = Color(0x4DFFFFFF) // 30% border

// Neon Glowing Accent Colors
val NeonGreen = Color(0xFF00E676)
val NeonCyan = Color(0xFF00E5FF)
val NeonPink = Color(0xFFE040FB)
val NeonYellow = Color(0xFFFFD600)

// Text Colors
val TextWhite = Color(0xFFFFFFFF)
val TextGrayMuted = Color(0xFFA0A0A0)
val TextGraySub = Color(0xFF707070)

// Backward compatibility colors (mapped to new palette to prevent crashes in unchanged code)
val BoldPrimaryBlue = NeonCyan
val BoldPrimaryContainer = GlassBg
val BoldOnPrimaryContainer = TextWhite
val BoldBgSoftLight = PitchBlack
val BoldTextDark = TextWhite
val BoldTextMuted = TextGrayMuted
val BoldBorderLine = GlassBorder
val BoldSurfaceGrey = GlassBg
val BoldSurfaceBlue = GlassBgSelected

val ActiveEmerald = NeonGreen
val CoolTeal = NeonCyan
val IntensityAmber = NeonYellow
val SteelSlate = DarkGreyBg
val CharcoalSlate = PitchBlack
val LightSlate = GlassBg
val PureWhite = TextWhite
val MutedSlate = TextGrayMuted

val PrimaryGreen = NeonGreen
val PrimaryLightBg = PitchBlack
val PrimaryLightSurface = GlassBg
val DarkText = TextWhite
val SolidBlack = PitchBlack
