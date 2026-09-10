package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import java.util.UUID

enum class BaseThemeType(val displayName: String) {
    WHITE("White"),
    DARK("Dark"),
    GLOSSY("Glossy"),
    LIQUID_GLASS("Liquid Glass"),
    GLOW("Glow"),
    CUSTOM("Custom")
}

data class ThemeConfig(
    val id: String = "white",
    val name: String = "White",
    val baseType: BaseThemeType = BaseThemeType.WHITE,
    val backgroundColor: Long = 0xFFFFFFFF,
    val surfaceColor: Long = 0xFFF8F9FC,
    val primaryTextColor: Long = 0xFF0F172A,
    val secondaryTextColor: Long = 0xFF64748B,
    val accentColor: Long = 0xFF0F172A,
    val buttonColor: Long = 0xFF0F172A,
    val buttonTextColor: Long = 0xFFFFFFFF,
    val borderColor: Long = 0xFFE2E8F0,
    val borderRadiusDp: Float = 22f,
    val shadowIntensity: Float = 0.04f,
    val glassTransparency: Float = 1.0f,
    val glowColor: Long = 0xFF8B5CF6,
    val glowIntensity: Float = 0.0f,
    val glowRadiusDp: Float = 16f,
    val animationScale: Float = 1.0f,
    val uiBrightness: Float = 1.0f,
    val isGlossy: Boolean = false,
    val isLiquidGlass: Boolean = false,
    val isGlow: Boolean = false
) {
    fun toColor(hex: Long): Color = Color(hex)

    fun getBackground(): Color = Color(backgroundColor)
    fun getSurface(): Color = Color(surfaceColor).copy(alpha = glassTransparency.coerceIn(0.15f, 1.0f))
    fun getPrimaryText(): Color = Color(primaryTextColor)
    fun getSecondaryText(): Color = Color(secondaryTextColor)
    fun getAccent(): Color = Color(accentColor)
    fun getButton(): Color = Color(buttonColor)
    fun getButtonText(): Color = Color(buttonTextColor)
    fun getBorder(): Color = Color(borderColor)
    fun getGlow(): Color = Color(glowColor)

    fun toJson(): String {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("baseType", baseType.name)
        obj.put("backgroundColor", backgroundColor)
        obj.put("surfaceColor", surfaceColor)
        obj.put("primaryTextColor", primaryTextColor)
        obj.put("secondaryTextColor", secondaryTextColor)
        obj.put("accentColor", accentColor)
        obj.put("buttonColor", buttonColor)
        obj.put("buttonTextColor", buttonTextColor)
        obj.put("borderColor", borderColor)
        obj.put("borderRadiusDp", borderRadiusDp.toDouble())
        obj.put("shadowIntensity", shadowIntensity.toDouble())
        obj.put("glassTransparency", glassTransparency.toDouble())
        obj.put("glowColor", glowColor)
        obj.put("glowIntensity", glowIntensity.toDouble())
        obj.put("glowRadiusDp", glowRadiusDp.toDouble())
        obj.put("animationScale", animationScale.toDouble())
        obj.put("uiBrightness", uiBrightness.toDouble())
        obj.put("isGlossy", isGlossy)
        obj.put("isLiquidGlass", isLiquidGlass)
        obj.put("isGlow", isGlow)
        return obj.toString()
    }

    companion object {
        val WhitePreset = ThemeConfig(
            id = "preset_white",
            name = "White",
            baseType = BaseThemeType.WHITE,
            backgroundColor = 0xFFFFFFFF,
            surfaceColor = 0xFFF8F9FC,
            primaryTextColor = 0xFF0F172A,
            secondaryTextColor = 0xFF64748B,
            accentColor = 0xFF0F172A,
            buttonColor = 0xFF0F172A,
            buttonTextColor = 0xFFFFFFFF,
            borderColor = 0xFFE2E8F0,
            borderRadiusDp = 22f,
            shadowIntensity = 0.04f,
            glassTransparency = 1.0f,
            glowIntensity = 0.0f,
            isGlossy = false,
            isLiquidGlass = false,
            isGlow = false
        )

        val DarkPreset = ThemeConfig(
            id = "preset_dark",
            name = "Dark",
            baseType = BaseThemeType.DARK,
            backgroundColor = 0xFF0B0D12,
            surfaceColor = 0xFF161A23,
            primaryTextColor = 0xFFF8FAFC,
            secondaryTextColor = 0xFF94A3B8,
            accentColor = 0xFFE2E8F0,
            buttonColor = 0xFF272F3E,
            buttonTextColor = 0xFFFFFFFF,
            borderColor = 0xFF242B38,
            borderRadiusDp = 22f,
            shadowIntensity = 0.35f,
            glassTransparency = 1.0f,
            glowIntensity = 0.0f,
            isGlossy = false,
            isLiquidGlass = false,
            isGlow = false
        )

        val GlossyPreset = ThemeConfig(
            id = "preset_glossy",
            name = "Glossy",
            baseType = BaseThemeType.GLOSSY,
            backgroundColor = 0xFF0E131F,
            surfaceColor = 0xFF1A2234,
            primaryTextColor = 0xFFF8FAFC,
            secondaryTextColor = 0xFF94A3B8,
            accentColor = 0xFF38BDF8,
            buttonColor = 0xFF0284C7,
            buttonTextColor = 0xFFFFFFFF,
            borderColor = 0xFF38BDF8,
            borderRadiusDp = 24f,
            shadowIntensity = 0.40f,
            glassTransparency = 0.94f,
            glowIntensity = 0.15f,
            isGlossy = true,
            isLiquidGlass = false,
            isGlow = false
        )

        val LiquidGlassPreset = ThemeConfig(
            id = "preset_liquid_glass",
            name = "Liquid Glass",
            baseType = BaseThemeType.LIQUID_GLASS,
            backgroundColor = 0xFF070B14,
            surfaceColor = 0xFF131D30,
            primaryTextColor = 0xFFF1F5F9,
            secondaryTextColor = 0xFF94A3B8,
            accentColor = 0xFF60A5FA,
            buttonColor = 0xFF2563EB,
            buttonTextColor = 0xFFFFFFFF,
            borderColor = 0xFF60A5FA,
            borderRadiusDp = 26f,
            shadowIntensity = 0.30f,
            glassTransparency = 0.68f,
            glowColor = 0xFF3B82F6,
            glowIntensity = 0.28f,
            isGlossy = false,
            isLiquidGlass = true,
            isGlow = false
        )

        val GlowPreset = ThemeConfig(
            id = "preset_glow",
            name = "Glow",
            baseType = BaseThemeType.GLOW,
            backgroundColor = 0xFF080611,
            surfaceColor = 0xFF130E24,
            primaryTextColor = 0xFFFAF5FF,
            secondaryTextColor = 0xFFA89BC7,
            accentColor = 0xFFC084FC,
            buttonColor = 0xFF9333EA,
            buttonTextColor = 0xFFFFFFFF,
            borderColor = 0xFFA855F7,
            borderRadiusDp = 22f,
            shadowIntensity = 0.50f,
            glassTransparency = 0.94f,
            glowColor = 0xFFA855F7,
            glowIntensity = 0.85f,
            glowRadiusDp = 18f,
            isGlossy = false,
            isLiquidGlass = false,
            isGlow = true
        )

        fun getDefaultPreset(type: BaseThemeType): ThemeConfig {
            return when (type) {
                BaseThemeType.WHITE -> WhitePreset
                BaseThemeType.DARK -> DarkPreset
                BaseThemeType.GLOSSY -> GlossyPreset
                BaseThemeType.LIQUID_GLASS -> LiquidGlassPreset
                BaseThemeType.GLOW -> GlowPreset
                BaseThemeType.CUSTOM -> WhitePreset.copy(
                    id = UUID.randomUUID().toString(),
                    name = "Custom Theme",
                    baseType = BaseThemeType.CUSTOM
                )
            }
        }

        fun fromJson(jsonStr: String): ThemeConfig? {
            return try {
                val obj = JSONObject(jsonStr)
                ThemeConfig(
                    id = obj.optString("id", "white"),
                    name = obj.optString("name", "White"),
                    baseType = try {
                        BaseThemeType.valueOf(obj.optString("baseType", "WHITE"))
                    } catch (e: Exception) {
                        BaseThemeType.WHITE
                    },
                    backgroundColor = obj.optLong("backgroundColor", WhitePreset.backgroundColor),
                    surfaceColor = obj.optLong("surfaceColor", WhitePreset.surfaceColor),
                    primaryTextColor = obj.optLong("primaryTextColor", WhitePreset.primaryTextColor),
                    secondaryTextColor = obj.optLong("secondaryTextColor", WhitePreset.secondaryTextColor),
                    accentColor = obj.optLong("accentColor", WhitePreset.accentColor),
                    buttonColor = obj.optLong("buttonColor", WhitePreset.buttonColor),
                    buttonTextColor = obj.optLong("buttonTextColor", WhitePreset.buttonTextColor),
                    borderColor = obj.optLong("borderColor", WhitePreset.borderColor),
                    borderRadiusDp = obj.optDouble("borderRadiusDp", 22.0).toFloat(),
                    shadowIntensity = obj.optDouble("shadowIntensity", 0.04).toFloat(),
                    glassTransparency = obj.optDouble("glassTransparency", 1.0).toFloat(),
                    glowColor = obj.optLong("glowColor", 0xFFA855F7),
                    glowIntensity = obj.optDouble("glowIntensity", 0.0).toFloat(),
                    glowRadiusDp = obj.optDouble("glowRadiusDp", 16.0).toFloat(),
                    animationScale = obj.optDouble("animationScale", 1.0).toFloat(),
                    uiBrightness = obj.optDouble("uiBrightness", 1.0).toFloat(),
                    isGlossy = obj.optBoolean("isGlossy", false),
                    isLiquidGlass = obj.optBoolean("isLiquidGlass", false),
                    isGlow = obj.optBoolean("isGlow", false)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

val LocalAppTheme = compositionLocalOf { ThemeConfig.WhitePreset }
