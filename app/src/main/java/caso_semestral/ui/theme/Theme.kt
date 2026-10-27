package com.example.caso_semestral.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta ESFOCAR: verde institucional y dorado.
// Se definen también los colores de contenedor y superficie, porque son los que usan
// la barra inferior, el menú lateral, las tarjetas y los campos de texto
// (con los valores de la plantilla se veían morados y rosados).
private val Verde = Color(0xFF1F4D2B)
private val Dorado = Color(0xFF7A5C00)

private val EsfocarColorScheme = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9E7CD),
    onPrimaryContainer = Color(0xFF0B2713),

    secondary = Dorado,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E8D4),      // indicador de la barra inferior y del menú
    onSecondaryContainer = Color(0xFF0F2A16),

    tertiary = Dorado,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE08A),
    onTertiaryContainer = Color(0xFF251A00),

    background = Color(0xFFF8FAF6),
    onBackground = Color(0xFF191C18),
    surface = Color(0xFFF8FAF6),
    onSurface = Color(0xFF191C18),
    surfaceVariant = Color(0xFFDFE5DB),
    onSurfaceVariant = Color(0xFF424940),
    outline = Color(0xFF72796F),
    outlineVariant = Color(0xFFC2C9BE),

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F5EE),
    surfaceContainer = Color(0xFFECF0E8),
    surfaceContainerHigh = Color(0xFFE6EBE2),
    surfaceContainerHighest = Color(0xFFE1E6DC)   // color de fondo de las tarjetas
)

// Solo modo claro y sin color dinámico, para que la app siempre se vea con los colores institucionales
@Composable
fun Caso_semestralTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsfocarColorScheme,
        typography = Typography,
        content = content
    )
}
