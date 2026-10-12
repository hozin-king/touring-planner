package com.hozinking.touringplanner.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// "Night ride" — aspal gelap, amber lampu depan, tanpa warna norak.
val Amber = Color(0xFFFFB020)
val AmberDim = Color(0xFF8A5E14)
val Asphalt = Color(0xFF0D0E12)
val CardBg = Color(0xFF16181F)
val RailBg = Color(0xFF262A34)
val Muted = Color(0xFF9AA0AE)
val LeafGreen = Color(0xFF4ADE80)
val SkyBlue = Color(0xFF7DD3FC)
val Lilac = Color(0xFFC4B5FD)
val SignalRed = Color(0xFFFF6B5E)

private val Scheme = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF201503),
    background = Asphalt,
    onBackground = Color(0xFFF2F3F5),
    surface = CardBg,
    onSurface = Color(0xFFF2F3F5),
    surfaceVariant = Color(0xFF22262F),
    onSurfaceVariant = Muted,
    error = SignalRed,
    onError = Color(0xFF2A0D0B),
)

@Composable
fun TouringPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
