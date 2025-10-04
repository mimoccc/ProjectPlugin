package org.mjdev.plugins.projectplugin.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import javax.swing.UIManager

private val DraculaPrimary = Color(0xFFBD93F9)
private val DraculaPrimaryVariant = Color(0xFF6272A4)
private val DraculaSecondary = Color(0xFF50FA7B)
private val DraculaBackground = Color(0xFF282A36)
private val DraculaSurface = Color(0xFF1E1F29)
private val DraculaOnPrimary = Color(0xFF1E1F29)
private val DraculaOnBackground = Color(0xFFF8F8F2)
private val DraculaOnSurface = Color(0xFFE6E6E6)

private val LightPrimary = Color(0xFF3F51B5)
private val LightPrimaryVariant = Color(0xFF303F9F)
private val LightSecondary = Color(0xFF03A9F4)
private val LightBackground = Color(0xFFFFFFFF)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightOnBackground = Color(0xFF212121)
private val LightOnSurface = Color(0xFF212121)

private fun isDarkTheme(): Boolean = runCatching {
    val isDark = UIManager.getLookAndFeel()?.defaults?.get("ui.theme.is.dark") as? Boolean
    isDark ?: run {
        UIManager.getLookAndFeel()?.name?.lowercase().orEmpty().let { theme ->
            theme.contains("darcula") || theme.contains("dark")
        } ?: false
    }
}.getOrNull() ?: false

fun uiColor(
    key: String,
    fallback: Color
): Color = runCatching {
    UIManager.getColor(key)?.let { Color(it.rgb) } ?: fallback
}.getOrNull() ?: fallback

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val dark = isDarkTheme()
    val colors = if (dark) {
        darkColors(
            primary = uiColor("Button.background", Color(0xFF3C3F41)),
            primaryVariant = uiColor("Button.default.focusColor", Color(0xFF2B2B2B)),
            secondary = uiColor("focusCellBackground", Color(0xFF009688)),
            background = uiColor("Panel.background", Color(0xFF2B2B2B)),
            surface = uiColor("Panel.background", Color(0xFF2B2B2B)),
            onPrimary = uiColor("Button.foreground", Color.White),
            onSecondary = uiColor("Label.foreground", Color.White),
            onBackground = uiColor("Label.foreground", Color.White),
            onSurface = uiColor("Label.foreground", Color(0xFFD0D0D0)),
        )
    } else {
        lightColors(
            primary = LightPrimary,
            primaryVariant = LightPrimaryVariant,
            secondary = LightSecondary,
            background = LightBackground,
            surface = LightSurface,
            onPrimary = LightOnPrimary,
            onSecondary = LightOnSurface,
            onBackground = LightOnBackground,
            onSurface = LightOnSurface,
        )
    }

    MaterialTheme(colors = colors, content = content)
}
