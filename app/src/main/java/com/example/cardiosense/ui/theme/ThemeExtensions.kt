package com.example.cardiosense.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CardioSenseColors(
    val successGreen: Color,
    val warningYellow: Color,
    val errorRed: Color,
    val infoBlue: Color,
    val ecgBackground: Color,
    val ecgLineGreen: Color,
    val scannerPulseOuter: Color,
    val scannerPulseInner: Color,
    val scannerCenter: Color,
    val signalStrengthGood: Color,
    val signalStrengthPoor: Color,
    val heartRed: Color,
    val heartRedBackground: Color
)

private val LightCardioSenseColors = CardioSenseColors(
    successGreen = SuccessGreen,
    warningYellow = WarningYellow,
    errorRed = ErrorRed,
    infoBlue = InfoBlue,
    ecgBackground = EcgBackground,
    ecgLineGreen = EcgLineGreen,
    scannerPulseOuter = ScannerPulseOuter,
    scannerPulseInner = ScannerPulseInner,
    scannerCenter = ScannerCenter,
    signalStrengthGood = SignalStrengthGood,
    signalStrengthPoor = SignalStrengthPoor,
    heartRed = HeartRed,
    heartRedBackground = HeartRedBackground
)

private val DarkCardioSenseColors = CardioSenseColors(
    successGreen = SuccessGreenDark,
    warningYellow = WarningYellowDark,
    errorRed = ErrorRedDark,
    infoBlue = InfoBlueDark,
    ecgBackground = EcgBackgroundDark,
    ecgLineGreen = EcgLineGreenDark,
    scannerPulseOuter = ScannerPulseOuterDark,
    scannerPulseInner = ScannerPulseInnerDark,
    scannerCenter = ScannerCenterDark,
    signalStrengthGood = SignalStrengthGoodDark,
    signalStrengthPoor = SignalStrengthPoorDark,
    heartRed = HeartRedDark,
    heartRedBackground = HeartRedBackgroundDark
)

val LocalCardioSenseColors = staticCompositionLocalOf { LightCardioSenseColors }

val MaterialTheme.cardioSenseColors: CardioSenseColors
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkCardioSenseColors else LightCardioSenseColors
