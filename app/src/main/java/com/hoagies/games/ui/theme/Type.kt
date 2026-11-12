package com.hoagies.games.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Material's defaults are tuned for dense UIs. Everything here starts a step larger,
// with generous line height. The user's text-size setting multiplies on top of this.
private val base = Typography()

private fun TextStyle.sized(size: Int, line: Int, weight: FontWeight? = null) =
    copy(fontSize = size.sp, lineHeight = line.sp, fontWeight = weight ?: fontWeight)

internal val HoagiesTypography = Typography(
    displaySmall = base.displaySmall.sized(38, 46, FontWeight.SemiBold),
    headlineLarge = base.headlineLarge.sized(34, 42, FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.sized(30, 38, FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.sized(26, 34, FontWeight.SemiBold),
    titleLarge = base.titleLarge.sized(24, 32, FontWeight.SemiBold),
    titleMedium = base.titleMedium.sized(20, 28, FontWeight.SemiBold),
    titleSmall = base.titleSmall.sized(18, 26, FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.sized(20, 30),
    bodyMedium = base.bodyMedium.sized(18, 27),
    bodySmall = base.bodySmall.sized(16, 24),
    labelLarge = base.labelLarge.sized(18, 24, FontWeight.SemiBold),
    labelMedium = base.labelMedium.sized(16, 22, FontWeight.Medium),
    labelSmall = base.labelSmall.sized(14, 20, FontWeight.Medium),
)
