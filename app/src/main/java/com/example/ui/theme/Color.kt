package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { true }

// Static Dark Palette
val DarkBackground = Color(0xFF141218)
val DarkSurface = Color(0xFF211F26)
val DarkSurfaceElevated = Color(0xFF2B2930)
val DarkSurfaceNav = Color(0xFF1D1B20)
val DarkOutline = Color(0xFF49454F)
val DarkOutlineVariant = Color(0xFF79747E)

val DarkPrimary = Color(0xFFD0BCFF)
val DarkOnPrimary = Color(0xFF381E72)
val DarkPrimaryContainer = Color(0xFF4F378B)
val DarkOnPrimaryContainer = Color(0xFFEADDFF)

val DarkSecondary = Color(0xFFCCC2DC)
val DarkOnSecondary = Color(0xFF332D41)
val DarkSecondaryContainer = Color(0xFF4A4458)
val DarkOnSecondaryContainer = Color(0xFFE8DEF8)

val DarkTertiary = Color(0xFFEFB8C8)
val DarkOnTertiary = Color(0xFF492532)
val DarkTertiaryContainer = Color(0xFF633B48)
val DarkOnTertiaryContainer = Color(0xFFFFD8E4)

val DarkTextPrimary = Color(0xFFE6E1E5)
val DarkTextSecondary = Color(0xFF938F99)
val DarkTextDisabled = Color(0xFF605D66)

val DarkPaid = Color(0xFF81C784)
val DarkPaidContainer = Color(0xFF1B3828)
val DarkPaidText = Color(0xFFA7F3D0)

val DarkPending = Color(0xFFFFB74D)
val DarkPendingContainer = Color(0xFF3E2805)
val DarkPendingText = Color(0xFFFDE68A)

val DarkOverdue = Color(0xFFF87171)
val DarkOverdueContainer = Color(0xFF3F1414)
val DarkOverdueText = Color(0xFFFECACA)

val DarkDraft = Color(0xFF938F99)
val DarkDraftContainer = Color(0xFF36343B)
val DarkDraftText = Color(0xFFE6E1E5)


// Static Light Palette (Polished, Crisp, Modern)
val LightBackground = Color(0xFFF6F8FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFEDF2F7)
val LightSurfaceNav = Color(0xFFE2E8F0)
val LightOutline = Color(0xFFCBD5E1)
val LightOutlineVariant = Color(0xFF94A3B8)

val LightPrimary = Color(0xFF6366F1)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE0E7FF)
val LightOnPrimaryContainer = Color(0xFF312E81)

val LightSecondary = Color(0xFF64748B)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFF1F5F9)
val LightOnSecondaryContainer = Color(0xFF1E293B)

val LightTertiary = Color(0xFF0D9488)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFCCFBF1)
val LightOnTertiaryContainer = Color(0xFF134E4A)

val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)
val LightTextDisabled = Color(0xFF94A3B8)

val LightPaid = Color(0xFF16A34A)
val LightPaidContainer = Color(0xFFDCFCE7)
val LightPaidText = Color(0xFF15803D)

val LightPending = Color(0xFFEA580C)
val LightPendingContainer = Color(0xFFFFEDD5)
val LightPendingText = Color(0xFFC2410C)

val LightOverdue = Color(0xFFDC2626)
val LightOverdueContainer = Color(0xFFFEE2E2)
val LightOverdueText = Color(0xFFB91C1C)

val LightDraft = Color(0xFF64748B)
val LightDraftContainer = Color(0xFFF1F5F9)
val LightDraftText = Color(0xFF334155)


// Dynamic Composable Color Getters that seamlessly react to theme changes
val ImmersiveBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkBackground else LightBackground

val ImmersiveSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSurface else LightSurface

val ImmersiveSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSurfaceElevated else LightSurfaceElevated

val ImmersiveSurfaceNav: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSurfaceNav else LightSurfaceNav

val ImmersiveOutline: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOutline else LightOutline

val ImmersiveOutlineVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOutlineVariant else LightOutlineVariant

val ImmersivePrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPrimary else LightPrimary

val ImmersiveOnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOnPrimary else LightOnPrimary

val ImmersivePrimaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPrimaryContainer else LightPrimaryContainer

val ImmersiveOnPrimaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOnPrimaryContainer else LightOnPrimaryContainer

val ImmersiveSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSecondary else LightSecondary

val ImmersiveOnSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOnSecondary else LightOnSecondary

val ImmersiveSecondaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSecondaryContainer else LightSecondaryContainer

val ImmersiveOnSecondaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOnSecondaryContainer else LightOnSecondaryContainer

val ImmersiveTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTertiary else LightTertiary

val ImmersiveOnTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOnTertiary else LightOnTertiary

val ImmersiveTertiaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTertiaryContainer else LightTertiaryContainer

val ImmersiveOnTertiaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOnTertiaryContainer else LightOnTertiaryContainer

val ImmersiveTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextPrimary else LightTextPrimary

val ImmersiveTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextSecondary else LightTextSecondary

val ImmersiveTextDisabled: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextDisabled else LightTextDisabled

val ImmersivePaid: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPaid else LightPaid

val ImmersivePaidContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPaidContainer else LightPaidContainer

val ImmersivePaidText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPaidText else LightPaidText

val ImmersivePending: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPending else LightPending

val ImmersivePendingContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPendingContainer else LightPendingContainer

val ImmersivePendingText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkPendingText else LightPendingText

val ImmersiveOverdue: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOverdue else LightOverdue

val ImmersiveOverdueContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOverdueContainer else LightOverdueContainer

val ImmersiveOverdueText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkOverdueText else LightOverdueText

val ImmersiveDraft: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkDraft else LightDraft

val ImmersiveDraftContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkDraftContainer else LightDraftContainer

val ImmersiveDraftText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkDraftText else LightDraftText

